
import sys, time, random, re
import h2o_cmd
import h2o_nodes
import h2o_browse as h2b
from h2o_test import dump_json, verboseprint, check_sandbox_for_errors

def checkForBadFP(value, name='min_value', nanOkay=False, infOkay=False, json=None):
    # if we passed the json, dump it for debug
    if 'Infinity' in str(value) and not infOkay:
        if json:
            print dump_json(json)
        raise Exception("Infinity in inspected %s can't be good for: %s" % (str(value), name))
    if 'NaN' in str(value) and not nanOkay:
        if json:
            print dump_json(json)
        raise Exception("NaN in inspected %s can't be good for: %s" % (str(value), name))

def checkScalarResult(resultExec, resultKey, allowEmptyResult=False, nanOkay=False):
    # make the common problems easier to debug
    verboseprint("checkScalarResult resultExec:", dump_json(resultExec))

    if 'funstr' not in resultExec:
        emsg = "checkScalarResult: 'funstr' missing"
    if 'result' not in resultExec:
        emsg = "checkScalarResult: 'result' missing"
    if 'scalar' not in resultExec:
        emsg = "checkScalarResult: 'scalar' missing"
    if 'num_cols' not in resultExec:
        emsg = "checkScalarResult: 'num_cols' missing"
    if 'num_rows' not in resultExec:
        emsg = "checkScalarResult: 'num_rows' missing"
    elif 'cols' not in resultExec:
        emsg = "checkScalarResult: 'cols' missing"
    else:
        emsg = None
        num_cols = resultExec["num_cols"]
        num_rows = resultExec["num_rows"]
        cols = resultExec["cols"]
        # print "cols:", dump_json(cols)

    if emsg:
        print "\nKey: '" + str(resultKey) + "' resultExec:\n", dump_json(resultExec)
        sys.stdout.flush()
        raise Exception("exec result (resultExec) missing what we expected. Look at json above. " + emsg)

    if (cols and (not num_rows or num_rows==0) ) and not allowEmptyResult:
        print "resultExec[0]:", dump_json(resultExec)
        raise Exception ("checkScalarResult says 'cols' exist in exec json response,"+\
            " but num_rows: %s is 0 or None. Is that an expected 'empty' key state?" % num_rows+\
            " Use 'allowEmptyResult if so.")

    # Cycle thru rows and extract all the meta-data into a dict?   
    # assume "0" and "row" keys exist for each list entry in rows
    # FIX! the key for the value can be 0 or 1 or ?? (apparently col?) Should change H2O here

    # cols may not exist..if the result was just scalar?
    if not cols:
        # just return the scalar result then
        scalar = resultExec['scalar']
        if scalar is None:
            raise Exception("both cols and scalar are null: %s %s" % (cols, scalar))
        checkForBadFP(scalar, json=resultExec, nanOkay=nanOkay)
        return scalar

    metaDict = cols[0]
    for key,value in metaDict.items():
        print "Inspect metaDict:", key, value
            
    min_value = metaDict['min']
    stype = metaDict['type']
    # if it's an enum col, it's okay for min to be NaN ..
    checkForBadFP(min_value, json=metaDict, nanOkay=nanOkay or stype=='Enum')
    return min_value

def fill_in_expr_template(exprTemplate, colX=None, n=None, row=None, keyX=None, m=None):
    # FIX! does this push col2 too far? past the output col?
    # just a string? 
    execExpr = exprTemplate
    if colX is not None:
        ### print "Assume colX %s is zero-based..added 1 for R based exec2" % colX
        execExpr = re.sub('<col1>', str(colX+1), execExpr)
        # this is just another value
        execExpr = re.sub('<col2>', str(colX+2), execExpr)
    if n is not None:
        execExpr = re.sub('<n>', str(n), execExpr)
        execExpr = re.sub('<n-1>', str(n-1), execExpr)
    if row is not None:
        execExpr = re.sub('<row>', str(row), execExpr)
    if keyX is not None:
        execExpr = re.sub('<keyX>', str(keyX), execExpr)
    if m is not None:
        execExpr = re.sub('<m>', str(m), execExpr)
        execExpr = re.sub('<m-1>', str(m-1), execExpr)
    ### verboseprint("\nexecExpr:", execExpr)
    ### print "execExpr:", execExpr
    return execExpr


def exec_expr(node=None, execExpr=None, resultKey=None, timeoutSecs=10, ignoreH2oError=False, **kwargs):
    if not node:
        node = h2o_nodes.nodes[0]
    start = time.time()
    # FIX! Exec has 'escape_nan' arg now. should we test?
    # 5/14/13 removed escape_nan=0

    kwargs = {'str': execExpr} 
    resultExec = h2o_cmd.runExec(node, timeoutSecs=timeoutSecs, ignoreH2oError=ignoreH2oError, **kwargs)
    verboseprint('exec took', time.time() - start, 'seconds')
    verboseprint(resultExec)

    if 'cols' in resultExec and resultExec['cols']: # not null
        if 'funstr' in resultExec and resultExec['funstr']: # not null
            raise Exception("cols and funstr shouldn't both be in resultExec: %s" % dump_json(resultExec))
        else:
            print "Frame return"
            # if test said to look at a resultKey, it's should be in h2o k/v store
            # inspect a result key?
            if resultKey is not None:
                kwargs = {'str': resultKey} 
                resultExec = h2o_cmd.runExec(node, timeoutSecs=timeoutSecs, ignoreH2oError=ignoreH2oError, **kwargs)
                verboseprint("resultExec2:", dump_json(resultExec))

            # handles the 1x1 data frame result. Not really interesting if bigger than 1x1?
            result = resultExec['cols'][0]['min']
        
    else: 
        if 'funstr' in resultExec and resultExec['funstr']: # not null
            print "function return"
            result = resultExec['funstr']
        else:
            ### print "scalar return"
            result = resultExec['scalar']
            
    return resultExec, result


def exec_zero_list(zeroList,timeoutSecs=60):
    # zero the list of Results using node[0]
    for exprTemplate in zeroList:
        execExpr = fill_in_expr_template(exprTemplate,0, 0, 0, None)
        (resultExec, result) = exec_expr(h2o_nodes.nodes[0], execExpr, None,timeoutSecs)


def exec_expr_list_rand(lenNodes, exprList, keyX, 
    # exec2 uses R "start with 1" behavior?
    minCol=1, maxCol=55, 
    minRow=1, maxRow=400000, 
    maxTrials=200, 
    timeoutSecs=10, ignoreH2oError=False, allowEmptyResult=False, nanOkay=False):

    trial = 0
    while trial < maxTrials: 
        exprTemplate = random.choice(exprList)

        # UPDATE: all execs are to a single node. No mixed node streams
        # eliminates some store/store race conditions that caused problems.
        # always go to node 0 (forever?)
        if lenNodes is None:
            execNode = 0
        else:
            # execNode = random.randint(0,lenNodes-1)
            execNode = 0
        ## print "execNode:", execNode

        colX = random.randint(minCol,maxCol)

        # FIX! should tune this for covtype20x vs 200x vs covtype.data..but for now
        row = str(random.randint(minRow,maxRow))

        execExpr = fill_in_expr_template(exprTemplate, colX, ((trial+1)%4)+1, row, keyX)
        (resultExec, result) = exec_expr(h2o_nodes.nodes[execNode], execExpr, None, 
            timeoutSecs, ignoreH2oError)

        checkScalarResult(resultExec, None, allowEmptyResult=allowEmptyResult, nanOkay=nanOkay)

        if keyX:
            inspect = h2o_cmd.runInspect(key=keyX)
            print keyX, \
                "    numRows:", "{:,}".format(inspect['numRows']), \
                "    numCols:", "{:,}".format(inspect['numCols'])

        sys.stdout.write('.')
        sys.stdout.flush()

        ### h2b.browseJsonHistoryAsUrlLastMatch("Inspect")
        # slows things down to check every iteration, but good for isolation
        if check_sandbox_for_errors():
            raise Exception(
                "Found errors in sandbox stdout or stderr, on trial #%s." % trial)
        trial += 1
        print "Trial #", trial, "completed\n"

def exec_expr_list_across_cols(lenNodes, exprList, keyX, 
    minCol=0, maxCol=55, timeoutSecs=10, incrementingResult=True, **kwargs):
    colResultList = []
    for colX in range(minCol, maxCol):
        for i, exprTemplate in enumerate(exprList):

            # do each expression at a random node, to facilate key movement
            # UPDATE: all execs are to a single node. No mixed node streams
            # eliminates some store/store race conditions that caused problems.
            # always go to node 0 (forever?)
            if lenNodes is None:
                execNode = 0
            else:
                ### execNode = random.randint(0,lenNodes-1)
                ### print execNode
                execNode = 0

            execExpr = fill_in_expr_template(exprTemplate, colX, colX, 0, keyX)
            if incrementingResult: # the Result<col> pattern
                resultKey = "Result"+str(colX)
            else: # assume it's a re-assign to self
                resultKey = keyX

            # v2
            (resultExec, result) = exec_expr(h2o_nodes.nodes[execNode], execExpr, None, timeoutSecs, **kwargs)
            # print "\nexecResult:", dump_json(resultExec)

            ### h2b.browseJsonHistoryAsUrlLastMatch("Inspect")
            # slows things down to check every iteration, but good for isolation
            if check_sandbox_for_errors():
                raise Exception(
                    "Found errors in sandbox stdout or stderr, on trial #%s." % trial)

        ### print "Column #", colX, "completed\n"
        colResultList.append(result)

    return colResultList


