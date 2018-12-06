package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

import org.junit.Test;

import java.util.ArrayList;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

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

        Path path1 = new Path (locA, locB, new ArrayList<>());
        ArrayList<Path> paths = new ArrayList<Path>(0);
        paths.add(path1);


        assertThatThrownBy(() -> {
            g.addLocation(locA);
            g.addLocation(locA);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            g.addLocation(locB, paths);
            g.addLocation(locB, paths);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test // This may be better to include in the method above? Check when filling out the tests.
    public void testAddLocationFailOnNull() throws Exception {
        Graph g = new Graph();

        assertThatThrownBy(() -> g.addLocation(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> g.addLocation(null, new ArrayList<Path>(0)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testAddPath() throws Exception{
        Graph g = new Graph();
        Location locA = new Location (new Point(0, 0), 0, "locA");
        Location locB = new Location (new Point(0,1), 0, "locB");
        Location locC = new Location (new Point(0,2), 0, "locC");

        Path path1 = new Path (locC, locB, new ArrayList<User>());
        Path path2 = new Path (locA, locC, new ArrayList<User>());
        Path path3 = new Path (locA, locA, new ArrayList<User>());

        g.addLocation(locA);
        g.addLocation(locB);

        assertThatThrownBy(() -> g.addPath(path1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> g.addPath(path2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> g.addPath(path3)).isInstanceOf(IllegalArgumentException.class);

    }

    @Test // This may be better to include in the method above? Check when filling out the tests.
    public void testAddPathFailsWithUnknownLocation() throws Exception{
        Graph g = new Graph();
        Location locA = new Location (new Point(0, 0), 0, "locA");
        g.addLocation(locA);
        assertThatThrownBy(() -> g.addPath(new Path(null, locA, new ArrayList<>())))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> g.addPath(new Path(null, null, new ArrayList<>())))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> g.addPath(new Path(locA, null, new ArrayList<>())))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testNoDuplicateLocationCodes() throws Exception{
        Graph g = new Graph();
        Location locA1 = new Location (new Point(0, 0), 0, "locA");
        Location locA2 = new Location (new Point(0, 1), 0, "locA");

        assertThatThrownBy(() -> {
            g.addLocation(locA1);
            g.addLocation(locA2);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            g.addLocation(locA1, new ArrayList<>());
            g.addLocation(locA2, new ArrayList<>());
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNoDuplicatePositions() throws Exception{
        Graph g = new Graph();
        Location locA = new Location (new Point(0, 0), 0, "locA", "Location A");
        Location locB = new Location (new Point(0, 0), 0, "locB", "Location B");

        assertThatThrownBy(() -> {
            g.addLocation(locA);
            g.addLocation(locB);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            g.addLocation(locA, new ArrayList<>());
            g.addLocation(locB, new ArrayList<>());
        }).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testGetPathsFromLocationsReturnsCorrectPaths() throws Exception{
        Graph g = new Graph();
        Location locA = new Location (new Point(0, 0), 0, "locA", "Location A");
        Location locB = new Location (new Point(1, 0), 1, "locB", "Location B");
        Location locC = new Location (new Point(2,0),2, "locC", "Location C");
        Location locD = new Location (new Point (3,0), 4, "locD", "Location D");

        Path path1 = new Path(locA, locB, new ArrayList<>());
        Path path2 = new Path(locA, locC, new ArrayList<>());

        g.addLocation(locA);
        g.addLocation(locB);
        g.addLocation(locC);
        g.addPath(path1);
        g.addPath(path2);


        ArrayList<Path> paths = new ArrayList<>();
        paths.add(path1);
        paths.add(path2);

        assertThat(g.getPathsFromLocation(locA).equals(paths)).isTrue();

        assertThat(g.getPathsFromLocationCode(locA.code).equals(paths)).isTrue();

        assertThatThrownBy(() -> {
            g.getPathsFromLocation(locD);
        }).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> {
            g.getPathsFromLocationCode(locD.code);
        }).isInstanceOf(RuntimeException.class);

    }

    @Test
    public void testGetLocationsReturnsAllAddedLocations() throws Exception{
        Graph g = new Graph();
        Location locA = new Location (new Point(0, 0), 0, "locA", "Location A");
        Location locB = new Location (new Point(1, 0), 1, "locB", "Location B");

        g.addLocation(locA);
        g.addLocation(locB);

        assertThat(g.getLocationByCode("locA").equals(locA)).isTrue();
        assertThat(g.getLocationByCode("locB").equals((locB))).isTrue();

        assertThatThrownBy(() -> {
            g.getLocationByCode("locC");
        }).isInstanceOf(IllegalArgumentException.class);



    }

}
