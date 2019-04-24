package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Drawable for a Voronoi diagram. Uses the Tektosyne API to generate a Voronoi diagram from pseudo
 * randomly-generated points and draws the edges in white on the canvas.
 */

public class VoronoiDrawable extends Drawable {

    private Paint p = new Paint();

    VoronoiDrawable() {
        p.setColor(Color.WHITE);
        p.setStrokeWidth(5);
    }

    private ArrayList<PointD> generatePoints() {
        int width = getBounds().width();
        int height = getBounds().height();

        // Set points for the 4 corners
        PointD p1 = new PointD(-10,-10);
        PointD p2 = new PointD(-10,height+10);
        PointD p3 = new PointD(width+10,-10);
        PointD p4 = new PointD(width+10,height+10);

        ArrayList<PointD> points = new ArrayList<>(Arrays.asList(p1,p2,p3,p4));

        int xInterval = 10;
        int yInterval = 10;

        double scaleX = width/((double) xInterval);
        double scaleY = height/((double) yInterval);
        Random random = new Random();
        int count = 0;

        // Get 7 randomly-generated points that are not too close to one another
        while (count < 7) {
            int x = random.nextInt(xInterval + 1);
            int y = random.nextInt(yInterval + 1);

            PointD newPoint = new PointD(x*scaleX, y*scaleY);
            if (points.stream().noneMatch(point -> distanceBetweenPoints(point,newPoint) < 100)) {
                points.add(newPoint);
                count++;
            }
        }

        return points;
    }

    private double distanceBetweenPoints(PointD p1, PointD p2) {
        return Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        ArrayList<PointD> points = generatePoints();

        VoronoiResults voronoiResults = Voronoi.findAll(points.toArray(new PointD[points.size()]));
        VoronoiEdge[] voronoiEdges = voronoiResults.voronoiEdges;
        PointD[] voronoiVertices = voronoiResults.voronoiVertices;

        Arrays.stream(voronoiEdges).forEach(edge -> {
            float x1 = (float) voronoiVertices[edge.vertex1].x;
            float y1 = (float) voronoiVertices[edge.vertex1].y;
            float x2 = (float) voronoiVertices[edge.vertex2].x;
            float y2 = (float) voronoiVertices[edge.vertex2].y;

            canvas.drawLine(x1, y1, x2, y2, p);
        });
    }

    @Override
    public int getAlpha() {
        return super.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        p.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        p.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
