package uk.ac.bris.cs.spe.navigationaltool.graph;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Path {

    public final Location locA;
    public final Location locB;

    public final int length;
    public final Point direction;

    public final List<User> users;

    public Path(Location a, Location b, List<User> users){
        this.locA = a;
        this.locB = b;

        // Assuming straight line:
        length = (int) Math.sqrt(Math.pow(locA.x - locB.x, 2) + Math.pow(locA.y - locB.y, 2));
        // This is undirected, so use backwards too.
        direction = new Point(locA.x - locB.x, locA.y - locB.y);

        this.users = users;
    }

    public Location getOtherLocation(Location l){
        if(l == locA) return locB;
        else return locA;
    }

    public List<Location> getLocations(){
        List<Location> locations = new ArrayList<>();
        locations.add(locA);
        locations.add(locB);
        return locations;
    }

    public boolean allowsUser(User user){
        return users.contains(user);
    }

    public int getLength(){
        return length;
    }

    public Point getDirection(){
        return direction;
    }

    public Location getLocA() {
        return locA;
    }

    public Location getLocB() {
        return locB;
    }

    public List<User> getUser() {
        return users;
    }

    public boolean equals(Object o){
        if(!(o instanceof Path)) return false;

        // Checks forward and backwards
        if(((Path) o).locA != locA){
            if(((Path) o).locA != locB) return false;
            else if(((Path) o).locB != locA) return false;
        }
        else if(((Path) o).locB != locB) return false;
        if(((Path) o).users.size() != users.size()) return false;
        if(!((Path) o).users.contains(users))  return false;

        return true;
    }

    public int hashCode(){
        // May have to change this, not sure if order of the users might affect the hashcode (which could break stuff)
        return Objects.hash(locA.hashCode() + locB.hashCode(), users.hashCode());
    }

}
