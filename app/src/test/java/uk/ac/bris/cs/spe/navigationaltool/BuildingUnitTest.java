package uk.ac.bris.cs.spe.navigationaltool;

import org.junit.Test;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.navigator.BreadthFirstNavigator;

/**
 *  This class contains unit tests written for the Building class' methods.
 *
 *  // TODO: update this with correct location for the Building.
 * @see
 */
public class BuildingUnitTest {

    ///// Construction Tests /////

    @Test
    public void testOnlyAndAllListedLocationsInGraph() throws Exception{
        Building building = new Building ("test.buildingLayout", new BreadthFirstNavigator());
        Graph g = building.getGraph();


    }

    @Test
    public void testOnlyAndAllListedPathsInGraph() throws Exception{

    }

    @Test
    public void testNoFileFails() throws Exception{

    }

    @Test
    public void testBadFileLayoutFails() throws Exception{

    }

    @Test
    public void testDuplicateLocationFails() throws Exception{

    }

    @Test
    public void testDuplicatePathFails() throws Exception{

    }

    @Test // TODO: See if this is actually necessary with the implementation we have.
    public void testAddingLocationWithUnknownFloorFails() throws Exception{

    }

    @Test // ie: If you have two 'islands' of locations that do not have connecting paths, then this should fail after loading.
    public void testHavingUnconnectedLocationsFails() throws Exception{

    }

    ///// Construction Tests End /////


    @Test
    public void testSetNavigatorChangesNavigator() throws Exception{

    }

    @Test
    public void testGetNameIsCorrect() throws Exception{

    }

    @Test
    public void testGetGraphReturnsCorrectGraph() throws Exception{

    }
}
