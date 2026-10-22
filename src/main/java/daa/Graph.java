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

}