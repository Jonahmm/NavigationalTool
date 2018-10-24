package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Node {

    public final Point location;
    public final int floor;

    public final String roomCode;
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

    public Point getLocation() {
        return location;
    }

    public int getFloor() {
        return floor;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getAdditionalName() {
        return additionalName;
    }

}
