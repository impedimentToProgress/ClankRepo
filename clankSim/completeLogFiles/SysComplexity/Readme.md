To generate the data files for the SysComplexity plot:

Process ClankSim logs:

find ../ -d 1 -name "*.log" -exec python ../../processClankSimOutput.py {} \;

Edit ../../combineResults.py by uncommenting the appropriate lines to filter out results with buffers that you do not want (around line 30).

For the R, R+W, and R+W+WBB cases, run the command below and rename the resulting outputs accordingly:

python ../../combineResults.py `ls *.bin.out_XX_all.NoAPB.dat`
manually: combined*.NoAPB.dat -> (R|RW|RWWB)*.NoAPB.dat

For the R+W+WBB+AP case run:

python ../../combineResults.py *.bin.out_XX_all.dat
manually: combined*.dat -> RWWBAPB*.dat

Finally, for the R+W+WBB+AP+C case run:

python ../../combineResults.py *.bin.out.pi_XX_all.pi.dat
manually: combined*.dat -> RWWBAPBC*.dat

Note that you only need to keep the pareto results.
