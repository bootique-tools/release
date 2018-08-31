package io.bootique.tools.release.service.maven;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The implementation here is basically an adjacency list, but a {@link Map} is
 * used to map each vertex to its list of adjacent vertices.
 *
 * This object is not thread safe.
 *
 * @param <V> A type of a vertex.
 */
class Graph<V> {

    /**
     * {@link LinkedHashMap} is used for supporting insertion order.
     */
    private Map<V, List<V>> neighbors = new LinkedHashMap<>();
    private Map<V, Integer> inDegree = new LinkedHashMap<>();

    Graph() {
    }

    /**
     * Add a vertex to the graph. Nothing happens if vertex is already in graph.
     */
    void add(V vertex) {
        neighbors.putIfAbsent(vertex, new ArrayList<>());
        inDegree.putIfAbsent(vertex, 0);
    }

    /**
     * Add an edge to the graph; if either vertex does not exist, it's added.
     * This implementation allows the creation of multi-edges and self-loops.
     */
    void add(V from, V to) {
        this.add(from);
        this.add(to);
        neighbors.get(from).add(to);
        inDegree.compute(to, (k, v) -> v == null ? 1 : ++v);
    }

    /**
     * Return (as a List) the topological sort of the vertices.
     * Throws an exception if cycles are detected.
     */
    List<V> topSort() {
        Deque<V> zeroDegree = new ArrayDeque<>();
        int counter = neighbors.size();
        @SuppressWarnings("unchecked")
        V[] result = (V[])new Object[counter];

        for (Map.Entry<V, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                zeroDegree.push(entry.getKey());
            }
        }

        while (!zeroDegree.isEmpty()) {
            V v = zeroDegree.pop();
            result[--counter] = v;

            for (V neighbor : neighbors.get(v)) {
                inDegree.computeIfPresent(neighbor, (k, val) -> {
                    if(--val == 0) {
                        zeroDegree.push(k);
                    }
                    return val;
                });
            }
        }

        // Check that we have used the entire graph (if not, there was a cycle)
        if (counter != 0) {
            Set<V> remainingKeys = new HashSet<>(neighbors.keySet());
            throw new IllegalStateException("Cycle detected in list." + remainingKeys);
        }

        return Arrays.asList(result);
    }

    /**
     * String representation of graph.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (Map.Entry<V, List<V>> entry : neighbors.entrySet()) {
            s.append("\n    ").append(entry.getKey()).append(" -> ").append(entry.getValue());
        }

        return s.toString();
    }

}