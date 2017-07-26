import sys
from operator import itemgetter

MEM_BYTES = 65536
HW_BITS_BASE = 1#(16 * 32) + (MEM_BYTES * 8)

benchmarks = []
allResults = []
prefix = ''

def main():
    if len(sys.argv) != 2:
        print 'Usage: python processClankSimOutput.py sim.log'
        return

    filename = sys.argv[1]

    # Determine if this is a special experiment
    global prefix
    prefix = filename.split('/')[-1].split('.')
    if len(prefix) > 2:
        prefix = '.' + prefix[-2]
    else:
        prefix = ''

    collectFileNames(filename)
    print benchmarks

    collectResultsPerBenchmark(filename)

    print 'Unsorted'
    for bench in xrange(0, len(allResults)):
        print allResults[bench][0]

    sortResultsBySWThenHW()

    print '\nSorted'
    for bench in xrange(0, len(allResults)):
        print allResults[bench][0]
    
    printResultsPerBench('all')

    printResultsAllBest()

    removeNonParetoPointsFromEachBenchmark()

    print '\nPareto'
    for bench in xrange(0, len(allResults)):
        print allResults[bench][0]

    printResultsPerBench('pareto')

    printParetoResultsSuper()

def sortResultsBySWThenHW():
    # Create a new set of results, but sorted
    for bench in xrange(0, len(allResults)):
        # Sort the results by run time overhead, then hw overhead
        sortedResults = sorted(allResults[bench], key=itemgetter(7, 5))
        
        allResults[bench] = sortedResults

def printResultsPerBench(pScope):
    for bench in xrange(0, len(allResults)):
        # Print a file for gnuplot with all results
        outfile = open(benchmarks[bench] + '_' + pScope + prefix + '.dat', 'w')

        for res in allResults[bench]:
            for item in res:
                outfile.write(str(item) + '\t')
            outfile.write('\n')

        outfile.close()

def printResultsAllBest():
    best = {}
    for bench in xrange(0, len(allResults)):
        if len(allResults[bench]) == 0:
            continue
        for res in allResults[bench]:
            key = str(res[0]) + '\t' + str(res[1]) + '\t' + str(res[2]) + '\t' + str(res[3]) + '\t' + str(res[4])
            if key in best:
                if res[7] <= best[key][1]:
                    best[key] = [res[5], res[6], res[7], res[8], benchmarks[bench]]
            else:
                best[key] = [res[5], res[6], res[7], res[8], benchmarks[bench]]

    unsorted = []
    for key in best:
        unsorted.append((key, best[key][0], best[key][1], best[key][2], best[key][3], best[key][4]))

    sortedResults = sorted(unsorted, key=itemgetter(3, 1))

    # Print a file for gnuplot with all results
    outfile = open(benchmarks[bench][0:-2] + 'XX_all' + prefix + '.dat', 'w')
    
    for item in sortedResults:
        for res in item:
            outfile.write(str(res) + '\t')
        outfile.write('\n')

    outfile.close()


def removeNonParetoPointsFromEachBenchmark():
    for bench in xrange(0, len(allResults)):
        pareto = []
        
        # Keep only pareto data points
        lowestHWOverhead = 9999.9
        highestSWOverhead = 0.0
        for res in allResults[bench]:
            if res[5] < lowestHWOverhead and res[7] > highestSWOverhead:
                pareto.append(res)
                # Update the new lowest hw overhead
                lowestHWOverhead = res[5]
                highestSWOverhead = res[7]
    
        # Replace the non-pareto results with the pareto results
        allResults[bench] = pareto

def printParetoResultsSuper():
    # Build a combined Pareto list from all benchmarks
    # Annotate with benchmark name along the way
    currentPareto = []
    for bench in xrange(0, len(allResults)):
        for res in allResults[bench]:
            res.append(benchmarks[bench])
            currentPareto.append(res)

    # Sort the results by run time overhead, then hw overhead
    sortedResults = sorted(currentPareto, key=itemgetter(7, 5))

    # Print a file for gnuplot with only results on the pareto frontier
    outfile = open(benchmarks[bench][0:-2] + 'XX_pareto' + prefix + '.dat', 'w')
        
    lowestHWOverhead = 9999.9
    highestSWOverhead = 0.0
    for res in sortedResults:
        if res[5] < lowestHWOverhead and res[7] > highestSWOverhead:
            for item in res:
                outfile.write(str(item) + '\t')
            outfile.write('\n')
                
            # Update the new lowest hw overhead
            lowestHWOverhead = res[5]
            highestSWOverhead = res[7]

    outfile.close()

def collectFileNames(filename):
    infile = open(filename, 'r')
    
    numResults = 0
    for line in infile.readlines():
        parts = line.strip().split(' ')
        if len(parts) == 2 and parts[0] == 'File:':
            subparts = parts[1].split('/')
            #subparts = subparts[len(subparts) - 1].split('.')
            if numResults == 0 and len(benchmarks) > 0:
                benchmarks[len(benchmarks) - 1] = subparts[len(subparts) - 1]
            else:
                benchmarks.append(subparts[len(subparts) - 1])

            numResults = 0
        else:
            numResults = numResults + 1

    infile.close()

def collectResultsPerBenchmark(filename):
    benchmark = 0
    results = []
    infile = open(filename, 'r')

    for line in infile.readlines():
        # Start saving results for the next benchmark
        if line.startswith('File:'):
            if len(results) > 0:
                allResults.append(results)
            results = []
            continue

        parts = line.strip().split(',')
        if len(parts) >= 4:
            readBufferEntries = int(parts[0])
            writeBufferEntries = int(parts[1])
            writebackBufferEntries = int(parts[2])
            entryBits = int(parts[3])
            addressPrefixEntries = int(parts[4])
            addedBits = (float(parts[6]) / HW_BITS_BASE) + 1
            runtime = float(parts[9]) / 100
            
            reex = float(parts[8]) / 100
            chkpt = float(parts[7]) / 100

            results.append([readBufferEntries, writeBufferEntries, writebackBufferEntries, entryBits, addressPrefixEntries, addedBits, runtime, chkpt, reex])

    allResults.append(results)

    # Make sure that the number of benchmarks agrees with what we read from the file
    assert(len(benchmarks) == len(allResults))

##############################################################################

if __name__ == "__main__":
    main()
