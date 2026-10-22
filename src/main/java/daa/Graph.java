package daa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;


public final class Graph {
    public record Edge(int u, int v, int weight) {}
    public record Arc(int to, int weight) {}

    private final int vertexCount;
    private final boolean directed;
    private final ArrayList<Edge> edges = new ArrayList<>();
    private final ArrayList<Arc>[] adjacency;

    @SuppressWarnings("unchecked")
    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Negative vertex count");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        adjacency = (ArrayList<Arc>[]) new ArrayList<?>[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacency[vertex] = new ArrayList<>();
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public boolean isDirected() {
        return directed;
    }

    public ArrayList<Edge> edges() {
        return edges;
    }

    public ArrayList<Arc> neighbors(int vertex) {
        return adjacency[vertex];
    }

    public void addEdge(int from, int to, int weight) {
        if (from < 0 || from >= vertexCount || to < 0 || to >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of range");
        }
        edges.add(new Edge(from, to, weight));
        adjacency[from].add(new Arc(to, weight));
        if (!directed) {
            adjacency[to].add(new Arc(from, weight));
        }
    }

    public static Graph read(Path path) throws IOException {
        try (Scanner input = new Scanner(Files.newBufferedReader(path))) {
            int n = input.nextInt();
            int m = input.nextInt();
            String kind = input.next();
            if (!kind.equals("directed") && !kind.equals("undirected")) {
                throw new IOException("Expected directed or undirected");
            }

            Graph graph = new Graph(n, kind.equals("directed"));
            for (int i = 0; i < m; i++) {
                graph.addEdge(
                        input.nextInt(),
                        input.nextInt(),
                        input.nextInt()
                );
            }
            if (input.hasNext()) {
                throw new IOException("Extra tokens after edge list");
            }
            return graph;
        } catch (NoSuchElementException exception) {
            throw new IOException("Incomplete graph file", exception);
        }
    }
}