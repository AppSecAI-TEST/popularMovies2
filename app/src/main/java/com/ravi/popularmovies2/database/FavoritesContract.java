package com.ravi.popularmovies2.database;

import android.net.Uri;
import android.provider.BaseColumns;

 public class FavoritesContract {

    // The authority, which is how your code knows which Content Provider to access
     static final String AUTHORITY = "com.ravi.popularmovies2";

    // The base content URI = "content://" + <authority>
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

     static final String PATH_FAVORITES = "favorites";

     public static final class FavoritesEntry implements BaseColumns {

         public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

         static final String TABLE_NAME = "favorites";

         public static final String COLUMN_ID = "movieId";
         public static final String COLUMN_TITLE = "title";
         public static final String COLUMN_POSTER_PATH = "poster";
         public static final String COLUMN_SYNOPSIS = "synopsis";
         public static final String COLUMN_RATING = "rating";
         public static final String COLUMN_RELEASE_DATE = "release_date";
    }
}
