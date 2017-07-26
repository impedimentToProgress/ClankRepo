find . -d 1 -name "*.log" -exec python ../processClankSimOutput.py {} \;

find . -d 1 -name "*.bin.out.pi_XX_all.pi.dat" -exec grep ^"8\\t1\\t8\\t6\\t4" {} \; > typicalOverhead.pi.dat
python ../combineResults.py *.bin.out.pi_XX_all.pi.dat
grep ^"8\\t1\\t8\\t6\\t4" combined_XX_all.pi.dat >> typicalOverhead.pi.dat

find . -d 1 -name "*.bin.out_XX_all.dat" -exec grep ^"8\\t1\\t8\\t6\\t4" {} \; > typicalOverhead.dat
python ../combineResults.py *.bin.out_XX_all.dat
grep ^"8\\t1\\t8\\t6\\t4" combined_XX_all.dat >> typicalOverhead.dat

rm *.dat