# DAA Assignment 3 - Graphs
Java 17+ Maven project implementing DSU, Kruskal, Prim, iterative Kosaraju, Kahn, and DAG shortest paths. Bonus tasks are intentionally omitted. See report.pdf for proofs, complexity, reference checks, analysis, and plots.

RUN:

For running this assignment you have to first of all runs algorithmic test and after succesfully testing run Benchmark and you will got results.csv







Layout

- `src/main/java/daa/Graph.java`: shared adjacency-list graph and file reader.
- `src/main/java/daa/DSU.java` and `Metrics.java`: disjoint sets and operation counters.
- `src/main/java/daa/Kruskal.java`, `Prim.java`, `Kosaraju.java`, `Kahn.java`, and `DagShortestPath.java`: one class per algorithm.
- `src/main/java/daa/MSTResult.java`, `SCCResult.java`, and `PathResult.java`: result types.
- `src/main/java/daa/Benchmark.java`: exact seeded generators, measurements, cross-checks, CSV writer.
- `src/test/java/daa/AssignmentTest.java`: JUnit 5 tests and independent small-graph oracles.
- `data/`: small disconnected, cyclic, and negative-weight DAG inputs. Format: `n m directed|undirected`, followed by `u v w` per edge.
- `results/results.csv`: 50 benchmark rows; `results/plots/`: three requested PNG charts.
- `REPORT.md`: explanation of design, correctness, metrics, and algorithm choice.

`Graph` accepts self-loops, parallel edges, isolated vertices, and disconnected graphs. The MST methods return a forest with a component count. Kahn returns `Optional.empty()` for a cycle. `dagShortestPath` rejects cyclic graphs; every zero-indegree vertex starts at distance zero. `Paths.pathTo(v)` reconstructs the chosen route. Metrics count DSU `find` calls and successful unions, edge scans, weight comparisons, and explicit DFS stack depth.
