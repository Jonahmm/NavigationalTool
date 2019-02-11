package uk.ac.bris.cs.spe.navigationaltool;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    ArrayList<Location> locations = new ArrayList<>();
    ListView list;
    SearchView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        list = findViewById(R.id.search_list);
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        sv = searchView;
        //searchView.requestFocus();

        return true;
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ArrayList<Location> ls = (ArrayList<Location>) intent.getSerializableExtra("LOCATIONS");
            //Get distinct locations
            for(Location l : ls) {
                if(locations.stream().noneMatch(m -> l.getCode().equals(m.getCode())))
                    locations.add(l);
            }
            search(query);
        }
    }


    void search(String s) {
        List<Location> filtered = locations.stream().filter(
                l -> l.getCode().contains(s.toUpperCase())
                        || (l.hasName() && l.getName().toUpperCase().contains(s.toUpperCase())))
                .distinct().collect(Collectors.toList());
        list.setAdapter(new LocationListAdapter(this, filtered));
    }

    private boolean updateSearch(String s) {
        if(s.isEmpty()) {
            list.setAdapter(new LocationListAdapter(this, locations));
            return false;
        }
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
