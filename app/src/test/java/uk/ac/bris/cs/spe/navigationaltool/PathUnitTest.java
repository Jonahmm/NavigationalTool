package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;

import static org.assertj.core.api.Java6Assertions.*;

/**
 *  This class contains unit tests written for the Graph class' methods.
 *
 * @see uk.ac.bris.cs.spe.navigationaltool.graph.Path
 */
public class PathUnitTest {

    @Test
    public void testNullArgumentInConstructorFails() throws Exception{
        Location loc1 = new Location (new Point(0,0), 1, "loc1");
        Location loc2 = new Location (new Point(1,0), 2, "loc2");

        assertThatThrownBy(() -> {
            Path p1 = new Path (null, loc1, new ArrayList<>());
        }).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> {
            Path p2 = new Path (loc1, null, new ArrayList<>());
        }).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> {
            Path p3 = new Path (loc1, loc2, null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testBadUserFails() throws Exception{
        Location locA = new Location(new Point(0,0), 0, "locA");
        Location locB = new Location(new Point(0,1), 1, "locB");
        List<User> users = new ArrayList<>();

        users.add(null);

        // There's a null user - the only possible invalid user i can think of
        assertThatThrownBy(() -> {
            Path p = new Path (locA, locB, users);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNoUserFails() throws Exception{
        Location locA = new Location(new Point(0,0), 0, "locA");
        Location locB = new Location(new Point(0,1), 1, "locB");
        List<User> users = new ArrayList<>();

        assertThatThrownBy(() -> {
            Path p = new Path (locA, locB, users);
        }).isInstanceOf(IllegalArgumentException.class);
    }

}
