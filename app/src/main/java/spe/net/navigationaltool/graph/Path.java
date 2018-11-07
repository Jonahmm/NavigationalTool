package spe.net.navigationaltool.graph;

import android.graphics.Point;

public class Path {

    public final Location locationA;
    public final Location locationB;

    public final int length;
    public final Point direction;

    public boolean disabled;
    public AccessLevel accessLevel;

    public Path(Location a, Location b){
        this.locationA = a;
        this.locationB = b;

        // Assuming straight line:
        length = (int) Math.sqrt(Math.pow(locationA.location.x - locationB.location.x, 2) + Math.pow(locationA.location.y - locationB.location.y, 2));
        // This is undirected, so use backwards too.
        direction = new Point(locationA.location.x - locationB.location.x, locationA.location.y - locationB.location.y);


        // Just as an assumption, most routes are corridors.
        disabled = true;
        // As an assumption, most places will require a ucard to get to.
        accessLevel = AccessLevel.STUDENTCARD;
    }

    public Path(Location a, Location b, AccessLevel access, boolean disabledRoute){
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

    public Location getLocationA() {
        return locationA;
    }

    public Location getLocationB() {
        return locationB;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

}
