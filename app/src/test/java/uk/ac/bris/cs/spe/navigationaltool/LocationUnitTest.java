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
            Location loc = new Location(null, 0, "loc");
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            Location loc = new Location(new Point(0, 0), 0, null);
        }).isInstanceOf(IllegalArgumentException.class);

        // second constructor
        assertThatThrownBy(() -> {
            Location loc = new Location(null, 0, "loc", "Location");
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            Location loc = new Location(new Point(0, 0), 0, null, "Location");
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            Location loc = new Location(new Point(0, 0), 0, "loc", null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

}
