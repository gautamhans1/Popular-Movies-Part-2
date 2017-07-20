package gautamhans.xyz.paginationtmdb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import gautamhans.xyz.paginationtmdb.network.TMDbAPI;
import gautamhans.xyz.paginationtmdb.pojos.MovieDetailsPOJO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Gautam on 20-Jul-17.
 */

public class MovieDetails extends AppCompatActivity {

    private ActionBar actionBar;
    private String movie_id, movie_title;
    private ProgressBar progressBar;
    private Retrofit retrofit;
    private TMDbAPI tmDbAPI;
    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w300/";
    private TextView movieTitle, movieTagLine, movieReleasedOn, movieSynopsis, movieRatingText;
    private RatingBar movieRating;
    private ImageView moviePoster;
    private Context context = MovieDetails.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        actionBar = getSupportActionBar();

        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if(intent!=null){
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

        retrofit = new Retrofit.Builder()
                .baseUrl(TMDbAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();



        loadMovieDetails();

    }

    private void loadMovieDetails(){
        progressBar.setVisibility(View.VISIBLE);

        tmDbAPI = retrofit.create(TMDbAPI.class);

        tmDbAPI.getMovieDetails(Integer.parseInt(movie_id), getString(R.string.tmdb)).enqueue(new Callback<MovieDetailsPOJO>() {
            @Override
            public void onResponse(Call<MovieDetailsPOJO> call, Response<MovieDetailsPOJO> response) {
                if(response.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    MovieDetailsPOJO movieDetailsPOJO = response.body();
                    if(actionBar!=null){
                        actionBar.setTitle(movieDetailsPOJO.getTitle());
                    }
                    Glide.with(context).load(POSTER_BASE_URL + movieDetailsPOJO.getPosterPath()).placeholder(R.drawable.noposter)
                            .error(R.drawable.noposter).into(moviePoster);
                    movieTitle.setText(movieDetailsPOJO.getTitle());
                    movieTagLine.setText(movieDetailsPOJO.getTagline());
                    movieReleasedOn.setText(movieDetailsPOJO.getReleaseDate());
                    movieSynopsis.setText(movieDetailsPOJO.getOverview());
                    movieRating.setRating(Float.parseFloat(String.valueOf(movieDetailsPOJO.getVoteAverage())));

                }
            }

            @Override
            public void onFailure(Call<MovieDetailsPOJO> call, Throwable t) {
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
