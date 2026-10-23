package daa;

import java.util.ArrayList;

public final class Kruskal {
    private Kruskal() {}

    public static MSTResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Undirected graph required");
        }

    }
}