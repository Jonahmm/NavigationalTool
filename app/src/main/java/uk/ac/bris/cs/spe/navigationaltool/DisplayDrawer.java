package uk.ac.bris.cs.spe.navigationaltool;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.DijkstraNavigator;

public class DisplayDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnPhotoTapListener{

    private Building building;
    private int access;
    private Boolean disabl;
    PhotoView mapView;
    Bitmap map;
    Bitmap buf; Canvas canvas; float fct;
    private boolean srchShown = false;
    int highlightIntervals[] = {30,90,180};

    private Location selectedLocation = null;

    private static final int NEAR_DISTANCE = 300;
    private static final int MAP_SIDE_LENGTH = 4320;

    private Paint pathPaint, highlightPaint, originPaint, destPaint, selectPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        access = getPreferences(MODE_PRIVATE).getInt(getString(R.string.saved_access), R.id.item_ug);
        disabl = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.saved_disabl), false);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Set menu items to be checked depending on saved values
        navigationView.getMenu().findItem(intToId(access)).setChecked(true);
        Switch s = navigationView.getMenu().findItem(R.id.disabled_item)
                .getActionView().findViewById(R.id.disabled_switch);
        s.setChecked(disabl);

        navigationView.setNavigationItemSelectedListener(this);
        loadBuilding();

        map = BitmapFactory.decodeResource(getResources(), R.drawable.map0);

        mapView = (PhotoView) findViewById(R.id.mapviewer);
        mapView.setImageBitmap(map);
        mapView.setMaximumScale(12);


        setListeners();
        refreshBuffer();
        initPaints();
    }

    private void initPaints() {
        pathPaint = new Paint();
        pathPaint.setColor(Color.RED); pathPaint.setAntiAlias(true); pathPaint.setStrokeWidth(3);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.CYAN); highlightPaint.setAntiAlias(true);
        highlightPaint.setStrokeWidth(4); highlightPaint.setStyle(Paint.Style.STROKE);

        originPaint = new Paint();
        originPaint.setColor(Color.BLUE); originPaint.setAntiAlias(true);

        destPaint = new Paint(originPaint); destPaint.setColor(Color.GREEN);
        selectPaint = new Paint(originPaint); selectPaint.setColor(Color.RED);

     }

    void loadBuilding() {
        try {
            building = new Building("0", new DijkstraNavigator(),
                    getApplicationContext());
            snackMsg( "Imported " + building.getGraph().getAllLocations().size()
                            + " locations.");
        } catch (IOException e) {
            snackMsg("Error importing building");
        } catch (IllegalArgumentException e) {
            snackMsg(e.getMessage());
        }
    }

    void setListeners() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                //drawPathOnImage(new Point(100,100), new Point(200,400));
                ArrayList<Path> done = new ArrayList<>();
                for (Location l : building.getGraph().getAllLocations()) {
                    for (Path p : building.getGraph().getPathsFromLocation(l)) {
                        //Log.v("Drawing path", p.locA.getLocationString() + p.locB.getLocationString());
                        if (!done.contains(p) && p.locA.x != 0 && p.locA.y != 0 && p.locB.x != 0 && p.locB.y != 0) {
                            drawPathToBuffer(p.locA.getLocation(), p.locB.getLocation());
                            done.add(p);
                        }
                    }
                    if (!l.getLocation().equals(0,0)) drawTextToBuffer(l.code, l.getLocation());
                }
                displayBuffer();
            }
        });

        ImageButton navBtn = findViewById(R.id.navButton);
        navBtn.setOnClickListener(view -> {
            EditText navFrom = findViewById(R.id.navFrom);
            EditText navTo   = findViewById(R.id.navTo);
            navTo.clearFocus(); navFrom.clearFocus();

            Location to = building.getGraph().getBestMatchLocation(navTo.getText().toString());
            Set<Location> tos = building.getGraph().getLocationsByCode(to==null?null:to.getCode());

            if (tos.isEmpty()) {alertMsg("No location(s) \"" + navTo.getText().toString() + "\" found"); return;}
            navTo.setText(to.code);

            Location from = building.getGraph().getBestMatchLocation(navFrom.getText().toString());
            Set<Location> froms = building.getGraph().getLocationsByCode(from==null?null:from.getCode());
            if (froms.isEmpty()) {
                //Just find the navTo on a map
                snackMsg("No origin specified, highlighting destination.");
                building.getGraph().getAllLocations().stream()
                        .filter(l -> l.getCode().startsWith(navTo.getText().toString().toUpperCase())
                                || (l.hasName() && l.getName().startsWith(navTo.getText().toString().toUpperCase())))
                        .findFirst().ifPresent(DisplayDrawer.this::highlightLocation);
            }
            else {
                refreshBuffer();
                try {
                    List<Path> paths = new ArrayList<>();
                    for (Location src : froms) {
                        for (Location dst : tos) {
                            List<Path> p = building.getNavigator().navigate(src, dst, building.getGraph(), getUserFromParams(access, disabl));
                            if (weight(p) < weight(paths)) {
                                paths = p;
                                from = src;
                                to = dst;
                            }
                        }
                    }


                    canvas.drawCircle(from.getX() * fct, from.getY() * fct, 10, originPaint);
                    for (Path p : paths) {
                        drawPathToBuffer(p.locA.getLocation(), p.locB.getLocation());
                    }
                    canvas.drawCircle(to.getX() * fct, to.getY() * fct, 10, destPaint);
                    displayBuffer();
                }
                catch (IllegalArgumentException e) {
                    alertMsg("No path found for specified access level");
                }

                navFrom.setText(from.code);
            }
            findViewById(R.id.mapviewer).requestFocus();

        });

        mapView.setOnPhotoTapListener(this);

        NavigationView navigationView = findViewById(R.id.nav_view);
        Switch s = navigationView.getMenu().findItem(R.id.disabled_item).getActionView()
                .findViewById(R.id.disabled_switch);
        s.setOnCheckedChangeListener((compoundButton, b) -> {
            disabl = b;
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            e.putBoolean(getString(R.string.saved_disabl), disabl);
            e.apply();

        });
    }

    private int weight(List<Path> p) {
        if (p.isEmpty()) return Integer.MAX_VALUE;
        return p.stream().reduce(0, (d,e) -> d + e.length, Integer::sum);
    }

    void highlightLocation(Location l) {
        refreshBuffer();
        for (int i : highlightIntervals) canvas.drawCircle(l.x * fct, l.y * fct, i, highlightPaint);
        displayBuffer();
    }


    //Quick and dirty menu -> User implementation
    private User getUserFromParams(Integer access, Boolean disabl) {
        switch (intToId(access)) {
            case R.id.item_ug: return disabl ? User.DISABLED_STUDENT : User.STUDENT;
            case R.id.item_staff: return disabl ? User.DISABLED_STAFF : User.STAFF;
            default: return User.STUDENT;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.show_search) {
            srchShown = !srchShown;

            LinearLayout searches = (LinearLayout) findViewById(R.id.searches);
            searches.setVisibility(srchShown ? View.VISIBLE : View.GONE);
            item.setIcon(srchShown ? R.drawable.ic_collapse : R.drawable.ic_search);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id != R.id.disabled_item) {
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            access = idToInt(id);
            e.putInt(getString(R.string.saved_access), access);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            e.apply();
        }

        return true;
    }


    private void drawPathToBuffer(Point p1, Point p2) {

        canvas.drawLine(p1.x * fct, p1.y * fct, p2.x * fct, p2.y * fct, pathPaint);
        //mapView.setImageBitmap(tmp);
    }

    private void drawTextToBuffer(String text, Point loc) {
        Paint p = new Paint();
        p.setColor(Color.BLACK); p.setTextSize(20); p.setAntiAlias(true);
        canvas.drawText(text, (float) (loc.x - (p.getTextSize() * text.length() * 0.4)) * fct, (loc.y - (p.getTextSize() / 2)) * fct, p);
    }

    private void drawLocToBuffer(Location l) {
        drawTextToBuffer(l.hasName() ? l.getCode() + ", " + l.getName() : l.getCode(), l.getLocation());
    }

    private void displayBuffer() {
        mapView.setImageBitmap(buf.copy(buf.getConfig(), false));
    }

    private void refreshBuffer() {
        buf = map.copy(map.getConfig(), true);
        canvas = new Canvas(buf);
//        Log.d("Map width ", Integer.toString(map.getWidth()));
        fct = (float) map.getWidth() / getResources().getInteger(R.integer.map_width);
//        Log.d("Got factor ", Float.toString(fct));

//        for (Location l : building.getGraph().getAllLocations()) {
//            drawLocToBuffer(l);
//        }
        //displayBuffer();
    }

    private void snackMsg(String s) {
        Snackbar.make(findViewById(R.id.constraint_layout), s, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void alertMsg(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s)
                .setCancelable(false)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        final float xx = x * MAP_SIDE_LENGTH;
        final float yy = y * MAP_SIDE_LENGTH;

        Optional<Location> ol = building.getGraph().getAllLocations().stream().filter(
                l -> absDist(l, xx, yy) < NEAR_DISTANCE
        ).reduce((a,b) -> (absDist(a,xx,yy) < absDist(b,xx,yy)) ? a : b);

        if (ol.isPresent()) {
            selectLocation(ol.get());
            return;
        }
        deselect();

        //snackMsg("Nothing here: " + x +"," + y);
    }

    private void deselect() {
        selectedLocation = null;
        refreshBuffer();
        displayBuffer();
    }

    private double absDist(Location l, float x, float y) {
        return Math.sqrt(Math.pow(l.getX() - x, 2) + Math.pow(l.getY() - y, 2));
    }

    private void selectLocation(Location l) {
        if (selectedLocation != null) navigate(selectedLocation, l);
        else {
            refreshBuffer();
            dotLocation(l, selectPaint);
            drawLocToBuffer(l);
            displayBuffer();
            snackMsg(l.hasName() ? l.getCode() + " " + l.getName() : l.getCode());
        }
        selectedLocation = l;
    }

    private void navigate(Location from, Location to) {
        try {
            refreshBuffer();
            drawPathListToBuffer(
            building.getNavigator().navigate(from, to, building.getGraph(), getUserFromParams(access, disabl))
            );
            dotLocation(from, originPaint);
            dotLocation(to, destPaint);
            displayBuffer();
        } catch (IllegalArgumentException e) {
            snackMsg("No path found for specified access level");
        }
    }

    private void dotLocation(Location l, Paint paint) {
        canvas.drawCircle(l.getX() * fct, l.getY() * fct, 20, paint);
    }

    private void drawPathListToBuffer(List<Path> paths) {
        for (Path p : paths) {
            drawPathToBuffer(p.getLocA().getLocation(), p.getLocB().getLocation());
        }
    }

    private int idToInt(int id) {
        switch (id) {
            case R.id.item_ug:    return 1;
            case R.id.item_pg:    return 2;
            case R.id.item_phd:   return 3;
            case R.id.item_staff: return 4;
            default: return 1;
        }
    }
    private int intToId(int in) {
        switch (in) {
            case 1: return R.id.item_ug;
            case 2: return R.id.item_pg;
            case 3: return R.id.item_phd;
            case 4: return R.id.item_staff;
            default: return R.id.item_ug;
        }
    }
}
