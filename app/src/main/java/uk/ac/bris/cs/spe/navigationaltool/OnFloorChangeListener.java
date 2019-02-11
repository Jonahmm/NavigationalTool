package uk.ac.bris.cs.spe.navigationaltool;

/**
 * A callback to be invoked when a {@link MapView} changes its currently displayed floor.
 * Introduced so that {@link DisplayDrawer} can update its floor indicator even when the floor is
 * changed from an internal {@link MapView#showFloorBuffer(String)} call.
 */
public interface OnFloorChangeListener {

    /**
     * Called when {@code mapView}'s floor updates
     * @param mapView The {@link MapView} the event originated from. Not necessary for current
     *                implementation but included for potential use in future
     * @param newFloor The floor code of the new floor
     */
    void onFloorChanged(MapView mapView, String newFloor);
}
