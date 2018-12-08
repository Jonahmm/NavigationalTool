package uk.ac.bris.cs.spe.navigationaltool.graph;

import android.graphics.Point;

import java.util.Objects;

public class Location {

    public final int x, y;
    public final int floor;

    public final String code;
    public String name;

    public Location(int x, int y, int floor, String code){
        this.x = x;
        this.y = y;

        this.floor = floor;
        this.code = code;
    }

    public Location(int x, int y, int floor, String code, String name){
        this(x,y,floor,code);
        this.name = name;
    }


    public Location(Point loc, int floor, String code){
        this(loc.x, loc.y, floor, code);
    }

    public Location(Point loc, int floor, String code, String name){
        this(loc.x, loc.y, floor, code);
        this.name = name;
    }

    public Point getLocation() {
        return new Point(x,y);
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
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

    // For Debug purposes only
    public String getLocationString(){
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(Object o){
        if(!(o instanceof Location))          return false;
        if(!((Location) o).code.equals(code)) return false;
        if(((Location) o).floor != floor)     return false;
        if(((Location) o).getX() != getX())   return false;
        if(((Location) o).getY() != getY())   return false;

        return true;
    }

    public int hashCode(){
        return Objects.hash(x, y, floor, code, name);
    }

}
