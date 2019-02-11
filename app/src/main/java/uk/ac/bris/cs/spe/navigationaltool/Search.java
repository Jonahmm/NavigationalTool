package uk.ac.bris.cs.spe.navigationaltool;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import uk.ac.bris.cs.spe.navigationaltool.database.DatabaseConstants;

public class Search extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list_item_view);
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    public void doMySearch(String query){
        SQLiteHelper sqLiteHelper = ((DisplayDrawer) getApplicationContext()).getDbHelper();
        Cursor cursor = sqLiteHelper.getReadableDatabase().rawQuery("SELECT " + DatabaseConstants.COL_LOC_ID + ", " +
                DatabaseConstants.COL_LOC_NAME + " FROM " + DatabaseConstants.TABLE_LOC +
                " WHERE upper(" + DatabaseConstants.COL_LOC_NAME + ") like '%" + query.toUpperCase() + "%'", null);
        ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.container_list_item_view, cursor,
                new String[] {DatabaseConstants.COL_LOC_NAME }, new int[]{R.id.list_item});
        ListView mainListView = findViewById(R.id.list_item);
        mainListView.setAdapter(adapter);
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

        searchView.requestFocus();

        return true;
    }
}
