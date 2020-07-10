package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * "repositories": {
 *   "totalCount": 31,
 *   "nodes": []
 * }
 */
public class Node<T> {

    private int totalCount;

    @JsonProperty("nodes")
    private List<T> nodes;

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setNodes(List<T> nodes) {
        this.nodes = nodes;
    }

    public List<T> getNodes() {
        return nodes;
    }
}
