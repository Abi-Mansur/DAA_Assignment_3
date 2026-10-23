package daa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public final class Kosaraju {
    private Kosaraju() {}

    public static SCCResult run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Directed graph required");
        }

        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finished = 0;




    }
}