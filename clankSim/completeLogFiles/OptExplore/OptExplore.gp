set terminal postscript eps enhanced color dashlength 2 lw 2 'Times-Roman' 20;
set size 1.0,1.0;
set output 'OptExplore.eps';
set grid xtics ytics;
set ylabel "Average Checkpoint Overhead";
set xlabel "Buffer Capacity (bits)";
set yrange [1.0: 1.4]
set xrange [0: 1000]
set xtics mirror
set ytics mirror
set key
set datafile separator "\t"

set arrow from 30, 1.00 to 30, 1.40 nohead ls 0 lw 8 
#set arrow from 0, 1.01 to 1000, 1.01 nohead ls 0 lw 8


set ytics ("0\%%" 1.00, "5\%%" 1.05, "10\%%" 1.10, "15\%%" 1.15, "20\%%" 1.20, "25\%%" 1.25, "30\%%" 1.30, "35\%%" 1.35, "40\%%" 1.40)

plot 'combined_00_pareto.pi.dat' u 6:8 w lines lc rgb "#C0C0C0" lw 3 t "No Optimizations",\
     'combined_1F_pareto.pi.dat' u 6:8 w lines lc rgb "grey50" lw 3 t "All Optimizations",\
     'combined_XX_pareto.pi.dat' u 6:8 w lines lc rgb "black" lw 3t "Profiled",\
     'combined_XX_pareto.pi.01.dat' u 6:8 w lines dt 2 lc rgb "red" t "Ignore False Writes",\
     'combined_XX_pareto.pi.02.dat' u 6:8 w lines dt 3 lc rgb "blue" t "Remove Duplicates",\
     'combined_XX_pareto.pi.04.dat' u 6:8 w lines dt 4 lc rgb "green" t "No WF Overflow",\
     'combined_XX_pareto.pi.08.dat' u 6:8 w lines dt 5 lc rgb "orange" t "Ignore TEXT",\
     'combined_XX_pareto.pi.10.dat' u 6:8 w lines dt 6 lc rgb "purple" t "Latest Chkpt";



