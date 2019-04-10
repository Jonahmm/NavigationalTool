package uk.ac.bris.cs.spe.navigationaltool.graph;

public class ChildLocation extends Location {
    private Location parent;

    public ChildLocation(int id, int x, int y, String f, Location l) {
        super(id, x, y, f, "CHILD");
        parent = l;
    }

    @Override
    public String getCode() {
        return parent.getCode();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public boolean hasName() {
        return parent.hasName();
    }
}
