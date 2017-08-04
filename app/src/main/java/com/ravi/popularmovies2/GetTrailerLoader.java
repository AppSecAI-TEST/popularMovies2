package com.ravi.popularmovies2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.ravi.popularmovies2.model.Trailers;
import com.ravi.popularmovies2.utils.JsonKeys;
import com.ravi.popularmovies2.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class GetTrailerLoader extends AsyncTaskLoader<ArrayList<Trailers>> {

    private String urlString;

    private ArrayList<Trailers> trailerList;

    GetTrailerLoader(Context context, String urlString) {
        super(context);
        this.urlString = urlString;
    }

    @Override
    protected void onStartLoading() {
        if (trailerList != null) {
            deliverResult(trailerList);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<Trailers> loadInBackground() {
        ArrayList<Trailers> trailersData = new ArrayList<>();
        try {
            String response = NetworkUtils.getJson(urlString);
            JSONArray jsonArray = new JSONObject(response).getJSONArray(JsonKeys.RESULTS_KEY);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject trailerObject = jsonArray.getJSONObject(i);
                Trailers trailerItem = new Trailers();
                trailerItem.setId(trailerObject.getString(JsonKeys.ID_KEY));
                trailerItem.setKey(trailerObject.getString(JsonKeys.KEY));
                trailerItem.setName(trailerObject.getString(JsonKeys.NAME_KEY));
                trailersData.add(trailerItem);
            }
        }catch (IOException ioex){
            ioex.printStackTrace();
            return null;
        }catch (JSONException jex){
            jex.printStackTrace();
            return null;
        }
        return trailersData;
    }

    @Override
    public void deliverResult(ArrayList<Trailers> data) {
        trailerList = data;
        super.deliverResult(data);
    }
}