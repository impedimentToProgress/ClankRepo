set terminal postscript eps enhanced color dashlength 4 lw 3 'Times-Roman' 20;
set size 1.0,1.0;
set output 'SysComplexity.eps';
set grid xtics ytics;
set ylabel "Average Checkpoint Overhead";
set xlabel "Buffer Capacity (bits)";
set yrange [1.0: 1.5]
set xrange [0: 1000]
set xtics mirror
set ytics mirror
set key
set datafile separator "\t"

set arrow from 30, 1.00 to 30, 1.50 nohead ls 0 lw 8 
#set arrow from 0, 1.01 to 1000, 1.01 nohead ls 0 lw 8

set ytics ("0\%%" 1.00, "5\%%" 1.05, "10\%%" 1.10, "15\%%" 1.15, "20\%%" 1.20, "25\%%" 1.25, "30\%%" 1.30, "35\%%" 1.35, "40\%%" 1.40, "45\%%" 1.45, "50\%%" 1.50)

plot 'R_XX_pareto.NoAPB.dat' u 6:8 w lines dt 2 lc rgb "black" t "R",\
     'RW_XX_pareto.NoAPB.dat' u 6:8 w lines dt 3 lc rgb "black" t "R+W",\
     'RWWB_XX_pareto.NoAPB.dat' u 6:8 w lines dt 4 lc rgb "black" t "R+W+B",\
     'RWWBAPB_XX_pareto.dat' u 6:8 w lines dt 5 lc rgb "black" t "R+W+B+A",\
     'RWWBAPBC_XX_pareto.dat' u 6:8 w lines ls 1 lc rgb "black" t "R+W+B+A+C" ;



