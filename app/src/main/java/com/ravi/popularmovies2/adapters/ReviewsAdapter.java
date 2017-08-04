package com.ravi.popularmovies2.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ravi.popularmovies2.R;
import com.ravi.popularmovies2.model.Reviews;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {

    private ArrayList<Reviews> reviewsList;

    /**
     * Creates a ReviewsAdapter.
     */
    public ReviewsAdapter(ArrayList<Reviews> list) {
        this.reviewsList = list;
    }

    /**
     * Cache of the children views for reviews list item.
     */
    class ReviewsViewHolder extends RecyclerView.ViewHolder {
        TextView content, author;
        Context context;

        ReviewsViewHolder(View view) {
            super(view);
            content = view.findViewById(R.id.tv_reviews_item_content);
            author = view.findViewById(R.id.tv_reviews_item_author);
            context = view.getContext();
        }
    }

    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_item, parent, false);

        return new ReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsViewHolder holder, int position) {
        Reviews currentItem = reviewsList.get(position);
        holder.content.setText(holder.context.getString(R.string.review_content, currentItem.getContent()));
        holder.author.setText(holder.context.getString(R.string.author_name, currentItem.getAuthor()));
    }

    @Override
    public int getItemCount() {
        if (null == reviewsList) return 0;
        return reviewsList.size();
    }
}