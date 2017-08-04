package com.ravi.popularmovies2;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ravi.popularmovies2.adapters.MovieAdapter;
import com.ravi.popularmovies2.adapters.ReviewsAdapter;
import com.ravi.popularmovies2.model.Reviews;
import com.ravi.popularmovies2.utils.Constants;
import com.ravi.popularmovies2.utils.ItemDecorationVertical;
import com.ravi.popularmovies2.utils.JsonKeys;
import com.ravi.popularmovies2.utils.NetworkUtils;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Reviews>> {

    RecyclerView reviewsRecycler;
    ProgressBar progressIndicator;

    ArrayList<Reviews> reviewsList;
    ReviewsAdapter adapter;

    private static final int REVIEWS_LOADER_ID = 3;
    private int movieId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reviews);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView title = (TextView) findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.reviews));

        reviewsRecycler = (RecyclerView) findViewById(R.id.rv_reviews_reviewRecycler);
        progressIndicator = (ProgressBar) findViewById(R.id.pb_reviews_progress);

        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.addItemDecoration(new ItemDecorationVertical(getResources().getInteger(R.integer.vertical_item_spacing)));

        reviewsList = new ArrayList<>();
        movieId = getIntent().getIntExtra(JsonKeys.ID_KEY, -1);

        adapter = new ReviewsAdapter(reviewsList);
        reviewsRecycler.setAdapter(adapter);

        if (NetworkUtils.isInternetConnected(this)) {
            getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<ArrayList<Reviews>> onCreateLoader(int id, Bundle args) {
        progressIndicator.setVisibility(View.VISIBLE);
        if (NetworkUtils.isInternetConnected(this)) {
            Uri builtUri = Uri.parse(Constants.BASE_URL)
                    .buildUpon()
                    .appendPath(String.valueOf(movieId))
                    .appendEncodedPath(Constants.REVIEWS_PATH)
                    .appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value))
                    .build();
            return new GetReviewsLoader(this, builtUri.toString());
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Reviews>> loader, ArrayList<Reviews> data) {
        progressIndicator.setVisibility(View.GONE);
        // check for internet connection as I do not want the data to be removed
        // when screen is rotated and the loader is called again
        if (NetworkUtils.isInternetConnected(this)) {
            reviewsList.clear();
            reviewsList.addAll(data);
            adapter.notifyDataSetChanged();
            if (reviewsList == null) {
                Toast.makeText(this, getString(R.string.movie_error_message), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Reviews>> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
