import sys

possibleProgramIdempotent = {}
impossibleProgramIdempotent = {}

def main():
    if len(sys.argv) != 2:
        print 'Usage: python filterDynamicProgramIdempotentAccesses.py memAccesses.log'
        return

    filename = sys.argv[1]

    infile = open(filename, 'r')
    for line in infile.readlines():
        parts = line.split()

        # Ignore any abnormal lines
        if len(parts) < 5:
            continue

        type = parts[2]
        address = parts[3]

        # Make sure the address isn't already in the impossible list
        if address in impossibleProgramIdempotent:
            continue

        # If the address is in the possible list already, make sure
        # the type of access jives with the original type
        if address in possibleProgramIdempotent:
            typeOrig = possibleProgramIdempotent[address]

            assert(typeOrig == 'R' or typeOrig == 'W')

            # No problem if this access is the same as the original
            if typeOrig == type:
                continue
            else:
                # W->R is fine, as long as another write never comes along
                if typeOrig == 'W':
                    possibleProgramIdempotent[address] = 'R'
                # If original type was read, only reads can follow
                else:
                    del possibleProgramIdempotent[address]
                    impossibleProgramIdempotent[address] = type
        else:
            possibleProgramIdempotent[address] = type

    infile.close()

    #print 'Number of possible Program Idempotent memory addresses:',
    #print len(possibleProgramIdempotent)
    #print 'Number of impossible memory addresses:',
    #print len(impossibleProgramIdempotent)

    infile = open(filename, 'r')
    for line in infile.readlines():
        parts = line.split()

        if len(parts) < 5:
            print line
            continue

        type = parts[2]
        address = parts[3]

        if address in possibleProgramIdempotent:
            continue

        print line

##############################################################################

main()