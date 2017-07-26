set terminal postscript eps enhanced color dashlength 2 lw 2 'Times-Roman' 20;
set size 2.0,1.0;
set output 'TypicalOverhead.eps';
set grid ytics;
set xlabel "Benchmark";
set ylabel "Total Overhead (x baseline)";
#set xrange [0:38]
#set yrange [1:2.5]

set xtics ("basicmath" 2, "dijkstra" 6, "fft" 10, "patricai" 14, "jpeg" 18, "sha" 22, "string" 26, "avg" 31, "NV-CPU" 34, "Dino" 36) font ",20" nomirror 

#set ytics ("0" 0, "0.5" 50, "1.0" 100, "1.5" 150, "2.0" 200, "" 25, "" 75, "" 125, "" 175) mirror

set label "D" at 1.0,1.3 tc rgb "black" font ",20" rotate left front
set label "D+WBB" at 2.0,1.3 tc rgb "black" font ",20" rotate left front
set label "D+WBB+WDT+C" at 3.0,1.3 tc rgb "black" font ",20" rotate left front

set arrow from 28.5,1 to 28.5,2.5 nohead ls 0 lw 10

plot 'typicalOverhead.x.txt' u 7 w boxes fs solid .35 lc rgb "black" lw 0 t 'Re-execution' ,\
     'typicalOverhead.x.txt' u 6 w boxes fs solid .65 lc rgb "black" lw 0 t 'Checkpoint' ,\
     'typicalOverhead.x.txt' u 1 w boxes fs solid 1 lc rgb "black" lw 0 t 'Hardware' ;
#     'relOver.txt' u 1:2 w boxes fs solid 1 ls 1 lw 2 t '';
