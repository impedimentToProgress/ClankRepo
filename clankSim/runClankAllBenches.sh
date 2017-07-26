#!/bin/sh
PATH_TO_BENCH=../thumbulator/tests

EXT=bin.out
#EXT=bin.out.pi

#RESFILE_EXT=.log
#RESFILE_EXT=.pi.log
RESFILE_EXT=.NoAPB.log
#RESFILE_EXT=.NoAPB.pi.log

FLAGS='-Xms16G -Xmx16G'

#java $FLAGS Clank $PATH_TO_BENCH/vcflags.$EXT 8 > vcflags$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/regress.$EXT 8 > regress$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/randmath.$EXT 8 > randmath$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/limits.$EXT 8 > limits$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/overflow.$EXT 8 > overflow$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/crc.$EXT 8 > crc$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/rsa.$EXT 8 > rsa$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/rc4.$EXT 8 > rc4$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/aes.$EXT 8 > aes$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/lzfx.$EXT 8 > lzfx$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/adpcm_encode.$EXT 8 > adpcm_encode$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/picojpeg.$EXT 8 > picojpeg$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/susan.$EXT 8 > susan$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/adpcm_decode.$EXT 8 > adpcm_decode$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/patricia.$EXT 8 > patricia$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/sha.$EXT 8 > sha$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/blowfish.$EXT 8 > blowfish$RESFILE_EXT
#java $FLAGS Clank $PATH_TO_BENCH/bitcount.$EXT 8 > bitcount$RESFILE_EXT
java $FLAGS Clank $PATH_TO_BENCH/dijkstra.$EXT 8 > dijkstra$RESFILE_EXT
java $FLAGS Clank $PATH_TO_BENCH/basicmath.$EXT 8 > basicmath$RESFILE_EXT
java $FLAGS Clank $PATH_TO_BENCH/stringsearch.$EXT 8 > stringsearch$RESFILE_EXT
java $FLAGS Clank $PATH_TO_BENCH/fft.$EXT 8 > fft$RESFILE_EXT
java $FLAGS Clank $PATH_TO_BENCH/qsort.$EXT 8 > qsort$RESFILE_EXT
