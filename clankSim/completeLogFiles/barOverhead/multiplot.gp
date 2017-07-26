set terminal postscript eps enhanced monochrome dashed dashlength 4 'Times-Roman' 20 size 7, 4;
set output 'overheadBar.eps';
set multiplot layout 2, 1 title "Total Overhead (x baseline)" font ",18"
set tmargin .5
unset title
set grid ytics;
set xrange [0:72]
set yrange [1:1.6]

set ytics ("1.0" 1, "1.1" 1.1, "1.2" 1.2, "1.3" 1.3, "1.4" 1.4, "1.5" 1.5, "1.6" 1.6)

set xtics ("adpcm\\_dec" 3, "adpcm\\_enc" 9, "aes" 15, "basicmath" 21, "bitcount" 27, "blowfish" 33, "crc" 39, "dijkstra" 45, "fft" 51, "limits*" 57, "lzfx" 63, "overflow*" 69, "patricia" 75, "picojpeg" 81, "qsort" 87, "randmath*" 93, "rc4" 99, "regress" 105, "rsa" 111, "sha" 117, "stringsearch" 123, "susan" 129, "vcflags*" 135, "average" 141) font ",16" nomirror 

set label "16,0,0,0" at 1.0,1.11 tc rgb "black" font "Times Bold,14" rotate left front
set label "8,8,0,0" at 2.0,1.11 tc rgb "black" font "Times Bold,14" rotate left front
set label "8,4,2,0" at 3.0,1.11 tc rgb "black" font "Times Bold,14" rotate left front
set label "16,8,4,4" at 4.0,1.11 tc rgb "black" font "Times Bold,14" rotate left front
set label "16,8,4,4+C+WDT" at 5.0,1.11 tc rgb "black" font "Times Bold,14" rotate left front

set label "1.657" at 13.0,1.48 tc rgb "black" font "Times Bold,14" rotate left front
set key font ",14"

plot 'overheadBar.txt' u :8 w boxes fs solid 1 ls 1 lw 2 t 'Re-execution',\
     'overheadBar.txt' u :9 w boxes fs solid .65 ls 1 lw 2 t 'Checkpoint' ,\
     'overheadBar.txt' u :10 w boxes fs solid .35 ls 1 lw 2 t 'Hardware' ;

set xlabel "Benchmark";
set xrange [72:144]
unset key

set label "16,0,0,0" at 139.0,1.38 tc rgb "black" font "Times Bold,14" rotate left front
set label "8,8,0,0" at 140.0,1.40 tc rgb "black" font "Times Bold,14" rotate left front
set label "8,4,2,0" at 141.0,1.40 tc rgb "black" font "Times Bold,14" rotate left front
set label "16,8,4,4" at 142.0,1.379 tc rgb "black" font "Times Bold,14" rotate left front
set label "16,8,4,4+C+WDT" at 143.0,1.207 tc rgb "black" font "Times Bold,14" rotate left front

set arrow from 138,1 to 138,1.6 nohead ls 0 lw 6

set label "1.727" at 97.0,1.48 tc rgb "white" font "Times Bold,14" rotate left front
set label "1.713" at 98.0,1.48 tc rgb "white" font "Times Bold,14" rotate left front
set label "1.749" at 99.0,1.48 tc rgb "white" font "Times Bold,14" rotate left front
set label "2.717" at 115.0,1.48 tc rgb "white" font "Times Bold,14" rotate left front
set label "2.684" at 116.0,1.48 tc rgb "white" font "Times Bold,14" rotate left front

plot 'overheadBar.txt' u :8 w boxes fs solid 1 ls 1 lw 2 t 'Re-execution',\
     'overheadBar.txt' u :9 w boxes fs solid .65 ls 1 lw 2 t 'Checkpoint' ,\
     'overheadBar.txt' u :10 w boxes fs solid .35 ls 1 lw 2 t 'Hardware' ;
