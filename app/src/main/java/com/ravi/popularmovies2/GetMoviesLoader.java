package com.ravi.popularmovies2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.ravi.popularmovies2.model.Movies;
import com.ravi.popularmovies2.utils.JsonKeys;
import com.ravi.popularmovies2.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

class GetMoviesLoader extends AsyncTaskLoader<ArrayList<Movies>> {

    private String urlString;

    private ArrayList<Movies> moviesList;

    GetMoviesLoader(Context context, String urlString) {
        super(context);
        this.urlString = urlString;
    }

    @Override
    protected void onStartLoading() {
        if(moviesList != null){
            deliverResult(moviesList);
        }else{
            forceLoad();
        }
    }

    @Override
    public ArrayList<Movies> loadInBackground() {
        ArrayList<Movies> moviesData = new ArrayList<>();
        try {
            String response = NetworkUtils.getJson(urlString);
            JSONArray jsonArray = new JSONObject(response).getJSONArray(JsonKeys.RESULTS_KEY);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject movieObject = jsonArray.getJSONObject(i);
                Movies movieItem = new Movies();
                movieItem.setId(movieObject.getInt(JsonKeys.ID_KEY));
                movieItem.setMovieName(movieObject.getString(JsonKeys.TITLE_KEY));
                movieItem.setReleaseDate(movieObject.getString(JsonKeys.RELEASE_DATE_KEY));
                movieItem.setSynopsis(movieObject.getString(JsonKeys.OVERVIEW_KEY));
                movieItem.setVoteAverage((float) movieObject.getDouble(JsonKeys.AVERAGE_VOTE_KEY));
                movieItem.setPosterPath(movieObject.getString(JsonKeys.POSTER_PATH_KEY));
                movieItem.setFavourite(false);
                moviesData.add(movieItem);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return null;
        } catch (JSONException jex) {
            jex.printStackTrace();
            return null;
        }
        return moviesData;
    }

    @Override
    public void deliverResult(ArrayList<Movies> data) {
        moviesList = data;
        super.deliverResult(data);
    }
}
