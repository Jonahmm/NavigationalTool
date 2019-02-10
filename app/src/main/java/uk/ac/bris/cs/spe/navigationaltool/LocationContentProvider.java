package uk.ac.bris.cs.spe.navigationaltool;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import uk.ac.bris.cs.spe.navigationaltool.database.DatabaseConstants;

public class LocationContentProvider extends ContentProvider {
    private static final String AUTHORITY = "uk.ac.bris.cs.spe.navigationaltool.LocationContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + DatabaseConstants.TABLE_LOC);


    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseConstants.TABLE_LOC);
        String orderBy = DatabaseConstants.COL_LOC_NAME + " asc";
        Cursor cursor = qb.query(getDbHelper().getReadableDatabase(),
                new String[] { DatabaseConstants.COL_LOC_ID,
                        DatabaseConstants.COL_LOC_NAME }, null,
                null, null, null, orderBy);
        return cursor;
    }

    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.uk.ac.bris.cs.spe.navigationaltool.LocationContentProvider";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    public SQLiteHelper getDbHelper() {
        return ((DisplayDrawer)getContext()).getDbHelper();
    }
}
