package uk.ac.bris.cs.spe.navigationaltool.navigator;

import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;

import static java.util.Objects.requireNonNull;

public interface Navigator {

    // Maybe we should pass this the graph too?
    public List<Path> navigate(Location start, Location end, Graph graph, User user);


    public default List<Path> navigate(int idStart, int idEnd, Graph graph, User user){
        Location start = graph.getLocationById(idStart);
        Location end = graph.getLocationById(idEnd);

        requireNonNull(start);
        requireNonNull(end);

        return navigate(start, end, graph, user);
    }
}
