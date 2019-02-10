package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.BreadthFirstNavigator;
import uk.ac.bris.cs.spe.navigationaltool.navigator.DijkstraNavigator;
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedBuildingUnitTest {
//    @Test
//    public void useAppContext() throws Exception {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//        Log.d("test","this works");
//
//        assertEquals("spe.net.navigationaltool", appContext.getPackageName());
//    }

    private Context context = InstrumentationRegistry.getTargetContext();
    private List<User> onlyStaff = new ArrayList<>(Arrays.asList(User.DISABLED_STAFF, User.STAFF));
    private List<User> notDisabled = new ArrayList<>(Arrays.asList(User.STUDENT, User.STAFF));
    private List<User> allUsers = new ArrayList<>(Arrays.asList(User.STUDENT, User.STAFF, User.DISABLED_STUDENT, User.DISABLED_STAFF));

    ///// Construction Tests /////

    @Test
    public void testOnlyAndAllListedLocationsInGraph() throws Exception{
        Building building = new Building ("test/goodTest/goodTest", new BreadthFirstNavigator(), context);
        Graph g = building.getGraph();

        Location locA = new Location(0, 0, 0,"0","locA", "Location A");
        Location locB = new Location(1, 0,1,"0","locB", "Location B");
        Location locC = new Location(2, 2, 3,"0","locC", "Location C");
        Location locD = new Location(3, 4, 1,"0","locD", "Location D");

        assertThat(g.getLocationByCode("locA").equals(locA)).isTrue();
        assertThat(g.getLocationByCode("locB").equals(locB)).isTrue();
        assertThat(g.getLocationByCode("locC").equals(locC)).isTrue();
        assertThat(g.getLocationByCode("locD").equals(locD)).isTrue();
    }

    @Test
    public void testOnlyAndAllListedPathsInGraph() throws Exception{
        Building building = new Building ("test/goodTest/goodTest", new BreadthFirstNavigator(), context);
        Graph g = building.getGraph();

        Location locA = new Location(0, 0, 0,"0","locA", "Location A");
        Location locB = new Location(1, 0,1,"0","locB", "Location B");
        Location locC = new Location(2, 2, 3,"0","locC", "Location C");
        Location locD = new Location(3, 4, 1,"0","locD", "Location D");

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
        assertThatThrownBy(() -> new Building("incorrect file name", new BreadthFirstNavigator(),context))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testBadFileLayoutFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/badFileLayout/badFileLayout", new BreadthFirstNavigator(), context))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicateLocationFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/duplicateLocations/duplicateLocations", new BreadthFirstNavigator(), context))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicatePathFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/duplicatePaths/duplicatePaths", new BreadthFirstNavigator(), context))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test // ie: If you have two 'islands' of locations that do not have connecting paths, then this should fail after loading.
    public void testHavingUnconnectedLocationsFails() throws Exception{
        assertThatThrownBy(() -> new Building("test/unconnectedLocations/unconnectedLocations", new BreadthFirstNavigator(), context))
                .isInstanceOf(IllegalArgumentException.class);
    }

    ///// Construction Tests End /////

    @Test
    public void testSetNavigatorChangesNavigator() throws Exception{
        Navigator bfs = new BreadthFirstNavigator();
        Building building = new Building("test/goodTest/goodTest", bfs, context);
        assertThat(building.getNavigator()).isEqualTo(bfs);

        Navigator dijk = new DijkstraNavigator();
        building.setNavigator(dijk);
        assertThat(building.getNavigator()).isEqualTo(dijk);
    }

    @Test
    public void testGetNameIsCorrect() throws Exception {
        Building building = new Building("test/goodTest/goodTest", new BreadthFirstNavigator(), context);
        assertThat(building.getName().equals("goodTest")).isTrue();

    }

    @Test
    public void testNullArgs(){
        assertThatThrownBy(() -> new Building("test/goodTest/goodTest", new BreadthFirstNavigator(), null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Building("test/goodTest/goodTest", null, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Building(null, new BreadthFirstNavigator(), null))
                .isInstanceOf(NullPointerException.class);
    }
}
