package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Point;

import org.junit.Test;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import static org.assertj.core.api.Java6Assertions.*;

/**
 *  This class contains unit tests written for the Graph class' methods.
 *
 * @see uk.ac.bris.cs.spe.navigationaltool.graph.Location
 */
public class LocationUnitTest {

    @Test
    public void testNullArgumentInConstructorFails() throws Exception {
        // first constructor
        assertThatThrownBy(() -> {
            Location loc = new Location(0, null, "0", "loc");
        }).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> {
            Location loc = new Location(0, 0, 0, "0", null);
        }).isInstanceOf(NullPointerException.class);

        // second constructor
        assertThatThrownBy(() -> {
            Location loc = new Location(0, null, "0", "loc", "Location");
        }).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> {
            Location loc = new Location(0, 0, 0, "0", null, "Location");
        }).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> {
            Location loc = new Location(0, 0, 0, "0", "loc", null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEqualsMethodCorrectlyFlagsEqualLocations(){
        Location a = new Location(0, 0,0,"0","testA");
        Location b = new Location(0, 451, 459313, "-5", "testB", "second room");

        assertThat(a.equals(a)).isTrue();
        assertThat(b.equals(b)).isTrue();

        assertThat(a.equals(b)).isFalse();
        assertThat(b.equals(a)).isFalse();

        assertThat(b.hashCode() == b.hashCode()).isTrue();
        assertThat(a.hashCode() != b.hashCode()).isTrue();
    }

}
