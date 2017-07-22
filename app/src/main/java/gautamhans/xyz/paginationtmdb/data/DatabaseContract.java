package gautamhans.xyz.paginationtmdb.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Gautam on 22-Jul-17.
 */

public class DatabaseContract {

    public static final String AUTHORITY = "gautamhans.xyz.paginationtmdb";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final class DatabaseEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";
        public static final String MOVIE_TITLE = "title";
        public static final String MOVIE_ID = "movie_id";
        public static final String MOVIE_TAG_LINE = "tag_line";
        public static final String MOVIE_POSTER = "poster";
        public static final String MOVIE_SYNOPSIS = "synopsis";
        public static final String MOVIE_RELEASE = "release";
        public static final String MOVIE_RATING = "rating";


    }


}
