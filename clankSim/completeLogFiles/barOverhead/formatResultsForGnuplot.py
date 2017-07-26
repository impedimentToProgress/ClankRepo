import sys
from operator import itemgetter

def main():
    f = open(sys.argv[1])

    tots_hw = [0,0,0,0,0,0]
    tots_chkpt = [0,0,0,0,0,0]
    tots_total = [0,0,0,0,0,0]
    count = 0

    for line in f.readlines():
        if len(line) < 5:
            continue
        
        # Create filler line and throw away one result
        if count % 6 != 4:
            print line
            # Keep total to calc average
            tots_hw[count % 6] = tots_hw[count % 6] + float(line.split('\t')[9])
            tots_chkpt[count % 6] = tots_chkpt[count % 6] + float(line.split('\t')[8])
            tots_total[count % 6] = tots_total[count % 6] + float(line.split('\t')[7])

            if count % 6 == 5 and count <> 0:
                print 'space\t0\t0\t0\t0\t0\t0\t0\t0\t0'

        count = count + 1

    count = count / 6
    print 'space\t0\t0\t0\t0\t0\t0\t0\t0\t0'
    for x in xrange(0, 6):
        print 'average', (x+1), '\tx\tx\tx\tx\tx', tots_total[x]/count, tots_chkpt[x]/count, tots_hw[x]/count
    print 'space\t0\t0\t0\t0\t0\t0\t0\t0\t0'
    
##############################################################################

main()
