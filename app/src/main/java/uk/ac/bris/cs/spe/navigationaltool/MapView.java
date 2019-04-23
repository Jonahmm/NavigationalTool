package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;

// This is all functionality that was previously in MapActivity, but has been moved to help keep the code cleaner.

/**
 * Handles all graphic changes to the map.
 */
public class MapView extends PhotoView {

    /**
     * The original bitmaps for each floor
     */
    private Map<String, Bitmap> maps = new ArrayMap<>();

    /**
     * Editable bitmaps for each floor
     */
    private Map<String, Bitmap> bufs = new ArrayMap<>();

    /**
     * Canvas for drawing to each floor
     */
    private Map<String, Canvas> canv = new ArrayMap<>();

    /**
     * Keeps track of which floor is displayed
     */
    private String currentFloor;

    /**
     * The value st x * fct = y where x is a location and y is that of its representation on the map
     */
    private float fct;

    /**
     * Paints for navigation
     */
    private Paint pathPaint, originPaint, destPaint, selectPaint, otherFloorPaint,
            donePaint, doneOtherPaint;

    /**
     * The {@link OnFloorChangeListener} to be notified when the floor displayed changes
     */
    private OnFloorChangeListener onFloorChangeListener;

    static int RESET_NONE = 0;
    static int RESET_CURRENT = 1;
    static int RESET_ALL = -1;


    // Default constructors taken from the parent.
    public MapView(Context context) { this(context, null); }
    public MapView(Context context, AttributeSet attr) {this(context, attr, 0); }
    public MapView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        initPaints();
    }

    /**
     * Loads the paints (used by the canvas) that draw various graphics to the screen, done here to
     * avoid creating a new Paint every time we draw something
     */
    private void initPaints() {
        pathPaint = new Paint();
        pathPaint.setColor(0xFF206680); pathPaint.setAntiAlias(true); pathPaint.setStrokeWidth(20);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        originPaint = new Paint();
        originPaint.setColor(Color.BLUE); originPaint.setAntiAlias(true);

        destPaint = new Paint(originPaint); destPaint.setColor(Color.GREEN);
        selectPaint = new Paint(originPaint); selectPaint.setColor(Color.RED);
        selectPaint.setAlpha(192);

        otherFloorPaint = new Paint(pathPaint);
        otherFloorPaint.setAlpha(64);

        donePaint = new Paint(pathPaint);
        donePaint.setColor(0xFF3FCCFF);

        doneOtherPaint = new Paint(donePaint);
        doneOtherPaint.setAlpha(64);
    }

    void setMaps(Map<String, Bitmap> maps) {
        this.maps = maps;
        for (Map.Entry<String, Bitmap> entry : maps.entrySet()) {
            Bitmap b = entry.getValue();
            b = b.copy(b.getConfig(), true);
            bufs.put(entry.getKey(), b);
            canv.put(entry.getKey(), new Canvas(b));
        }
    }

    /**
     * Set the listener to be notified on floor changes
     * @param l The {@link OnFloorChangeListener} to be set as the new {@link #onFloorChangeListener}
     */
    void setOnFloorChangeListener(OnFloorChangeListener l) {
        onFloorChangeListener = l;
    }

    /**
     * If {@link #onFloorChangeListener} isn't null, notify it of a floor change
     */
    private void notifyOnFloorChangeListener() {
        if (onFloorChangeListener != null)
            onFloorChangeListener.onFloorChanged(this, currentFloor);
    }

    /*-----------------*
     *  MAIN GRAPHICS  *
     *-----------------*/

    /**
     * Sets the MapView to display the given floor.
     * @param floor The floor to display.
     * @param reset Whether to clear the floor of added graphics.
     */
    public void setFloor(String floor, int reset){
        if(maps.containsKey(floor)) {
            if(reset==RESET_CURRENT) refreshBuffer(floor);
            else if (reset==RESET_ALL) refreshAllFloors();
            showFloorBuffer(floor);
        }
        else{
            throw new IllegalArgumentException("No floor named \"" + floor + "\" was found.");
        }
    }

    /**
     * Draws the given route.
     * @param from The starting location.
     * @param to The final location.
     * @param paths The list of paths that connect "from" and "to".
     */
    public void drawRoute(Location from, Location to, Collection<Path> paths){
        refreshAllFloors();
        drawPathsToBuffer(paths, pathPaint, otherFloorPaint);
    }

    /**
     * Draw the done paths and those yet to do in different colours. Not very optimised as yet
     */
    public void drawRoute(Route route) {
        refreshAllFloors();
        Collection<String> floors = getFloors(route.getAllPaths());
        drawPathsToBuffer(route.getPathsToDo(), pathPaint, otherFloorPaint, floors);
        drawPathsToBuffer(route.getDonePaths(), donePaint,  doneOtherPaint, floors);
        dotLocation(route.getStart(), originPaint);
        dotLocation( route.getEnd() ,  destPaint );
        showFloorBuffer(route.getCurrentStepStartPoint().getFloor());
    }

    /**
     * Draws a dot at a location, also clears all routes on the map.
     * @param l The location to draw.
     */
    public void drawLocation(Location l){
        refreshAllFloors();
        dotLocation(l, selectPaint);
        //drawLocToBuffer(l);
        showFloorBuffer(l.getFloor());
    }

    /**
     * Clears all floor images of any graphics which have been added to them.
     */
    private void refreshAllFloors(){
        maps.keySet().forEach(this::refreshBuffer);
    }


    /*-------------------*
     *  HELPER GRAPHICS  *
     *-------------------*/

    /**
     * Draws a 40px-diameter dot on the buffer at the given Location
     * @param l The Location giving the point to draw at
     * @param paint The paint to use for the dot
     */
    private void dotLocation(Location l, Paint paint) {
        canv.get(l.getFloor()).drawCircle(l.getX() * fct, l.getY() * fct, 20, paint);
    }

    private void drawPathToBuffer(Path p, Paint pathPaint) {
        canv.get(p.getLocA().getFloor()).drawLine(p.getLocA().getX() * fct, p.getLocA().getY() * fct,
                p.getLocB().getX() * fct, p.getLocB().getY() * fct, pathPaint);
        if (p.isTransFloor()) {
            //Draw it to the floor of B too
            canv.get(p.getLocB().getFloor()).drawLine(p.getLocA().getX() * fct, p.getLocA().getY() * fct,
                    p.getLocB().getX() * fct, p.getLocB().getY() * fct, pathPaint);
        }
    }

    /**
     * Slightly different to {@link #drawPathToBuffer(Path, Paint)}, this method draws the path to
     * every floor specified, using different paints to distinguish whether the path is on the floor
     * or not
     * @param p The path to be drawn
     * @param thisFloor The paint to use when a path is wholly or partly on a floor
     * @param otherFloor The paint to use otherwise
     * @param floors The list of floors to use: if a route only uses some floors, only draw to those
     *               involved.
     */
    private void drawPathToBuffer(Path p, Paint thisFloor, Paint otherFloor, Collection<String> floors) {
        for (String f : floors) {
            canv.get(f).drawLine(p.getLocA().getX() * fct, p.getLocA().getY() * fct,
                    p.getLocB().getX() * fct, p.getLocB().getY() * fct,
                    (f.equals(p.getLocA().getFloor()) || f.equals(p.getLocB().getFloor())) ?
                    thisFloor : otherFloor);
        }
    }

    /**
     * Like {@link #drawPathToBuffer(Path, Paint)} but lots
     * @param paths The collection of paths to draw
     */
    void drawPathsToBuffer(Collection<Path> paths, Paint pathPaint) {
        for (Path p : paths) {
            drawPathToBuffer(p, pathPaint);
        }
    }

    /**
     * Like {@link #drawPathToBuffer(Path, Paint, Paint, Collection)} but lots
     * @param paths The collection of paths to draw
     */
    private void drawPathsToBuffer(Collection<Path> paths, Paint thisFloor, Paint otherFloor,
            Collection<String> floors) {
        for (Path p : paths) drawPathToBuffer(p, thisFloor, otherFloor, floors);
    }

    /**
     * Wrapper for {@link #drawPathsToBuffer(Collection, Paint, Paint, Collection)} that generates
     * the floors collection
     */
    private void drawPathsToBuffer(Collection<Path> paths, Paint thisFloor, Paint otherFloor) {
        Collection<String> floors = getFloors(paths);
        drawPathsToBuffer(paths, thisFloor, otherFloor, floors);
    }


    /**
     * Replaces the map on screen with the buffer from memory
     */
    private void showFloorBuffer(String floor) {
        setImageBitmap(bufs.get(floor).copy(bufs.get(floor).getConfig(), false));
        currentFloor = floor;
        notifyOnFloorChangeListener();
    }

    /**
     * Resets the image buffer to be identical to map.
     */
    private void refreshBuffer(String floor) {
        bufs.replace(floor, maps.get(floor).copy(maps.get(floor).getConfig(), true));
        canv.replace(floor, new Canvas(bufs.get(floor)));
    }


    /*----------*
     *   MISC   *
     *----------*/

    public void updateFCT(){
        fct = (float) maps.get(currentFloor).getWidth() / getResources().getInteger(R.integer.map_width);
    }

    /**
     * Gets all the floors incorporated in a route. Not very efficient but not terrible either
     * @param paths The collection of paths to work on
     * @return The floors used by {@code paths}
     */
    private Set<String> getFloors(Collection<Path> paths) {
        Set<String> fs = new ArraySet<>();

        paths.forEach(p -> {
            fs.add(p.getLocA().getFloor());
            fs.add(p.getLocB().getFloor());
        });
        return fs.stream().distinct().collect(Collectors.toSet());
    }

    public String getCurrentFloor() {
        return currentFloor;
    }

    public float translate(float x) {return fct * x;}
}
