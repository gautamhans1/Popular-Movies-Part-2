package gautamhans.xyz.paginationtmdb.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gautam on 22-Jul-17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moviesDb.db";

    private static final int VERSION = 7;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + DatabaseContract.DatabaseEntry.TABLE_NAME + " (" +
                DatabaseContract.DatabaseEntry._ID + " INTEGER PRIMARY KEY, " +
                DatabaseContract.DatabaseEntry.MOVIE_TITLE + " TEXT NOT NULL, " +
                DatabaseContract.DatabaseEntry.MOVIE_ID + " INTEGER NOT NULL, " +
                DatabaseContract.DatabaseEntry.MOVIE_TAG_LINE + " TEXT, " +
                DatabaseContract.DatabaseEntry.MOVIE_SYNOPSIS + " TEXT UNIQUE, " +
                DatabaseContract.DatabaseEntry.MOVIE_RATING + " REAL, " +
                DatabaseContract.DatabaseEntry.MOVIE_RELEASE + " TEXT NOT NULL, " +
                DatabaseContract.DatabaseEntry.MOVIE_POSTER + " BLOB);";

        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.DatabaseEntry.TABLE_NAME);
        onCreate(db);
    }
}
