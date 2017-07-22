package gautamhans.xyz.paginationtmdb.network;

import gautamhans.xyz.paginationtmdb.models.MovieDetailsPOJO;
import gautamhans.xyz.paginationtmdb.models.Reviews;
import gautamhans.xyz.paginationtmdb.models.TopRatedMovies;
import gautamhans.xyz.paginationtmdb.models.Trailers;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Gautam on 20-Jul-17.
 */

public interface TMDbAPI {
    public static final String BASE_URL = "http://api.themoviedb.org/3/";

    @GET("movie/{type}")
    Call<TopRatedMovies> getMovies(@Path("type") String type, @Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/popular")
    Call<TopRatedMovies> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/{id}")
    Call<MovieDetailsPOJO> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/trailers")
    Call<Trailers> getTrailers(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<Reviews> getReviews(@Path("id") int id, @Query("api_key") String apiKey);
}
