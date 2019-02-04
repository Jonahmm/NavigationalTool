package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;
import java.util.Map;

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

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public MapView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }



    /*----------*
     * GRAPHICS *
     *----------*/

    public void setFloor(String floor){
        if(maps.containsKey(floor)) {
            currentFloor = floor;
            setImageBitmap(maps.get(floor));
        }
        else{
            throw new IllegalArgumentException("No floor named \"" + floor + "\" was found.");
        }
    }


    /**
     * Draws a 40px-diameter dot on the buffer at the given Location
     * @param l The Location giving the point to draw at
     * @param paint The paint to use for the dot
     */
    public void dotLocation(Location l, Paint paint) {
        canv.get(l.getFloor()).drawCircle(l.getX() * fct, l.getY() * fct, 20, paint);
    }

    /**
     * Draws a path between two Points on the buffer. Uses the value of fct to scale the given
     * points to their counterparts on the map
     * @param p1
     * @param p2
     */
    public void drawPathToBuffer(Point p1, Point p2, Paint pathPaint) {
        canv.get(currentFloor).drawLine(p1.x * fct, p1.y * fct, p2.x * fct, p2.y * fct, pathPaint);
    }

    public void drawPathToBuffer(Path p, Paint pathPaint) {
        canv.get(p.getLocA().getFloor()).drawLine(p.getLocA().getX() * fct, p.getLocA().getY() * fct,
                p.getLocB().getX() * fct, p.getLocB().getY() * fct, pathPaint);
        if (p.isTransFloor()) {
            //Draw it to the floor of B too
            canv.get(p.getLocB().getFloor()).drawLine(p.getLocA().getX() * fct, p.getLocA().getY() * fct,
                    p.getLocB().getX() * fct, p.getLocB().getY() * fct, pathPaint);
        }
    }

    /**
     * guess
     * @param text
     * @param loc
     */
    public void drawTextToBuffer(String floor, String text, Point loc) {
        //TODO Extract this paint
        Paint p = new Paint();
        p.setColor(Color.BLACK); p.setTextSize(20); p.setAntiAlias(true);
        canv.get(floor).drawText(text, (float) (loc.x - (p.getTextSize() * text.length() * 0.4)) * fct,
                (loc.y - (p.getTextSize() / 2)) * fct, p);
    }

    /**
     * Uses {@link #drawTextToBuffer(String, String, Point)} to write either code + name or just code
     * @param l
     */
    public void drawLocToBuffer(Location l) {
        drawTextToBuffer(l.getFloor(), l.hasName() ? l.getCode() + ", " + l.getName() : l.getCode(), l.getLocation());
    }

    /**
     * Like {@link #drawPathToBuffer(Point, Point, Paint pathPaint)} but lots
     * @param paths The list of paths to draw
     */
    public void drawPathListToBuffer(List<Path> paths, Paint pathPaint) {
        for (Path p : paths) {
            drawPathToBuffer(p, pathPaint);
        }
    }

    /**
     * Replaces the map on screen with the buffer from memory
     */
    public void showFloorBuffer(String floor) {
        setImageBitmap(bufs.get(floor).copy(bufs.get(floor).getConfig(), false));
        setScale(DisplayDrawer.MAP_MIN_SCALE);
        currentFloor = floor;
    //    TextView tv = findViewById(R.id.floor_name);
    //    tv.setText(building.getFloorMap().getOrDefault(currentFloor, "ERROR"));
    }

    /**
     * Resets the image buffer to be identical to map. Recalculates fct because… not sure. That
     * could probably be done only once in onCreate tbh but it's not the expensive part of this
     * method.
     */
    public void refreshBuffer(String floor) {
        bufs.replace(floor, maps.get(floor).copy(maps.get(floor).getConfig(), true));
        canv.replace(floor, new Canvas(bufs.get(floor)));
        fct = (float) maps.get(floor).getWidth() / getResources().getInteger(R.integer.map_width);
    }


    /*----------*
     *   MISC   *
     *----------*/

    public void updateFCT(){
        fct = (float) maps.get(currentFloor).getWidth() / getResources().getInteger(R.integer.map_width);
    }

}
