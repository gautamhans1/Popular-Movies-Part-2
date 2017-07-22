package gautamhans.xyz.paginationtmdb.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import gautamhans.xyz.paginationtmdb.R;
import gautamhans.xyz.paginationtmdb.models.Result;
import gautamhans.xyz.paginationtmdb.utils.PaginationAdapterCallback;

/**
 * Created by Gautam on 20-Jul-17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    List<Result> data;
    private Context context;
    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;
    private PaginationAdapterCallback mCallback;

    private MovieClickListener movieClickListener;

    public MovieAdapter(List<Result> data, Context context, MovieClickListener movieClickListener) {
        this.data = data;
        this.context = context;
        this.movieClickListener = movieClickListener;
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.rv_movies, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.ViewHolder holder, int position) {
        holder.movieTitleView.setText(data.get(position).getTitle());
        Glide.with(context).load(POSTER_BASE_URL + data.get(position).getPosterPath())
                // TODO Make a new Placeholder Image for Movie Poster
                .placeholder(R.drawable.noposter)
                .error(R.drawable.noposter)
                .into(holder.moviePoster);
        holder.itemView.setTag(data.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Result r) {
        data.add(r);
        notifyItemInserted(data.size() - 1);
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = data.size() - 1;
        Result result = getItem(position);

        if (result != null) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }
    // Helper Methods

    public Result getItem(int position) {
        return data.get(position);
    }

    public void addAll(List<Result> moveResults) {
        for (Result result : moveResults) {
            add(result);
        }
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Result());
    }

    public interface MovieClickListener {
        void onMovieClick(String id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView movieTitleView;
        private ImageView moviePoster;
        private CardView cardView;
        private View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = getAdapterPosition();
                String id = String.valueOf(data.get(clickedPosition).getId());
                Log.d(String.valueOf(this), "Movie ID: " + id);
                movieClickListener.onMovieClick(id);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            movieTitleView = (TextView) itemView.findViewById(R.id.movie_title);
            moviePoster = (ImageView) itemView.findViewById(R.id.moviePoster);
            cardView = (CardView) itemView.findViewById(R.id.movies_cardview);
            cardView.setOnClickListener(clickListener);
            movieTitleView.setOnClickListener(clickListener);
            moviePoster.setOnClickListener(clickListener);
        }
    }
}
