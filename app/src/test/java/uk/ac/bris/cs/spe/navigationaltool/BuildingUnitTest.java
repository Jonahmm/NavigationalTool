package uk.ac.bris.cs.spe.navigationaltool;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.BreadthFirstNavigator;
import uk.ac.bris.cs.spe.navigationaltool.navigator.DijkstraNavigator;

import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 *  This class contains unit tests written for the Building class' methods.
 *
 * @see uk.ac.bris.cs.spe.navigationaltool.Building;
 */
public class BuildingUnitTest {

    private DisplayDrawer d = new DisplayDrawer();
    private List<User> onlyStaff = new ArrayList<>(Arrays.asList(User.DISABLED_STAFF, User.STAFF));
    private List<User> notDisabled = new ArrayList<>(Arrays.asList(User.STUDENT, User.STAFF));
    private List<User> allUsers = new ArrayList<>(Arrays.asList(User.STUDENT, User.STAFF, User.DISABLED_STUDENT, User.DISABLED_STAFF));

    ///// Construction Tests /////

    @Test
    public void testOnlyAndAllListedLocationsInGraph() throws Exception{
        Building building = new Building ("test/goodTest", new BreadthFirstNavigator(), d.getApplicationContext());
        Graph g = building.getGraph();

        Location locA = new Location(0, 0,"Ground Floor","locA", "Location A");
        Location locB = new Location(0,1,"First Floor","locB", "Location B");
        Location locC = new Location(2, 3,"Second Floor","locC", "Location C");
        Location locD = new Location(4, 1,"Basement","locD", "Location D");

        assertThat(g.getLocationByCode("locA").equals(locA)).isTrue();
        assertThat(g.getLocationByCode("locB").equals(locB)).isTrue();
        assertThat(g.getLocationByCode("locC").equals(locC)).isTrue();
        assertThat(g.getLocationByCode("locD").equals(locD)).isTrue();
    }

    @Test
    public void testOnlyAndAllListedPathsInGraph() throws Exception{
        Building building = new Building ("test/goodTest", new BreadthFirstNavigator(), d.getApplicationContext());
        Graph g = building.getGraph();

        Location locA = new Location(0, 0,"Ground Floor","locA", "Location A");
        Location locB = new Location(0,1,"First Floor","locB", "Location B");
        Location locC = new Location(2, 3,"Second Floor","locC", "Location C");
        Location locD = new Location(4, 1,"Basement","locD", "Location D");

        Path p1 = new Path (locA, locB, notDisabled);
        Path p2 = new Path (locB, locC, onlyStaff);
        Path p3 = new Path (locA, locC, allUsers);
        Path p4 = new Path (locD, locB, onlyStaff);

        assertThat(g.getPathsFromLocationCode("locA").equals(Arrays.asList(p1,p3))).isTrue();
        assertThat(g.getPathsFromLocationCode("locB").equals(Arrays.asList(p2))).isTrue();
        assertThat(g.getPathsFromLocationCode("locC").isEmpty()).isTrue();
        assertThat(g.getPathsFromLocationCode("locD").equals(Arrays.asList(p4))).isTrue();
    }

    @Test
    public void testBadArgumentFails() throws Exception{
        assertThatThrownBy(() -> new Building("incorrect file name", new BreadthFirstNavigator(),d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building("goodTest", null, d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building(null, new BreadthFirstNavigator(), d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBadFileLayoutFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/badFileLayout", new BreadthFirstNavigator(), d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicateLocationFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/duplicateLocations", new BreadthFirstNavigator(), d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicatePathFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/duplicatePaths", new BreadthFirstNavigator(), d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test // ie: If you have two 'islands' of locations that do not have connecting paths, then this should fail after loading.
    public void testHavingUnconnectedLocationsFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/unconnectedLocations", new BreadthFirstNavigator(), d.getApplicationContext()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    ///// Construction Tests End /////

    @Test
    public void testSetNavigatorChangesNavigator() throws Exception{
        Building building = new Building("test/goodTest", new BreadthFirstNavigator(), d.getApplicationContext());
        assertThat(building.getNavigator() instanceof BreadthFirstNavigator).isTrue();

        building.setNavigator(new DijkstraNavigator());
        assertThat(building.getNavigator() instanceof DijkstraNavigator).isTrue();
    }

    @Test
    public void testGetNameIsCorrect() throws Exception {
        Building building = new Building("test/goodTest", new BreadthFirstNavigator(), d.getApplicationContext());
        assertThat(building.getName().equals("goodTest")).isTrue();

    }

    @Test
    public void testNullArgs(){
        assertThatThrownBy(() -> new Building("test/goodTest", new BreadthFirstNavigator(), null))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building("test/goodTest", null, null))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building(null, new BreadthFirstNavigator(), null))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
