package gautamhans.xyz.paginationtmdb.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

import gautamhans.xyz.paginationtmdb.R;
import gautamhans.xyz.paginationtmdb.pojos.Result;
import gautamhans.xyz.paginationtmdb.utils.PaginationAdapterCallback;
import retrofit2.http.POST;

/**
 * Created by Gautam on 20-Jul-17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    List<Result> data;
    private Context context;
    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;
    private PaginationAdapterCallback mCallback;

    public MovieAdapter(List<Result> data, Context context) {
        this.data = data;
        this.context = context;
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
        .into(holder.moviePoster);
        holder.itemView.setTag(data.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView movieTitleView;
        private ImageView moviePoster;

        public ViewHolder(View itemView) {
            super(itemView);
            movieTitleView = (TextView) itemView.findViewById(R.id.movie_title);
            moviePoster = (ImageView) itemView.findViewById(R.id.moviePoster);
        }
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
}
