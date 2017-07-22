package gautamhans.xyz.paginationtmdb;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import gautamhans.xyz.paginationtmdb.adapters.FavoritesCursorAdapter;
import gautamhans.xyz.paginationtmdb.adapters.MovieAdapter;
import gautamhans.xyz.paginationtmdb.data.DatabaseContract;
import gautamhans.xyz.paginationtmdb.models.Result;
import gautamhans.xyz.paginationtmdb.models.TopRatedMovies;
import gautamhans.xyz.paginationtmdb.network.TMDbAPI;
import gautamhans.xyz.paginationtmdb.utils.PaginationAdapterCallback;
import gautamhans.xyz.paginationtmdb.utils.PaginationScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements PaginationAdapterCallback, MovieAdapter.MovieClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, FavoritesCursorAdapter.FavoriteMovieClickListener {

    public static final String[] MOVIE_COLUMNS = {
            DatabaseContract.DatabaseEntry._ID,
            DatabaseContract.DatabaseEntry.MOVIE_TITLE,
            DatabaseContract.DatabaseEntry.MOVIE_ID,
            DatabaseContract.DatabaseEntry.MOVIE_TAG_LINE,
            DatabaseContract.DatabaseEntry.MOVIE_SYNOPSIS,
            DatabaseContract.DatabaseEntry.MOVIE_RATING,
            DatabaseContract.DatabaseEntry.MOVIE_RELEASE,
            DatabaseContract.DatabaseEntry.MOVIE_POSTER
    };


    private static final int PAGE_START = 1;
    private static final int TASK_LOADER_ID = 640;
    private RecyclerView recyclerView;
    private TMDbAPI tmDbAPI;
    private ProgressBar progressBar, progressBar2;
    private Context context = MainActivity.this;
    private MovieAdapter movieAdapter;
    private FavoritesCursorAdapter favoritesCursorAdapter;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES = 10;
    private int currentPage = PAGE_START;
    private Retrofit retrofit;
    private GridLayoutManager gridLayoutManager;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton fab_popular, fab_top_rated, fab_favorite_movies;
    private String type = "popular";
    private Toast mToast;
    private PullRefreshLayout pullRefreshLayout;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_popular_movies:
                    type = "popular";
                    currentPage = 1;
                    loadFirstPage();
                    getSupportActionBar().setTitle("Popular Movies");
                    floatingActionMenu.close(true);
                    break;

                case R.id.fab_top_rated:
                    type = "top_rated";
                    currentPage = 1;
                    loadFirstPage();
                    getSupportActionBar().setTitle("Top Rated Movies");
                    floatingActionMenu.close(true);
                    break;

                case R.id.fab_show_favorites:
                    getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, MainActivity.this);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(context, getResources().getInteger(R.integer.gridSize));
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_menu);
        fab_popular = (FloatingActionButton) findViewById(R.id.fab_popular_movies);
        fab_top_rated = (FloatingActionButton) findViewById(R.id.fab_top_rated);
        fab_favorite_movies = (FloatingActionButton) findViewById(R.id.fab_show_favorites);
        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_menu);
        floatingActionMenu.setMenuButtonColorNormal(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        floatingActionMenu.setMenuButtonColorPressed(ContextCompat.getColor(context, R.color.colorPrimaryDarker));
        fab_popular.setOnClickListener(clickListener);
        fab_top_rated.setOnClickListener(clickListener);
        fab_favorite_movies.setOnClickListener(clickListener);

        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                loadFirstPage();
            }


        });

        try {
            if (type.contentEquals("popular")) {
                getSupportActionBar().setTitle("Popular Movies");
            } else if (type.contentEquals("top_rated")) {
                getSupportActionBar().setTitle("Top Rated Movies");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        retrofit = new Retrofit.Builder()
                .baseUrl(TMDbAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        recyclerView.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        loadFirstPage();

    }

    private void loadFirstPage() {

        progressBar.setVisibility(View.VISIBLE);

        tmDbAPI = retrofit.create(TMDbAPI.class);
        Call<TopRatedMovies> call = tmDbAPI.getMovies(type, getString(R.string.tmdb), currentPage);
        call.enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {

                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    pullRefreshLayout.setRefreshing(false);
                    List<Result> data = response.body().getResults();
                    TOTAL_PAGES = response.body().getTotalPages();
                    Log.d("Response", "Total Movies: " + data.size() + "\nTotal Pages: " + TOTAL_PAGES);
                    movieAdapter = new MovieAdapter(data, context, MainActivity.this);
                    recyclerView.setAdapter(movieAdapter);
                    if (currentPage <= TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void loadNextPage() {
        progressBar2.setVisibility(View.VISIBLE);
        tmDbAPI = retrofit.create(TMDbAPI.class);
        Call<TopRatedMovies> call = tmDbAPI.getMovies(type, getString(R.string.tmdb), currentPage);
        call.enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                if (response.isSuccessful()) {
                    progressBar2.setVisibility(View.INVISIBLE);
                    movieAdapter.removeLoadingFooter();
                    isLoading = false;

                    List<Result> data = response.body().getResults();
                    movieAdapter.addAll(data);

                    if (currentPage != TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {

            }
        });
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    @Override
    public void onMovieClick(String id) {
        Intent intent = new Intent(context, MovieDetails.class);
        Bundle extras = new Bundle();
        extras.putString("id", id);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            private Cursor mData;

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(DatabaseContract.DatabaseEntry.CONTENT_URI,
                            MOVIE_COLUMNS,
                            null,
                            null,
                            null);
                } catch (Exception e) {
                    Log.e("Loader Result: ", "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

        };
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        movieAdapter = null;
        Log.d("Cursor contents: \n", "" + DatabaseUtils.dumpCursorToString(data));
        favoritesCursorAdapter = new FavoritesCursorAdapter(MainActivity.this, MainActivity.this);
        favoritesCursorAdapter.swapCursor(data);
        recyclerView.setAdapter(favoritesCursorAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        favoritesCursorAdapter.swapCursor(null);
    }


}
