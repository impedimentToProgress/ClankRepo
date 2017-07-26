#!/usr/bin/python

import sys

def main():
    if len(sys.argv) != 2:
        print "Usage: python parseWDTs.py file.ext"
        return

    infile = open(sys.argv[1], "r")

    time = ""
    best = 9999.0
    bestLine = ""
    for line in infile:
        line = line.strip()
        parts = line.split(" ")

        # Check for time or result statement
        # if not, find the best result for the current time
        if len(parts) != 3: 
            # Result lines have 8 parts
            if len(parts) != 8:
                continue
            # Remember the best result so we can print it later
            thisOvr = float(parts[7])
            if thisOvr < best:
                best = thisOvr
                bestLine = line

        if parts[0] == "On":
            time = parts[2]
        elif parts[0] == "Best":
            resParts = bestLine.split(",")
            print time + "\t" + parts[2] + "\t" + resParts[5].strip() + "\t" + resParts[6].strip() + "\t" + resParts[7].strip()

    infile.close()
###############################################

main()
