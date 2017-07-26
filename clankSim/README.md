### Compile the benchmarks

see `../thumbulator/tests/buildAll.sh`

### Create memory trace logs

Configure Thumbulator to output memory accesses.

Recompile Thumbulator.

For each benchmark (`.bin` file in `../thumbulator/tests`), run `./sim_main benchmark.bin > memAccessLog.out` to simulate the benchmark and generate and save a memory access log file for the benchmark.

### Remove program idempotent memory accesses

run `filterDynamicProgramIdempotentAccesses.py`, passing a memory trace log file as a command line parameter and redirecting output to a new memory trace log file.

### Simulate Clank

Configure the search space in `Clank.java`.

Recompile all java files.

Update the path and memory access log files to simulate in `runClankAllBenches.sh`.

Run Clank simulation.

`sh runClankAllBenches.sh`

This creates a Clank sim results file (`.log`) for each memory access log file simulated.

### Process Clank simulation results

`find . -name "*.log" -exec python ../processClankSimOutput.py {} \;`

This creates a `.dat` file for each configuration of Clank and one that holds the best results accross configurations simulated for each benchmark.  The `.dat` file is usefull for plotting Clank simulation results. There are two sets of `.dat` files produced, one contains all results sorted by software overhead and a file that contains only Pareto optimal results.

### Generate benchmark-average results

`python combineResults *[.out | .pi]_XX_all.dat`
