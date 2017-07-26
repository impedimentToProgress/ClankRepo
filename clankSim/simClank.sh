#!/bin/sh
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/basicmath.bin > basicmath.txt &
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/dijkstra.bin > dijkstra.txt &
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/fft.bin > fft.txt &
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/patricia.bin > patricia.txt &
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/picojpeg.bin > picojpeg.txt &
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/sha.bin > sha.txt &
./sim_main ~/Desktop/ropcop-docs/thumbulator/bareBench/stringsearch.bin > stringsearch.txt &

java Clank basicmath.txt > basicmath.log &
java Clank sha.txt > sha.log &
java Clank patricia.txt > patricia.log &
java Clank stringsearch.txt > stringsearch.log &
java Clank picojpeg.txt > picojpeg.log &
java Clank fft.txt > fft.log &
java Clank dijkstra.txt > dijkstra.log &

