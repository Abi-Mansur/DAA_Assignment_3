package daa;

import java.util.PriorityQueue;


public final class Prim {
    private Prim() {}

    private record Candidate(int vertex, int weight) {}

    public static MSTResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Undirected graph required");
        }

        boolean[] visited = new boolean[graph.vertexCount()];
        PriorityQueue<Candidate> queue = new PriorityQueue<>((left, right) -> {
            metrics.comparisons++;
            return Integer.compare(left.weight(), right.weight());
        });

        long totalWeight = 0;
        int selected = 0;
        int components = 0;

        for (int root = 0; root < graph.vertexCount(); root++) {
            if (visited[root]) {
                continue;
            }
            components++;
            queue.add(new Candidate(root, 0));

            while (!queue.isEmpty()) {
                Candidate candidate = queue.remove();
                if (visited[candidate.vertex()]) {
                    continue; // The queue may contain obsolete entries.
                }

                visited[candidate.vertex()] = true;
                if (candidate.vertex() != root) {
                    totalWeight += candidate.weight();
                    selected++;
                }

                for (Graph.Arc arc : graph.neighbors(candidate.vertex())) {
                    metrics.edgeScans++;
                    if (!visited[arc.to()]) {
                        queue.add(new Candidate(arc.to(), arc.weight()));
                    }
                }
            }
        }

        return new MSTResult(totalWeight, selected, components);
    }
}