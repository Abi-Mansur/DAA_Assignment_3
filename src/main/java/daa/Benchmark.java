package daa;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;


public final class Benchmark {
    private Benchmark() {}

    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final long[] EXPECTED_MST = {
            147970, 30363, 468911, 97589, 1486139,
            309550, 4715515, 968517, 15012094, 3056681
    };
    private static final long[] EXPECTED_SCC = {
            27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1
    };
    private static final long[] EXPECTED_DISTANCE_SUM = {
            396274, 252411, 1271911, 723157, 3941828,
            2381342, 12551891, 7376612, 40096539, 23512087
    };

    public static Graph generate(int n, int m, String kind) {
        boolean undirected = kind.equals("undirected");
        boolean dag = kind.equals("dag");
        if (!undirected && !dag && !kind.equals("directed")) {
            throw new IllegalArgumentException("Unknown graph kind: " + kind);
        }

        Random random = new Random(42);
        Graph graph = new Graph(n, !undirected);
        int[] rank = new int[n];
        if (undirected || dag) {
            for (int i = 0; i < n; i++) {
                rank[i] = i;
            }
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = rank[i];
                rank[i] = rank[j];
                rank[j] = temp;
            }
        }

        if (undirected) {
            for (int i = 1; i < n; i++) {
                int parent = rank[random.nextInt(i)];
                int child = rank[i];
                graph.addEdge(parent, child, 1 + random.nextInt(1000));
            }
        }

        while (graph.edges().size() < m) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);
            while (to == from) {
                to = random.nextInt(n);
            }
            if (dag && rank[from] > rank[to]) {
                int temp = from;
                from = to;
                to = temp;
            }
            graph.addEdge(from, to, 1 + random.nextInt(1000));
        }
        return graph;
    }

    private record Row(
            String algorithm, String graph, int n, int m,
            double timeMs, Metrics metrics, long result) {
        String csv() {
            return String.format(Locale.ROOT,
                    "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d",
                    algorithm, graph, n, m, timeMs,
                    metrics.dsuOps, metrics.edgeScans,
                    metrics.comparisons, metrics.maxDepth, result);
        }
    }

    @FunctionalInterface
    private interface Run {
        long execute(Metrics metrics);
    }

    private static Row measure(String algorithm, String graphName, Graph graph, Run run) {
        long[] times = new long[5];
        long result = Long.MIN_VALUE;
        Metrics firstMetrics = null;
        for (int trial = 0; trial < times.length; trial++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            long current = run.execute(metrics);
            times[trial] = System.nanoTime() - start;
            if (trial == 0) {
                result = current;
                firstMetrics = metrics;
            } else if (result != current) {
                throw new AssertionError("Non-deterministic result: " + algorithm);
            }
        }
        Arrays.sort(times);
        return new Row(algorithm, graphName, graph.vertexCount(), graph.edges().size(),
                times[2] / 1_000_000.0, firstMetrics, result);
    }

    private static void check(String name, long actual, long expected, int n, int m) {
        if (actual != expected) {
            throw new AssertionError(name + " n=" + n + " m=" + m
                    + ": got " + actual + ", expected " + expected);
        }
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        Files.createDirectories(output.getParent());
        ArrayList<Row> rows = new ArrayList<>();
        int referenceIndex = 0;

        for (int n : SIZES) {
            for (int density : new int[] {4, 20}) {
                int m = n * density;
                System.out.printf("n=%d m=%d%n", n, m);

                Graph undirected = generate(n, m, "undirected");
                Row kruskalRow = measure("Kruskal", "undirected_" + density + "n",
                        undirected, metrics -> Kruskal.run(undirected, metrics).weight());
                Row primRow = measure("Prim", "undirected_" + density + "n",
                        undirected, metrics -> Prim.run(undirected, metrics).weight());
                check("Kruskal/Prim", kruskalRow.result(), primRow.result(), n, m);
                check("MST", kruskalRow.result(), EXPECTED_MST[referenceIndex], n, m);
                rows.add(kruskalRow);
                rows.add(primRow);
                System.gc();

                Graph directed = generate(n, m, "directed");
                Row sccRow = measure("Kosaraju", "directed_" + density + "n",
                        directed, metrics -> Kosaraju.run(directed, metrics).count());
                check("SCC", sccRow.result(), EXPECTED_SCC[referenceIndex], n, m);
                check("Kosaraju scans", sccRow.metrics().edgeScans, 2L * m, n, m);
                rows.add(sccRow);
                System.gc();

                Graph dag = generate(n, m, "dag");
                Row kahnRow = measure("Kahn", "dag_" + density + "n", dag,
                        metrics -> {
                            int[] order = Kahn.order(dag, metrics).orElseThrow();
                            if (!Kahn.isValid(dag, order)) {
                                throw new AssertionError("Invalid topological order");
                            }
                            return order.length;
                        });
                Row pathRow = measure("DagShortestPath", "dag_" + density + "n",
                        dag, metrics -> DagShortestPath.sum(DagShortestPath.run(dag, metrics).distance()));
                check("Kahn scans", kahnRow.metrics().edgeScans, m, n, m);
                check("DAG scans", pathRow.metrics().edgeScans, m, n, m);
                check("DAG sum", pathRow.result(), EXPECTED_DISTANCE_SUM[referenceIndex], n, m);
                rows.add(kahnRow);
                rows.add(pathRow);
                System.gc();

                System.out.printf("  MST=%d SCC=%d DAG sum=%d%n",
                        kruskalRow.result(), sccRow.result(), pathRow.result());
                referenceIndex++;
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            writer.newLine();
            for (Row row : rows) {
                writer.write(row.csv());
                writer.newLine();
            }
        }
        System.out.println("Wrote " + rows.size() + " rows to " + output);
    }
}

