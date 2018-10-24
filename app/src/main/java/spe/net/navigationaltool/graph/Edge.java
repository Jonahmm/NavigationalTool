package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Edge {

    public final Node nodeA;
    public final Node nodeB;

    public final int length;
    public final Point direction;

    public boolean disabled;
    public AccessLevel accessLevel;

    public Edge(Node a, Node b){
        this.nodeA = a;
        this.nodeB = b;

        // Assuming straight line:
        length = (int) Math.sqrt(Math.pow(nodeA.location.x - nodeB.location.x, 2) + Math.pow(nodeA.location.y - nodeB.location.y, 2));
        // This is undirected, so use backwards too.
        direction = new Point(nodeA.location.x - nodeB.location.x, nodeA.location.y - nodeB.location.y);


        // Just as an assumption, most routes are corridors.
        disabled = true;
        // As an assumption, most places will require a ucard to get to.
        accessLevel = AccessLevel.STUDENTCARD;
    }

    public Edge(Node a, Node b, AccessLevel access, boolean disabledRoute){
        this(a, b);

        disabled = disabledRoute;
        accessLevel = access;
    }

    public int getLength(){
        return length;
    }

    public Point getDirection(){
        return direction;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

}
