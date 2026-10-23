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

        DSU dsu = DSU.makeSet(graph.vertexCount());
        long totalWeight = 0;
        int selected = 0;

        for (Graph.Edge edge : sorted) {
            metrics.edgeScans++;
            if (dsu.find(edge.u()) != dsu.find(edge.v())) {
                dsu.union(edge.u(), edge.v());
                totalWeight += edge.weight();
                selected++;
                if (selected == graph.vertexCount() - 1) {
                    break;
                }
            }
        }

        metrics.dsuOps = dsu.findCalls() + dsu.successfulUnions();
        return new MSTResult(totalWeight, selected, dsu.components());
    }
}