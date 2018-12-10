package uk.ac.bris.cs.spe.navigationaltool;

import org.junit.Test;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.navigator.BreadthFirstNavigator;
import uk.ac.bris.cs.spe.navigationaltool.navigator.DijkstraNavigator;

import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 *  This class contains unit tests written for the Building class' methods.
 *
 *  // TODO: update this with correct location for the Building.
 * @see
 */
public class BuildingUnitTest {

    private String testBuildingFileLocation = ""; // TODO: make this the path directly to the fire
    private String badBuildingFileLocation = ""; // TODO: make this the path to the directory, name files after their failures

    ///// Construction Tests /////

    @Test
    public void testOnlyAndAllListedLocationsInGraph() throws Exception{
        Building building = new Building (testBuildingFileLocation, new BreadthFirstNavigator());
        Graph g = building.getGraph();

        // TODO: Requires creating the test files & knowing what's in them, and converting that into this.

    }

    @Test
    public void testOnlyAndAllListedPathsInGraph() throws Exception{
        // TODO: Requires creating the test files & knowing what's in them, and converting that into this.
    }

    @Test
    public void testBadArgumentFails() throws Exception{
        assertThatThrownBy(() -> new Building("incorrect file name.bad file extension", new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building(testBuildingFileLocation, null))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building(null, new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBadFileLayoutFails() throws Exception{
        assertThatThrownBy(() -> new Building(badBuildingFileLocation + "layout", new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicateLocationFails() throws Exception{
        assertThatThrownBy(() -> new Building(badBuildingFileLocation + "duplicateLocation", new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDuplicatePathFails() throws Exception{
        assertThatThrownBy(() -> new Building(badBuildingFileLocation + "duplicatePath", new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test // TODO: See if this is actually necessary with the implementation we have.
    public void testAddingLocationWithUnknownFloorFails() throws Exception{
        assertThatThrownBy(() -> new Building(badBuildingFileLocation + "LocationFloor", new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test // ie: If you have two 'islands' of locations that do not have connecting paths, then this should fail after loading.
    public void testHavingUnconnectedLocationsFails() throws Exception{
        assertThatThrownBy(() -> new Building(badBuildingFileLocation + "unconnectedLocations", new BreadthFirstNavigator()))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    ///// Construction Tests End /////

    @Test
    public void testSetNavigatorChangesNavigator() throws Exception{
        Building building = new Building(testBuildingFileLocation, new BreadthFirstNavigator());
        assertThat(building.getNavigator() instanceof BreadthFirstNavigator).isTrue();

        building.setNavigator(new DijkstraNavigator());
        assertThat(building.getNavigator() instanceof DijkstraNavigator).isTrue();
    }

    @Test
    public void testGetNameIsCorrect() throws Exception {
        Building building = new Building(testBuildingFileLocation, new BreadthFirstNavigator());
        assertThat(building.getName().equals(testBuildingFileLocation)).isTrue();

    }

    @Test
    public void testNullArgs(){
        assertThatThrownBy(() -> new Building(testBuildingFileLocation, new BreadthFirstNavigator(), null))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building(testBuildingFileLocation, null, null))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Building(null, new BreadthFirstNavigator(), null))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
