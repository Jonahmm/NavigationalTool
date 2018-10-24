package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Node {

    public Point location;
    public int floor;

    public String roomCode;
    public String additionalName;

    public Node(Point loc, int floor, String roomCode){
        this.location = loc;
        this.floor = floor;

        this.roomCode = roomCode;
    }

    public Node(Point loc, int floor, String roomCode, String additionalName){
        this(loc, floor, roomCode);
        this.additionalName = additionalName;
    }

}
