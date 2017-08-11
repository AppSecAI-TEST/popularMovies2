package com.ravi.popularmovies2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ravi.popularmovies2.R;
import com.ravi.popularmovies2.database.FavoritesContract;
import com.ravi.popularmovies2.utils.Constants;
import com.ravi.popularmovies2.utils.OnItemClickHandler;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private OnItemClickHandler callback;

    public FavoritesAdapter(Context mContext, OnItemClickHandler callback) {
        this.mContext = mContext;
        this.callback = callback;
    }

    // Inner class for creating ViewHolders
    class FavoritesViewHolder extends RecyclerView.ViewHolder {

        ImageView posterImage;

        FavoritesViewHolder(View itemView) {
            super(itemView);

            posterImage = itemView.findViewById(R.id.iv_grid_item_poster);
        }
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.grid_item, parent, false);

        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoritesAdapter.FavoritesViewHolder holder, int position) {
        final int adapterPosition = position;
        mCursor.moveToPosition(position);
        Glide.with(mContext).
                load(Constants.IMAGE_BASE_URL + mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH))).
                into(holder.posterImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onClick(adapterPosition, mCursor);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }
}
