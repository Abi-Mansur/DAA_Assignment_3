package daa;

import java.util.ArrayList;

public final class Kruskal {
    private Kruskal() {}

    public static MSTResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Undirected graph required");
        }

        ArrayList<Graph.Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((left, right) -> {
            metrics.comparisons++;
            return Integer.compare(left.weight(), right.weight());
        });

    }
}