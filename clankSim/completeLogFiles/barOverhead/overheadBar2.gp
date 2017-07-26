set terminal postscript eps enhanced monochrome dashed dashlength 4 'Times-Roman' 20;
set size 2.0,1.0;
set output 'overheadBar2.eps';
set grid ytics;
set xlabel "Benchmark";
set ylabel "Total Overhead (x baseline)";
set xrange [72:144]
set yrange [1:1.6]

set xtics ("adpcm dec" 3, "adpcm enc" 9, "aes" 15, "basicmath" 21, "bitcount" 27, "blowfish" 33, "crc" 39, "dijkstra" 45, "fft" 51, "limits" 57, "lzfx" 63, "overflow" 69, "patricia" 75, "picojpeg" 81, "qsort" 87, "randmath" 93, "rc4" 99, "regress" 105, "rsa" 111, "sha" 117, "stringsearch" 123, "susan" 129, "vcflags" 135, "AVERAGE" 141) font ",20" nomirror 

set label "8,8,0,0" at 139.0,1.315 tc rgb "black" font ",20" rotate left front
set label "8,4,2,0" at 140.0,1.315 tc rgb "black" font ",20" rotate left front
set label "16,8,4,4" at 141.0,1.301 tc rgb "black" font ",20" rotate left front
set label "16,8,4,4+WDT" at 142.0,1.219 tc rgb "black" font ",20" rotate left front
set label "16,8,4,4+WDT+C" at 143.0,1.184 tc rgb "black" font ",20" rotate left front

set arrow from 138,1 to 138,1.5 nohead ls 0 lw 10

set label "1.713" at 97.0,1.5 tc rgb "white" font ",20" rotate left front
set label "1.749" at 98.0,1.5 tc rgb "white" font ",20" rotate left front
set label "2.684" at 115.0,1.5 tc rgb "white" font ",20" rotate left front

plot 'overheadBar.txt' u :8 w boxes fs solid 1 ls 1 lw 2 t 'Re-execution',\
     'overheadBar.txt' u :9 w boxes fs solid .65 ls 1 lw 2 t 'Checkpoint' ,\
     'overheadBar.txt' u :10 w boxes fs solid .35 ls 1 lw 2 t 'Hardware' ;
