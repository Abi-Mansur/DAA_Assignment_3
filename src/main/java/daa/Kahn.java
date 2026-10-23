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

        int count = 0;
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order[count++] = vertex;

            for (Graph.Arc arc : graph.neighbors(vertex)) {
                metrics.edgeScans++;
                if (--indegree[arc.to()] == 0) {
                    queue.add(arc.to());
                }
            }
        }

        return count == graph.vertexCount()
                ? Optional.of(order)
                : Optional.empty();
    }

    public static boolean isValid(Graph graph, int[] order) {
        if (order.length != graph.vertexCount()) {
            return false;
        }

        int[] position = new int[graph.vertexCount()];
        Arrays.fill(position, -1);
        for (int i = 0; i < order.length; i++) {
            int vertex = order[i];
            if (vertex < 0
                    || vertex >= graph.vertexCount()
                    || position[vertex] >= 0) {
                return false;
            }
            position[vertex] = i;
        }

        for (Graph.Edge edge : graph.edges()) {
            if (position[edge.u()] >= position[edge.v()]) {
                return false;
            }
        }
        return true;
    }
}