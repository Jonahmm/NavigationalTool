package uk.ac.bris.cs.spe.navigationaltool.graph;

import android.graphics.Point;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Location {
    final int id;

    public final int x, y;
    public final String floor;

    public final String code;
    public String name;

    public Location(int id, int x, int y, String floor, String code){
        this.id = id;
        requireNonNull(code);
        if (code.equals("")) throw new IllegalArgumentException("Location cannot have empty code");
        this.x = x;
        this.y = y;

        this.floor = floor;
        this.code = code;
    }

    //Edited to ensure "" goes to null name
    public Location(int id, int x, int y, String floor, String code, String name){
        this(id,x,y,floor,code);
        requireNonNull(name);
        if (!name.equals("")) this.name = name;
    }


    public Location(int id, Point loc, String floor, String code){
        this(id, loc.x, loc.y, floor, code);
    }

    public Location(int id, Point loc, String floor, String code, String name){
        this(id, loc.x, loc.y, floor, code);
        this.name = name;
    }

    public Point getLocation() {
        return new Point(x,y);
    }

    public int getId() { return id; }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public String getFloor() {
        return floor;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean hasName() {
        return name != null;
    }

    // For Debug purposes only
    public String getLocationString(){
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(Object o){
        if(!(o instanceof Location))          return false;
        if(!((Location) o).code.equals(code)) return false;
        if(!((Location) o).floor.equals(floor))     return false;
        if(((Location) o).getX() != getX())   return false;
        if(((Location) o).getY() != getY())   return false;

        return true;
    }

    public int hashCode(){
        return Objects.hash(x, y, floor, code, name);
    }

}
