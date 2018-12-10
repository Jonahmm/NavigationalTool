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
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;

import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 *  This class contains unit tests written for the Building class' methods.
 *
 *  // TODO: update this with correct location for the Building.
 * @see
 */
public class BuildingUnitTest {

    ///// Construction Tests /////

    @Test
    public void testOnlyAndAllListedLocationsInGraph() throws Exception {
        Building building = new Building("test1.buildingLocations", new BreadthFirstNavigator());
        Graph g = building.getGraph();

        List<User> allUsers = new ArrayList<>(Arrays.asList(User.STAFF, User.STUDENT, User.DISABLED_STAFF, User.DISABLED_STUDENT));
        List<User> onlyStaff = new ArrayList<>(Arrays.asList(User.STAFF, User.DISABLED_STUDENT));
        List<User> noDisabled = new ArrayList<>(Arrays.asList(User.STAFF, User.STUDENT));

        Location locA = new Location(0, 0, 0, "locA", "Location A");
        Location locB = new Location(1, 0, 1, "locB", "Location B");
        Location locC = new Location(3, 2, 2, "locC", "Location C");
        Location locD = new Location(0, 4, -1, "locD", "Location D");

        Path p1 = new Path(locA, locB, noDisabled);
        Path p2 = new Path(locB, locC, onlyStaff);
        Path p3 = new Path(locA, locC, allUsers);
        Path p4 = new Path(locD, locB, onlyStaff);

        // check locations
        assertThat(g.getLocationByCode("locA").equals(locA)).isTrue();
        assertThat(g.getLocationByCode("locB").equals(locB)).isTrue();
        assertThat(g.getLocationByCode("locC").equals(locC)).isTrue();
        assertThat(g.getLocationByCode("locD").equals(locD)).isTrue();
    }

    @Test
    public void testOnlyAndAllListedPathsInGraph() throws Exception{
        Building building = new Building ("test1.buildingLocations", new BreadthFirstNavigator());
        Graph g = building.getGraph();

        List<User> allUsers = new ArrayList<>(Arrays.asList(User.STAFF,User.STUDENT,User.DISABLED_STAFF,User.DISABLED_STUDENT));
        List<User> onlyStaff = new ArrayList<>(Arrays.asList(User.STAFF,User.DISABLED_STUDENT));
        List<User> noDisabled = new ArrayList<>(Arrays.asList(User.STAFF,User.STUDENT));

        Location locA = new Location(0,0,0,"locA","Location A");
        Location locB = new Location(1,0,1,"locB","Location B");
        Location locC = new Location(3,2,2,"locC","Location C");
        Location locD = new Location(0,4,-1,"locD","Location D");

        Path p1 = new Path (locA,locB,noDisabled);
        Path p2 = new Path (locB,locC,onlyStaff);
        Path p3 = new Path (locA,locC, allUsers);
        Path p4 = new Path (locD,locB,onlyStaff);

        // check paths
        assertThat(g.getPathsFromLocationCode("locA").equals(new ArrayList<>(Arrays.asList(p1,p3)))).isTrue();
        assertThat(g.getPathsFromLocationCode("locB").equals(new ArrayList<>(Arrays.asList(p2)))).isTrue();
        assertThat(g.getPathsFromLocationCode("locC").isEmpty()).isTrue();
        assertThat(g.getPathsFromLocationCode("locD").equals(new ArrayList<>(Arrays.asList(p4)))).isTrue();
    }

    @Test
    public void testNoFileFails() throws Exception{
        assertThatThrownBy(() -> {
            Building building = new Building ("test.buildingLocations", new BreadthFirstNavigator());
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBadFileLayoutFails() throws Exception{
        assertThatThrownBy(() -> {
            Building building = new Building ("test2.buildingLocations", new BreadthFirstNavigator());
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicateLocationFails() throws Exception{

    }

    @Test
    public void testDuplicatePathFails() throws Exception{

    }

    @Test // TODO: See if this is actually necessary with the implementation we have. = probably not necessary
    public void testAddingLocationWithUnknownFloorFails() throws Exception{
//        assertThatThrownBy(() -> {
//            Building building = new Building("test2.buildingLocations",new BreadthFirstNavigator());
//        }).isInstanceOf(RuntimeException.class);
    }

    @Test // ie: If you have two 'islands' of locations that do not have connecting paths, then this should fail after loading.
    public void testHavingUnconnectedLocationsFails() throws Exception{
        assertThatThrownBy(() -> {
            Building building = new Building("test3.buildingLocations",new BreadthFirstNavigator());
        }).isInstanceOf(RuntimeException.class);
    }

    ///// Construction Tests End /////


    @Test
    public void testSetNavigatorChangesNavigator() throws Exception{
        Navigator nav1 = new BreadthFirstNavigator();
        Navigator nav2 = new BreadthFirstNavigator();
        Building building = new Building("test1.buildingLocations", nav1);
        building.setNavigator(nav2);

        assertThat(building.getNavigator().equals(nav2)).isTrue();
    }

    @Test
    public void testGetNameIsCorrect() throws Exception{
        Building building = new Building("test1.buildingLocations", new BreadthFirstNavigator());

        assertThat(building.getName().equals("test1.buildingLocations")).isTrue();
    }

    @Test // is this necessary? already done in the first 2 tests.
    public void testGetGraphReturnsCorrectGraph() throws Exception{
//        Building building = new Building()
    }
}
