package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

import org.junit.Test;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;

/**
 *  This class contains unit tests written for the Navigator class' methods.
 *
 *  // TODO: update this with correct location for the Navigator.
 * @see
 */
public class NavigatorUnitTest {

    @Test
    public void testNavigatorFindsOptimalRoute() throws Exception{
        Graph graph = new Graph();
    }

    @Test
    public void testNavigatorFindsOnlyAllowedRoutes() throws Exception{

    }

    @Test
    public void testNavigatorFailsWhenNoRoute() throws Exception{
        Graph graph = new Graph();

        Location a = new Location(new Point(0,0), 0, "a");
        Location b = new Location(new Point(0,1), 0, "b");
        Location c = new Location(new Point(1,0), 0, "c");
        Location d = new Location(new Point(1,1), 0, "d");
        Location z = new Location(new Point(2,1), 0, "z");

        graph.addLocation(a);
        graph.addLocation(b);
        graph.addLocation(c);
        graph.addLocation(d);
        graph.addLocation(z);

//        List<User>

        graph.addPath(new Path(a,b,));

    }

}
