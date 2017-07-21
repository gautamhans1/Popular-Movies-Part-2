package gautamhans.xyz.paginationtmdb;

import android.content.Context;
import android.content.Intent;
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

import com.bumptech.glide.Glide;

import java.util.List;

import gautamhans.xyz.paginationtmdb.network.TMDbAPI;
import gautamhans.xyz.paginationtmdb.pojos.MovieDetailsPOJO;
import gautamhans.xyz.paginationtmdb.pojos.Trailers;
import gautamhans.xyz.paginationtmdb.pojos.Youtube;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Gautam on 20-Jul-17.
 */

public class MovieDetails extends AppCompatActivity {

    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w300/";
    private static String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private ActionBar actionBar;
    private String movie_id, movie_title, trailerPath, trailerUrl;
    private ProgressBar progressBar;
    private Retrofit retrofit;
    private TMDbAPI tmDbAPI, trailerAPI;
    private TextView movieTitle, movieTagLine, movieReleasedOn, movieSynopsis, movieRatingText;
    private RatingBar movieRating;
    private ImageView moviePoster, bgImage;
    private boolean isTrailerFound = false;
    private Context context = MovieDetails.this;
    private FrameLayout frameLayout;
    private RelativeLayout relativeLayout;
    private View line1, line2;
    private View.OnClickListener watchTrailer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.watch_trailer_layout) {
                //TODO catch exceptions
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
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
        movieRating = (RatingBar) findViewById(R.id.movie_rating);
        moviePoster = (ImageView) findViewById(R.id.movie_poster);
        movieRatingText = (TextView) findViewById(R.id.movie_rating_text);
        relativeLayout = (RelativeLayout) findViewById(R.id.watch_trailer_layout);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        relativeLayout.setOnClickListener(watchTrailer);

        retrofit = new Retrofit.Builder()
                .baseUrl(TMDbAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        loadMovieDetails();

    }

    private void loadMovieDetails() {
        progressBar.setVisibility(View.VISIBLE);

        tmDbAPI = retrofit.create(TMDbAPI.class);

        tmDbAPI.getMovieDetails(Integer.parseInt(movie_id), getString(R.string.tmdb)).enqueue(new Callback<MovieDetailsPOJO>() {
            @Override
            public void onResponse(Call<MovieDetailsPOJO> call, Response<MovieDetailsPOJO> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
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
                    movieTitle.setText(movieDetailsPOJO.getTitle());
                    movieTagLine.setText(movieDetailsPOJO.getTagline());
                    movieReleasedOn.setText(movieDetailsPOJO.getReleaseDate());
                    movieSynopsis.setText(movieDetailsPOJO.getOverview());
                    movieRating.setRating(Float.parseFloat(String.valueOf(movieDetailsPOJO.getVoteAverage())));
                    movieRatingText.setText(String.valueOf(movieDetailsPOJO.getVoteAverage()));
                    loadTrailer();
                }
            }

            @Override
            public void onFailure(Call<MovieDetailsPOJO> call, Throwable t) {
                t.printStackTrace();
            }
        });

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

}
