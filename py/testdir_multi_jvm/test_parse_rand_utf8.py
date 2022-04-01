import unittest, random, sys, time, os
sys.path.extend(['.','..','../..','py'])

import h2o, h2o_cmd, h2o_import as h2i, h2o_exec as h2e
import codecs

print "apparently need to have at least one normal character otherwise the parse doesn't work right"
print "just doing single char here"

# semicolon ..h2o apparently can auto-detect as separator. so don't use it.
# https://0xdata.atlassian.net/browse/HEX-1951
# ordinalChoices = range(0x0, 0x80) # doesn't include last value ..allow 7f
ordinalChoices = range(0x0, 0x100) # doesn't include last value ..allow 7f

ordinalChoices.remove(0x00) # nul This causes problems. other jira
ordinalChoices.remove(0x0d) # cr
ordinalChoices.remove(0x0a) # lf
ordinalChoices.remove(0x01) # hiveseparator. don't want it autodetecting the hive separator
# ordinalChoices.remove(0x3b) # semicolon Why is this a problem

# white space (tab and space) will throw the col count off?. I guess they cause na 
# (since we're just doing single char here)
ordinalChoices.remove(0x20) # space
ordinalChoices.remove(0x09) # HT (horizontal tab) causes NA?

ordinalChoices.remove(0x22) # double quote
# ordinalChoices.remove(0x27) # apostrophe. should be legal if single quotes not enabled
ordinalChoices.remove(0x2c) # comma. don't put extra commas in

# have to exclude numbers, otherwise the mix of ints and enums will flip things to NA
ordinalChoices.remove(0x30) # 0
ordinalChoices.remove(0x31) # 1
ordinalChoices.remove(0x32) # 2
ordinalChoices.remove(0x33) # 3
ordinalChoices.remove(0x34) # 4
ordinalChoices.remove(0x35) # 5
ordinalChoices.remove(0x36) # 6
ordinalChoices.remove(0x37) # 7
ordinalChoices.remove(0x38) # 8
ordinalChoices.remove(0x39) # 9
# print ordinalChoices

def generate_random_utf8_string(length=1, multi=False, row=0, col=0):
    # want to handle more than 256 numbers
    cList = []
    for i in range(length):
        r = random.choice(ordinalChoicesMulti if multi else ordinalChoices)
        if (row==1 and col==1 and i==0):
            while (r==0x40 or r==0x23): # @ is 0x40, # is 0x23
                # rechoose
                r = random.choice(ordinalChoicesMulti if multi else ordinalChoices)
        # ToDo: Shouldn't encode it here. Then we wouldn't have to decode it to unicode before writing.
        # c = unichr(r).encode('utf-8')
        # fine for it to be unicode obj when we use the result below
        cList.append(unichr(r))
    return "".join(cList)

def write_syn_dataset(csvPathname, rowCount, colCount, colSepChar=",", rowSepChar="\n", SEED=12345678):
    r1 = random.Random(SEED)
    dsf = codecs.open(csvPathname, encoding='utf-8', mode='w+')
    for i in range(rowCount):
        rowData = []
        for j in range(colCount):
            r = generate_random_utf8_string(length=2, row=i, col=j)
            rowData.append(r)
        rowDataCsv = colSepChar.join(rowData)
        dsf.write(rowDataCsv + rowSepChar)
    dsf.close()

class Basic(unittest.TestCase):
    def tearDown(self):
        h2o.check_sandbox_for_errors()

    @classmethod
    def setUpClass(cls):
        global SEED
        SEED = h2o.setup_random_seed()
        h2o.init(2,java_heap_GB=1,use_flatfile=True)

    @classmethod
    def tearDownClass(cls):
        h2o.tear_down_cloud()

    def test_parse_rand_utf8(self):
        SYNDATASETS_DIR = h2o.make_syn_dir()
        tryList = [
            # do two cols to detect bad eol behavior
            (1000, 2, 'cA', 120),
            (1000, 2, 'cG', 120),
            (1000, 2, 'cH', 120),
            ]

        print "What about messages to log (INFO) about unmatched quotes (before eol)"
        # got this ..trying to avoid for now
        # Exception: rjson error in parse: Argument 'source_key' error: Parser setup appears to be broken, got AUTO

        for (rowCount, colCount, hex_key, timeoutSecs) in tryList:
            SEEDPERFILE = random.randint(0, sys.maxint)
            csvFilename = 'syn_' + str(SEEDPERFILE) + "_" + str(rowCount) + 'x' + str(colCount) + '.csv'
            csvPathname = SYNDATASETS_DIR + '/' + csvFilename

            print "\nCreating random", csvPathname
            write_syn_dataset(csvPathname, rowCount, colCount, SEED=SEEDPERFILE)
            parseResult = h2i.import_parse(path=csvPathname, schema='put', header=0,
                hex_key=hex_key, timeoutSecs=timeoutSecs, doSummary=False)
            print "Parse result['destination_key']:", parseResult['destination_key']
            inspect = h2o_cmd.runInspect(None, parseResult['destination_key'], timeoutSecs=60)
        
            print "inspect:", h2o.dump_json(inspect)
            numRows = inspect['numRows']
            self.assertEqual(numRows, rowCount, msg='Wrong numRows: %s %s' % (numRows, rowCount))
            numCols = inspect['numCols']
            self.assertEqual(numCols, colCount, msg='Wrong numCols: %s %s' % (numCols, colCount))

            for k in range(colCount):
                naCnt = inspect['cols'][k]['naCnt']
                self.assertEqual(0, naCnt, msg='col %s naCnt %d should be 0' % (k, naCnt))

                stype = inspect['cols'][k]['type']
                self.assertEqual("Enum", stype, msg='col %s type %s should be Enum' % (k, stype))

if __name__ == '__main__':
    h2o.unit_main()
