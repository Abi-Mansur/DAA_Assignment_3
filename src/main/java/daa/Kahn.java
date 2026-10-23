package daa;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;

public final class Kahn {
    private Kahn() {}

    public static Optional<int[]> order(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Directed graph required");
        }

        int[] indegree = new int[graph.vertexCount()];
        int[] order = new int[graph.vertexCount()];
        for (Graph.Edge edge : graph.edges()) {
            indegree[edge.v()]++;
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegree[vertex] == 0) {
                queue.add(vertex);
            }
        }


    }
}