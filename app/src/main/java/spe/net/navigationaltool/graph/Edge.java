package spe.net.navigationaltool.graph;

public class Edge {

    public Node nodeA;
    public Node nodeB;

    public boolean disabled;
    public AccessLevel accessLevel;

    public Edge(Node a, Node b){
        this.nodeA = a;
        this.nodeB = b;

        disabled = true; // Just as an assumption, most routes are corridors.
        accessLevel = AccessLevel.STUDENTCARD; // As an assumption, most places will require a ucard to get to.
    }

    public Edge(Node a, Node b, AccessLevel access, boolean disabledRoute){
        this(a, b);

        disabled = disabledRoute;
        accessLevel = access;
    }




}
