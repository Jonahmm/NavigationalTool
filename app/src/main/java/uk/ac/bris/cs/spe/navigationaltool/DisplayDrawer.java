package uk.ac.bris.cs.spe.navigationaltool;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
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
    /**
     * The scale st the map's width == screen width (should be set depending on AR, but this works for 16:9)
     */
    private static final float MAP_MIN_SCALE = 1.25f;
    /**
     * The distance (in px) that locations should be < to a screen tap in order to be selected
     */
    private static final int NEAR_DISTANCE = 300;

    /**
     * The map on screen. Kept global to avoid a lot of {@link #findViewById(int)} calls
     */
    PhotoView mapView;
    /**
     * The original image of the map. Doesn't change once set.
     */
    Bitmap map;
    /**
     * The buffer image kept in memory that we draw onto
     */
    Bitmap buf;
    /**
     * For drawing onto the buffer
     */
    Canvas canvas;

    /**
     * The value st x * fct = y where x is a location and y is that of its representation on the map
     */
    float fct;

    /**
     * Used for old system.
     * @see #highlightLocation(Location)
     */
    int highlightIntervals[] = {30,90,180};

    /**
     * The building object to be worked on. Loaded using {@link #loadBuilding()}
     */
    private Building building;

    /**
     * Because we can't store User objects primitively, it makes sense to convert them into types
     * that can denote access rights and ability.
     */
    private int access;
    /**
     * True if user requires accessible route
     */
    private Boolean disabl;

    /**
     * Used for old system
     */
    private boolean srchShown = false;

    /**
     * Stores the selected location
     */
    private Location selectedLocation = null;
    /**
     * Stores the navigation source location
     */
    private Location navigationSrc = null;
    /**
     * Stores the navigation destination location
     */
    private Location navigationDst = null;

    /**
     * Keeps track of what a selection (either by tap or search) is for
     */
    private enum Selecting {SELECTION, NAVSRC, NAVDST}

    /**
     * Defines the current state of the program
     */
    private Selecting selecting = Selecting.SELECTION;

    private Paint pathPaint, highlightPaint, originPaint, destPaint, selectPaint;

    /*----------------*
     * INITIALISATION *
     *----------------*/


    /**
     * Initialises the options menu
     * @param menu The menu to inflate
     * @return idk, see {@link android.support.v7.app.AppCompatActivity#onCreateOptionsMenu(Menu)}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_drawer, menu);
        return true;
    }

    /**
     * Initialise and populate the activity. Further details are in code comments
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Load access requirements from saved preferences (if set, otherwise assume able UG)
        access = getPreferences(MODE_PRIVATE).getInt(getString(R.string.saved_access), 1);
        disabl = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.saved_disabl), false);

        //Makes the drawer behave
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

        //Allows updating access requirements from menu
        navigationView.setNavigationItemSelectedListener(this);

        loadBuilding();

        //Initialise image
        map = BitmapFactory.decodeResource(getResources(), R.drawable.map0);
        mapView = findViewById(R.id.mapviewer);
        mapView.setImageBitmap(map);
        mapView.setMaximumScale(12f);


        setListeners();
        refreshBuffer();
        initPaints();
        mapView.setScale(MAP_MIN_SCALE);
        mapView.setMinimumScale(MAP_MIN_SCALE);
        /* The above doesn't seem to actually update the view, it waits until you interact with it,
           which is *really* annoying */
    }

    /**
     * Where the magic happens. This one sets all the listeners (classes (but basically just methods)
     * that respond to events)
     */
    void setListeners() {
        //For testing, draws all paths on screen
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

        //Used by old system. BAD.
        ImageButton navBtn = findViewById(R.id.navButton);
        //Recommend hiding this atrocity with the [-] button on the left
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

        //Sets up selection by tap
        mapView.setOnPhotoTapListener(this);

        //Sets up disabled switch
        NavigationView navigationView = findViewById(R.id.nav_view);
        Switch s = navigationView.getMenu().findItem(R.id.disabled_item).getActionView()
                .findViewById(R.id.disabled_switch);
        s.setOnCheckedChangeListener((compoundButton, b) -> {
            disabl = b;
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            e.putBoolean(getString(R.string.saved_disabl), disabl);
            e.apply();

        });

        //Button to start nav
        Button navgo = findViewById(R.id.selected_get_directions);
        navgo.setOnClickListener(e -> {
            startNavigationTo(selectedLocation);
        });

        //Navigation buttons
        Button navs = findViewById(R.id.navigation_src_btn);
        navs.setOnClickListener(e -> startNavSelect(navs));
        Button navd = findViewById(R.id.navigation_dst_btn);
        navd.setOnClickListener(e -> startNavSelect(navd));
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

    }

    /**
     * Attempts to load the building from assets. Should never fail on a working build as the assets
     * don't change, but handles errors because java doesn't like it if it doesn't
     */
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

    /*--------------*
     * PROGRAM FLOW *
     *--------------*/

    /**
     * Handles the back button: uses {@link #exitNavigation()} or {@link #deselect()} when nav or
     * selection are active respectively
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (navigationDst != null || navigationSrc != null) exitNavigation();
            else if (selectedLocation != null) {
                deselect();
            }
            else super.onBackPressed();
        }
    }

    /**
     * Handles options item events; currently only used by old system
     * @param item The MenuItem that was selected
     * @return idk, see {@link android.support.v7.app.AppCompatActivity#onOptionsItemSelected(MenuItem)}
     */
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

    /**
     * Handles the changing of access requirements (staff/stu/etc) Note that using the disabled
     * switch is not handled here as its use doesn't count as 'Selecting' a menu item
     */
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

    /*------------*
     * NAVIGATION *
     *------------*/

    /**
     * Changes the app state st when you select a location, it is used for navigation.
     * Sets the text, icon and listener of the button it came from
     * @param btn The Button object that this came from, one of navigation_src_btn or
     *            navigation_dst_btn
     */
    private void startNavSelect(Button btn) {
        cancelNavSelect(findViewById(btn.getId() == R.id.navigation_src_btn ? R.id.navigation_dst_btn
                : R.id.navigation_src_btn));
        selecting = btn.getId() == R.id.navigation_src_btn ? Selecting.NAVSRC : Selecting.NAVDST;
        btn.setText(R.string.selecting_text);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_close,0);
        btn.refreshDrawableState();
        btn.setOnClickListener(e -> cancelNavSelect(btn));
    }

    /**
     * The navigation-specific counterpart to {@link #deselect()}. If a location was selected before
     * we started navigation, go back to showing it. Otherwise just hides the nav UI
     */
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

    /**
     * Reverses the effects of {@link #startNavSelect}
     * @param btn The Button object that this came from, one of navigation_src_btn or
     *            navigation_dst_btn
     */
    private void cancelNavSelect(Button btn) {
        selecting = Selecting.SELECTION;
        Location l = btn.getId() == R.id.navigation_src_btn ? navigationSrc : navigationDst;

        btn.setText(l != null ? (l.hasName() ? l.getName() : l.getCode()) : getString(R.string.click_to_edit));
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);
        btn.setOnClickListener(e -> startNavSelect(btn));

    }

    /**
     * Brings up the navigation interface with the given location as the destination
     * @param l The location to nav to. Currently this is always == selectedLocation, but once
     *          we have a new search interface this may be called separately
     */
    private void startNavigationTo(Location l) {
        navigationSrc = null;
        resetNavButtons();
        setNavigationDst(l);
        bottomBarShowNavigation();
    }

    /**
     * Sets the source of the current navigation route to the given {@link Location}, including
     * setting button text and performing the navigation if appropriate
     * @param l The {@link Location} to be the new source
     */
    private void setNavigationSrc(Location l) {
        navigationSrc = l;
        Button btn = findViewById(R.id.navigation_src_btn);
        cancelNavSelect(btn);
        if (navigationDst != null) doNavigation();
        selecting = Selecting.SELECTION;
        formatNav();
    }

    /**
     * Sets the navigation buttons back to their original state.
     */
    private void resetNavButtons() {
        Button btn = findViewById(R.id.navigation_src_btn);
        btn.setText(R.string.click_to_edit);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);
        btn = findViewById(R.id.navigation_dst_btn);
        btn.setText(R.string.click_to_edit);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);

    }

    /**
     * Basically identical to {@link #setNavigationSrc(Location)}. The two could be combined but
     * that would require more parameters
     * @see #setNavigationSrc(Location)
     * @param l The {@link Location} to be the new destination
     */
    private void setNavigationDst(Location l) {
        navigationDst = l;
        Button btn = findViewById(R.id.navigation_dst_btn);
        cancelNavSelect(btn);
        if (navigationSrc != null) doNavigation();
        selecting = Selecting.SELECTION;
        formatNav();
    }

    /**
     * The big one, the one we've all been waiting for, The actual point of our app.
     *
     * Uses the Navigator to compute the path between all pairs of nodes x,y, where the codes of x
     * and y match those of navigationSrc and navigationDst respectively, then selects the path with the lowest
     * total weight using {@link #weight(List)}. It draws this route to the buffer using
     * {@link #drawPathListToBuffer(List)}, places dots at the start and end of the route, then
     * shows the buffer.
     */
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

    /*-----------*
     * SELECTION *
     *-----------*/

    /**
     * Selection by search. Note that it calls {@link #select(Location)} not
     * {@link #showLocation(Location)} as selection may be used for navigation not just display
     * @param view The origin of the event. In our case always == mapView
     * @param x The 0-1 value denoting the x on the image of the tap
     * @param y The 0-1 value denoting the y on the image of the tap
     */
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

    /**
     * Shows or sets navigation parameters based on the current state
     * @param l The selected {@link Location}
     */
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

    /**
     * Resets selection parameters. The check is used to avoid unnecessary graphics operations
     */
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

    /**
     * Marks and zooms in on a location on the map
     * @param l The location to be shown
     */
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

        selectedLocation = l;
    }

    /*---------------------------------------------------*
     * UI (Specifically, methods that deal only with UI) *
     *---------------------------------------------------*/

    /**
     * Hides the navigation element of the layout and shows the one for selection
     */
    private void bottomBarShowSelection() {
        ConstraintLayout botBox = findViewById(R.id.bottom_box);
        botBox.setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.selected_details).setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.navigation_panel).setVisibility(View.GONE);

    }

    /**
     * Inverse of {@link #bottomBarShowNavigation()}
     */
    private void bottomBarShowNavigation() {
        ConstraintLayout botBox = findViewById(R.id.bottom_box);
        botBox.setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.navigation_panel).setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.selected_details).setVisibility(View.GONE);
    }

    /**
     * Hides the box that shows selection or nav info
     */
    private void bottomBarHide() {
        findViewById(R.id.bottom_box).setVisibility(View.GONE);
    }

    /**
     * Helper method used to display a message to the user in the form of a SnackBar
     * @param s The string to be displayed
     * @see android.support.design.widget.Snackbar
     */
    private void snackMsg(String s) {
        Snackbar.make(findViewById(R.id.constraint_layout), s, Snackbar.LENGTH_SHORT)
                .show();
    }

    /**
     * Same purpose as {@link #snackMsg(String)} but uses an alert to be more prominent
     * @param s The string to be displayed
     */
    private void alertMsg(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s)
                .setCancelable(false)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Place the destination and swap buttons below the source button if there is not enough space.
     * Not working very well currently
     */
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

    /*----------*
     * GRAPHICS *
     *----------*/

    /**
     * Draws a 40px-diameter dot on the buffer at the given Location
     * @param l The Location giving the point to draw at
     * @param paint The paint to use for the dot
     */
    private void dotLocation(Location l, Paint paint) {
        canvas.drawCircle(l.getX() * fct, l.getY() * fct, 20, paint);
    }

    /**
     * Highlights a location on the map using concentric circles
     * @deprecated since introduction of selection system
     * @param l
     */
    void highlightLocation(Location l) {
        refreshBuffer();
        for (int i : highlightIntervals) canvas.drawCircle(l.x * fct, l.y * fct, i, highlightPaint);
        displayBuffer();
    }

    /**
     * Draws a path between two Points on the buffer. Uses the value of fct to scale the given
     * points to their counterparts on the map
     * @param p1
     * @param p2
     */
    private void drawPathToBuffer(Point p1, Point p2) {
        canvas.drawLine(p1.x * fct, p1.y * fct, p2.x * fct, p2.y * fct, pathPaint);
    }

    /**
     * guess
     * @param text
     * @param loc
     */
    private void drawTextToBuffer(String text, Point loc) {
        Paint p = new Paint();
        p.setColor(Color.BLACK); p.setTextSize(20); p.setAntiAlias(true);
        canvas.drawText(text, (float) (loc.x - (p.getTextSize() * text.length() * 0.4)) * fct,
                (loc.y - (p.getTextSize() / 2)) * fct, p);
    }

    /**
     * what it says on the tin
     * @param l
     */
    private void drawLocToBuffer(Location l) {
        drawTextToBuffer(l.hasName() ? l.getCode() + ", " + l.getName() : l.getCode(), l.getLocation());
    }

    /**
     * Like {@link #drawPathToBuffer(Point, Point)} but lots
     * @param paths The list of paths to draw
     */
    private void drawPathListToBuffer(List<Path> paths) {
        for (Path p : paths) {
            drawPathToBuffer(p.getLocA().getLocation(), p.getLocB().getLocation());
        }
    }

    /**
     * Replaces the map on screen with the buffer from memory
     */
    private void displayBuffer() {
        mapView.setImageBitmap(buf.copy(buf.getConfig(), false));
        mapView.setScale(MAP_MIN_SCALE);

    }

    /**
     * Resets the image buffer to be identical to map. Recalculates fct because… not sure. That
     * could probably be done only once in onCreate tbh but it's not the expensive part of this
     * method.
     */
    private void refreshBuffer() {
        buf = map.copy(map.getConfig(), true);
        canvas = new Canvas(buf);
        fct = (float) map.getWidth() / getResources().getInteger(R.integer.map_width);
    }

    /*---------*
     * HELPERS *
     *---------*/

    /**
     * Used by @link doNavigation() to find the best route
     * @param p A list of paths
     * @return The sum of the weights of all paths in p
     */
    private int weight(List<Path> p) {
        if (p.isEmpty()) return Integer.MAX_VALUE;
        return p.stream().reduce(0, (d,e) -> d + e.length, Integer::sum);
    }

    /**
     * Gets a User object from access requirements
     */
    private User getUserFromParams(Integer access, Boolean disabl) {
        switch (intToId(access)) {
            case R.id.item_ug: return disabl ? User.DISABLED_STUDENT : User.STUDENT;
            case R.id.item_staff: return disabl ? User.DISABLED_STAFF : User.STAFF;
            default: return User.STUDENT;
        }
    }

    /**
     * @param l A location
     * @param x X-value of a point
     * @param y Y-value of a point
     * @return the Euclidean distance (in px) of a Location l from the point x,y
     */
    private double absDist(Location l, float x, float y) {
        return Math.sqrt(Math.pow(l.getX() - x, 2) + Math.pow(l.getY() - y, 2));
    }

    /**
     * @deprecated used by old system
     * @param from The source location
     * @param to The destination location
     */
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

    /**
     * Because we can't store User objects primitively, it makes sense to convert them into types
     * that can denote access rights and ability. This function converts the ID of a MenuItem
     * (specifically, those in the drawer) to an int which we can store using SharedPreferences. We
     * can't just use the int ID of the MenuItem because its value can change between builds.
     * This value corresponds to that of the {@link #access} variable that stores the current access
     * rights.
     * @param id The ID of the MenuItem that has been selected
     * @return The integer value representing the access level corresponding to the MenuItem
     * @see #onCreate(Bundle) for an example of usage
     */
    private int idToInt(int id) {
        switch (id) {
            case R.id.item_ug:    return 1;
            case R.id.item_pg:    return 2;
            case R.id.item_phd:   return 3;
            case R.id.item_staff: return 4;
            default: return 1;
        }
    }

    /**
     * Inverse of {@link #idToInt(int)}
     * @param in The value to convert to the ID
     * @return The ID of the MenuItem corresponding the the access level represented by {@code in}
     */
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
