import sys
from operator import itemgetter

allResults = {}
prefix = ''

def addFileToResults(filename):
    print '\t' + filename
    infile = open(filename, 'r')

    # Determine if this is a special experiment
    global prefix
    if prefix == '':
        prefix = filename.split('/')[-1].split('_')[-1].split('.')
        if len(prefix) > 2:
            prefix = '.' + prefix[-2]
        else:
            prefix = ''
    
    for line in infile.readlines():
        parts = line.split()
        if len(parts) < 7:
            continue
        
        addedHW = float(parts[5])
        addedSW = float(parts[6])
        chkpt = float(parts[7])
        reex = float(parts[8])

        # Only keep results with no write buffer or write-back buffer
        #if int(parts[1]) > 0 or int(parts[2]) > 0:
        # Only keep results with no write-back buffer
        #if int(parts[2]) > 0:
        #    continue

        key = parts[0] + '\t' + parts[1] + '\t' + parts[2] + '\t' + parts[3] + '\t' + parts[4]
        if key not in allResults:
            allResults[key] = [addedHW, addedSW, chkpt, reex, 1]
        else:
            allResults[key][0] += addedHW
            allResults[key][1] += addedSW
            allResults[key][2] += chkpt
            allResults[key][3] += reex
            allResults[key][4] += 1

def averageResults():
    for res in allResults:
        allResults[res][0] /= allResults[res][4]
        allResults[res][1] /= allResults[res][4]
        allResults[res][2] /= allResults[res][4]
        allResults[res][3] /= allResults[res][4]

def warnIfUnequalNumResults():
    numResults = 0
    for res in allResults:
        if numResults == 0:
            numResults = allResults[res][4]
        elif allResults[res][4] != numResults:
            print 'WARNING: Unequal number of results'
            return

def main():
    if len(sys.argv) < 2:
        print 'Usage: python combineResults.py [files.dat]+'
        return

    print 'Processing:'
    for fn in sys.argv[1:]:
        addFileToResults(fn)

    warnIfUnequalNumResults()

    averageResults()

    convertDictToList()

    sortResultsBySWThenHW()

    printResults('all', sys.argv[1].split('_')[-2])

    removeNonParetoPoints()

    printResults('pareto', sys.argv[1].split('_')[-2])

def printResults(pType, config):
    outfile = open('combined_' + config + '_' + pType + prefix + '.dat', 'w') 

    for res in allResults:
        for part in res:
            outfile.write(str(part) + '\t')
        outfile.write('\n')

    outfile.close()

def convertDictToList():
    global allResults
    temp = []
    for res in allResults:
        temp.append((res, allResults[res][0], allResults[res][1], allResults[res][2], allResults[res][3]))

    allResults = temp

def sortResultsBySWThenHW():
    global allResults
    # Sort the results by chkpt overhead, then hw overhead
    temp = sorted(allResults, key=itemgetter(3, 1))
    allResults = temp

def removeNonParetoPoints():
    global allResults
    pareto = []
        
    # Keep only pareto data points
    lowestHWOverhead = 9999.9
    highestSWOverhead = 0.0
    for res in allResults:
        if res[1] < lowestHWOverhead and res[3] > highestSWOverhead:
            pareto.append(res)
            # Update the new lowest hw overhead
            lowestHWOverhead = res[1]
            highestSWOverhead = res[3]
    
    # Replace the non-pareto results with the pareto results
    allResults = pareto

##############################################################################

main()
