package gautamhans.xyz.popularmovies.utils;

import android.content.Context;
import android.database.Cursor;

import gautamhans.xyz.popularmovies.data.DatabaseContract;

/**
 * Created by Gautam on 22-Jul-17.
 */

public class RandomUtils {

    public static boolean doesMovieAlreadyExist(Context context, String movie_id) {
        Cursor cursor = context.getContentResolver().query(DatabaseContract.DatabaseEntry.CONTENT_URI,
                null,
                DatabaseContract.DatabaseEntry.MOVIE_ID + " = ?",
                new String[]{movie_id},
                null);

        int numRows = 0;
        try {
            assert cursor != null;
            numRows = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numRows != 0;
    }


}
