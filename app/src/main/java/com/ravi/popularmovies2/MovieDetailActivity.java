package com.ravi.popularmovies2;

import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ravi.popularmovies2.adapters.TrailerAdapter;
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

public class MovieDetailActivity extends AppCompatActivity implements View.OnClickListener,
        OnItemClickHandler,
        LoaderManager.LoaderCallbacks<ArrayList<Trailers>> {

    private ImageView poster, favouritesIcon;
    private TextView movieTitle, averageVote, releaseYear, summary;
    private RecyclerView trailersRecycler;

    // Object to hold data of the movie passed via intent
    private Movies movieDetail;

    private ArrayList<Trailers> trailerList;
    private TrailerAdapter adapter;

    private static final int TRAILERS_LOADER_ID = 2;

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

        if (NetworkUtils.isInternetConnected(this)) {
            getSupportLoaderManager().initLoader(TRAILERS_LOADER_ID, null, this);
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private void setData() {
        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(trailerList, this);
        trailersRecycler.addItemDecoration(new ItemDecorationVertical(getResources().getInteger(R.integer.vertical_item_spacing)));
        trailersRecycler.setAdapter(adapter);
        movieDetail = (Movies) getIntent().getSerializableExtra("detail");
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
    public Loader<ArrayList<Trailers>> onCreateLoader(int id, Bundle args) {
        if (NetworkUtils.isInternetConnected(this)) {
            Uri builtUri = Uri.parse(Constants.BASE_URL)
                    .buildUpon()
                    .appendPath(String.valueOf(movieDetail.getId()))
                    .appendEncodedPath(String.valueOf(Constants.TRAILER_PATH))
                    .appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value))
                    .build();
            return new GetTrailerLoader(this, builtUri.toString());
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Trailers>> loader, ArrayList<Trailers> data) {
        Log.v("TRAILER SIZE", "" + data.size());
        // check for internet connection as I do not want the data to be removed
        // when screen is rotated and the loader is called again
        if (NetworkUtils.isInternetConnected(this)) {
            if (data != null) {
                trailerList.clear();
                trailerList.addAll(data);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.trailer_error_message), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Trailers>> loader) {

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
        if (movieDetail.isFavourite()) {
            favouritesIcon.setImageResource(R.drawable.ic_favourite_empty);
        } else {
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
