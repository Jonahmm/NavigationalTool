package uk.ac.bris.cs.spe.navigationaltool;

public class PathDataHolder {
    public LocationDataHolder a;
    public LocationDataHolder b;
    public String user = "";

    PathDataHolder(LocationDataHolder aa, LocationDataHolder bb) {
        a = aa; b = bb;
    }

    PathDataHolder(LocationDataHolder aa, LocationDataHolder bb, String u) {
        a = aa; b = bb; user = u;
    }

    boolean equals(PathDataHolder c) {
        return (c.a==a && c.b==b) || (c.a==b && c.b==a);
    }
}
