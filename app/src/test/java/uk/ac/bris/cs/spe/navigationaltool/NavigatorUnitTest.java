package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

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
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 *  This class contains unit tests written for the Navigator class' methods.
 *
 *  @see uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;
 */
public class NavigatorUnitTest {

    @Test
    public void testNavigatorFindsOptimalRoute() {
        Graph graph = new Graph();
        List<User> allUsers = new ArrayList<>(Arrays.asList(User.STUDENT, User.DISABLED_STUDENT, User.STAFF, User.DISABLED_STAFF));
        Navigator nav = new DijkstraNavigator();

        Location a = new Location(0,0, "0", "a");
        Location b = new Location(0,1, "0", "b");
        Location c = new Location(1,0, "0", "c");

        Path p1 = new Path (a, b, allUsers);
        Path p2 = new Path (b, c, allUsers);
        Path p3 = new Path (a, c, allUsers);

        graph.addLocation(a); graph.addLocation(b); graph.addLocation(c);
        graph.addPath(p1); graph.addPath(p2); graph.addPath(p3);

        List<Path> result = nav.navigate(a, c, graph, User.STUDENT);

        assertThat(result.equals(new ArrayList<>(Arrays.asList(p3)))).isTrue();
    }

    @Test
    public void testNavigatorFindsOnlyAllowedRoutes() throws Exception{
        Graph graph = new Graph();
        List<User> onlyStaff = new ArrayList<>(Arrays.asList(User.DISABLED_STAFF, User.STAFF));
        List<User> notDisabled = new ArrayList<>(Arrays.asList(User.STUDENT, User.STAFF));
        Navigator nav = new DijkstraNavigator();

        Location a = new Location(0,0, "0", "a");
        Location b = new Location(0,1, "0", "b");
        Location c = new Location(1,0, "0", "c");
        Location d = new Location(2,0, "0", "d");

        Path p1 = new Path (a, b, notDisabled); // disabled not allowed
        Path p2 = new Path (b, c, notDisabled); // disabled not allowed
        Path p3 = new Path (a, c, new ArrayList<>(Arrays.asList(User.STAFF))); // only non-disabled staff allowed
        Path p4 = new Path (a, d, onlyStaff); // only staff
        Path p5 = new Path (d, c, onlyStaff); // only staff

        graph.addLocation(a); graph.addLocation(b); graph.addLocation(c); graph.addLocation(d);
        graph.addPath(p1); graph.addPath(p2); graph.addPath(p3); graph.addPath(p4); graph.addPath(p5);

        List<Path> result = nav.navigate(a, c, graph, User.STUDENT);
        assertThat(result.equals(new ArrayList<>(Arrays.asList(p1, p2)))).isTrue();

        result = nav.navigate(a, c, graph, User.DISABLED_STAFF);
        assertThat(result.equals(new ArrayList<>(Arrays.asList(p4, p5)))).isTrue();
    }

    @Test
    public void testNavigatorFailsWhenNoRoute() throws Exception{
        Graph graph = new Graph();
        Navigator nav = new DijkstraNavigator();

        Location a = new Location(0,0, "0", "a");
        Location b = new Location(0,1, "0", "b");
        Location c = new Location(1,0, "0", "c");

        Path p1 = new Path(a, b, new ArrayList<>(Arrays.asList(User.STAFF,User.DISABLED_STAFF)));
        Path p2 = new Path(b, c, new ArrayList<>(Arrays.asList(User.STAFF)));
        Path p3 = new Path(a, c, new ArrayList<>(Arrays.asList(User.STUDENT, User.STAFF)));

        graph.addLocation(a);
        graph.addLocation(b);
        graph.addLocation(c);
        graph.addPath(p1);
        graph.addPath(p2);
        graph.addPath(p3);

        assertThatThrownBy(() -> {
            List<Path> result = nav.navigate(a, c, graph, User.DISABLED_STUDENT);
        }).isInstanceOf(IllegalArgumentException.class);
    }

}
