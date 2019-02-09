package uk.ac.bris.cs.spe.navigationaltool;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import uk.ac.bris.cs.spe.navigationaltool.database.DatabaseConstants;

public class LocationListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 42;
    private CursorAdapter _adapter;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list);
        _adapter = new SimpleCursorAdapter(this,
                R.layout.container_list_item_view, null,
                new String[] { DatabaseConstants.COL_LOC_NAME },
                new int[] { R.id.list_item });
        setListAdapter(_adapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) {
            return null;
        }
        return new CursorLoader(LocationListActivity.this,
                LocationContentProvider.CONTENT_URI,
                new String[] { DatabaseConstants.COL_LOC_ID, DatabaseConstants.COL_LOC_NAME }, null, null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        _adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        _adapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
}
