package daa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public record PathResult(long[] distance, int[] parent) {
    public List<Integer> pathTo(int vertex) {
        ArrayList<Integer> path = new ArrayList<>();
        for (int current = vertex; current != -1; current = parent[current]) {
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }
}