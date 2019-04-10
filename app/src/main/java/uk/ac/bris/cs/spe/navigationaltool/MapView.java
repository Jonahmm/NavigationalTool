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

// This is all functionality that was previously in DisplayDrawer, but has been moved to help keep the code cleaner.

/**
 * Handles all graphic changes to the map.
 */
public class MapView extends PhotoView {

    /**
     * The original bitmaps for each floor
     */
    Map<String, Bitmap> maps = new ArrayMap<>();

    /**
     * Editable bitmaps for each floor
     */
    Map<String, Bitmap> bufs = new ArrayMap<>();

    /**
     * Canvas for drawing to each floor
     */
    Map<String, Canvas> canv = new ArrayMap<>();

    /**
     * Keeps track of which floor is displayed
     */
    String currentFloor;

    /**
     * The value st x * fct = y where x is a location and y is that of its representation on the map
     */
    float fct;

    /**
     * Paints for navigation
     */
    Paint pathPaint, highlightPaint, originPaint, destPaint, selectPaint, otherFloorPaint;

    /**
     * The {@link OnFloorChangeListener} to be notified when the floor displayed changes
     */
    OnFloorChangeListener onFloorChangeListener;

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
        pathPaint.setColor(Color.RED); pathPaint.setAntiAlias(true); pathPaint.setStrokeWidth(10);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.CYAN); highlightPaint.setAntiAlias(true);
        highlightPaint.setStrokeWidth(4); highlightPaint.setStyle(Paint.Style.STROKE);

        originPaint = new Paint();
        originPaint.setColor(Color.BLUE); originPaint.setAntiAlias(true);

        destPaint = new Paint(originPaint); destPaint.setColor(Color.GREEN);
        selectPaint = new Paint(originPaint); selectPaint.setColor(Color.RED);
        selectPaint.setAlpha(192);

        otherFloorPaint = new Paint(pathPaint);
        otherFloorPaint.setAlpha(64);
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
        dotLocation(from, originPaint);
        dotLocation(to, destPaint);
        showFloorBuffer(from.getFloor());
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

    /**
     * Draws a path between two Points on the buffer. Uses the value of fct to scale the given
     * points to their counterparts on the map
     */
    private void drawPathToBuffer(Point p1, Point p2, Paint pathPaint) {
        canv.get(currentFloor).drawLine(p1.x * fct, p1.y * fct, p2.x * fct, p2.y * fct, pathPaint);
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
     * guess
     */
    private void drawTextToBuffer(String floor, String text, Point loc) {
        //TODO Extract this paint
        Paint p = new Paint();
        p.setColor(Color.BLACK); p.setTextSize(20); p.setAntiAlias(true);
        canv.get(floor).drawText(text, (float) (loc.x - (p.getTextSize() * text.length() * 0.4)) * fct,
                (loc.y - (p.getTextSize() / 2)) * fct, p);
    }

    /**
     * Uses {@link #drawTextToBuffer(String, String, Point)} to write either code + name or just code
     */
    private void drawLocToBuffer(Location l) {
        drawTextToBuffer(l.getFloor(), l.hasName() ? l.getCode() + ", " + l.getName() : l.getCode(), l.getLocation());
    }

    /**
     * Like {@link #drawPathToBuffer(Point, Point, Paint pathPaint)} but lots
     * @param paths The collection of paths to draw
     */
    private void drawPathsToBuffer(Collection<Path> paths, Paint pathPaint) {
        for (Path p : paths) {
            drawPathToBuffer(p, pathPaint);
        }
    }

    /**
     * Like {@link #drawPathToBuffer(Path, Paint, Paint, Collection)} but lots
     * @param paths The collection of paths to draw
     */
    private void drawPathsToBuffer(Collection<Path> paths, Paint thisFloor, Paint otherFloor) {
        Collection<String> floors = getFloors(paths);
        for (Path p : paths) {
            drawPathToBuffer(p, thisFloor, otherFloor, floors);
        }
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
     * Resets the image buffer to be identical to map. Recalculates fct because… not sure. That
     * could probably be done only once in onCreate tbh but it's not the expensive part of this
     * method.
     */
    private void refreshBuffer(String floor) {
        bufs.replace(floor, maps.get(floor).copy(maps.get(floor).getConfig(), true));
        canv.replace(floor, new Canvas(bufs.get(floor)));
        updateFCT();
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

}
