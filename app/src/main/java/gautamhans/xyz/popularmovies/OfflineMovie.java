package gautamhans.xyz.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.like.LikeButton;
import com.like.OnLikeListener;

import java.io.ByteArrayOutputStream;

import gautamhans.xyz.popularmovies.data.DatabaseContract;
import gautamhans.xyz.popularmovies.network.TMDbAPI;
import gautamhans.xyz.popularmovies.utils.RandomUtils;
import retrofit2.Retrofit;

public class OfflineMovie extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER_ID = 11;
    private ActionBar actionBar;
    private LikeButton likeButton;
    private String movie_id, movie_title, trailerPath, trailerUrl, posterPath, movieTitleText, movieTagLineText, movieSynopsisText, movieReleaseText;
    private float movieRatingFloat;
    private ProgressBar progressBar;
    private Bitmap moviePosterBitmap;
    private Retrofit retrofit;
    private Toast mToast;
    private TMDbAPI tmDbAPI, trailerAPI, reviewsAPI;
    private TextView movieTitle, movieTagLine,
            movieReleasedOn, movieReleasedOnText, movieRatingText, movieSynopsis, reviewTvDetails;
    private RatingBar movieRating;
    private ImageView moviePoster, bgImage;
    private boolean isTrailerFound = false;
    private Context context = OfflineMovie.this;
    private FrameLayout frameLayout;
    private RelativeLayout relativeLayout, reviewLayout;
    private View line1, line2, line3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_movie);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            movie_id = extras.getString("id");
            movie_title = extras.getString("title");
        }

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        movieTitle = (TextView) findViewById(R.id.movie_title);
        movieTagLine = (TextView) findViewById(R.id.movie_tag_line);
        movieReleasedOn = (TextView) findViewById(R.id.release_date);
        movieSynopsis = (TextView) findViewById(R.id.movie_synopsis);
        movieSynopsis = (TextView) findViewById(R.id.movie_synopsis_tv);
        movieRating = (RatingBar) findViewById(R.id.movie_rating);
        moviePoster = (ImageView) findViewById(R.id.movie_poster);
        movieRatingText = (TextView) findViewById(R.id.movie_rating_text);
        relativeLayout = (RelativeLayout) findViewById(R.id.watch_trailer_layout);
        movieReleasedOnText = (TextView) findViewById(R.id.release_date_tv);
        reviewLayout = (RelativeLayout) findViewById(R.id.read_reviews_layout);
        reviewTvDetails = (TextView) findViewById(R.id.review_tv_details);
        likeButton = (LikeButton) findViewById(R.id.fav_button);

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                boolean isFavorite = RandomUtils.doesMovieAlreadyExist(context, movie_id);
                if (isFavorite) {
                    Log.d("Movie State: ", String.valueOf(isFavorite));
                    likeButton.setLiked(true);
                } else {
                    insertData();
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                int rowDeleted = getContentResolver().delete(DatabaseContract.DatabaseEntry.CONTENT_URI,
                        DatabaseContract.DatabaseEntry.MOVIE_ID + " = ?",
                        new String[]{movie_id});

                if (rowDeleted != 0) {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(context, "Removed from favorites! :(", Toast.LENGTH_LONG);
                    mToast.show();
                }
            }
        });

        likeButton.setLiked(true);
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);

    }



    private void insertData() {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DatabaseEntry.MOVIE_TITLE, movieTitleText);
        values.put(DatabaseContract.DatabaseEntry.MOVIE_TAG_LINE, movieTagLineText);

        //converting bitmap to png byte array for sql
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        moviePosterBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        values.put(DatabaseContract.DatabaseEntry.MOVIE_POSTER, byteArray);
        values.put(DatabaseContract.DatabaseEntry.MOVIE_SYNOPSIS, movieSynopsisText);
        values.put(DatabaseContract.DatabaseEntry.MOVIE_RELEASE, movieReleaseText);
        values.put(DatabaseContract.DatabaseEntry.MOVIE_RATING, movieRatingFloat);
        values.put(DatabaseContract.DatabaseEntry.MOVIE_ID, movie_id);


        Uri uri = getContentResolver().insert(DatabaseContract.DatabaseEntry.CONTENT_URI,
                values);

        if (uri != null) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG);
            mToast.show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor cursor;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                progressBar.setVisibility(View.VISIBLE);
                if (cursor != null) {
                    deliverResult(cursor);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                cursor = context.getContentResolver().query(
                        DatabaseContract.DatabaseEntry.CONTENT_URI,
                        null,
                        DatabaseContract.DatabaseEntry.MOVIE_ID + " = ?",
                        new String[]{movie_id},
                        null
                );
                return cursor;
            }

            @Override
            public void deliverResult(Cursor data) {
                cursor = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            progressBar.setVisibility(View.GONE);
            // indexes of columns in db
            int moviePosterIndex = data.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_POSTER);
            int movieTagLineIndex = data.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_TAG_LINE);
            int movieRatingIndex = data.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_RATING);
            int movieReleasedIndex = data.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_RELEASE);
            int movieSynopsisIndex = data.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_SYNOPSIS);

            data.moveToFirst();

            movieTitle.setText(movie_title);
            movieTagLine.setText(data.getString(movieTagLineIndex));
            movieRating.setRating(data.getFloat(movieRatingIndex));
            movieReleasedOn.setText(data.getString(movieReleasedIndex));
            movieSynopsis.setText(data.getString(movieSynopsisIndex));

            byte[] byteArray = data.getBlob(moviePosterIndex);
            moviePoster.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
