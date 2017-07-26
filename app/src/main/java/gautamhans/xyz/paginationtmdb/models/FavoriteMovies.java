package gautamhans.xyz.paginationtmdb.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gautam on 26-Jul-17.
 */

public class FavoriteMovies implements Parcelable {
    private String posterPath;
    private String movieTitle;


    public FavoriteMovies(String posterPath, String movieTitle) {
        this.posterPath = posterPath;
        this.movieTitle = movieTitle;
    }

    protected FavoriteMovies(Parcel in) {
        posterPath = in.readString();
        movieTitle = in.readString();
    }

    public static final Creator<FavoriteMovies> CREATOR = new Creator<FavoriteMovies>() {
        @Override
        public FavoriteMovies createFromParcel(Parcel in) {
            return new FavoriteMovies(in);
        }

        @Override
        public FavoriteMovies[] newArray(int size) {
            return new FavoriteMovies[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(posterPath);
        dest.writeString(movieTitle);
    }
}
