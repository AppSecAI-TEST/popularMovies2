package com.ravi.popularmovies2;

import android.content.Intent;
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

import com.ravi.popularmovies2.adapters.MovieAdapter;
import com.ravi.popularmovies2.model.Movies;
import com.ravi.popularmovies2.utils.Constants;
import com.ravi.popularmovies2.utils.ItemDecorationGrid;
import com.ravi.popularmovies2.utils.NetworkUtils;
import com.ravi.popularmovies2.utils.OnItemClickHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnItemClickHandler, LoaderManager.LoaderCallbacks<ArrayList<Movies>> {

    RecyclerView movieRecycler;
    ProgressBar progressBar;
    String url;
    private static final int MOVIES_LOADER_ID = 1;
    MovieAdapter adapter;
    ArrayList<Movies> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        if (savedInstanceState != null && savedInstanceState.containsKey("movies")) {
            movieList = (ArrayList<Movies>) savedInstanceState.getSerializable("movies");
        }
        adapter = new MovieAdapter(movieList, this);
        movieRecycler.setAdapter(adapter);

        url = Constants.POPULAR_URL;
        if (NetworkUtils.isInternetConnected(this)) {
            getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is called when we change the filter type, so that the previous data is removed
     * and the recycler view becomes empty before loading the next data
     */
    private void invalidateData() {
        movieList.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public Loader<ArrayList<Movies>> onCreateLoader(int id, Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        if (NetworkUtils.isInternetConnected(this)) {
            Uri builtUri = Uri.parse(url)
                    .buildUpon()
                    .appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value))
                    .build();
            return new GetMoviesLoader(this, builtUri.toString());
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movies>> loader, ArrayList<Movies> data) {
        progressBar.setVisibility(View.GONE);
        // check for internet connection as I do not want the data to be removed
        // when screen is rotated and the loader is called again
        if(NetworkUtils.isInternetConnected(this)){
            movieList.clear();
            movieList.addAll(data);
            adapter.notifyDataSetChanged();
            if (movieList == null) {
                Toast.makeText(this, getString(R.string.movie_error_message), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(int position) {
        startActivity(new Intent(MainActivity.this, MovieDetailActivity.class).putExtra("detail", movieList.get(position)));
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movies>> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("movies", movieList);
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
        invalidateData();
        switch (id) {
            case R.id.action_popular:
                url = Constants.POPULAR_URL;
                initNetworkCall();
                break;

            case R.id.action_topRated:
                url = Constants.TOP_RATED_URL;
                initNetworkCall();
                break;

            case R.id.action_favourites:
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNetworkCall(){
        if (NetworkUtils.isInternetConnected(this)) {
            getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }
}
