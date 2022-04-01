
import getpass, inspect, sys, argparse, unittest
from h2o_get_ip import get_ip_address

# print "h2o_args"
# Global disable. used to prevent browsing when running nosetests, or when given -bd arg
# Defaults to true, if user=jenkins, h2o.unit_main isn't executed, so parse_our_args isn't executed.
# Since nosetests doesn't execute h2o.unit_main, it should have the browser disabled.

# this should be imported in full to init these before unit_main might be called
browse_disable = True
browse_json = False
verbose = False
ip_from_cmd_line = None
port_from_cmd_line = None
network_from_cmd_line = None
config_json = None
debugger = False
random_udp_drop = False
force_tcp = False
random_seed = None
beta_features = True
sleep_at_tear_down = False
abort_after_import = False
clone_cloud_json = None
disable_time_stamp = False
debug_rest = False
long_test_case = False
usecloud = None
# optionally checks expected size if usecloud is used
# None means no check
usecloud_size = None


python_cmd_ip = get_ip_address(ipFromCmdLine=ip_from_cmd_line)

# no command line args if run with just nose
python_cmd_args = ""
# don't really know what it is if nosetests did some stuff. Should be just the test with no args
python_cmd_line = ""
python_username = getpass.getuser()

# The stack is deeper with nose, compared to command line with python
# Walk thru the stack looking for ^test_", since we know tests always start with "test_"
# from nose case:
# inspect.stack()[2] (<frame object at 0x11e7150>, 'test_speedrf_many_cols_enum.py', 5, '<module>', ['import h2o, h2o_cmd, h2o_hosts, h2o_rf, h2o_gbm\n'], 0)

# h2o_sandbox.py has some attempts (commented out) at looking for python test names (logged) in h2o stdout
# to serve as marker boundaries for log scraping (instead of incremental line counting)
# so good to get this correct (will be used by the h2o_nodes[0].h2o_log_msg() (2/LogAndEcho)

def find_python_test_name():
    python_test_name = "unknown"
    for s in inspect.stack():
        # print s
        if s[1].startswith('test_') or '/test_' in s[1]:
            # jenkins gets this assign, but not the unit_main one?
            python_test_name = s[1]
            break
    return python_test_name

python_test_name = find_python_test_name()
# print "    Test: %s" % python_test_name

def parse_our_args():
    parser = argparse.ArgumentParser()
    # can add more here
    parser.add_argument('-bd', '--browse_disable',
        help="Disable any web browser stuff. Needed for batch. nosetests and jenkins disable browser through other means already, so don't need",
        action='store_true')
    parser.add_argument('-b', '--browse_json',
        help='Pops a browser to selected json equivalent urls. Selective. Also keeps test alive (and H2O alive) till you ctrl-c. Then should do clean exit',
        action='store_true')
    parser.add_argument('-v', '--verbose',
        help='increased output',
        action='store_true')
    # I guess we don't have a -port at the command line
    parser.add_argument('-ip', '--ip', type=str,
        help='IP address to use for single host H2O with psutil control')
    parser.add_argument('-p', '--port', type=int,
        help='base port to use for single host H2O with psutil control (used for start of multi jvm too)')
    parser.add_argument('-network', '--network', type=str,
        help='network/mask (shorthand form) to use to resolve multiple possible IPs')
    parser.add_argument('-cj', '--config_json',
        help='Use this json format file to provide multi-host defaults. Overrides the default file pytest_config-<username>.json. These are used only if you do build_cloud_with_hosts()')
    parser.add_argument('-dbg', '--debugger',
        help='Launch java processes with java debug attach mechanisms',
        action='store_true')
    parser.add_argument('-rud', '--random_udp_drop',
        help='Drop 20 pct. of the UDP packets at the receive side',
        action='store_true')
    parser.add_argument('-s', '--random_seed', type=int,
        help='initialize SEED (64-bit integer) for random generators')
    parser.add_argument('-bf', '--beta_features',
        help='enable or switch to beta features (import2/parse2)',
        action='store_true')
    parser.add_argument('-slp', '--sleep_at_tear_down',
        help='open browser and time.sleep(3600) at tear_down_cloud() (typical test end/fail)',
        action='store_true')
    parser.add_argument('-aai', '--abort_after_import',
        help='abort the test after printing the full path to the first dataset used by import_parse/import_only',
        action='store_true')
    parser.add_argument('-ccj', '--clone_cloud_json', type=str,
        help='a h2o-nodes.json file can be passed (see build_cloud(create_json=True). This will create a cloned set of node objects, so any test that builds a cloud, can also be run on an existing cloud without changing the test')
    parser.add_argument('-dts', '--disable_time_stamp',
        help='Disable the timestamp on all stdout. Useful when trying to capture some stdout (like json prints) for use elsewhere',
        action='store_true')
    parser.add_argument('-debug_rest', '--debug_rest',
        help='Print REST API interactions to rest.log',
        action='store_true')
    parser.add_argument('-nc', '--nocolor',
        help="don't emit the chars that cause color printing",
        action='store_true')
    parser.add_argument('-long', '--long_test_case',
        help="some tests will vary behavior to more, longer cases",
        action='store_true')
    parser.add_argument('-uc', '--usecloud',
        nargs='?', const='localhost:54321', type=str,
        help="ip:port of cloud to send tests to instead of starting clouds.")
    parser.add_argument('-ucs', '--usecloud_size',
        help="optionally say the size of the usecloud, code will check size is as expected")

    parser.add_argument('unittest_args', nargs='*')
    args = parser.parse_args()

    # disable colors if we pipe this into a file to avoid extra chars
    if args.nocolor:
        h2p.disable_colors()

    global browse_disable, browse_json, verbose, ip_from_cmd_line, port_from_cmd_line, config_json, debugger
    global random_udp_drop
    global random_seed, beta_features, sleep_at_tear_down, abort_after_import
    global clone_cloud_json, disable_time_stamp, debug_rest, long_test_case, usecloud, usecloud_size

    browse_disable = args.browse_disable or getpass.getuser() == 'jenkins'
    browse_json = args.browse_json
    verbose = args.verbose
    ip_from_cmd_line = args.ip
    port_from_cmd_line = args.port
    network_from_cmd_line = args.network
    config_json = args.config_json
    debugger = args.debugger
    random_udp_drop = args.random_udp_drop
    random_seed = args.random_seed
    # beta_features is hardwired to True
    # beta_features = args.beta_features
    sleep_at_tear_down = args.sleep_at_tear_down
    abort_after_import = args.abort_after_import
    clone_cloud_json = args.clone_cloud_json
    disable_time_stamp = args.disable_time_stamp
    debug_rest = args.debug_rest
    long_test_case = args.long_test_case
    usecloud = args.usecloud
    usecloud_size = args.usecloud_size

    # Set sys.argv to the unittest args (leav sys.argv[0] as is)
    # FIX! this isn't working to grab the args we don't care about
    # Pass "--failfast" to stop on first error to unittest. and -v
    # won't get this for jenkins, since it doesn't do parse_our_args
    sys.argv[1:] = ['-v', "--failfast"] + args.unittest_args
    # sys.argv[1:] = args.unittest_args

def unit_main():
    # print "unit_main"

    parse_our_args()
    global python_test_name, python_cmd_args, python_cmd_line, python_cmd_ip, python_username
    # if I remember correctly there was an issue with using sys.argv[0]
    # under nosetests?.
    # yes, see above. We just duplicate it here although sys.argv[0] might be fine here
    # Use the top of stack!
    python_test_name = find_python_test_name()
    python_cmd_args = " ".join(sys.argv[1:])
    python_cmd_line = "python %s %s" % (python_test_name, python_cmd_args)
    python_username = getpass.getuser()
    # depends on ip_from_cmd_line
    python_cmd_ip = get_ip_address(ipFromCmdLine=ip_from_cmd_line)
    # if test was run with nosetests, it wouldn't execute unit_main() so we won't see this
    # so this is correct, for stuff run with 'python ..."
    print "\nunit_main. Test: %s    command line: %s" % (python_test_name, python_cmd_line)
    unittest.main()
