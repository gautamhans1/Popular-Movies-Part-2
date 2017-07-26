package gautamhans.xyz.paginationtmdb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.io.ByteArrayOutputStream;
import java.util.List;

import gautamhans.xyz.paginationtmdb.data.DatabaseContract;
import gautamhans.xyz.paginationtmdb.network.TMDbAPI;
import gautamhans.xyz.paginationtmdb.models.MovieDetailsPOJO;
import gautamhans.xyz.paginationtmdb.models.Trailers;
import gautamhans.xyz.paginationtmdb.models.Youtube;
import gautamhans.xyz.paginationtmdb.utils.RandomUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Gautam on 20-Jul-17.
 */

public class MovieDetails extends AppCompatActivity {

    private static final int TASK_LOADER_ID = 0;
    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w300/";
    private static String POSTER_ORIGINAL_BASE_URL = "http://image.tmdb.org/t/p/original/";
    private static String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
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
    private Context context = MovieDetails.this;
    private FrameLayout frameLayout;
    private RelativeLayout relativeLayout, reviewLayout;
    private View line1, line2, line3;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.watch_trailer_layout:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.movie_poster:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(POSTER_ORIGINAL_BASE_URL + posterPath)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.read_reviews_layout:
                    Intent intent = new Intent(context, gautamhans.xyz.paginationtmdb.Reviews.class);
                    Bundle extras = new Bundle();
                    extras.putString("movie_id", movie_id);
                    extras.putString("movie_title", movieTitleText);
                    intent.putExtras(extras);
                    startActivity(intent);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            movie_id = extras.getString("id");
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
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        relativeLayout.setOnClickListener(clickListener);
        moviePoster.setOnClickListener(clickListener);
        reviewLayout.setOnClickListener(clickListener);
        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                boolean isFavorite = RandomUtils.doesMovieAlreadyExist(context, movie_id);
                if(isFavorite){
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

                if(rowDeleted!=0) {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(context, "Removed from favorites! :(", Toast.LENGTH_LONG);
                    mToast.show();
                }
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(TMDbAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        loadMovieDetails();
        loadTrailer();
        isFavorite();
    }

    private void loadMovieDetails() {
        progressBar.setVisibility(View.VISIBLE);
        movieRating.setVisibility(View.GONE);
        movieSynopsis.setVisibility(View.GONE);
        movieReleasedOnText.setVisibility(View.GONE);
        reviewTvDetails.setVisibility(View.GONE);
        likeButton.setVisibility(View.GONE);

        tmDbAPI = retrofit.create(TMDbAPI.class);

        tmDbAPI.getMovieDetails(Integer.parseInt(movie_id), getString(R.string.tmdb)).enqueue(new Callback<MovieDetailsPOJO>() {
            @Override
            public void onResponse(Call<MovieDetailsPOJO> call, Response<MovieDetailsPOJO> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    movieRating.setVisibility(View.VISIBLE);
                    movieSynopsis.setVisibility(View.VISIBLE);
                    movieReleasedOnText.setVisibility(View.VISIBLE);
                    reviewTvDetails.setVisibility(View.VISIBLE);
                    likeButton.setVisibility(View.VISIBLE);
                    MovieDetailsPOJO movieDetailsPOJO = response.body();
                    if (actionBar != null) {
                        actionBar.setTitle(movieDetailsPOJO.getTitle());
                    }

                    /*Blur Transformation*/
//                    //TODO bring a placeholder here
//                    Glide.with(context).load(POSTER_BASE_URL + movieDetailsPOJO.getPosterPath()).asBitmap().transform(new BlurTransformation(context, 100)).into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                            Drawable drawable = new BitmapDrawable(resource);
//
//                            frameLayout.setBackground(drawable);
//                        }
//                    });

                    Glide.with(context).load(POSTER_BASE_URL + movieDetailsPOJO.getPosterPath()).placeholder(R.drawable.noposter)
                            .error(R.drawable.noposter).into(moviePoster);
                    Glide.with(context).load(POSTER_BASE_URL + movieDetailsPOJO.getPosterPath())
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    moviePosterBitmap = resource;
                                }
                            });

                    posterPath = movieDetailsPOJO.getPosterPath();
                    movieTitle.setText(movieDetailsPOJO.getTitle());
                    movieTitleText = movieDetailsPOJO.getTitle();
                    movieTagLine.setText(movieDetailsPOJO.getTagline());
                    movieTagLineText = movieDetailsPOJO.getTagline();
                    movieReleasedOn.setText(movieDetailsPOJO.getReleaseDate());
                    movieReleaseText = movieDetailsPOJO.getReleaseDate();
                    movieSynopsis.setText(movieDetailsPOJO.getOverview());
                    movieSynopsisText = movieDetailsPOJO.getOverview();
                    movieRating.setRating(Float.parseFloat(String.valueOf(movieDetailsPOJO.getVoteAverage())));
                    movieRatingFloat = Float.valueOf(String.valueOf(movieDetailsPOJO.getVoteAverage()));
                    movieRatingText.setText(String.valueOf(movieDetailsPOJO.getVoteAverage()));

                   isFavorite();
                }
            }



            @Override
            public void onFailure(Call<MovieDetailsPOJO> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void isFavorite(){
        if(RandomUtils.doesMovieAlreadyExist(context, movie_id)){
            likeButton.setLiked(true);
            Log.d("doesMovieExist:", "True");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isFavorite();
    }

    private void loadTrailer() {
        trailerAPI = retrofit.create(TMDbAPI.class);
        trailerAPI.getTrailers(Integer.parseInt(movie_id), getString(R.string.tmdb)).enqueue(new Callback<Trailers>() {
            @Override
            public void onResponse(Call<Trailers> call, Response<Trailers> response) {
                if (response.isSuccessful()) {
                    List<Youtube> youtube = response.body().getYoutube();
                    Log.d("Youtube Size", ": " + youtube.size());
                    Log.d("Movie ID", ": " + movie_id);
                    for (int i = 0; i < youtube.size(); i++) {
                        System.out.println(youtube.get(i).getType());
                        if (youtube.get(i).getType().contentEquals("Trailer")) {
                            trailerPath = youtube.get(i).getSource();
                            Log.d("trailerPath: ", trailerPath);
                            isTrailerFound = true;
                        }
                        if (isTrailerFound) break;
                    }

                    if (isTrailerFound) {
                        line1.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.VISIBLE);
                        line2.setVisibility(View.VISIBLE);
                        line3.setVisibility(View.VISIBLE);
                        trailerUrl = YOUTUBE_BASE_URL + trailerPath;
                        Log.d("Trailer Link: ", trailerUrl);
                    }
                }
            }

            @Override
            public void onFailure(Call<Trailers> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private void insertData(){
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

        if(uri != null) {
            if(mToast!=null){
                mToast.cancel();
            }
            mToast = Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG);
            mToast.show();
        }
    }

}
