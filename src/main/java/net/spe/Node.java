package net.spe;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private List<DirectedEdge> edges;

    public Node() {
        edges = new ArrayList<>();
    }

    public Node(List<DirectedEdge> edges) {
        this.edges = new ArrayList<>(edges);
    }

    public List<DirectedEdge> getEdges() {
        return edges;
    }

    public void addEdge(DirectedEdge edge) {
        if (edge.getSource() != this) throw new IllegalArgumentException("Cannot add edge that does not originate at this node");
        else edges.add(edge);
    }

    public void removeEdge(DirectedEdge edge) {
        if (!edges.contains(edge)) throw new IllegalArgumentException("Cannot remove edge that is not connected to this node");
        else edges.remove(edge);
    }
}
