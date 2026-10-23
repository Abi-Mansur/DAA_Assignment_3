package daa;

public final class DSU {
    private final int[] parent;
    private final int[] size;
    private int componentCount;
    private long findCalls;
    private long successfulUnions;

    private DSU(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative set size");
        }
        parent = new int[n];
        size = new int[n];
        componentCount = n;
        for (int vertex = 0; vertex < n; vertex++) {
            parent[vertex] = vertex;
            size[vertex] = 1;
        }
    }

    public static DSU makeSet(int n) {
        return new DSU(n);
    }

    public int find(int vertex) {
        findCalls++;
        int root = vertex;
        while (parent[root] != root) {
            root = parent[root];
        }

        while (parent[vertex] != vertex) {
            int next = parent[vertex];
            parent[vertex] = root;
            vertex = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false;
        }

        if (size[rootX] < size[rootY]) {
            int temp = rootX;
            rootX = rootY;
            rootY = temp;
        }
        parent[rootY] = rootX;
        size[rootX] += size[rootY];
        componentCount--;
        successfulUnions++;
        return true;
    }

    public int components() {
        return componentCount;
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnions() {
        return successfulUnions;
    }
}