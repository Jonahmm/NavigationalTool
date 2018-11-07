package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Location {

    public final Point location;
    public final int floor;

    public final String code;
    public String name;

    public Location(Point loc, int floor, String code){
        this.location = loc;
        this.floor = floor;

        this.code = code;
    }

    public Location(Point loc, int floor, String code, String name){
        this(loc, floor, code);
        this.name = name;
    }

    public Point getLocation() {
        return location;
    }

    public int getFloor() {
        return floor;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
