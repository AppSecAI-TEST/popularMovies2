package com.ravi.popularmovies2.utils;

import android.database.Cursor;

public interface OnItemClickHandler {
    /**
     * The interface that receives onClick messages.
     */
    void onClick(int position, Cursor cursorData);
}
