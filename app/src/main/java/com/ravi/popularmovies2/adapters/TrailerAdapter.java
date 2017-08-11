package com.ravi.popularmovies2.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ravi.popularmovies2.R;
import com.ravi.popularmovies2.model.Trailers;
import com.ravi.popularmovies2.utils.OnItemClickHandler;

import java.util.ArrayList;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private ArrayList<Trailers> trailerList;
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
    public TrailerAdapter(ArrayList<Trailers> list, OnItemClickHandler clickHandler) {
        this.trailerList = list;
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    class TrailerViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        Context context;

        TrailerViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_trailer_item_title);
            context = view.getContext();
        }
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_item, parent, false);

        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Trailers currentItem = trailerList.get(position);
        final int adapterPosition = holder.getAdapterPosition();
        holder.title.setText(currentItem.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickHandler.onClick(adapterPosition, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == trailerList) return 0;
        return trailerList.size();
    }
}
