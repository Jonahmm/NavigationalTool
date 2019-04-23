package uk.ac.bris.cs.spe.navigationaltool;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;

/**
 * Takes a serialized {@link ArrayList} of {@link Location Locations}, and presents the locations
 * as well as some quick-access shortcuts, passing the selected {@link Location} back to the calling
 * activity.
 */
public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    ArrayList<Location> locations = new ArrayList<>();
    List<Location> filtered = new ArrayList<>();
    List<Location> recent;
    LinearLayout extra;
    ListView list;
    SearchView sv;
    String dir;
    public static final int RESULT_SELECT_ON_MAP = 2;
    public static final int REQ_FOR_NAVIGATION = 1;
    public static final int REQ_JUST_SEARCH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        list = findViewById(R.id.search_list);
        extra = findViewById(R.id.search_extra);
        handleIntent(getIntent());
        list.setOnItemClickListener((adapterView, view, i, id)
                -> returnLocation((Location) list.getAdapter().getItem(i)));
        list.setOnItemLongClickListener((adapterView, view, i, l) -> {
            if (extra.getVisibility() == View.VISIBLE) { //i.e. displaying recents

                recent.remove(i);
                list.setAdapter(new LocationListAdapter(this, recent));

                saveRecent();
                return true;
            }
            return false;
        });
        Button map = findViewById(R.id.search_mapselect);
        map.setOnClickListener(e -> returnAction(RESULT_SELECT_ON_MAP));
    }

    private void returnLocation(Location l) {
        recent.remove(l);
        recent.add(0,l);
        saveRecent();
        Intent data = new Intent();
        data.putExtra("RESULT", l);
        setResult(RESULT_OK, data);
        finish();
    }

    private void  returnAction (int result) {
        setResult(result);
        finish();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        sv = searchView;

        return true;
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            locations = (ArrayList<Location>) intent.getSerializableExtra("LOCATIONS");
            //Get locations
            filtered = locations;
            dir = intent.getStringExtra("PATH");
            // Cut off path separator - just concatenate
            dir = dir.substring(0, dir.indexOf('/'));
            recent = loadRecent();
            findViewById(R.id.search_mapselect).setVisibility(
                    intent.getIntExtra("MAPBTNVIS", View.VISIBLE));
            updateSearch("");
            
        } else finish();
    }

    private int dp(int in) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, in, getResources().getDisplayMetrics());
    }

    private List<Location> loadRecent() {
        List<Location> ls = new ArrayList<>();
        try {
            BufferedReader b = new BufferedReader(
                    new InputStreamReader(openFileInput(dir + "recent")));
            List<String> ss = new ArrayList<>();
            String ln;
            while ((ln = b.readLine()) != null) ss.add(ln);
            ss.forEach(s -> locations.stream()
                    .filter(l -> l.getId() == Integer.parseInt(s)).findFirst()
                    .ifPresent(ls::add));
            b.close();
            return ls;
        } catch (IOException e) {
            Log.e("*", "Failed getting recent locations: " + e.getMessage());
            return ls;
        }
    }

    private void saveRecent() {
        try {
            BufferedWriter f = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(dir + "recent", MODE_PRIVATE)));
            for (Location l : recent) {
                f.write(Integer.toString(l.getId()));
                f.newLine();
            }
            f.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not save recent locations");
        }
    }

    void search(String s) {
        filtered = locations.stream().filter(
                l -> l.getCode().contains(s.toUpperCase())
                        || (l.hasName() && l.getName().toUpperCase().contains(s.toUpperCase())))
                .collect(Collectors.toList());
        list.setAdapter(new LocationListAdapter(this, filtered));
    }

    private boolean updateSearch(String s) {
        if (s.isEmpty()) {
            list.setAdapter(new LocationListAdapter(this, recent));
            extra.setVisibility(View.VISIBLE);
            return false;
        } else extra.setVisibility(View.GONE);
        search(s);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return updateSearch(s);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return updateSearch(s);
    }

}
