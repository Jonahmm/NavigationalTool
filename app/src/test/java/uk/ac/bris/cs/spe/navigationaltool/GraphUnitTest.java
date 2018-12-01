package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import static org.assertj.core.api.Java6Assertions.*;

/**
 *  This class contains unit tests written for the Graph class' methods.
 *
 * @see uk.ac.bris.cs.spe.navigationaltool.graph.Graph
 */
public class GraphUnitTest {

    @Test
    public void testAddLocation() throws Exception{
        Graph g = new Graph();
        Location locA = new Location (new Point(0, 0), 0, "locA");
        Location locB = new Location (new Point(0,1), 0, "locB");

        List<User> users = new ArrayList<>(1);
        Path pathA = new Path (locA, locB, users);
        ArrayList<Path> paths = new ArrayList<Path>(0);
        paths.add(pathA);

        assertThatThrownBy(() -> {
            g.addLocation(locA);
            g.addLocation(locA);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            g.addLocation(locB, paths);
            g.addLocation(locB, paths);
        }).isInstanceOf(IllegalArgumentException.class);


    }

    @Test (expected = NullPointerException.class)// This may be better to include in the method above? Check when filling out the tests.
    public void testAddLocationFailOnNull() throws Exception {
        Graph g = new Graph();
        g.addLocation(null);

    }

    @Test
    public void testAddPath() throws Exception{

    }

    @Test // This may be better to include in the method above? Check when filling out the tests.
    public void testAddPathFailsWithUnknownLocation() throws Exception{

    }

    @Test
    public void testNoDuplicateLocationCodes() throws Exception{

    }

    @Test
    public void testNoDuplicatePositions() throws Exception{

    }

    @Test
    public void testGetPathsFromLocationsReturnsCorrectPaths() throws Exception{

    }

    @Test
    public void testGetLocationsReturnsAllAddedLocations() throws Exception{

    }

}
