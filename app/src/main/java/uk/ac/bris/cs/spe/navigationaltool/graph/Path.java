package uk.ac.bris.cs.spe.navigationaltool.graph;

import android.graphics.Point;

import java.util.List;

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
        length = (int) Math.sqrt(Math.pow(locA.location.x - locB.location.x, 2) + Math.pow(locA.location.y - locB.location.y, 2));
        // This is undirected, so use backwards too.
        direction = new Point(locA.location.x - locB.location.x, locA.location.y - locB.location.y);

        this.users = users;
    }

    public Location getOtherLocation(Location l){
        if(l == locA) return locB;
        else return locA;
    }

    public Location[] getLocations(){
        // TODO
        return null;
    }

    public boolean allowsUser(User user){
        // TODO
        return false;
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

}
