package gautamhans.xyz.paginationtmdb.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gautamhans.xyz.paginationtmdb.R;
import gautamhans.xyz.paginationtmdb.pojos.ReviewResult;

/**
 * Created by Gautam on 21-Jul-17.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private Context context;
    private List<ReviewResult> data;

    private ReviewClickListener reviewClickListener;

    public interface ReviewClickListener{
        void onReviewClick(String url);
    }

    public ReviewsAdapter(Context context, List<ReviewResult> data, ReviewClickListener reviewClickListener) {
        this.context = context;
        this.data = data;
        this.reviewClickListener = reviewClickListener;
    }

    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_reviews, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapter.ViewHolder holder, int position) {
        holder.author.setText(data.get(position).getAuthor());
        holder.review.setText(data.get(position).getContent());
        holder.url.setText(data.get(position).getUrl());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView author, review, url;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            author = (TextView) itemView.findViewById(R.id.review_author);
            review = (TextView) itemView.findViewById(R.id.review_text);
            url = (TextView) itemView.findViewById(R.id.review_url_more);
            cardView = (CardView) itemView.findViewById(R.id.cardViewReviews);
            cardView.setOnClickListener(reviewClicked);
            url.setOnClickListener(reviewClicked);
        }

        private View.OnClickListener reviewClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                reviewClickListener.onReviewClick(data.get(position).getUrl());
            }
        };
    }
}
