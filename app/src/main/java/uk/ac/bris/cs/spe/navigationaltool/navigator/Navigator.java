package uk.ac.bris.cs.spe.navigationaltool.navigator;

import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;

public interface Navigator {

    // Maybe we should pass this the graph too?
    public List<Path> navigate(Location start, Location end);

}
