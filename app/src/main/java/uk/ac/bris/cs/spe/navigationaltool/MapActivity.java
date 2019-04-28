package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.DijkstraNavigator;
import uk.ac.bris.cs.spe.navigationaltool.navigator.OptimisedNavigator;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnPhotoTapListener{
    /**
     * The distance (in px) that locations should be < to a screen tap in order to be selected
     */
    private static final int NEAR_DISTANCE = 300;

    /**
     * The map on screen. Kept global to avoid a lot of {@link #findViewById(int)} calls
     */
    MapView mapView;

    /**
     * The building object to be worked on. Loaded using {@link BuildingLoader()}
     */
    private Building building;

    /**
     * True if user requires accessible route
     */
    private Boolean disabl;

    /**
     * True if user has staff access rights
     */
    private Boolean staff;

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
    private enum Selecting {NONE, SELECTION, NAVSRC, NAVDST}

    /**
     * Defines the current state of the program
     */
    private Selecting selecting = Selecting.SELECTION;

    /**
     * Used to create directions and remember the current route.
     */
    private Route route = null;

    /*----------------*
     * INITIALISATION *
     *----------------*/


    /**
     * Initialise and populate the activity. Further details are in code comments
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int bindex = getPreferences(MODE_PRIVATE).getInt(getString(R.string.saved_building), 0);

        staff  = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.saved_staff), false);
        disabl = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.saved_disabl), false);

        //Makes the drawer behave
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        //Set menu items to be checked depending on saved values
        navigationView.getMenu().findItem(intToId(bindex)).setChecked(true);

        Switch s = navigationView.getMenu().findItem(R.id.disabled_item)
                .getActionView().findViewById(R.id.sswitch);
        s.setChecked(disabl);

        s = navigationView.getMenu().findItem(R.id.staff_switch)
                .getActionView().findViewById(R.id.sswitch);
        s.setChecked(staff);

        //Allows updating access requirements from menu
        navigationView.setNavigationItemSelectedListener(this);
        
        new BuildingLoader().execute(loadFromIndex(bindex));
//        setListeners();

        //Called here to a) correctly set scale and b) update floor indicator

        //Set up the voronoi diagram
        View navHeader = navigationView.getHeaderView(0);
        ImageView voronoiDiagram = navHeader.findViewById(R.id.voronoi);
        VoronoiDrawable voronoiDrawable = new VoronoiDrawable();
        voronoiDiagram.setImageDrawable(voronoiDrawable);
    }

    /**
     * Loads the {@link Building} from the given path, setting up the UI once done.
     */
    private class BuildingLoader extends AsyncTask<String, Integer, Building> {

        @Override
        protected Building doInBackground(String... strings) {

            try {
                Building b = new Building(strings[0], new OptimisedNavigator(), getApplicationContext());
                loadMaps(b);
                return b;
            } catch (IOException e) {
                alertMsg("Error loading building!");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            ProgressBar p = findViewById(R.id.loading_wait);
            p.setVisibility(View.VISIBLE);
            p.setClickable(true);

            selectedLocation = navigationDst = navigationSrc = null;
            bottomBarHide();
            visUIElements(View.GONE);
        }

        @Override
        protected void onPostExecute(Building b) {
            ProgressBar p = findViewById(R.id.loading_wait);
            p.setVisibility(View.GONE);
            p.setClickable(false);
            p.setIndeterminate(false);
            postLoadMaps(b);
            populateFloorsList(b);
            building = b;
            setListeners();
            // Sets floor indicator text
            mapView.setFloor(b.getDefaultFloor(), MapView.RESET_NONE);
            visUIElements(View.VISIBLE);
            setTitle(building.getName());
        }
    }

    /**
     * Called asynchronously in {@link BuildingLoader#doInBackground(String...)}, this loads and
     * sets all the map images.
     */
    private void loadMaps(Building b) {
        mapView = findViewById(R.id.mapviewer);

        Map<String, Bitmap> maps = new ArrayMap<>();
        try {
            for (String s : b.getFloorMap().keySet())
                maps.put(s, BitmapFactory.decodeStream(getAssets()
                        .open(b.getDirectory() + "map" + s + ".png")));
        } catch (IOException e) {
            throw new RuntimeException("Map could not be loaded!");
        }
        mapView.setMaps(maps);
        //Initialise image


    }

    /**
     * Called synchronously in {@link BuildingLoader#onPostExecute(Building)}, sets the UI
     * properties of {@link #mapView}
     */
    private void postLoadMaps(Building b) {
        mapView.setFloor(b.getDefaultFloor(), MapView.RESET_NONE);
        mapView.updateFCT();
        mapView.setMaximumScale(12f);

        mapView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mapView.setMinimumScale(mapView.getScale());
    }

    /**
     * Initialises the options menu
     * @param menu The menu to inflate
     * @return idk, see {@link android.support.v7.app.AppCompatActivity#onCreateOptionsMenu(Menu)}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_drawer, menu);
        // Get the SearchView and set the searchable configuration
        MenuItem srch = menu.findItem(R.id.app_bar_search);
        srch.setOnMenuItemClickListener(e -> {
            startSearch(SearchActivity.REQ_JUST_SEARCH);
            return true;
        });

        return true;
    }

    /**
     * Where the magic happens. This one sets all the listeners (classes (but basically just methods)
     * that respond to events)
     */
    void setListeners() {
        FloatingActionButton fab = findViewById(R.id.floor_select);
        fab.setOnClickListener(view -> {
            LinearLayout fl = findViewById(R.id.floors_box);
            fl.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        });
        FloatingActionButton nav = findViewById(R.id.navigation_show);
        nav.setOnClickListener(e -> {
            resetNavButtons();
            bottomBarShowNavigation();
        });


        //Sets up selection by tap
        mapView.setOnPhotoTapListener(this);
        mapView.setOnFloorChangeListener((m,f) -> {
            TextView fn = findViewById(R.id.floor_name);
            fn.setText(building.getFloorMap().get(f));
        });

        //Sets up disabled switch
        NavigationView navigationView = findViewById(R.id.nav_view);
        Switch s = navigationView.getMenu().findItem(R.id.disabled_item).getActionView()
                .findViewById(R.id.sswitch);
        s.setOnCheckedChangeListener((compoundButton, b) -> {
            disabl = b;
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            e.putBoolean(getString(R.string.saved_disabl), disabl);
            e.apply();
        });
        s = navigationView.getMenu().findItem(R.id.staff_switch).getActionView()
                .findViewById(R.id.sswitch);
        s.setOnCheckedChangeListener((cb, b) -> {
            staff = b;
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            e.putBoolean(getString(R.string.saved_staff), staff);
            e.apply();
        });

        //Button to start nav
        findViewById(R.id.selected_get_directions).setOnClickListener(v -> startNavigationTo(selectedLocation));

        //Navigation buttons
        Button navs = findViewById(R.id.navigation_src_btn);
        navs.setOnClickListener(v -> startNavSelect(navs));
        Button navd = findViewById(R.id.navigation_dst_btn);
        navd.setOnClickListener(v -> startNavSelect(navd));


        findViewById(R.id.navigation_swap_btn).setOnClickListener(v -> {
            Location src = navigationSrc;
            Location dst = navigationDst;
        //These are set to null to avoid the first setNavigationSrc call trying to calculate a route
            navigationSrc = null;
            navigationDst = null;
            setNavigationSrc(dst);
            setNavigationDst(src);
        });

        CheckBox c = findViewById(R.id.navigation_show_dir);
        c.setOnClickListener(v -> {
            boolean b = c.isChecked();
            findViewById(  R.id.navigation_editor  ).setVisibility(!b ? View.VISIBLE : View.GONE);
            findViewById(R.id.navigation_directions).setVisibility( b ? View.VISIBLE : View.GONE);
            String rtfrom = getString(R.string.route_from);
            ((TextView) findViewById(R.id.navigation_title)).setText(
                    b ? rtfrom + " " + navigationSrc.getCode() + " to " + navigationDst.getCode()
                      : rtfrom
            );
        });

        ImageButton ib = findViewById(R.id.nav_dir_next);
        View.OnClickListener l = (v -> {
            if (route == null) return;
            if (route.next()) mapView.drawRoute(route);
            ((TextView) findViewById(R.id.nav_dir_text)).setText(route.getCurrentInstruction());
        });
        ib.setOnClickListener(l);
        findViewById(R.id.nav_dir_text).setOnClickListener(l);

        ib = findViewById(R.id.nav_dir_prev);
        ib.setOnClickListener(v -> {
            if (route == null) return;
            if (route.prev()) mapView.drawRoute(route);
            ((TextView) findViewById(R.id.nav_dir_text)).setText(route.getCurrentInstruction());
        });

        //Button to display image of room
        Button vr_image = findViewById(R.id.selected_360);
        vr_image.setOnClickListener(e -> openVrPanoramaView());
    }

    /**
     * Initialises all the floor-selection buttons
     */
    private void populateFloorsList(Building building) {
        LinearLayout fl = findViewById(R.id.floors_box);
        fl.removeAllViews();
        FloatingActionButton b;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,8,0,8);
        for (String f : building.getFloorMap().keySet()) {
            b = new FloatingActionButton(this);
            b.setLayoutParams(lp);
            b.setImageDrawable(new FabTextDrawable(f.toUpperCase(), Color.WHITE));
            b.setOnClickListener(v -> {
                mapView.setFloor(f, MapView.RESET_NONE);
                fl.setVisibility(View.GONE);
                findViewById(R.id.floor_select).setVisibility(View.VISIBLE);
            });
            b.setSize(FloatingActionButton.SIZE_MINI);
            fl.addView(b);
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
            LinearLayout fb = findViewById(R.id.floors_box);
            if (fb.getVisibility()==View.VISIBLE) {
                fb.setVisibility(View.GONE);
                findViewById(R.id.floor_select).setVisibility(View.VISIBLE);
            }
//            else if (findViewById(R.id.bottom_box).getVisibility() == View.VISIBLE)
//                bottomBarHide();
            else if (navigationDst != null || navigationSrc != null) exitNavigation();
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles the changing of access requirements (staff/stu/etc) Note that using the disabled
     * switch is not handled here as its use doesn't count as 'Selecting' a menu item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id != R.id.disabled_item && id != R.id.staff_switch) {
            SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
            int index = idToInt(id);
            e.putInt(getString(R.string.saved_building), index);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            e.apply();
            new BuildingLoader().execute(loadFromIndex(index));
        }

        return true;
    }

    private void openVrPanoramaView() {
        Intent intent = new Intent(this, VrView.class);
        intent.putExtra("CODE", selectedLocation.getCode());
        intent.putExtra("PATH", building.getDirectory());
        startActivity(intent);
    }

    /**
     * An {@link AsyncTask} that sets up, runs and presents navigation.
     */
    public void startSearch(int req) {
        Intent intent = new Intent(Intent.ACTION_SEARCH, null, this, SearchActivity.class);
        intent.putExtra("LOCATIONS", building.getPrincipalLocations());
        intent.putExtra("MAPBTNVIS", req == SearchActivity.REQ_FOR_NAVIGATION
                ? View.VISIBLE : View.GONE);
        intent.putExtra("PATH", building.getDirectory());
        startActivityForResult(intent, req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK) {
            select((Location) data.getSerializableExtra("RESULT"));
        }
        else if (requestCode == SearchActivity.REQ_FOR_NAVIGATION && resultCode == RESULT_CANCELED) {
            cancelNavSelect(findViewById(selecting == Selecting.NAVDST
                    ? R.id.navigation_dst_btn : R.id.navigation_src_btn));
        }
        else if (resultCode != SearchActivity.RESULT_SELECT_ON_MAP) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        // Nothing actually needs doing - remain in nav selection mode
    }

    /*------------*
     * NAVIGATION *
     *------------*/
    /**
     * Launches search, and changes the app state st when you select a location, it is used for navigation.
     * Sets the text, icon and listener of the button it came from
     * @param btn The Button object that this came from, one of navigation_src_btn or
     *            navigation_dst_btn
     */
    private void startNavSelect(Button btn) {
        cancelNavSelect(findViewById(btn.getId() == R.id.navigation_src_btn ? R.id.navigation_dst_btn
                : R.id.navigation_src_btn));
        selecting = btn.getId() == R.id.navigation_src_btn ? Selecting.NAVSRC : Selecting.NAVDST;
        startSearch(SearchActivity.REQ_FOR_NAVIGATION);
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
        resetNavView();
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

        btn.setText(l != null ? (l.hasName() ? l.getName() : l.getCode()) + " " : getString(R.string.click_to_edit));
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_edit,0);
        btn.setOnClickListener(e -> startNavSelect(btn));

    }

    /**
     * Brings up the navigation interface with the given location as the destination
     * @param l The location to nav to. Currently this is always == selectedLocation
     */
    private void startNavigationTo(Location l) {
        navigationSrc = null;
        resetNavButtons();
        bottomBarShowNavigation();
        setNavigationDst(l);
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
        selecting = Selecting.SELECTION;
        showDirIfPossible();
        doNavigation();
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
        selecting = Selecting.SELECTION;
        showDirIfPossible();
        doNavigation();
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
     * Instantiates a new {@link NavigateTask}, which…
     * Uses the {@link uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator} to compute the path
     * between all pairs of nodes x,y, where the codes of x and y match those of navigationSrc and
     * navigationDst respectively, then selects the path with the lowest total weight using
     * {@link #weight(List)}. It draws this route to the buffer using
     * {@link MapView#drawRoute(Route)}.
     */
    private void doNavigation() {
        if (navigationDst == null || navigationSrc == null) return;
        new NavigateTask().execute(building);
    }

    private class NavigateTask extends AsyncTask<Building, Integer, List<Path>> {

        User user;
        Location from, to;

        ProgressBar p;
        /**
         * Prepare for navigation; set up progress bar and hide views that can interfere with the
         * process. Instantiates {@code user, from, to} in this class to provide some measure of
         * thread-safety
         */
        @Override
        protected void onPreExecute() {
            selecting = Selecting.NONE;
            user = getUserFromParams(staff, disabl);
            from = navigationSrc;
            to = navigationDst;
            p = findViewById(R.id.calculating_wait);
            p.setProgress(0, false);
            p.setVisibility(View.VISIBLE);
            bottomBarHide();
            findViewById (R.id.floor_select)  .setVisibility(View.GONE);
            findViewById(R.id.navigation_show).setVisibility(View.GONE);
        }
        /**
         * Use the building's navigator to calculate routes between all potential origin-destination
         * pairs, selecting the shortest (if present) as the route.
         * @param buildings only ever {{@link #building}}
         * @return The list representing the route found (or empty)
         */
        @Override
        protected List<Path> doInBackground(Building... buildings) {
            Building b = buildings[0];
            List<Path> paths = new ArrayList<>();
            Set<Location> froms = b.getGraph().getLocationsByCode(from.getCode());
            Set<Location>  tos  = b.getGraph().getLocationsByCode( to .getCode());
            float mult = 100f / (froms.size() * tos.size());
            int count = 0;
            for (Location l : froms) {
                if (b.getGraph().getPathsFromLocation(l).size() != 0) {
                    for (Location m : tos) {
                        if (b.getGraph().getPathsFromLocation(m).size() != 0) {
                            try {
                                List<Path> p = b.getNavigator().navigate(l, m, b.getGraph(), user);
                                if (weight(p) < weight(paths)) {
                                    paths = p;
                                    from = l;
                                    to = m;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        ++count;
                        publishProgress((int) (count * mult));
                    }
                }
                else {
                    count += tos.size();
                    publishProgress((int) (count * mult));
                }
            }
            return paths;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            p.setProgress(values[0], true);
        }

        /**
         * Takes the result from {@link #doInBackground(Building...)} and creates a {@link Route}
         * object to generate directions. Hides the progress bar and the translucent filter, and
         * brings up any hidden UI elements again. Displays a message and removes the directions
         * option if no route was found.
         * @param paths A (potentially empty) list representing the route found (if any)
         */
        @Override
        protected void onPostExecute(List<Path> paths) {
            selecting = Selecting.SELECTION;
            p.setVisibility(View.GONE);
            bottomBarShowNavigation();
            if(paths != null && !paths.isEmpty()) {
//                mapView.drawRoute(from, to, paths);
                route = new Route(from, to, paths, building.getFloorMap());
                mapView.drawRoute(route);
                findViewById(R.id.navigation_show_dir).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.nav_dir_text)).setText(route.getCurrentInstruction());
            } else {
                alertMsg(getString(R.string.navigation_failure));
                findViewById(R.id.navigation_show_dir).setVisibility(View.GONE);
            }
            findViewById (R.id.floor_select).setVisibility(View.VISIBLE);

        }

    }

    /*-----------*
     * SELECTION *
     *-----------*/

    /**
     * Selection by search. Note that it calls {@link #select(Location)} not
     * {@link #showLocation(Location)} as selection may be used for navigation not just display
     * If the floor selector is shown, it hides it instead of search.
     * @param view The origin of the event. In our case always == {@link #mapView}
     * @param x The 0-1 value denoting the x on the image of the tap
     * @param y The 0-1 value denoting the y on the image of the tap
     */
    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        LinearLayout fb = findViewById(R.id.floors_box);
        if(fb.getVisibility()==View.VISIBLE) {
            fb.setVisibility(View.GONE);
            findViewById(R.id.floor_select).setVisibility(View.VISIBLE);
            return;
        }
        final float xx = x * getResources().getInteger(R.integer.map_width);
        final float yy = y * getResources().getInteger(R.integer.map_height);

        Map<Location, Double> memo = new ArrayMap<>();
        building.getGraph().getAllLocations().stream().filter(l -> l.floor.equals(mapView.getCurrentFloor()))
                .forEach(l -> memo.put(l,absDist(l,xx,yy)));
        Optional<Location> ol = memo.keySet().parallelStream().filter(l -> memo.get(l) < NEAR_DISTANCE)
                .reduce((a,b) -> memo.get(a) < memo.get(b) ? a : b);
        if (ol.isPresent()) {
            select(ol.get());
            return;
        }
        if (selecting == Selecting.SELECTION) deselect();
        else snackMsg("Nothing here");
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
        bottomBarHide();
        resetNavView();
        if (selectedLocation != null || navigationSrc != null || navigationDst != null) {
            selectedLocation = null;
            navigationSrc = null;
            navigationDst = null;
            selecting = Selecting.SELECTION;
      //      mapView.refreshBuffer(mapView.currentFloor);
      //      mapView.showFloorBuffer(mapView.currentFloor);
            mapView.setFloor(mapView.getCurrentFloor(), MapView.RESET_ALL);
        }

    }

    /**
     * Marks and zooms in on a location on the map, and sets it to be the selected location
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
        try {
            findViewById(R.id.selected_360).setVisibility(
                    Arrays.asList(Objects.requireNonNull(getAssets().list(building.getDirectory() + "images")))
                    .contains(l.getCode() + ".jpg")
                        ? View.VISIBLE : View.GONE);
        } catch (IOException e) {
            findViewById(R.id.selected_360).setVisibility(View.GONE);
        }
        mapView.drawLocation(l);

        Matrix m = new Matrix();
        mapView.getDisplayMatrix(m);
        float[] pts = {mapView.translate(l.getX()), mapView.translate(l.getY())};
        m.mapPoints(pts);
        mapView.setScale(4, pts[0], pts[1], true);

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
        findViewById(R.id.navigation_show).setVisibility(View.GONE);
    }
    /**
     * Inverse of {@link #bottomBarShowSelection()}
     */
    private void bottomBarShowNavigation() {
        resetNavView();
        ConstraintLayout botBox = findViewById(R.id.bottom_box);
        botBox.setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.navigation_panel).setVisibility(View.VISIBLE);
        botBox.findViewById(R.id.selected_details).setVisibility(View.GONE);

        ((CheckBox) findViewById(R.id.navigation_show_dir)).setChecked(false);
        findViewById(R.id.navigation_editor).setVisibility(View.VISIBLE);
        findViewById(R.id.navigation_directions).setVisibility(View.GONE);

        findViewById(R.id.navigation_show).setVisibility(View.GONE);
    }

    /**
     * Hides the box that shows selection or nav info
     */
    private void bottomBarHide() {
        findViewById(R.id.bottom_box).setVisibility(View.GONE);
        findViewById(R.id.navigation_show).setVisibility(View.VISIBLE);
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

    private void resetNavView() {
        findViewById(R.id.navigation_show_dir).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.navigation_title)).setText(R.string.route_from);
    }

    private void showDirIfPossible() {
        findViewById(R.id.navigation_show_dir).setVisibility(
                navigationSrc != null && navigationDst != null
                ? View.VISIBLE : View.GONE
        );
    }

    private void visUIElements(int vis) {
        findViewById(R.id.navigation_show).setVisibility(vis);
        findViewById(R.id.floor_select).setVisibility(vis);
//        findViewById(R.id.app_bar_search).setVisibility(vis);
    }

    /*---------*
     * HELPERS *
     *---------*/

    private int intToId(int in) {
        switch (in) {
            case 0: return R.id.building_physics;
            case 1: return R.id.building_maths;

            default: return R.id.building_physics;
        }
    }

    private int idToInt(int id) {
        switch (id) {
            case R.id.building_physics: return 0;
            case R.id.building_maths  : return 1;

            default: return 0;
        }
    }

    private String loadFromIndex(int in) {
        switch (in) {
            case  0: return "physics/physics";
            case  1: return "maths/maths";

            default: return "physics/physics";
        }
    }

    /**
     * Used by {@link #doNavigation()} to find the best route
     * @param p A list of paths
     * @return The sum of the weights of all paths in p
     */
    private int weight(List<Path> p) {
        if (p.isEmpty()) return Integer.MAX_VALUE;
        return p.stream().reduce(0, (d,e) -> d + e.getLength(), Integer::sum);
    }

    private User getUserFromParams(boolean staff, boolean disabled) {
        return staff
                ? (disabled ? User.DISABLED_STAFF : User.STAFF)
                : (disabled ? User.DISABLED_STUDENT : User.STUDENT);
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

}
