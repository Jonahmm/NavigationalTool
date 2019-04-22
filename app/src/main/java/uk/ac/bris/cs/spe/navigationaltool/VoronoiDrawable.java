package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;


public class VoronoiDrawable extends Drawable {

    private Paint p = new Paint();

    public VoronoiDrawable() {
//        p.setColor(Color.parseColor("#B01C2E"));
        p.setColor(Color.WHITE);
        p.setStrokeWidth(5);
    }

    private ArrayList<PointD> generatePoints() {
        int width = getBounds().width();
        int height = getBounds().height();

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

    private boolean isTooCloseOrTooFar(PointD p1, PointD p2) {
        return (Math.abs(p1.x-p2.x)<200 && Math.abs(p1.y-p2.y)<200);
//                || (Math.abs(p1.x-p2.x)>1000 || Math.abs(p1.y-p2.y)>1000);
    }

    private double distanceBetweenPoints(PointD p1, PointD p2) {
        return Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
    }

    @Override
    public void draw(Canvas canvas) {
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
//        int width = getBounds().width();
//        int height = getBounds().height();
//        canvas.drawLine(0, height, width, height, p);
    }

    @Override
    public int getAlpha() {
        return super.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
