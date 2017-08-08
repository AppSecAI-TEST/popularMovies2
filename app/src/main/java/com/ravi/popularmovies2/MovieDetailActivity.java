package com.ravi.popularmovies2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ravi.popularmovies2.adapters.TrailerAdapter;
import com.ravi.popularmovies2.database.FavoritesContract;
import com.ravi.popularmovies2.model.Movies;
import com.ravi.popularmovies2.model.Trailers;
import com.ravi.popularmovies2.utils.Constants;
import com.ravi.popularmovies2.utils.ItemDecorationVertical;
import com.ravi.popularmovies2.utils.JsonKeys;
import com.ravi.popularmovies2.utils.NetworkUtils;
import com.ravi.popularmovies2.utils.OnItemClickHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.COLUMN_ID;
import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH;
import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.COLUMN_RATING;
import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE;
import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.COLUMN_SYNOPSIS;
import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.COLUMN_TITLE;
import static com.ravi.popularmovies2.database.FavoritesContract.FavoritesEntry.CONTENT_URI;

public class MovieDetailActivity extends AppCompatActivity implements View.OnClickListener,
        OnItemClickHandler {

    private ImageView poster, favouritesIcon;
    private TextView movieTitle, averageVote, releaseYear, summary;
    private RecyclerView trailersRecycler;

    // Object to hold data of the movie passed via intent
    private Movies movieDetail;

    private ArrayList<Trailers> trailerList;
    private TrailerAdapter adapter;

    private static final int TRAILERS_LOADER_ID = 2;
    private static final int CHECK_MOVIE_LOADER_ID = 3;

    LoaderManager.LoaderCallbacks<ArrayList<Trailers>> trailerCallback;
    LoaderManager.LoaderCallbacks<Cursor> checkFavoriteCallback;

    boolean isFavourite = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView title = (TextView) findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.app_name));

        poster = (ImageView) findViewById(R.id.iv_detail_poster);
        movieTitle = (TextView) findViewById(R.id.tv_detail_title);
        averageVote = (TextView) findViewById(R.id.tv_detail_averageVote);
        releaseYear = (TextView) findViewById(R.id.tv_detail_releaseYear);
        summary = (TextView) findViewById(R.id.tv_detail_summary);
        favouritesIcon = (ImageView) findViewById(R.id.iv_detail_favourite);
        trailersRecycler = (RecyclerView) findViewById(R.id.rv_detail_trailerRecycler);

        trailersRecycler.setLayoutManager(new LinearLayoutManager(this));
        ViewCompat.setNestedScrollingEnabled(trailersRecycler, false);
        trailersRecycler.setHasFixedSize(true);
        setData();
        initLoaderCallbacks();
        getSupportLoaderManager().initLoader(CHECK_MOVIE_LOADER_ID, null, checkFavoriteCallback);
        if (NetworkUtils.isInternetConnected(this)) {
            getSupportLoaderManager().initLoader(TRAILERS_LOADER_ID, null, trailerCallback);
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private void initLoaderCallbacks(){
        trailerCallback  = new LoaderManager.LoaderCallbacks<ArrayList<Trailers>>() {
            @Override
            public Loader<ArrayList<Trailers>> onCreateLoader(int id, Bundle args) {
                if (NetworkUtils.isInternetConnected(MovieDetailActivity.this)) {
                    Uri builtUri = Uri.parse(Constants.BASE_URL)
                            .buildUpon()
                            .appendPath(String.valueOf(movieDetail.getId()))
                            .appendEncodedPath(String.valueOf(Constants.TRAILER_PATH))
                            .appendQueryParameter(getString(R.string.api_key), Constants.API_KEY)
                            .build();
                    return new GetTrailerLoader(MovieDetailActivity.this, builtUri.toString());
                } else {
                    return null;
                }
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Trailers>> loader, ArrayList<Trailers> data) {
                // check for internet connection as I do not want the data to be removed
                // when screen is rotated and the loader is called again
                if (NetworkUtils.isInternetConnected(MovieDetailActivity.this)) {
                    if (data != null) {
                        trailerList.clear();
                        trailerList.addAll(data);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MovieDetailActivity.this, getString(R.string.trailer_error_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Trailers>> loader) {

            }
        };

        checkFavoriteCallback  = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new GetFavouritesLoader(MovieDetailActivity.this, String.valueOf(movieDetail.getId()));
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if(data.getCount() >  0)
                    favouritesIcon.setImageResource(R.drawable.ic_favorite_filled);
                else
                    favouritesIcon.setImageResource(R.drawable.ic_favorite_empty);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void setData() {
        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(trailerList, this);
        trailersRecycler.addItemDecoration(new ItemDecorationVertical(getResources().getInteger(R.integer.vertical_item_spacing)));
        trailersRecycler.setAdapter(adapter);
        movieDetail = getIntent().getParcelableExtra("detail");
        movieTitle.setText(movieDetail.getMovieName());
        averageVote.setText(getString(R.string.average_vote, String.valueOf(movieDetail.getVoteAverage())));

        releaseYear.setText(getYearOfRelease());
        summary.setText(movieDetail.getSynopsis());
        favouritesIcon.setOnClickListener(this);
        Glide.with(this).load(movieDetail.getPosterPath())
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(poster);
    }

    private String getYearOfRelease() {
        try {
            DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
            Date releaseDate = df.parse(movieDetail.getReleaseDate());
            return df.format(releaseDate);
        } catch (ParseException pEx) {
            pEx.printStackTrace();
        }
        return "";
    }

    @Override
    public void onClick(int position) {
        Uri builtUri = Uri.parse(Constants.YOUTUBE_URL)
                .buildUpon()
                .appendQueryParameter("v", trailerList.get(position).getKey())
                .build();
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
        youtubeIntent.setData(Uri.parse(builtUri.toString()));
        startActivity(youtubeIntent);

    }

    @Override
    public void onClick(View view) {
        favoritesAction();
    }

    private void favoritesAction(){
        if (isFavourite) {
            // Build appropriate uri with String row id appended to delete
            String stringId = Integer.toString(movieDetail.getId());
            Uri uri = FavoritesContract.FavoritesEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringId).build();
            getContentResolver().delete(uri, null, null);
            favouritesIcon.setImageResource(R.drawable.ic_favorite_empty);
            isFavourite = false;
        } else {
//            Adding a new movie to favorites
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_ID, movieDetail.getId());
            contentValues.put(COLUMN_TITLE, movieDetail.getMovieName());
            contentValues.put(COLUMN_RELEASE_DATE, movieDetail.getReleaseDate());
            contentValues.put(COLUMN_RATING, movieDetail.getVoteAverage());
            contentValues.put(COLUMN_SYNOPSIS, movieDetail.getSynopsis());
            contentValues.put(COLUMN_POSTER_PATH, movieDetail.getPosterPath());

            // Insert the content values via a ContentResolver
            getContentResolver().insert(CONTENT_URI, contentValues);
            favouritesIcon.setImageResource(R.drawable.ic_favorite_filled);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reviews:
                startActivity(new Intent(this, ReviewsActivity.class).putExtra(JsonKeys.ID_KEY, movieDetail.getId()));
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
