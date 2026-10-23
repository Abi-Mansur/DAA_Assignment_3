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

        ArrayDeque<int[]> stack = new ArrayDeque<>();
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            stack.push(new int[] {start, 0});
            metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());

            while (!stack.isEmpty()) {
                int[] frame = stack.peek();
                int vertex = frame[0];

                if (frame[1] < graph.neighbors(vertex).size()) {
                    Graph.Arc arc = graph.neighbors(vertex).get(frame[1]++);
                    metrics.edgeScans++;
                    if (!visited[arc.to()]) {
                        visited[arc.to()] = true;
                        stack.push(new int[] {arc.to(), 0});
                        metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
                    }
                } else {
                    finishOrder[finished++] = vertex;
                    stack.pop();
                }
            }
        }

        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] reversed = (ArrayList<Integer>[]) new ArrayList<?>[n];
        for (int vertex = 0; vertex < n; vertex++) {
            reversed[vertex] = new ArrayList<>();
        }
        for (Graph.Edge edge : graph.edges()) {
            reversed[edge.v()].add(edge.u());
        }


        int[] component = new int[n];
        Arrays.fill(component, -1);
        int componentCount = 0;
        ArrayDeque<Integer> work = new ArrayDeque<>();

        for (int i = n - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (component[start] >= 0) {
                continue;
            }

            component[start] = componentCount;
            work.push(start);
            metrics.maxDepth = Math.max(metrics.maxDepth, work.size());

            while (!work.isEmpty()) {
                int vertex = work.pop();
                for (int next : reversed[vertex]) {
                    metrics.edgeScans++;
                    if (component[next] < 0) {
                        component[next] = componentCount;
                        work.push(next);
                        metrics.maxDepth = Math.max(metrics.maxDepth, work.size());
                    }
                }
            }
            componentCount++;
        }

        return new SCCResult(component, componentCount);
    }
}