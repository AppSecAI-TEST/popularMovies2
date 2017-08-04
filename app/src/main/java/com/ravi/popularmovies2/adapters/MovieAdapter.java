package com.ravi.popularmovies2.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ravi.popularmovies2.R;
import com.ravi.popularmovies2.model.Movies;
import com.ravi.popularmovies2.utils.OnItemClickHandler;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private ArrayList<Movies> movieList;
    /*
     * An on-click handler that I have defined to make it easy for an Activity to interface with
     * movie list RecyclerView
     */
    private final OnItemClickHandler mClickHandler;

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(ArrayList<Movies> list, OnItemClickHandler clickHandler) {
        this.movieList = list;
        mClickHandler = clickHandler;
    }



    /**
     * Cache of the children views for a movie list item.
     */
    class MovieViewHolder extends RecyclerView.ViewHolder {
        final ImageView moviePoster;
        Context context;

        MovieViewHolder(View view) {
            super(view);
            moviePoster = view.findViewById(R.id.iv_grid_item_poster);
            context = view.getContext();
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position) {
        final int adapterPosition = holder.getAdapterPosition();
        Movies currentItem = movieList.get(position);
        Glide.with(holder.context).load(currentItem.getPosterPath()).into(holder.moviePoster);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickHandler.onClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == movieList) return 0;
        return movieList.size();
    }
}
