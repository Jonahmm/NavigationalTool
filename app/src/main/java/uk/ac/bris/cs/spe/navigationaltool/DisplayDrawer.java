package uk.ac.bris.cs.spe.navigationaltool;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.navigator.BreadthFirstNavigator;

import android.graphics.*;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.util.ArrayList;

public class DisplayDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Building building;
    private int access;
    private Boolean disabl;
    Menu options;
    PhotoView mapView;
    Bitmap map;
    Bitmap buf; Canvas canvas; float fct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        access = getPreferences(MODE_PRIVATE).getInt(getString(R.string.saved_access), R.id.item_ug);
        disabl = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.saved_disabl), false);


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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Set menu items to be checked depending on saved values
        navigationView.getMenu().findItem(access).setChecked(true);
        navigationView.getMenu().findItem(R.id.disabled_switch).setChecked(disabl);

        navigationView.setNavigationItemSelectedListener(this);

        map = BitmapFactory.decodeResource(getResources(), R.drawable.mapg);
        refreshBuffer();

        mapView = (PhotoView) findViewById(R.id.mapviewer);
        mapView.setImageBitmap(map);
        mapView.setMaximumScale(12);

        try {
            building = new Building("ground", new BreadthFirstNavigator(),
                getApplicationContext());
            Snackbar.make(findViewById(R.id.constraint_layout), "Imported " +
                    building.getGraph().getAllLocations().size()
                    + " locations.",
                    Snackbar.LENGTH_LONG)
                    .show();
        } catch (IOException e) {
            Snackbar.make(findViewById(R.id.constraint_layout), "Error importing building", Snackbar.LENGTH_SHORT)
                    .show();
        } catch (IllegalArgumentException e) {
            Snackbar.make(findViewById(R.id.constraint_layout), e.getMessage(), Snackbar.LENGTH_SHORT)
                    .show();
        }


    }

    //Quick and dirty menu -> User implementation
    private User getUserFromParams(Integer access, Boolean disabl) {
        switch (access) {
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
        options = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
        int id = item.getItemId();

        if (id == R.id.disabled_switch) {
            e.putBoolean(getString(R.string.saved_disabl), item.isChecked());
            disabl = item.isChecked();
        } else {
            e.putInt(getString(R.string.saved_access), id);
            access = id;
        }
        e.apply();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void drawPathToBuffer(Point p1, Point p2) {
        //Bitmap tmp = Bitmap.createBitmap(map.getWidth(), map.getHeight(), map.getConfig());
        Paint p = new Paint();
        //TODO: Should be set globally
        p.setColor(Color.RED);
        p.setStrokeWidth(3);
        //canvas.drawBitmap(map, 0, 0, null);

        canvas.drawLine(p1.x * fct, p1.y * fct, p2.x * fct, p2.y * fct, p);
        //mapView.setImageBitmap(tmp);
    }

    private void drawTextToBuffer(String text, Point loc) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        canvas.drawText(text, (float) (loc.x - (p.getTextSize() * text.length() * 0.4)) * fct, (loc.y - (p.getTextSize() / 2)) * fct, p);
    }

    private void displayBuffer() {
        mapView.setImageBitmap(buf);
    }

    private void refreshBuffer() {
        buf = Bitmap.createBitmap(map.getWidth(), map.getHeight(), map.getConfig());
        canvas = new Canvas(buf);
        canvas.drawBitmap(map,0,0,null);
        Log.d("Map width ", Integer.toString(map.getWidth()));
        fct = (float) map.getWidth() / getResources().getInteger(R.integer.map_width);
        Log.d("Got factor ", Float.toString(fct));
    }

}
