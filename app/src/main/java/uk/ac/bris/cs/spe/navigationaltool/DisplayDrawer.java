package uk.ac.bris.cs.spe.navigationaltool;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        private static final float MAP_MIN_SCALE = 1.25f; Bitmap buf;
    Canvas canvas; float fctX;
    private boolean srchShown = false;
    int highlightIntervals[] = {30,90,180};

    private Location selectedLocation = null;
    private Location navigationSrc = null;
    private Location navigationDst = null;
float scale;

    private static final int NEAR_DISTANCE = 300;
    private Selecting selecting = Selecting.SELECTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        access = getPreferences(MODE_PRIVATE).getInt(getString(R.string.saved_access), R.id.item_ug);
        disabl = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.saved_disabl), false);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        //Set menu items to be checked depending on saved values
        navigationView.getMenu().findItem(intToId(access)).setChecked(true);
        Switch s = navigationView.getMenu().findItem(R.id.disabled_item)
                .getActionView().findViewById(R.id.disabled_switch);
        s.setChecked(disabl);

        navigationView.setNavigationItemSelectedListener(this);
        loadBuilding();

        map = BitmapFactory.decodeResource(getResources(), R.drawable.map0);

        mapView = findViewById(R.id.mapviewer);
        mapView.setImageBitmap(map);
        mapView.setMaximumScale(12f);


        setListeners();
        refreshBuffer();
        initPaints();
        mapView.setScale(MAP_MIN_SCALE);
        mapView.setMinimumScale(MAP_MIN_SCALE);
    }

    private Paint pathPaint, highlightPaint, originPaint, destPaint, selectPaint;

    void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            ArrayList<Path> done = new ArrayList<>();
            for (Location l : building.getGraph().getAllLocations()) {
                for (Path p : building.getGraph().getPathsFromLocation(l)) {
                    if (!done.contains(p) && p.locA.x != 0 && p.locA.y != 0 && p.locB.x != 0 && p.locB.y != 0) {
                        drawPathToBuffer(p.locA.getLocation(), p.locB.getLocation());
                        done.add(p);
                    }
                }
                if (!l.getLocation().equals(0,0)) drawTextToBuffer(l.code, l.getLocation());
            }
            displayBuffer();
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


                    canvas.drawCircle(from.getX() * fctX, from.getY() * fctX, 10, originPaint);
                    for (Path p : paths) {
                        drawPathToBuffer(p.locA.getLocation(), p.locB.getLocation());
                    }
                    canvas.drawCircle(to.getX() * fctX, to.getY() * fctX, 10, destPaint);
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

        Button navgo = findViewById(R.id.selected_get_directions);
        navgo.setOnClickListener(e -> {
            startNavigationTo(selectedLocation);
        });

        Button navs = findViewById(R.id.navigation_src_btn);
        navs.setOnClickListener(e -> startNavSelect(navs));
        Button navd = findViewById(R.id.navigation_dst_btn);
        navd.setOnClickListener(e -> startNavSelect(navd));
    }

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

    private void startNavSelect(Button btn) {
        cancelNavSelect(findViewById(btn.getId() == R.id.navigation_src_btn ? R.id.navigation_dst_btn
            : R.id.navigation_src_btn));
        selecting = btn.getId() == R.id.navigation_src_btn ? Selecting.NAVSRC : Selecting.NAVDST;
        btn.setText(R.string.selecting_text);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_close,0);
        btn.refreshDrawableState();
        btn.setOnClickListener(e -> cancelNavSelect(btn));
    }

    private void cancelNavSelect(Button btn) {
        selecting = Selecting.SELECTION;
        Location l = btn.getId() == R.id.navigation_src_btn ? navigationSrc : navigationDst;

        btn.setText(l != null ? (l.hasName() ? l.getName() : l.getCode()) : getString(R.string.click_to_edit));
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);
        btn.setOnClickListener(e -> startNavSelect(btn));

    }

    private void startNavigationTo(Location l) {
        navigationSrc = null;
        resetNavButtons();
        setNavigationDst(l);
        bottomBarShowNavigation();
    }

    private void resetNavButtons() {
        Button btn = findViewById(R.id.navigation_src_btn);
        btn.setText(R.string.click_to_edit);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);
        btn = findViewById(R.id.navigation_dst_btn);
        btn.setText(R.string.click_to_edit);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (selectedLocation != null) {
                if (navigationDst != null || navigationSrc != null) exitNavigation();
                else deselect();
            }
            else super.onBackPressed();
        }
    }

    private int weight(List<Path> p) {
        if (p.isEmpty()) return Integer.MAX_VALUE;
        return p.stream().reduce(0, (d,e) -> d + e.length, Integer::sum);
    }

    void highlightLocation(Location l) {
        refreshBuffer();
        for (int i : highlightIntervals) canvas.drawCircle(l.x * fctX, l.y * fctX, i, highlightPaint);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id != R.id.disabled_item) {
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            access = idToInt(id);
            e.putInt(getString(R.string.saved_access), access);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            e.apply();
        }

        return true;
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

            LinearLayout searches = findViewById(R.id.searches);
            searches.setVisibility(srchShown ? View.VISIBLE : View.GONE);
            item.setIcon(srchShown ? R.drawable.ic_collapse : R.drawable.ic_search);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        final float xx = x * getResources().getInteger(R.integer.map_width);
        final float yy = y * getResources().getInteger(R.integer.map_height);

        Map<Location, Double> memo = new HashMap<>();
        building.getGraph().getAllLocations().forEach(l -> memo.put(l,absDist(l,xx,yy)));
        Optional<Location> ol = memo.keySet().parallelStream().filter(l -> memo.get(l) < NEAR_DISTANCE)
                .reduce((a,b) -> memo.get(a) < memo.get(b) ? a : b);
        if (ol.isPresent()) {
            select(ol.get());
            return;
        }
        deselect();

        //snackMsg("Nothing here: " + x +"," + y);
    }


    private void drawPathToBuffer(Point p1, Point p2) {
        canvas.drawLine(p1.x * fctX, p1.y * fctX, p2.x * fctX, p2.y * fctX, pathPaint);
    }

    private void drawTextToBuffer(String text, Point loc) {
        Paint p = new Paint();
        p.setColor(Color.BLACK); p.setTextSize(20); p.setAntiAlias(true);
        canvas.drawText(text, (float) (loc.x - (p.getTextSize() * text.length() * 0.4)) * fctX, (loc.y - (p.getTextSize() / 2)) * fctX, p);
    }

    private void drawLocToBuffer(Location l) {
        drawTextToBuffer(l.hasName() ? l.getCode() + ", " + l.getName() : l.getCode(), l.getLocation());
    }

    private void displayBuffer() {
        mapView.setImageBitmap(buf.copy(buf.getConfig(), false));
        mapView.setScale(MAP_MIN_SCALE);

    }

    private void refreshBuffer() {
        buf = map.copy(map.getConfig(), true);
        canvas = new Canvas(buf);
        fctX = (float) map.getWidth() / getResources().getInteger(R.integer.map_width);
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

    private void select(Location l) {
        switch (selecting) {
            case NAVDST:
                setNavigationDst(l);
                break;
            case NAVSRC:
                setNavigationSrc(l);
                break;
            case SELECTION:
                showLocation(l);
                break;
            default:
                break;
        }
    }

    private void setNavigationSrc(Location l) {
        navigationSrc = l;
        Button btn = findViewById(R.id.navigation_src_btn);
        cancelNavSelect(btn);
        if (navigationDst != null) doNavigation();
        selecting = Selecting.SELECTION;
        formatNav();
    }

    private void setNavigationDst(Location l) {
        navigationDst = l;
        Button btn = findViewById(R.id.navigation_dst_btn);
        cancelNavSelect(btn);
        if (navigationSrc != null) doNavigation();
        selecting = Selecting.SELECTION;
        formatNav();
    }

    private void formatNav() {
        ImageButton sp = findViewById(R.id.navigation_swap_btn);
        ConstraintLayout nv = findViewById(R.id.navigation_panel);
        Log.d("Right bounds", sp.getRight() + " " + nv.getRight());
        ConstraintSet c = new ConstraintSet();
        c.clone(nv);
        if(sp.getRight() > nv.getRight()) {
            c.clear(R.id.navigation_dst_btn, ConstraintSet.TOP);
            c.connect(R.id.navigation_dst_btn, ConstraintSet.TOP, R.id.navigation_src_btn, ConstraintSet.BOTTOM);
            c.clear(R.id.navigation_dst_btn, ConstraintSet.LEFT);
        } else {
            c.connect(R.id.navigation_dst_btn, ConstraintSet.LEFT, R.id.navigation_src_btn, ConstraintSet.RIGHT);
            c.clear(R.id.navigation_dst_btn, ConstraintSet.TOP);
            c.connect(R.id.navigation_dst_btn, ConstraintSet.TOP, R.id.navigation_title, ConstraintSet.BOTTOM);
        }
        c.applyTo(nv);
    }

    private void deselect() {
        if (selectedLocation != null || navigationSrc != null || navigationDst != null) {
            selectedLocation = null;
            navigationSrc = null;
            navigationDst = null;
            selecting = Selecting.SELECTION;
            refreshBuffer();
            bottomBarHide();
            displayBuffer();
        }

    }

    private void exitNavigation() {
        navigationSrc = null;
        navigationDst = null;
        if (selectedLocation != null) {
            showLocation(selectedLocation);
        }
        else {
            bottomBarHide();
        }
    }

    private void showLocation(Location l) {
        navigationSrc = null;
        navigationDst = null;

        ConstraintLayout selBox = findViewById(R.id.bottom_box);
        bottomBarShowSelection();

        TextView tv = selBox.findViewById(R.id.selected_title);
        tv.setText(l.hasName() ? l.getName() : l.getCode());
        tv = selBox.findViewById(R.id.selected_subtitle);
        if (!l.hasName()) tv.setVisibility(View.GONE);
        else {
            tv.setText(l.getCode());
            tv.setVisibility(View.VISIBLE);
        }


        refreshBuffer();
        dotLocation(l, selectPaint);
        drawLocToBuffer(l);
        displayBuffer();

        Matrix m = new Matrix();
        mapView.getDisplayMatrix(m);
        float[] pts = {l.getX(), l.getY()};
        m.mapPoints(pts);
        mapView.setScale(4, pts[0] -200, pts[1]-250, false);
        //snackMsg(l.hasName() ? l.getCode() + " " + l.getName() : l.getCode());

        selectedLocation = l;
    }

    private double absDist(Location l, float x, float y) {
        //return Math.abs(l.getX() - x + l.getY() - y);
        return Math.sqrt(Math.pow(l.getX() - x, 2) + Math.pow(l.getY() - y, 2));
    }

    private void doNavigation() {
        User user = getUserFromParams(access, disabl);
        List<Path> paths = new ArrayList<>();
        Location from = null, to = null;
        for (Location l : building.getGraph().getLocationsByCode(navigationSrc.getCode())) {
            for (Location m : building.getGraph().getLocationsByCode(navigationDst.getCode())) {
                try {
                    List<Path> p = building.getNavigator().navigate(l,m,building.getGraph(),user);
                    if (weight(p) < weight(paths)) {
                        paths = p;
                        from = l; to = m;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if(from != null) {
            refreshBuffer();
            drawPathListToBuffer(paths);
            dotLocation(from, originPaint);
            dotLocation(to, destPaint);
            displayBuffer();
        } else alertMsg(getString(R.string.navigation_failure));
    }

    private void navigate(Location from, Location to) {
        try {
            refreshBuffer();
            drawPathListToBuffer(
            building.getNavigator().navigate(from, to, building.getGraph(), getUserFromParams(access, disabl)));
            dotLocation(from, originPaint);
            dotLocation(to, destPaint);
            displayBuffer();
        } catch (IllegalArgumentException e) {
            alertMsg("No path found for specified access level");
        }
    }

    private void bottomBarShowSelection() {
        ConstraintLayout botBox = findViewById(R.id.bottom_box);
        botBox.setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.selected_details).setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.navigation_panel).setVisibility(View.GONE);

    }

    private void bottomBarShowNavigation() {
        ConstraintLayout botBox = findViewById(R.id.bottom_box);
        botBox.setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.navigation_panel).setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.selected_details).setVisibility(View.GONE);
    }

    private void bottomBarHide() {
        findViewById(R.id.bottom_box).setVisibility(View.GONE);
    }

private enum Selecting {SELECTION, NAVSRC, NAVDST}

    private void dotLocation(Location l, Paint paint) {
        canvas.drawCircle(l.getX() * fctX, l.getY() * fctX, 20, paint);
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
