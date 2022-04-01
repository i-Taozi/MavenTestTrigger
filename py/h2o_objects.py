import sys, getpass, os, psutil, time, requests, errno, threading, inspect
import h2o_args
import h2o_os_util, h2o_print as h2p
import h2o_nodes
from h2o_test import \
    tmp_dir, tmp_file, flatfile_pathname, spawn_cmd, find_file, verboseprint, \
    dump_json, log, log_rest, check_sandbox_for_errors

# print "h2o_objects"

# used to drain stdout on the h2o objects below (before terminating a node)
def __drain(src, dst):
    for l in src:
        if type(dst) == type(0):
            # got this with random data to parse.. why? it shows up in our stdout?
            # UnicodeEncodeError: 'ascii' codec can't encode character u'\x86' in position 60:
            #  ordinal not in range(128)
            # could we be getting unicode object?
            try:
                os.write(dst, l)
            except:
                # os.write(dst,"kbn: non-ascii char in the next line?")
                os.write(dst,l.encode('utf8'))
        else:
            # FIX! this case probably can have the same issue?
            dst.write(l)
            dst.flush()
    src.close()
    if type(dst) == type(0):
        os.close(dst)


def drain(src, dst):
    t = threading.Thread(target=__drain, args=(src, dst))
    t.daemon = True
    t.start()

#*****************************************************************
class H2O(object):
    def __init__(self,
        use_this_ip_addr=None, port=54321, capture_output=True,
        force_ip=False, network=None,
        use_debugger=None, classpath=None,
        use_hdfs=False, use_maprfs=False,
        hdfs_version=None, hdfs_name_node=None, hdfs_config=None,
        aws_credentials=None,
        use_flatfile=False, java_heap_GB=None, java_heap_MB=None, java_extra_args=None,
        use_home_for_ice=False, node_id=None, username=None,
        random_udp_drop=False, force_tcp=False,
        redirect_import_folder_to_s3_path=None,
        redirect_import_folder_to_s3n_path=None,
        disable_h2o_log=False,
        enable_benchmark_log=False,
        h2o_remote_buckets_root=None,
        delete_keys_at_teardown=False,
        cloud_name=None,
        disable_assertions=None,
        sandbox_ignore_errors=False,
        ):

        if use_hdfs:
            # see if we can touch a 0xdata machine
            try:
                # long timeout in ec2...bad
                a = requests.get('http://172.16.2.176:80', timeout=1)
                hdfs_0xdata_visible = True
            except:
                hdfs_0xdata_visible = False

            # different defaults, depending on where we're running
            if hdfs_name_node is None:
                if hdfs_0xdata_visible:
                    hdfs_name_node = "172.16.2.176"
                else: # ec2
                    hdfs_name_node = "10.78.14.235:9000"

            if hdfs_version is None:
                if hdfs_0xdata_visible:
                    hdfs_version = "cdh4"
                else: # ec2
                    hdfs_version = "0.20.2"

        self.redirect_import_folder_to_s3_path = redirect_import_folder_to_s3_path
        self.redirect_import_folder_to_s3n_path = redirect_import_folder_to_s3n_path

        self.aws_credentials = aws_credentials
        self.port = port
        # None is legal for self.h2o_addr.
        # means we won't give an ip to the jar when we start.
        # Or we can say use use_this_ip_addr=127.0.0.1, or the known address
        # if use_this_addr is None, use 127.0.0.1 for urls and json
        # Command line arg 'ip_from_cmd_line' dominates:

        # ip_from_cmd_line and use_this_ip_addr shouldn't be used for mutli-node
        if h2o_args.ip_from_cmd_line:
            self.h2o_addr = h2o_args.ip_from_cmd_line
        else:
            self.h2o_addr = use_this_ip_addr

        self.force_ip = force_ip or (self.h2o_addr!=None)

        if self.h2o_addr:
            self.http_addr = self.h2o_addr
        else:
            self.http_addr = h2o_args.python_cmd_ip

        if h2o_args.network_from_cmd_line:
            self.network = h2o_args.network_from_cmd_line
        else:
            self.network = network
        
        # command line should always dominate for enabling
        if h2o_args.debugger: use_debugger = True
        self.use_debugger = use_debugger

        self.classpath = classpath
        self.capture_output = capture_output

        self.use_hdfs = use_hdfs
        self.use_maprfs = use_maprfs
        self.hdfs_name_node = hdfs_name_node
        self.hdfs_version = hdfs_version
        self.hdfs_config = hdfs_config

        self.use_flatfile = use_flatfile
        self.java_heap_GB = java_heap_GB
        self.java_heap_MB = java_heap_MB
        self.java_extra_args = java_extra_args

        self.use_home_for_ice = use_home_for_ice
        self.node_id = node_id

        if username:
            self.username = username
        else:
            self.username = getpass.getuser()

        # don't want multiple reports from tearDown and tearDownClass
        # have nodes[0] remember (0 always exists)
        self.sandbox_error_was_reported = False
        self.sandbox_ignore_errors = sandbox_ignore_errors

        self.random_udp_drop = random_udp_drop
        self.force_tcp = force_tcp
        self.disable_h2o_log = disable_h2o_log

        # this dumps stats from tests, and perf stats while polling to benchmark.log
        self.enable_benchmark_log = enable_benchmark_log
        self.h2o_remote_buckets_root = h2o_remote_buckets_root
        self.delete_keys_at_teardown = delete_keys_at_teardown
        self.disable_assertions = disable_assertions

        if cloud_name:
            self.cloud_name = cloud_name
        else:
            self.cloud_name = 'pytest-%s-%s' % (getpass.getuser(), os.getpid())

    def __str__(self):
        return '%s - http://%s:%d/' % (type(self), self.http_addr, self.port)

    def url(self, loc, port=None):
        # always use the new api port
        if port is None: port = self.port
        if loc.startswith('/'):
            delim = ''
        else:
            delim = '/'
        u = 'http://%s:%d%s%s' % (self.http_addr, port, delim, loc)
        return u


    def do_json_request(self, jsonRequest=None, fullUrl=None, timeout=10, params=None, returnFast=False,
        cmd='get', extraComment=None, ignoreH2oError=False, noExtraErrorCheck=False, **kwargs):
        # if url param is used, use it as full url. otherwise crate from the jsonRequest
        if fullUrl:
            url = fullUrl
        else:
            url = self.url(jsonRequest)

        # remove any params that are 'None'
        # need to copy dictionary, since can't delete while iterating
        if params is not None:
            params2 = params.copy()
            for k in params2:
                if params2[k] is None:
                    del params[k]
            paramsStr = '?' + '&'.join(['%s=%s' % (k, v) for (k, v) in params.items()])
        else:
            paramsStr = ''

        if extraComment:
            log('Start ' + url + paramsStr, comment=extraComment)
        else:
            log('Start ' + url + paramsStr)

        log_rest("")
        log_rest("----------------------------------------------------------------------\n")
        if extraComment:
            log_rest("# Extra comment info about this request: " + extraComment)
        if cmd == 'get':
            log_rest("GET")
        else:
            log_rest("POST")
        log_rest(url + paramsStr)

        # file get passed thru kwargs here
        try:
            if cmd == 'post':
                r = requests.post(url, timeout=timeout, params=params, **kwargs)
            else:
                r = requests.get(url, timeout=timeout, params=params, **kwargs)

        except Exception, e:
            # rethrow the exception after we've checked for stack trace from h2o
            # out of memory errors maybe don't show up right away? so we should wait for h2o
            # to get it out to h2o stdout. We don't want to rely on cloud teardown to check
            # because there's no delay, and we don't want to delay all cloud teardowns by waiting.
            exc_info = sys.exc_info()
            # use this to ignore the initial connection errors during build cloud when h2o is coming up
            if not noExtraErrorCheck: 
                h2p.red_print(
                    "ERROR: got exception on %s to h2o. \nGoing to check sandbox, then rethrow.." % (url + paramsStr))
                time.sleep(2)
                check_sandbox_for_errors(python_test_name=h2o_args.python_test_name);
            log_rest("")
            log_rest("EXCEPTION CAUGHT DOING REQUEST: " + str(e.message))
            raise exc_info[1], None, exc_info[2]

        log_rest("")
        try:
            if r is None:
                log_rest("r is None")
            else:
                log_rest("HTTP status code: " + str(r.status_code))
                if hasattr(r, 'text'):
                    if r.text is None:
                        log_rest("r.text is None")
                    else:
                        log_rest(r.text)
                else:
                    log_rest("r does not have attr text")
        except Exception, e:
            # Paranoid exception catch.  
            log('WARNING: ignoring unexpected exception on %s' + url + paramsStr)
            # Ignore logging exceptions in the case that the above error checking isn't sufficient.
            pass

        # fatal if no response
        if not r:
            raise Exception("Maybe bad url? no r in __do_json_request in %s:" % inspect.stack()[1][3])

        # this is used to open a browser on results, or to redo the operation in the browser
        # we don't' have that may urls flying around, so let's keep them all
        h2o_nodes.json_url_history.append(r.url)
        # if r.json():
        #     raise Exception("Maybe bad url? no r.json in __do_json_request in %s:" % inspect.stack()[1][3])

        rjson = None
        if returnFast:
            return
        try:
            rjson = r.json()
        except:
            print dump_json(r.text)
            if not isinstance(r, (list, dict)):
                raise Exception("h2o json responses should always be lists or dicts, see previous for text")

            raise Exception("Could not decode any json from the request.")

        # TODO: we should really only look in the response object.  This check
        # prevents us from having a field called "error" (e.g., for a scoring result).
        for e in ['error', 'Error', 'errors', 'Errors']:
            # error can be null (python None). This happens in exec2
            if e in rjson and rjson[e]:
                print "rjson:", dump_json(rjson)
                emsg = 'rjson %s in %s: %s' % (e, inspect.stack()[1][3], rjson[e])
                if ignoreH2oError:
                    # well, we print it..so not totally ignore. test can look at rjson returned
                    print emsg
                else:
                    print emsg
                    raise Exception(emsg)

        for w in ['warning', 'Warning', 'warnings', 'Warnings']:
            # warning can be null (python None).
            if w in rjson and rjson[w]:
                verboseprint(dump_json(rjson))
                print 'rjson %s in %s: %s' % (w, inspect.stack()[1][3], rjson[w])

        return rjson



    def stabilize(self, test_func, error, timeoutSecs=10, retryDelaySecs=0.5):
        '''Repeatedly test a function waiting for it to return True.

        Arguments:
        test_func      -- A function that will be run repeatedly
        error          -- A function that will be run to produce an error message
                          it will be called with (node, timeTakenSecs, numberOfRetries)
                    OR
                       -- A string that will be interpolated with a dictionary of
                          { 'timeTakenSecs', 'numberOfRetries' }
        timeoutSecs    -- How long in seconds to keep trying before declaring a failure
        retryDelaySecs -- How long to wait between retry attempts
        '''
        start = time.time()
        numberOfRetries = 0
        while time.time() - start < timeoutSecs:
            if test_func(self, tries=numberOfRetries, timeoutSecs=timeoutSecs):
                break
            time.sleep(retryDelaySecs)
            numberOfRetries += 1
            # hey, check the sandbox if we've been waiting a long time...rather than wait for timeout
            # to find the badness?. can check_sandbox_for_errors at any time
            if ((numberOfRetries % 50) == 0):
                check_sandbox_for_errors(python_test_name=h2o_args.python_test_name)

        else:
            timeTakenSecs = time.time() - start
            if isinstance(error, type('')):
                raise Exception('%s failed after %.2f seconds having retried %d times' % (
                    error, timeTakenSecs, numberOfRetries))
            else:
                msg = error(self, timeTakenSecs, numberOfRetries)
                raise Exception(msg)

    def wait_for_node_to_accept_connections(self, nodeList, timeoutSecs=15, noExtraErrorCheck=False):
        verboseprint("wait_for_node_to_accept_connections")

        def test(n, tries=None, timeoutSecs=timeoutSecs):
            try:
                n.get_cloud(noExtraErrorCheck=noExtraErrorCheck, timeoutSecs=timeoutSecs)
                return True
            except requests.ConnectionError, e:
                # Now using: requests 1.1.0 (easy_install --upgrade requests) 2/5/13
                # Now: assume all requests.ConnectionErrors are H2O legal connection errors.
                # Have trouble finding where the errno is, fine to assume all are good ones.
                # Timeout check will kick in if continued H2O badness.
                return False

        # get their http addr to represent the nodes
        expectedCloudStr = ",".join([str(n) for n in nodeList])
        self.stabilize(test, error=('waiting for initial connection: Expected cloud %s' % expectedCloudStr),
            timeoutSecs=timeoutSecs, # with cold cache's this can be quite slow
            retryDelaySecs=0.1) # but normally it is very fast

    def sandbox_error_report(self, done=None):
        # not clearable..just or in new value
        if done:
            self.sandbox_error_was_reported = True
        return (self.sandbox_error_was_reported)

    def get_args(self):
        args = ['java']

        # I guess it doesn't matter if we use flatfile for both now
        # defaults to not specifying
        # FIX! we need to check that it's not outside the limits of the dram of the machine it's running on?
        if self.java_heap_GB is not None:
            if not (1 <= self.java_heap_GB <= 256):
                raise Exception('java_heap_GB <1 or >256  (GB): %s' % (self.java_heap_GB))
            args += ['-Xms%dG' % self.java_heap_GB]
            args += ['-Xmx%dG' % self.java_heap_GB]

        if self.java_heap_MB is not None:
            if not (1 <= self.java_heap_MB <= 256000):
                raise Exception('java_heap_MB <1 or >256000  (MB): %s' % (self.java_heap_MB))
            args += ['-Xms%dm' % self.java_heap_MB]
            args += ['-Xmx%dm' % self.java_heap_MB]

        if self.java_extra_args is not None:
            args += ['%s' % self.java_extra_args]

        if self.use_debugger:
            # currently hardwire the base port for debugger to 8000
            # increment by one for every node we add
            # sence this order is different than h2o cluster order, print out the ip and port for the user
            # we could save debugger_port state per node, but not really necessary (but would be more consistent)
            debuggerBasePort = 8000
            if self.node_id is None:
                debuggerPort = debuggerBasePort
            else:
                debuggerPort = debuggerBasePort + self.node_id

            if self.http_addr:
                a = self.http_addr
            else:
                a = "localhost"

            if self.port:
                b = str(self.port)
            else:
                b = "h2o determined"

            # I guess we always specify port?
            print "You can attach debugger at port %s for jvm at %s:%s" % (debuggerPort, a, b)
            args += ['-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%s' % debuggerPort]

        if self.disable_assertions:
            print "WARNING: h2o is running with assertions disabled"
        else:
            args += ["-ea"]
            

        if self.use_maprfs:
            args += ["-Djava.library.path=/opt/mapr/lib"]

        if self.classpath:
            entries = [find_file('build/classes'), find_file('lib/javassist.jar')]
            entries += glob.glob(find_file('lib') + '/*/*.jar')
            entries += glob.glob(find_file('lib') + '/*/*/*.jar')
            args += ['-classpath', os.pathsep.join(entries), 'water.Boot']
        else:
            args += ["-jar", self.get_h2o_jar()]

        if 1==1:
            if self.hdfs_config:
                args += [
                    '-hdfs_config=' + self.hdfs_config
                ]

        if h2o_args.beta_features:
            args += ["-beta"]

        if self.network:
            args += ["-network=" + self.network]

        # H2O should figure it out, if not specified
        # DON"T EVER USE on multi-machine...h2o should always get it right, to be able to run on hadoop 
        # where it's not told
        # new 10/22/14. Allow forcing the ip when we do remote, for networks with bridges, where
        # h2o can't self identify (does -network work?)
        if self.force_ip and self.h2o_addr: # should always have an addr if force_ip...but..
            args += [
                '--ip=%s' % self.h2o_addr,
            ]

        # Need to specify port, since there can be multiple ports for an ip in the flatfile
        if self.port is not None:
            args += [
                "--port=%d" % self.port,
            ]

        if self.use_flatfile:
            args += [
                '--flatfile=' + self.flatfile,
            ]

        args += [
            '--ice_root=%s' % self.get_ice_dir(),
            # if I have multiple jenkins projects doing different h2o clouds, I need
            # I need different ports and different cloud name.
            # does different cloud name prevent them from joining up
            # (even if same multicast ports?)
            # I suppose I can force a base address. or run on another machine?
        ]
        args += [
            '--name=' + self.cloud_name
        ]

        # ignore the other -hdfs args if the config is used?
        if 1==0:
            if self.hdfs_config:
                args += [
                    '-hdfs_config=' + self.hdfs_config
                ]

        if self.use_hdfs:
            args += [
                # it's fine if hdfs_name has a ":9000" port or something too
                '-hdfs hdfs://' + self.hdfs_name_node,
                '-hdfs_version=' + self.hdfs_version,
            ]

        if self.use_maprfs:
            args += [
                # 3 slashes?
                '-hdfs maprfs:///' + self.hdfs_name_node,
                '-hdfs_version=' + self.hdfs_version,
            ]

        if self.aws_credentials:
            args += ['--aws_credentials=' + self.aws_credentials]

        # passed thru build_cloud in test, or global from commandline arg
        if self.random_udp_drop or h2o_args.random_udp_drop:
            args += ['--random_udp_drop']

        if self.force_tcp:
            args += ['--force_tcp']

        if self.disable_h2o_log:
            args += ['--nolog']

        # disable logging of requests, as some contain "error", which fails the test
        ## FIXED. better escape in check_sandbox_for_errors
        ## args += ['--no_requests_log']
        return args


#*****************************************************************
import h2o_methods

class LocalH2O(H2O):
    '''An H2O instance launched by the python framework on the local host using psutil'''

    def __init__(self, *args, **kwargs):
        super(LocalH2O, self).__init__(*args, **kwargs)
        self.rc = None
        # FIX! no option for local /home/username ..always the sandbox (LOG_DIR)
        self.ice = tmp_dir('ice.')
        self.flatfile = flatfile_pathname()
        # so we can tell if we're remote or local. Apparently used in h2o_import.py
        self.remoteH2O = False 

        h2o_os_util.check_port_group(self.port)
        h2o_os_util.show_h2o_processes()

        if self.node_id is not None:
            logPrefix = 'local-h2o-' + str(self.node_id)
        else:
            logPrefix = 'local-h2o'

        spawn = spawn_cmd(logPrefix, cmd=self.get_args(), capture_output=self.capture_output)
        self.ps = spawn[0]

    def get_h2o_jar(self):
        return find_file('target/h2o.jar')

    def get_flatfile(self):
        return self.flatfile
        # return find_file(flatfile_pathname())

    def get_ice_dir(self):
        return self.ice

    def is_alive(self):
        verboseprint("Doing is_alive check for LocalH2O", self.wait(0))
        return self.wait(0) is None

    def terminate_self_only(self):
        def on_terminate(proc):
            print("process {} terminated".format(proc))

        waitingForKill = False
        try:
            # we already sent h2o shutdown and waited a second. Don't bother checking if alive still.
            # send terminate...wait up to 3 secs, then send kill
            self.ps.terminate()
            gone, alive = wait_procs(procs=[self.ps], timeout=3, callback=on_terminate)
            if alive:
                self.ps.kill()
            # from http://code.google.com/p/psutil/wiki/Documentation: wait(timeout=None) Wait for process termination 
            # If the process is already terminated does not raise NoSuchProcess exception but just return None immediately. 
            # If timeout is specified and process is still alive raises TimeoutExpired exception. 
            # hmm. maybe we're hitting the timeout
            waitingForKill = True
            return self.wait(timeout=3)

        except psutil.NoSuchProcess:
            return -1
        except:
            if waitingForKill:
                # this means we must have got the exception on the self.wait()
                # just print a message
                print "\nUsed psutil to kill h2o process...but"
                print "It didn't die within 2 secs. Maybe will die soon. Maybe not! At: %s" % self.http_addr
            else:
                print "Unexpected exception in terminate_self_only: ignoring"
            # hack. 
            # psutil 2.x needs function reference
            # psutil 1.x needs object reference
            if hasattr(self.ps.cmdline, '__call__'):
                pcmdline = self.ps.cmdline()
            else:
                pcmdline = self.ps.cmdline
            print "process cmdline:", pcmdline
            return -1

    def terminate(self):
        # send a shutdown request first.
        # since local is used for a lot of buggy new code, also do the ps kill.
        # try/except inside shutdown_all now
        # new: moved this out..anyone using this should do h2o.nodes[0].shutdown_all first
        if 1==0:
            self.shutdown_all()
        if self.is_alive():
            print "\nShutdown didn't work fast enough for local node? : %s. Will kill though" % self
        self.terminate_self_only()

    def wait(self, timeout=0):
        if self.rc is not None:
            return self.rc
        try:
            self.rc = self.ps.wait(timeout)
            return self.rc
        except psutil.TimeoutExpired:
            return None

    def stack_dump(self):
        self.ps.send_signal(signal.SIGQUIT)

    # to see all the methods
    # print dump_json(dir(LocalH2O))

#*****************************************************************
class RemoteH2O(H2O):
    '''An H2O instance launched by the python framework on a specified host using openssh'''

    def __init__(self, host, *args, **kwargs):
        super(RemoteH2O, self).__init__(*args, **kwargs)

        # it gets set True if an address is specified for LocalH2o init. Override.
        if 'force_ip' in kwargs:
            self.force_ip = kwargs['force_ip']

        self.remoteH2O = True # so we can tell if we're remote or local
        self.jar = host.upload_file('target/h2o.jar')
        # need to copy the flatfile. We don't always use it (depends on h2o args)
        self.flatfile = host.upload_file(flatfile_pathname())
        # distribute AWS credentials
        if self.aws_credentials:
            self.aws_credentials = host.upload_file(self.aws_credentials)

        if self.hdfs_config:
            self.hdfs_config = host.upload_file(self.hdfs_config)

        if self.use_home_for_ice:
            # this will be the username used to ssh to the host
            self.ice = "/home/" + host.username + '/ice.%d.%s' % (self.port, time.time())
        else:
            self.ice = '/tmp/ice.%d.%s' % (self.port, time.time())

        self.channel = host.open_channel()
        ### FIX! TODO...we don't check on remote hosts yet

        # this fires up h2o over there
        cmd = ' '.join(self.get_args())
        # UPDATE: somehow java -jar on cygwin target (xp) can't handle /tmp/h2o*jar
        # because it's a windows executable and expects windows style path names.
        # but if we cd into /tmp, it can do java -jar h2o*jar.
        # So just split out the /tmp (pretend we don't know) and the h2o jar file name
        # Newer windows may not have this problem? Do the ls (this goes into the local stdout
        # files) so we can see the file is really where we expect.
        # This hack only works when the dest is /tmp/h2o*jar. It's okay to execute
        # with pwd = /tmp. If /tmp/ isn't in the jar path, I guess things will be the same as
        # normal.
        if 1 == 0: # enable if you want windows remote machines
            cmdList = ["cd /tmp"] # separate by ;<space> when we join
            cmdList += ["ls -ltr " + self.jar]
            cmdList += [re.sub("/tmp/", "", cmd)]
            self.channel.exec_command("; ".join(cmdList))
        else:
            self.channel.exec_command(cmd)

        if self.capture_output:
            if self.node_id is not None:
                logPrefix = 'remote-h2o-' + str(self.node_id)
            else:
                logPrefix = 'remote-h2o'

            logPrefix += '-' + host.h2o_addr

            outfd, outpath = tmp_file(logPrefix + '.stdout.', '.log')
            errfd, errpath = tmp_file(logPrefix + '.stderr.', '.log')

            drain(self.channel.makefile(), outfd)
            drain(self.channel.makefile_stderr(), errfd)
            comment = 'Remote on %s, stdout %s, stderr %s' % (
                self.h2o_addr, os.path.basename(outpath), os.path.basename(errpath))
        else:
            drain(self.channel.makefile(), sys.stdout)
            drain(self.channel.makefile_stderr(), sys.stderr)
            comment = 'Remote on %s' % self.h2o_addr

        log(cmd, comment=comment)

    def get_h2o_jar(self):
        return self.jar

    def get_flatfile(self):
        return self.flatfile

    def get_ice_dir(self):
        return self.ice

    def is_alive(self):
        verboseprint("Doing is_alive check for RemoteH2O")
        if self.channel.closed: return False
        if self.channel.exit_status_ready(): return False
        try:
            self.get_cloud(noExtraErrorCheck=True)
            return True
        except:
            return False

    def terminate_self_only(self):
        self.channel.close()

        # Don't check afterwards. api watchdog in h2o might complain
        if 1==0:
            time.sleep(1) # a little delay needed?
            # kbn: it should be dead now? want to make sure we don't have zombies
            # we should get a connection error. doing a is_alive subset.
            try:
                gc_output = self.get_cloud(noExtraErrorCheck=True)
                raise Exception("get_cloud() should fail after we terminate a node. It isn't. %s %s" % (self, gc_output))
            except:
                return True

    def terminate(self):
        # new, moved this out. anyone using terminate should send h2o shutdown once before this
        if 1==0:
            self.shutdown_all()
        self.terminate_self_only()

#*****************************************************************
class ExternalH2O(H2O):
    '''A cloned H2O instance assumed to be created by others, that we can interact with via json requests (urls)
       Gets initialized with state from json created by another build_cloud, so all methods should work 'as-if"
       the cloud was built by the test (normally).
       The normal build_cloud() parameters aren't passed here, the final node state is! (and used for init)
       The list should be complete, as long as created by build_cloud(create_json=True) or
       build_cloud_with_hosts(create_json=True)
       Obviously, no psutil or paramiko work done here.
    '''

    def __init__(self, nodeState):
        for k, v in nodeState.iteritems():
            verboseprint("init:", k, v)
            # hack because it looks like the json is currently created with "None" for values of None
            # rather than worrying about that, just translate "None" to None here. "None" shouldn't exist
            # for any other reason.
            if v == "None":
                v = None
            elif v == "false":
                v = False
            elif v == "true":
                v = True
                # leave "null" as-is (string) for now?

            setattr(self, k, v) # achieves self.k = v
            ## print "Cloned", len(nodeState), "things for a h2o node"

    def is_alive(self):
        verboseprint("Doing is_alive check for ExternalH2O")
        try:
            self.get_cloud()
            return True
        except:
            return False

    # no terminate_self_only method
    def terminate_self_only(self):
        raise Exception("terminate_self_only() not supported for ExternalH2O")

    def terminate(self):
        self.shutdown_all()


#*****************************************************************
class RemoteHost(object):
    def upload_file(self, f, progress=None):
        # FIX! we won't find it here if it's hdfs://172.16.2.151/ file
        f = find_file(f)
        if f not in self.uploaded:
            start = time.time()
            import md5

            m = md5.new()
            m.update(open(f).read())
            m.update(getpass.getuser())
            dest = '/tmp/' + m.hexdigest() + "-" + os.path.basename(f)

            # sigh. we rm/create sandbox in build_cloud now
            # (because nosetests doesn't exec h2o_main and we
            # don't want to code "clean_sandbox()" in all the tests.
            # So: we don't have a sandbox here, or if we do, we're going to delete it.
            # Just don't log anything until build_cloud()? that should be okay?
            # we were just logging this upload message..not needed.
            # log('Uploading to %s: %s -> %s' % (self.http_addr, f, dest))
            sftp = self.ssh.open_sftp()
            # check if file exists on remote side
            # does paramiko have issues with big files? (>1GB, or 650MB?). maybe we don't care.
            # This would arise (as mentioned in the source, line no 667, 
            # http://www.lag.net/paramiko/docs/paramiko.sftp_client-pysrc.html) when there is 
            # any error reading the packet or when there is EOFError

            # but I'm getting sftp close here randomly at sm.
            # http://stackoverflow.com/questions/22708942/python-paramiko-module-error-with-callback
            # http://stackoverflow.com/questions/15010540/paramiko-sftp-server-connection-dropped
            # http://stackoverflow.com/questions/12322210/handling-paramiko-sshexception-server-connection-dropped
            try:
                # note we don't do a md5 compare. so if a corrupted file was uploaded we won't re-upload 
                # until we do another build.
                sftp.stat(dest)
                print "{0} Skipping upload of file {1}. File {2} exists on remote side!".format(self, f, dest)
            except IOError, e:
                # if self.channel.closed or self.channel.exit_status_ready():
                #     raise Exception("something bad happened to our %s being used for sftp. keepalive? %s %s" % \
                #         (self, self.channel.closed, self.channel.exit_status_ready()))

                if e.errno == errno.ENOENT: # no such file or directory
                    verboseprint("{0} uploading file {1}".format(self, f))
                    sftp.put(f, dest, callback=progress)
                    # if you want to track upload times
                    ### print "\n{0:.3f} seconds".format(time.time() - start)
                elif e.errno == errno.EEXIST: # File Exists
                    pass
                else:
                    print "Got unexpected errno: %s on paramiko sftp." % e.errno
                    print "Lookup here: https://docs.python.org/2/library/errno.html"
                    # throw the exception again, if not what we expected
                    exc_info = sys.exc_info()
                    raise exc_info[1], None, exc_info[2]
            finally:
                sftp.close()
            self.uploaded[f] = dest
        sys.stdout.flush()
        return self.uploaded[f]

    def record_file(self, f, dest):
        '''Record a file as having been uploaded by external means'''
        self.uploaded[f] = dest

    def run_cmd(self, cmd):
        log('Running `%s` on %s' % (cmd, self))
        (stdin, stdout, stderr) = self.ssh.exec_command(cmd)
        stdin.close()

        sys.stdout.write(stdout.read())
        sys.stdout.flush()
        stdout.close()

        sys.stderr.write(stderr.read())
        sys.stderr.flush()
        stderr.close()

    def push_file_to_remotes(self, f, hosts):
        dest = self.uploaded[f]
        for h in hosts:
            if h == self: continue
            self.run_cmd('scp %s %s@%s:%s' % (dest, h.username, h.h2o_addr, dest))
            h.record_file(f, dest)

    def __init__(self, addr, username=None, password=None, **kwargs):

        import paramiko
        # To debug paramiko you can use the following code:
        #paramiko.util.log_to_file('/tmp/paramiko.log')
        #paramiko.common.logging.basicConfig(level=paramiko.common.DEBUG)

        # kbn. trying 9/23/13. Never specify -ip on java command line for multi-node
        # but self.h2o_addr is used elsewhere. so look at self.remoteH2O to disable in get_args()

        # by definition, this must be the publicly visible addrs, otherwise we can't ssh or browse!
        self.h2o_addr = addr
        self.http_addr = addr

        self.username = username # this works, but it's host state
        self.ssh = paramiko.SSHClient()

        # don't require keys. If no password, assume passwordless setup was done
        policy = paramiko.AutoAddPolicy()
        self.ssh.set_missing_host_key_policy(policy)
        self.ssh.load_system_host_keys()
        if password is None:
            self.ssh.connect(self.h2o_addr, username=username, **kwargs)
        else:
            self.ssh.connect(self.h2o_addr, username=username, password=password, **kwargs)

        # keep connection - send keepalive packet evety 5minutes
        self.ssh.get_transport().set_keepalive(300)
        self.uploaded = {}

    def remote_h2o(self, *args, **kwargs):
        return RemoteH2O(self, self.h2o_addr, *args, **kwargs)

    def open_channel(self):
        ch = self.ssh.get_transport().open_session()
        ch.get_pty() # force the process to die without the connection
        return ch

    def __str__(self):
        return 'ssh://%s@%s' % (self.username, self.h2o_addr)


