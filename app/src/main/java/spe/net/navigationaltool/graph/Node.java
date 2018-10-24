package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Node {

    public Point location;
    public int floor;

    public Node(Point loc, int floor){
        this.location = loc;
        this.floor = floor;
    }

}
