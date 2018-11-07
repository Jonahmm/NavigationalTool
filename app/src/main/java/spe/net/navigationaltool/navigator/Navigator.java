package spe.net.navigationaltool.navigator;

import java.util.List;

import spe.net.navigationaltool.graph.Location;
import spe.net.navigationaltool.graph.Path;

public interface Navigator {

    // Maybe we should pass this the graph too?
    public List<Path> navigate(Location start, Location end);

}
