package gautamhans.xyz.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

import gautamhans.xyz.popularmovies.adapters.FavoritesCursorAdapter;
import gautamhans.xyz.popularmovies.adapters.MovieAdapter;
import gautamhans.xyz.popularmovies.data.DatabaseContract;
import gautamhans.xyz.popularmovies.models.Result;
import gautamhans.xyz.popularmovies.models.TopRatedMovies;
import gautamhans.xyz.popularmovies.network.TMDbAPI;
import gautamhans.xyz.popularmovies.utils.PaginationAdapterCallback;
import gautamhans.xyz.popularmovies.utils.PaginationScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements PaginationAdapterCallback, MovieAdapter.MovieClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, FavoritesCursorAdapter.FavoriteMovieClickListner {

    // selection columns for query in content provider
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

    private static final String MOVIES_KEY = "movies";
    private static final String RECYCLER_VIEW_STATE = "recycler_view_state";
    private static final int PAGE_START = 1;
    private static final int TASK_LOADER_ID = 640;
    private boolean isFavoritesPressed;
    private ArrayList<Result> mMovies = null;
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
    private TextView mErrorView;


    //click listener for FAB
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_popular_movies:
                    if (isConnected()) {
                        type = "popular";
                        isFavoritesPressed = false;
                        currentPage = 1;
                        loadFirstPage();
                        getSupportActionBar().setTitle("Popular Movies");
                        floatingActionMenu.close(true);
                    } else {
                        isFavoritesPressed = true;
                        showFavorites();
                    }
                    break;

                case R.id.fab_top_rated:
                    if (isConnected()) {
                        type = "top_rated";
                        isFavoritesPressed = false;
                        currentPage = 1;
                        loadFirstPage();
                        getSupportActionBar().setTitle("Top Rated Movies");
                        floatingActionMenu.close(true);
                    } else {
                        isFavoritesPressed = true;
                        showFavorites();
                    }
                    break;

                case R.id.fab_show_favorites:
                    isFavoritesPressed = true;
                    getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, MainActivity.this).forceLoad();
                    floatingActionMenu.close(true);
                    try {
                        getSupportActionBar().setTitle("Favorites");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorView = (TextView) findViewById(R.id.error_favorites);

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        recyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(context, getResources().getInteger(R.integer.gridSize));
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //fab related things
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


        try {
            if (type.contentEquals("popular")) {
                getSupportActionBar().setTitle("Popular Movies");
            } else if (type.contentEquals("top_rated")) {
                getSupportActionBar().setTitle("Top Rated Movies");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MOVIES_KEY)) {
                Parcelable listState = savedInstanceState.getParcelable(RECYCLER_VIEW_STATE);
                mMovies = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
                movieAdapter.addAll(mMovies);
                gridLayoutManager.onRestoreInstanceState(listState);
            } else if (isFavoritesPressed) {
                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this).forceLoad();
            }
        }


        //retrofit client
        retrofit = new Retrofit.Builder()
                .baseUrl(TMDbAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        if (!isFavoritesPressed) {
            // listener method for pagination
            addMoreMovies();
        }

        if (isConnected())
            loadFirstPage();
        else
            showFavorites();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMovies != null) {
            Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(RECYCLER_VIEW_STATE, listState);
            if (!isFavoritesPressed) {
                outState.putParcelableArrayList(MOVIES_KEY, mMovies);
            }
        }
        super.onSaveInstanceState(outState);
    }

    private void addMoreMovies() {
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
    }

    // load initial page for either top rated/popular
    private void loadFirstPage() {

        mErrorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (!isFavoritesPressed) {
            addMoreMovies();
        }

        tmDbAPI = retrofit.create(TMDbAPI.class);
        Call<TopRatedMovies> call = tmDbAPI.getMovies(type, getString(R.string.tmdb), currentPage);
        call.enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {

                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    List<Result> data = response.body().getResults();
                    TOTAL_PAGES = response.body().getTotalPages();
                    Log.d("Response", "Total Movies: " + data.size() + "\nTotal Pages: " + TOTAL_PAGES);
                    movieAdapter = new MovieAdapter(data, context, MainActivity.this);
                    recyclerView.setAdapter(movieAdapter);
                    if (currentPage >= TOTAL_PAGES) isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // load Next Page for movie results
    public void loadNextPage() {
        if (!isFavoritesPressed)
            progressBar2.setVisibility(View.VISIBLE);
        tmDbAPI = retrofit.create(TMDbAPI.class);
        Call<TopRatedMovies> call = tmDbAPI.getMovies(type, getString(R.string.tmdb), currentPage);
        call.enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                if (response.isSuccessful()) {
                    progressBar2.setVisibility(View.GONE);
                    movieAdapter.removeLoadingFooter();
                    isLoading = false;

                    List<Result> data = response.body().getResults();
                    movieAdapter.addAll(data);

                    if (currentPage == TOTAL_PAGES) isLastPage = true;
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


    // Loader for loading Favorites from the ContentProvider
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // this cursor will store the favorite movies
            private Cursor mData;

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

        if (isFavoritesPressed) {
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this).forceLoad();
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() != 0) {
            mErrorView.setVisibility(View.GONE);
            Log.d("Cursor contents: \n", "" + DatabaseUtils.dumpCursorToString(data));
            favoritesCursorAdapter = new FavoritesCursorAdapter(MainActivity.this, MainActivity.this);
            //swapping the cursor for new data
            favoritesCursorAdapter.swapCursor(data);
            recyclerView.setAdapter(favoritesCursorAdapter);
        } else {
            showErrorView();
            favoritesCursorAdapter = null;
        }
    }

    private void showErrorView() {
        recyclerView.setAdapter(null);
        mErrorView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        favoritesCursorAdapter.swapCursor(null);
    }

    @Override
    public void onFavoriteMovieClick(String movie_id, String movie_title) {
        Intent intent = new Intent(this, OfflineMovie.class);
        Bundle extras = new Bundle();
        extras.putString("id", movie_id);
        extras.putString("title", movie_title);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void showFavorites() {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, "Please connect to internet to see either Popular or Top Rated Movies", Toast.LENGTH_LONG);
        mToast.show();
        try {
            getSupportActionBar().setTitle("Favorites");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this).forceLoad();
    }
}
