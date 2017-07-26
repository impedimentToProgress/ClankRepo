#!/bin/sh

find ../ -name "*.pi.log" -exec python processClankSimOutputFilterMask.py 01 {} \;
python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat
mv combined_XX_pareto.pi.dat combined_XX_pareto.pi.01.dat

find ../ -name "*.pi.log" -exec python processClankSimOutputFilterMask.py 02 {} \;
python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat
mv combined_XX_pareto.pi.dat combined_XX_pareto.pi.02.dat

find ../ -name "*.pi.log" -exec python processClankSimOutputFilterMask.py 04 {} \;
python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat
mv combined_XX_pareto.pi.dat combined_XX_pareto.pi.04.dat

find ../ -name "*.pi.log" -exec python processClankSimOutputFilterMask.py 08 {} \;
python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat
mv combined_XX_pareto.pi.dat combined_XX_pareto.pi.08.dat

find ../ -name "*.pi.log" -exec python processClankSimOutputFilterMask.py 10 {} \;
python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat
mv combined_XX_pareto.pi.dat combined_XX_pareto.pi.10.dat

rm combined_XX_all.pi.dat

# 00, 1F, XX
find ../ -name "*.pi.log" -exec python ../../processClankSimOutput.py {} \;
python ../../combineResults.py *.bin.out.pi_00_all.pi.dat
python ../../combineResults.py *.bin.out.pi_1F_all.pi.dat
python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat

# Generate the plot
gnuplot OptExplore.gp

# Clean things up
rm *.bin.out.pi_*_all.pi.dat
rm *.bin.out.pi_*_pareto.pi.dat
