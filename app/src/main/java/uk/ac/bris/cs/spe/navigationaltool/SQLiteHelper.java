package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.bris.cs.spe.navigationaltool.database.DatabaseConstants;

public class SQLiteHelper extends SQLiteOpenHelper implements DatabaseConstants {

    public static final int DB_VERSION = 2;

    public SQLiteHelper(Context context) {
        super(context,DB_NAME, null, DB_VERSION);
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LOC + "(" + COL_LOC_ID
                + " INTEGER PRIMARY KEY NOT NULL, " + " " + COL_LOC_NAME
                + " VARCHAR(50) NOT NULL);");
        insertLocation(db, "Powell");
        insertLocation(db, "G.12 Mott Lecture Theatre");
    }

    private static void insertLocation(SQLiteDatabase db, String location) {
        db.execSQL("INSERT INTO " + TABLE_LOC + " (" + COL_LOC_NAME
                + ") VALUES ('" + location + "');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
