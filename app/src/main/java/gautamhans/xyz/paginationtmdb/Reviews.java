package gautamhans.xyz.paginationtmdb;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import gautamhans.xyz.paginationtmdb.adapters.ReviewsAdapter;
import gautamhans.xyz.paginationtmdb.network.TMDbAPI;
import gautamhans.xyz.paginationtmdb.models.ReviewResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Gautam on 21-Jul-17.
 */

public class Reviews extends AppCompatActivity implements ReviewsAdapter.ReviewClickListener {

    private String movie_id, movie_title;
    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private Retrofit retrofit;
    private TMDbAPI reviewsAPI;
    private Context context = Reviews.this;
    private ProgressBar progressBar4;
    private TextView noReviewsView;
    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            movie_id = bundle.getString("movie_id", "");
            movie_title = bundle.getString("movie_title", "");
        }

        progressBar4 = (ProgressBar) findViewById(R.id.progressBar4);
        noReviewsView = (TextView) findViewById(R.id.no_reviews_tv);
        actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("Reviews: " + movie_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        retrofit = new Retrofit.Builder()
                .baseUrl(TMDbAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        loadReviews();
    }

    //TODO cleanup
    private void loadReviews() {
        progressBar4.setVisibility(View.VISIBLE);
        reviewsAPI = retrofit.create(TMDbAPI.class);
        reviewsAPI.getReviews(Integer.parseInt(movie_id), getString(R.string.tmdb)).enqueue(new Callback<gautamhans.xyz.paginationtmdb.models.Reviews>() {
            @Override
            public void onResponse(Call<gautamhans.xyz.paginationtmdb.models.Reviews> call, Response<gautamhans.xyz.paginationtmdb.models.Reviews> response) {
                if (response.isSuccessful()) {
                    progressBar4.setVisibility(View.GONE);
                    List<ReviewResult> data = response.body().getReviewResults();
                    if (data.size() != 0) {
                        adapter = new ReviewsAdapter(context, data, Reviews.this);
                        recyclerView.setAdapter(adapter);
                    } else {
                        showNoReviews();
                    }
                }
            }

            @Override
            public void onFailure(Call<gautamhans.xyz.paginationtmdb.models.Reviews> call, Throwable t) {
                t.printStackTrace();
                showNoReviews();
            }
        });
    }

    private void showNoReviews() {
        recyclerView.setVisibility(View.GONE);
        noReviewsView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReviewClick(String url) {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
