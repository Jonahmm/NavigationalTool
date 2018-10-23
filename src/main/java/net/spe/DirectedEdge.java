package net.spe;

public class DirectedEdge {
    private Node source, destination;

    public DirectedEdge(Node source, Node destination) {
        this.source = source;
        this.destination = destination;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }
}
