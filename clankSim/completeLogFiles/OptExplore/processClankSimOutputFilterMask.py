import sys

sys.path.append('../../')
import processClankSimOutput
from processClankSimOutput import HW_BITS_BASE

hexMask = 0

def main():
    if len(sys.argv) != 3:
        print 'Usage: python processClankSimOutputFilterMask.py hexMask sim.log'
        print 'Only outputs the best "XX" configuration'
        print 'Ignores results for configurations that DON\'T have a "1" in the same position that the passed hexMask has a "1"'
        print 'I.e., this outputs the best result for each buffer config that has the Clank optimzation(s) encoded in hexMask enabled'
        return

    filename = sys.argv[2]
    global hexMask
    hexMask = int(sys.argv[1], 16)

    # Determine if this is a special experiment
    processClankSimOutput.prefix = filename.split('/')[-1].split('.')
    if len(processClankSimOutput.prefix) > 2:
        processClankSimOutput.prefix = '.' + processClankSimOutput.prefix[-2]
    else:
        processClankSimOutput.prefix = ''

    collectFileNames(filename)
    print processClankSimOutput.benchmarks

    collectResultsPerBenchmark(filename)

    print 'Unsorted'
    for bench in xrange(0, len(processClankSimOutput.allResults)):
        print processClankSimOutput.allResults[bench][0]

    processClankSimOutput.sortResultsBySWThenHW()

    print '\nSorted'
    for bench in xrange(0, len(processClankSimOutput.allResults)):
        print processClankSimOutput.allResults[bench][0]
    
    processClankSimOutput.printResultsAllBest()

def collectFileNames(filename):
    infile = open(filename, 'r')
    
    numResults = 0
    ignoreConfig = False
    for line in infile.readlines():
        parts = line.strip().split(' ')
        if len(parts) == 2 and parts[0] == 'File:':
            subparts = parts[1].split('/')

            # Check if this is a config that we need to ignore
            config = int(subparts[-1].split('_')[-1], 16)
            if config != hexMask:
                numResults = 0
                continue

            if numResults == 0 and len(processClankSimOutput.benchmarks) > 0:
                processClankSimOutput.benchmarks[-1] = subparts[-1]
            else:
                processClankSimOutput.benchmarks.append(subparts[-1])

            numResults = 0
        else:
            numResults = numResults + 1

    infile.close()

def collectResultsPerBenchmark(filename):
    benchmark = 0
    results = []
    infile = open(filename, 'r')
    ignoreConfig = False

    for line in infile.readlines():
        # Start saving results for the next benchmark
        if line.startswith('File:'):
            ignoreConfig = False

            if len(results) > 0:
                processClankSimOutput.allResults.append(results)
            results = []

            # Check if this is a config that we need to ignore
            config = int(line.split('/')[-1].split('_')[-1], 16)
            if config != hexMask:
                ignoreConfig = True

            continue

        if ignoreConfig:
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

    if not ignoreConfig:
        processClankSimOutput.allResults.append(results)

    assert(len(processClankSimOutput.benchmarks) == len(processClankSimOutput.allResults))

##############################################################################

main()
