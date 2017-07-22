package gautamhans.xyz.paginationtmdb.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Gautam on 22-Jul-17.
 */

public class DatabaseContentProvider extends ContentProvider {

    private static final String TAG = DatabaseContentProvider.class.getSimpleName();
    private static final int MOVIES = 100;
    private static final int MOVIES_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(DatabaseContract.AUTHORITY, DatabaseContract.PATH_MOVIES, MOVIES);
//        uriMatcher.addURI(DatabaseContract.AUTHORITY, DatabaseContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        databaseHelper = new DatabaseHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (match){
            case MOVIES:
                retCursor = db.query(DatabaseContract.DatabaseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match){
            case MOVIES:
                long id = db.insert(DatabaseContract.DatabaseEntry.TABLE_NAME, null, values);
                if(id>0){
                    returnUri = ContentUris.withAppendedId(DatabaseContract.DatabaseEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int isDeleted = 0;
        switch (match){
            case MOVIES:
                isDeleted = db.delete(DatabaseContract.DatabaseEntry.TABLE_NAME, selection, selectionArgs);
                break;
        }
        if(isDeleted!=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return isDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
