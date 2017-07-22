package gautamhans.xyz.paginationtmdb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gautamhans.xyz.paginationtmdb.R;
import gautamhans.xyz.paginationtmdb.data.DatabaseContract;

/**
 * Created by Gautam on 22-Jul-17.
 */

public class FavoritesCursorAdapter extends RecyclerView.Adapter<FavoritesCursorAdapter.ViewHolder> {

    private Context context;
    private Cursor mCursor;
    private FavoriteMovieClickListener favoriteMovieClickListener;

    public FavoritesCursorAdapter(Context context, FavoriteMovieClickListener favoriteMovieClickListener) {
        this.context = context;
        this.favoriteMovieClickListener = favoriteMovieClickListener;
    }

    @Override
    public FavoritesCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_movies, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FavoritesCursorAdapter.ViewHolder holder, int position) {
//        int idIndex = mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_ID);
//        int movieTagLineIndex = mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_TAG_LINE);
//        int movieRatingIndex = mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_RATING);
//        int movieReleaseIndex;
        int movieTitleIndex = mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_TITLE);
        int moviePosterIndex = mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.MOVIE_TITLE);

        mCursor.moveToPosition(position);

        String movieTitle = mCursor.getString(movieTitleIndex);
        System.out.println("Title: " + movieTitle);
        byte[] byteArray = mCursor.getBlob(moviePosterIndex);

        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Drawable moviePoster = new BitmapDrawable(context.getResources(), bitmap);


        holder.moviePoster.setImageDrawable(moviePoster);
        holder.movieTitle.setText(movieTitle);


    }

    @Override
    public int getItemCount() {
        if(mCursor == null ){
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }

        if(c!=null)
        c.moveToFirst();

        this.mCursor = c;

        if (c != null) {
            this.notifyDataSetChanged();
        }

        return c;
    }

    public interface FavoriteMovieClickListener {
        void onMovieClick(String movie_id);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView movieTitle;
        private ImageView moviePoster;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            movieTitle = (TextView) itemView.findViewById(R.id.movie_title);
            moviePoster = (ImageView) itemView.findViewById(R.id.moviePoster);
            cardView = (CardView) itemView.findViewById(R.id.movies_cardview);
        }
    }
}
