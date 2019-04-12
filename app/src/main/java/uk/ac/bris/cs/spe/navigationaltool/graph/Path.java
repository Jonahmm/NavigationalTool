package uk.ac.bris.cs.spe.navigationaltool.graph;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Path {

    private static final int TRANS_FLOOR_DIST = 400;
    public final Location locA;
    public final Location locB;

    private final int length;
    private final Point direction;

    private final List<User> users;

    public Path(Location a, Location b, List<User> users){
        requireNonNull(a);
        requireNonNull(b);
        requireNonNull(users);

        if(users.size() == 0) throw new IllegalArgumentException("users cannot be an empty list");
        if(users.contains(null)) throw new IllegalArgumentException("users cannot contain a null value");

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

    public boolean isTransFloor() { return !locA.getFloor().equals(locB.getFloor());}

    public boolean isOnFloor(String s) {
        return s.equals(locA.getFloor()) || s.equals(locB.getFloor());
    }

    public int getLength(){
        if (isTransFloor()) return TRANS_FLOOR_DIST;
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
        if(!((Path) o).locA.equals(locA)){
            if(!((Path) o).locA.equals(locB)) return false;
            else if(!((Path) o).locB.equals(locA)) return false;
        }
        else if(!((Path) o).locB.equals(locB)) return false;
        if(((Path) o).users.size() != users.size()) return false;
        if(!((Path) o).users.containsAll(users))  return false;

        return true;
    }

    public int hashCode(){
        // May have to change this, not sure if order of the users might affect the hashcode (which could break stuff)
        return Objects.hash(locA.hashCode() + locB.hashCode(), users.hashCode());
    }

}
