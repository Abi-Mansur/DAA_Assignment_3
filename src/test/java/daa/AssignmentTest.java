package daa;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {
    @Test
    void dsuMatchesReference() {
        Random random = new Random(7);
        int n = 120;
        DSU dsu = DSU.makeSet(n);
        int[] label = new int[n];
        for (int i = 0; i < n; i++) {
            label[i] = i;
        }

        for (int trial = 0; trial < 1000; trial++) {
            int x = random.nextInt(n);
            int y = random.nextInt(n);
            int oldLabel = label[y];
            int newLabel = label[x];
            dsu.union(x, y);
            for (int i = 0; i < n; i++) {
                if (label[i] == oldLabel) {
                    label[i] = newLabel;
                }
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j));
                }
            }
            assertEquals(Arrays.stream(label).distinct().count(), dsu.components());
        }
    }

    @Test
    void mstAgreementAndForest() {
        Random random = new Random(8);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(30);
            Graph graph = new Graph(n, false);
            for (int vertex = 1; vertex < n; vertex++) {
                graph.addEdge(vertex, random.nextInt(vertex), random.nextInt(101) - 50);
            }
            for (int i = 0; i < 4 * n; i++) {
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(101) - 50);
            }
            MSTResult kruskalResult = Kruskal.run(graph, new Metrics());
            MSTResult primResult = Prim.run(graph, new Metrics());
            assertEquals(kruskalResult.weight(), primResult.weight());
            assertEquals(n - 1, kruskalResult.edges());
            assertEquals(n - 1, primResult.edges());
        }

        Graph disconnected = new Graph(5, false);
        disconnected.addEdge(0, 1, 2);
        disconnected.addEdge(2, 3, 4);
        assertEquals(3, Kruskal.run(disconnected, new Metrics()).components());
        assertEquals(Prim.run(disconnected, new Metrics()).weight(),
                Kruskal.run(disconnected, new Metrics()).weight());
    }

    private boolean reachable(Graph graph, int source, int target) {
        boolean[] seen = new boolean[graph.vertexCount()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        seen[source] = true;
        queue.add(source);
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            for (Graph.Arc arc : graph.neighbors(vertex)) {
                if (!seen[arc.to()]) {
                    seen[arc.to()] = true;
                    queue.add(arc.to());
                }
            }
        }
        return seen[target];
    }

    @Test
    void sccAgainstBruteForce() {
        Random random = new Random(9);
        for (int trial = 0; trial < 25; trial++) {
            int n = 2 + random.nextInt(80);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < 4 * n; i++) {
                graph.addEdge(random.nextInt(n), random.nextInt(n), 1);
            }
            SCCResult result = Kosaraju.run(graph, new Metrics());
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    boolean sameByReachability = reachable(graph, u, v)
                            && reachable(graph, v, u);
                    assertEquals(sameByReachability,
                            result.component()[u] == result.component()[v]);
                }
            }
        }
    }

    @Test
    void topologicalOrderAndCycle() {
        Graph dag = Benchmark.generate(100, 400, "dag");
        int[] order = Kahn.order(dag, new Metrics()).orElseThrow();
        assertTrue(Kahn.isValid(dag, order));

        Graph cycle = new Graph(3, true);
        cycle.addEdge(0, 1, 1);
        cycle.addEdge(1, 2, 1);
        cycle.addEdge(2, 0, 1);
        assertTrue(Kahn.order(cycle, new Metrics()).isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPath.run(cycle, new Metrics()));
    }

    @Test
    void dagDistancesAgainstBellmanFord() {
        Random random = new Random(10);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(40);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < 4 * n; i++) {
                int from = random.nextInt(n);
                int to = random.nextInt(n);
                if (from < to) {
                    graph.addEdge(from, to, random.nextInt(41) - 20);
                }
            }

            int[] indegree = new int[n];
            for (Graph.Edge edge : graph.edges()) {
                indegree[edge.v()]++;
            }
            long[] reference = new long[n];
            Arrays.fill(reference, Long.MAX_VALUE / 4);
            for (int vertex = 0; vertex < n; vertex++) {
                if (indegree[vertex] == 0) {
                    reference[vertex] = 0;
                }
            }
            for (int pass = 1; pass < n; pass++) {
                for (Graph.Edge edge : graph.edges()) {
                    long candidate = reference[edge.u()] + edge.weight();
                    if (candidate < reference[edge.v()]) {
                        reference[edge.v()] = candidate;
                    }
                }
            }

            PathResult result = DagShortestPath.run(graph, new Metrics());
            assertArrayEquals(reference, result.distance());
            for (int vertex = 0; vertex < n; vertex++) {
                List<Integer> path = result.pathTo(vertex);
                assertEquals(vertex, path.get(path.size() - 1));
                long pathWeight = 0;
                for (int i = 1; i < path.size(); i++) {
                    int from = path.get(i - 1);
                    int to = path.get(i);
                    long edgeWeight = result.distance()[to] - result.distance()[from];
                    assertTrue(graph.neighbors(from).stream().anyMatch(arc ->
                            arc.to() == to && arc.weight() == edgeWeight));
                    pathWeight += edgeWeight;
                }
                assertEquals(result.distance()[vertex], pathWeight);
            }
        }
    }

    @Test
    void stackSafety() {
        Graph path = new Graph(100_000, true);
        for (int vertex = 0; vertex < 99_999; vertex++) {
            path.addEdge(vertex, vertex + 1, 1);
        }
        Metrics metrics = new Metrics();
        assertEquals(100_000, Kosaraju.run(path, metrics).count());
        assertEquals(199_998, metrics.edgeScans);
        assertTrue(metrics.maxDepth > 0);
    }

    @Test
    void inputFiles() throws Exception {
        Graph disconnected = Graph.read(Path.of("data/disconnected.txt"));
        assertEquals(3, Kruskal.run(disconnected, new Metrics()).components());

        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        assertTrue(Kahn.order(cyclic, new Metrics()).isEmpty());

        Graph dag = Graph.read(Path.of("data/dag.txt"));
        assertTrue(Kahn.isValid(dag, Kahn.order(dag, new Metrics()).orElseThrow()));
        assertEquals(-2, DagShortestPath.run(dag, new Metrics()).distance()[2]);
    }

    @Test
    void graphEdgeCases() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, -1); // Self-loop.
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 2);  // Parallel edge.
        assertEquals(2, Kruskal.run(graph, new Metrics()).components());
        assertEquals(2, Prim.run(graph, new Metrics()).weight());
    }
}


