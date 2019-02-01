package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
        Location loc1 = new Location (0, 0,0, "1", "loc1");
        Location loc2 = new Location (1, 1,0, "2", "loc2");

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
        Location locA = new Location(0, 0,0, "0", "locA");
        Location locB = new Location(1, 0,1, "1", "locB");
        List<User> users = new ArrayList<>();

        users.add(null);

        // There's a null user - the only possible invalid user i can think of
        assertThatThrownBy(() -> {
            Path p = new Path (locA, locB, users);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNoUserFails() throws Exception{
        Location locA = new Location(0, 0,0, "0", "locA");
        Location locB = new Location(1, 0,1, "1", "locB");
        List<User> users = new ArrayList<>();

        assertThatThrownBy(() -> {
            Path p = new Path (locA, locB, users);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testEqualsAndHashCode(){
        Location a = new Location(0, 0,0,"0","0");
        Location b = new Location(1, 0,1,"0","1");
        Location c = new Location(2, 1,0,"0","2");

        List<User> allUsers = new ArrayList<>(Arrays.asList(User.STUDENT, User.DISABLED_STUDENT, User.STAFF, User.DISABLED_STAFF));
        List<User> onlyStaff = new ArrayList<>(Arrays.asList(User.DISABLED_STAFF, User.STAFF));



        assertThat((new Path(a,b,allUsers)).equals(new Path(a,b,allUsers))).isTrue();
        assertThat((new Path(a,b,allUsers)).equals(new Path(b,a,allUsers))).isTrue(); // A path is the same no matter what way around

        assertThat((new Path(a,b,onlyStaff)).equals(new Path(a,b,allUsers))).isFalse();
        assertThat((new Path(a,b,allUsers)).equals(new Path(a,b,onlyStaff))).isFalse();
        assertThat((new Path(a,c,allUsers)).equals(new Path(a,b,allUsers))).isFalse();
        assertThat((new Path(a,b,allUsers)).equals(new Path(a,c,allUsers))).isFalse();
        assertThat((new Path(a,b,allUsers)).equals(new Path(c,a,allUsers))).isFalse();
    }

}
