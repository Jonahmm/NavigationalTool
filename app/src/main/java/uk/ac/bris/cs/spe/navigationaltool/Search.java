package uk.ac.bris.cs.spe.navigationaltool;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import uk.ac.bris.cs.spe.navigationaltool.database.DatabaseConstants;

public class Search extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
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
        ListView mainListView = (ListView) findViewById(R.id.list_item);
        mainListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchViewOrigin = (SearchView) menu.findItem(R.id.search).getActionView();

        // Assumes current activity is the searchable activity
        searchViewOrigin.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchViewOrigin.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchViewOrigin.requestFocus();

        return true;
    }
}
