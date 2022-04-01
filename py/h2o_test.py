
import sys, os, glob, time, datetime, stat, json, tempfile, shutil, psutil, random, urlparse, getpass
import h2o_args
import h2o_nodes
import h2o_sandbox

# print "h2o_test"

# this is just for putting timestamp in front of all stdout
class OutWrapper:
    def __init__(self, out):
        self._out = out

    def write(self, x):
            # got this with random data to parse.. why? it shows up in our stdout?
            # UnicodeEncodeError:
            #  'ascii' codec can't encode character u'\x80' in position 41: ordinal not in range(128)
            # could we be getting unicode object, or is it just the bytes
            try:
                s = x.replace('\n', '\n[{0}] '.format(datetime.datetime.now()))
                self._out.write(s)
            except:
                self._out.write(s.encode('utf8'))

    def flush(self):
        self._out.flush()

def verboseprint(*args, **kwargs):
    if h2o_args.verbose:
        for x in args: # so you don't have to create a single string
            print x,
        for x in kwargs: # so you don't have to create a single string
            print x,
        print
        # so we can see problems when hung?
        sys.stdout.flush()

def sleep(secs):
    if getpass.getuser() == 'jenkins':
        period = max(secs, 120)
    else:
        period = secs
        # if jenkins, don't let it sleep more than 2 minutes
    # due to left over h2o.sleep(3600)
    time.sleep(period)

def find_file(base):
    # epand ~ or ~user with home dir
    f = os.path.expanduser(base)
    if not os.path.exists(f): f = '../' + base
    if not os.path.exists(f): f = '../../' + base
    if not os.path.exists(f): f = '../../../' + base
    if not os.path.exists(f): f = 'py/' + base
    # these 2 are for finding from h2o-perf
    if not os.path.exists(f): f = '../h2o/' + base
    if not os.path.exists(f): f = '../../h2o/' + base
    if not os.path.exists(f):
        raise Exception("unable to find file %s" % base)
    return f

# The cloud is uniquely named per user (only) and pid
# do the flatfile the same way
# Both are the user that runs the test. The config might have a different username on the
# remote machine (0xdiag, say, or hduser)
def flatfile_pathname():
    return (LOG_DIR + '/pytest_flatfile-%s' % getpass.getuser())

# used to rename the sandbox when running multiple tests in same dir (in different shells)
def get_sandbox_name():
    if os.environ.has_key("H2O_SANDBOX_NAME"):
        a = os.environ["H2O_SANDBOX_NAME"]
        print "H2O_SANDBOX_NAME", a
        return a
    else:
        return "sandbox"

# shutil.rmtree doesn't work on windows if the files are read only.
# On unix the parent dir has to not be readonly too.
# May still be issues with owner being different, like if 'system' is the guy running?
# Apparently this escape function on errors is the way shutil.rmtree can
# handle the permission issue. (do chmod here)
# But we shouldn't have read-only files. So don't try to handle that case.

def handleRemoveError(func, path, exc):
    # If there was an error, it could be due to windows holding onto files.
    # Wait a bit before retrying. Ignore errors on the retry. Just leave files.
    # Ex. if we're in the looping cloud test deleting sandbox.
    excvalue = exc[1]
    print "Retrying shutil.rmtree of sandbox. Will ignore errors. Exception was", excvalue.errno
    time.sleep(2)
    try:
        func(path)
    except OSError:
        pass

LOG_DIR = get_sandbox_name()

def clean_sandbox():
    IS_THIS_FASTER = False
    if os.path.exists(LOG_DIR):

        # shutil.rmtree hangs if symlinks in the dir? (in syn_datasets for multifile parse)
        # use os.remove() first
        # save_model creates a directory now. So delete anything 2 levels down

        # can't figure out how to deal with the model dir. going back to shutil.rmtree
        if 1==0:
            for f in glob.glob(LOG_DIR + '/syn_datasets/*/*'):
                verboseprint("cleaning", f)
                os.remove(f)
     
            for f in glob.glob(LOG_DIR + '/syn_datasets/*'):
                verboseprint("cleaning", f)
                os.remove(f)

        # shutil.rmtree fails to delete very long filenames on Windoze
        ### shutil.rmtree(LOG_DIR)
        # was this on 3/5/13. This seems reliable on windows+cygwin
        # I guess I changed back to rmtree below with something to retry, then ignore, remove errors.
        # is it okay now on windows+cygwin?
        ### os.system("rm -rf "+LOG_DIR)
        print "Removing", LOG_DIR, "(if slow, might be old ice dir spill files)"
        start = time.time()
        if IS_THIS_FASTER:
            try:
                os.system("rm -rf "+LOG_DIR)
            except OSError:
                pass
        else:
            shutil.rmtree(LOG_DIR, ignore_errors=False, onerror=handleRemoveError)

        elapsed = time.time() - start
        print "Took %s secs to remove %s" % (elapsed, LOG_DIR)
        # it should have been removed, but on error it might still be there

    if not os.path.exists(LOG_DIR):
        os.mkdir(LOG_DIR)

# who knows if this one is ok with windows...doesn't rm dir, just
# the stdout/stderr files
def clean_sandbox_stdout_stderr():
    if os.path.exists(LOG_DIR):
        files = []
        # glob.glob returns an iterator
        for f in glob.glob(LOG_DIR + '/*stdout*'):
            verboseprint("cleaning", f)
            os.remove(f)
        for f in glob.glob(LOG_DIR + '/*stderr*'):
            verboseprint("cleaning", f)
            os.remove(f)

def clean_sandbox_doneToLine():
    if os.path.exists(LOG_DIR):
        files = []
        # glob.glob returns an iterator
        for f in glob.glob(LOG_DIR + '/*doneToLine*'):
            verboseprint("cleaning", f)
            os.remove(f)


# just use a global here for the sticky state
sandbox_error_was_reported = False
def check_sandbox_for_errors(cloudShutdownIsError=False, sandboxIgnoreErrors=False, python_test_name=''):
    # dont' have both tearDown and tearDownClass report the same found error
    # only need the first
    global sandbox_error_was_reported
    if sandbox_error_was_reported: # gets current state
        return

    # Can build a cloud that ignores all sandbox things that normally fatal the test
    # Kludge, test will set this directly if it wants, rather than thru build_cloud parameter.
    # we need the sandbox_ignore_errors, for the test teardown_cloud..the state disappears!
    ignore = sandboxIgnoreErrors or (h2o_nodes.nodes and h2o_nodes.nodes[0].sandbox_ignore_errors)
    errorFound = h2o_sandbox.check_sandbox_for_errors(
        LOG_DIR=LOG_DIR,
        sandboxIgnoreErrors=ignore,
        cloudShutdownIsError=cloudShutdownIsError,
        python_test_name=python_test_name)

    if errorFound:
        sandbox_error_was_reported = True

def tmp_file(prefix='', suffix='', tmp_dir=None):
    if not tmp_dir:
        tmpdir = LOG_DIR
    else:
        tmpdir = tmp_dir

    fd, path = tempfile.mkstemp(prefix=prefix, suffix=suffix, dir=tmpdir)
    # make sure the file now exists
    # os.open(path, 'a').close()
    # give everyone permission to read it (jenkins running as
    # 0xcustomer needs to archive as jenkins
    permissions = stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP | stat.S_IROTH
    os.chmod(path, permissions)
    return (fd, path)


def tmp_dir(prefix='', suffix=''):
    return tempfile.mkdtemp(prefix=prefix, suffix=suffix, dir=LOG_DIR)

def make_syn_dir():
    # move under sandbox
    # the LOG_DIR must have been created for commands.log before any datasets would be created
    SYNDATASETS_DIR = LOG_DIR + '/syn_datasets'
    if os.path.exists(SYNDATASETS_DIR):
        shutil.rmtree(SYNDATASETS_DIR)
    os.mkdir(SYNDATASETS_DIR)
    return SYNDATASETS_DIR

def log_rest(s):
    if not h2o_args.debug_rest:
        return
    rest_log_file = open(os.path.join(LOG_DIR, "rest.log"), "a")
    rest_log_file.write(s)
    rest_log_file.write("\n")
    rest_log_file.close()

def log(cmd, comment=None):
    filename = LOG_DIR + '/commands.log'
    # everyone can read
    with open(filename, 'a') as f:
        f.write(str(datetime.datetime.now()) + ' -- ')
        # what got sent to h2o
        # f.write(cmd)
        # let's try saving the unencoded url instead..human readable
        if cmd:
            f.write(urlparse.unquote(cmd))
            if comment:
                f.write('    #')
                f.write(comment)
            f.write("\n")
        elif comment: # for comment-only
            f.write(comment + "\n")
            # jenkins runs as 0xcustomer,
            # and the file wants to be archived by jenkins who isn't in his group
    permissions = stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP | stat.S_IROTH
    os.chmod(filename, permissions)


def dump_json(j):
    return json.dumps(j, sort_keys=True, indent=2)

# can't have a list of cmds, because cmd is a list
# cmdBefore gets executed first, and we wait for it to complete
def spawn_cmd(name, cmd, capture_output=True, **kwargs):
    if capture_output:
        outfd, outpath = tmp_file(name + '.stdout.', '.log')
        errfd, errpath = tmp_file(name + '.stderr.', '.log')
        # everyone can read
        ps = psutil.Popen(cmd, stdin=None, stdout=outfd, stderr=errfd, **kwargs)
    else:
        outpath = '<stdout>'
        errpath = '<stderr>'
        ps = psutil.Popen(cmd, **kwargs)

    comment = 'PID %d, stdout %s, stderr %s' % (
        ps.pid, os.path.basename(outpath), os.path.basename(errpath))
    log(' '.join(cmd), comment=comment)
    return (ps, outpath, errpath)


def spawn_wait(ps, stdout, stderr, capture_output=True, timeout=None):
    rc = ps.wait(timeout)
    if capture_output:
        out = file(stdout).read()
        err = file(stderr).read()
    else:
        out = 'stdout not captured'
        err = 'stderr not captured'

    if rc is None:
        ps.terminate()
        raise Exception("%s %s timed out after %d\nstdout:\n%s\n\nstderr:\n%s" %
                        (ps.name, ps.cmdline, timeout or 0, out, err))
    elif rc != 0:
        raise Exception("%s %s failed.\nstdout:\n%s\n\nstderr:\n%s" %
                        (ps.name, ps.cmdline, out, err))
    return rc


def spawn_cmd_and_wait(name, cmd, capture_output=True, timeout=None, **kwargs):
    (ps, stdout, stderr) = spawn_cmd(name, cmd, capture_output, **kwargs)
    spawn_wait(ps, stdout, stderr, capture_output, timeout)

def check_h2o_version():
    # assumes you want to know about 3 ports starting at base_port
    command1Split = ['java', '-jar', find_file('target/h2o.jar'), '--version']
    command2Split = ['egrep', '-v', '( Java | started)']
    print "Running h2o to get java version"
    p1 = Popen(command1Split, stdout=PIPE)
    p2 = Popen(command2Split, stdin=p1.stdout, stdout=PIPE)
    output = p2.communicate()[0]
    print output


def setup_random_seed(seed=None):
    if h2o_args.random_seed is not None:
        SEED = h2o_args.random_seed
    elif seed is not None:
        SEED = seed
    else:
        SEED = random.randint(0, sys.maxint)
    random.seed(SEED)
    print "\nUsing random seed:", SEED
    return SEED

