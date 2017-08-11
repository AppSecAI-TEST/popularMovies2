package com.ravi.popularmovies2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ravi.popularmovies2.adapters.FavoritesAdapter;
import com.ravi.popularmovies2.adapters.MovieAdapter;
import com.ravi.popularmovies2.database.FavoritesContract;
import com.ravi.popularmovies2.model.Movies;
import com.ravi.popularmovies2.utils.Constants;
import com.ravi.popularmovies2.utils.ItemDecorationGrid;
import com.ravi.popularmovies2.utils.JsonKeys;
import com.ravi.popularmovies2.utils.NetworkUtils;
import com.ravi.popularmovies2.utils.OnItemClickHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnItemClickHandler {

    RecyclerView movieRecycler;
    ProgressBar progressBar;
    String url;
    private static final int MOVIES_LOADER_ID = 1;
    private static final int FAVORITES_LOADER_ID = 2;

    MovieAdapter adapter;
    FavoritesAdapter mAdapter;

    ArrayList<Movies> movieList;
    static boolean isCursorLoaded = false;

    private LoaderManager.LoaderCallbacks<ArrayList<Movies>> moviesLoaderResultCallback;
    private LoaderManager.LoaderCallbacks<Cursor> favoritesLoaderResultCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView title = (TextView) findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.app_name));

        movieRecycler = (RecyclerView) findViewById(R.id.rv_main_movieRecycler);
        progressBar = (ProgressBar) findViewById(R.id.pb_main_progress);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        movieRecycler.setLayoutManager(layoutManager);
        movieRecycler.setHasFixedSize(true);

        movieRecycler.addItemDecoration(new ItemDecorationGrid(getResources().getInteger(R.integer.grid_span),
                getResources().getDimensionPixelSize(R.dimen.grid_spacing), false));

        movieList = new ArrayList<>();
        url = Constants.POPULAR_URL;
        initCallbacks();

        if (!isCursorLoaded) {
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(JsonKeys.MOVIES_INSTANCE_KEY)) {
                    movieList = savedInstanceState.getParcelableArrayList(JsonKeys.MOVIES_INSTANCE_KEY);
                }
            } else { // load data if there is no saved instance state
                if (NetworkUtils.isInternetConnected(this))
                    initNetworkCall(MOVIES_LOADER_ID);
                else
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
            adapter = new MovieAdapter(movieList, this);
            movieRecycler.setAdapter(adapter);
        } else {
            initNetworkCall(FAVORITES_LOADER_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isCursorLoaded)
            initNetworkCall(FAVORITES_LOADER_ID);
    }

    private void initCallbacks() {
        moviesLoaderResultCallback = new LoaderManager.LoaderCallbacks<ArrayList<Movies>>() {
            @Override
            public Loader<ArrayList<Movies>> onCreateLoader(int id, Bundle args) {
                // if the cursor adapter has been set, change the adapter to arrayList adapter
                if (isCursorLoaded) {
                    movieRecycler.setAdapter(adapter);
                    mAdapter.swapCursor(null);
                    isCursorLoaded = false; // change the flag to say that cursor adapter is no more set
                }
                progressBar.setVisibility(View.VISIBLE);
                if (NetworkUtils.isInternetConnected(MainActivity.this)) {
                    Uri builtUri = Uri.parse(url)
                            .buildUpon()
                            .appendQueryParameter(getString(R.string.api_key), Constants.API_KEY)
                            .build();
                    return new GetMoviesLoader(MainActivity.this, builtUri.toString());
                } else {
                    return null;
                }
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Movies>> loader, ArrayList<Movies> data) {
                progressBar.setVisibility(View.GONE);
                // check for internet connection as I do not want the data to be removed
                // when screen is rotated and the loader is called again
                if (NetworkUtils.isInternetConnected(MainActivity.this)) {
                    movieList.clear();
                    movieList.addAll(data);
                    adapter.notifyDataSetChanged();
                    if (movieList == null) {
                        Toast.makeText(MainActivity.this, getString(R.string.movie_error_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Movies>> loader) {

            }
        };

        favoritesLoaderResultCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                progressBar.setVisibility(View.VISIBLE);
                return new GetFavouritesLoader(MainActivity.this, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                progressBar.setVisibility(View.GONE);
                if (data == null || data.getCount() <= 0) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    if (mAdapter == null) { // if adapter for favorites was not created before
                        mAdapter = new FavoritesAdapter(MainActivity.this, MainActivity.this);
                        movieRecycler.setAdapter(mAdapter);
                        mAdapter.swapCursor(data);
                    } else {
                        // if adapter for favorites was created before change the adapter
                        // to the cursorAdapter
                        movieRecycler.setAdapter(mAdapter);
                        mAdapter.swapCursor(data);
                    }
                    isCursorLoaded = true; // set to say that cursor adapter is loaded
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        };
    }

    /**
     * This method is called when we change the filter type, so that the previous data is removed
     * and the recycler view becomes empty before loading the next data
     */
    private void invalidateData() {
        movieList.clear();
        if (adapter != null)
            adapter.notifyDataSetChanged();
        else
            adapter = new MovieAdapter(movieList, MainActivity.this);
    }

    /**
     * This function is called when a grid item is clicked
     */
    @Override
    public void onClick(int position, Cursor cursor) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        if (cursor == null)
            intent.putExtra("detail", movieList.get(position));
        else {
            Movies movieDetail = new Movies();
            cursor.moveToPosition(position);
            movieDetail.setId(cursor.getInt(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_ID)));
            movieDetail.setPosterPath(cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH)));
            movieDetail.setMovieName(cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE)));
            movieDetail.setReleaseDate(cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE)));
            movieDetail.setSynopsis(cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_SYNOPSIS)));
            movieDetail.setVoteAverage(cursor.getFloat(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_RATING)));
            intent.putExtra("detail", movieDetail);
        }
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save instance only if the app is showing popular/top-rated list
        if (!isCursorLoaded)
            outState.putParcelableArrayList(JsonKeys.MOVIES_INSTANCE_KEY, movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_popular:
                url = Constants.POPULAR_URL;
                invalidateData();
                initNetworkCall(MOVIES_LOADER_ID);
                break;

            case R.id.action_topRated:
                url = Constants.TOP_RATED_URL;
                invalidateData();
                initNetworkCall(MOVIES_LOADER_ID);
                break;

            case R.id.action_favourites:
                initNetworkCall(FAVORITES_LOADER_ID);
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNetworkCall(int id) {
        if (NetworkUtils.isInternetConnected(this)) {
            if (getSupportLoaderManager().getLoader(id) == null) {
                if (id == MOVIES_LOADER_ID)
                    getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, moviesLoaderResultCallback);
                else
                    getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, favoritesLoaderResultCallback);
            } else {
                if (id == MOVIES_LOADER_ID)
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderResultCallback);
                else
                    getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, favoritesLoaderResultCallback);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }
}
