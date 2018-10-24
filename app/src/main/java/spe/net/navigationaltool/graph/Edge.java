package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Edge {

    public Node nodeA;
    public Node nodeB;

    public int length;
    public Point direction;

    public boolean disabled;
    public AccessLevel accessLevel;

    public Edge(Node a, Node b){
        this.nodeA = a;
        this.nodeB = b;

        // Assuming straight line:
        length = (int) Math.sqrt(Math.pow(nodeA.location.x - nodeB.location.x, 2) + Math.pow(nodeA.location.y - nodeB.location.y, 2));
        // This is undirected, so use backwards too.
        direction = new Point(nodeA.location.x - nodeB.location.x, nodeA.location.y - nodeB.location.y);
        
        disabled = true; // Just as an assumption, most routes are corridors.
        accessLevel = AccessLevel.STUDENTCARD; // As an assumption, most places will require a ucard to get to.
    }

    public Edge(Node a, Node b, AccessLevel access, boolean disabledRoute){
        this(a, b);

        disabled = disabledRoute;
        accessLevel = access;
    }




}
