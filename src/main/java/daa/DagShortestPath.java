package daa;

import java.util.Arrays;

public final class DagShortestPath {
    private DagShortestPath() {}

    public static PathResult run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Directed graph required");
        }

        int[] order = Kahn.order(graph, new Metrics())
                .orElseThrow(() -> new IllegalArgumentException("DAG required"));

        int[] indegree = new int[graph.vertexCount()];
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -1);
        for (Graph.Edge edge : graph.edges()) {
            indegree[edge.v()]++;
        }

        long infinity = Long.MAX_VALUE / 4;
        long[] distance = new long[graph.vertexCount()];
        Arrays.fill(distance, infinity);
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegree[vertex] == 0) {
                distance[vertex] = 0;            }
        }

        for (int vertex : order) {
            for (Graph.Arc arc : graph.neighbors(vertex)) {
                metrics.edgeScans++;
                long candidate = distance[vertex] + arc.weight();
                if (candidate < distance[arc.to()]) {
                    distance[arc.to()] = candidate;
                    parent[arc.to()] = vertex;
                }
            }
        }

        return new PathResult(distance, parent);
    }

    public static long sum(long[] values) {
        long total = 0;
        for (long value : values) {
            total += value;
        }
        return total;
    }
}