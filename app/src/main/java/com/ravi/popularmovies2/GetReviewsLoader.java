package com.ravi.popularmovies2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.ravi.popularmovies2.model.Reviews;
import com.ravi.popularmovies2.utils.JsonKeys;
import com.ravi.popularmovies2.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

class GetReviewsLoader extends AsyncTaskLoader<ArrayList<Reviews>> {

    private String urlString;
    private ArrayList<Reviews> reviewsList;

    GetReviewsLoader(Context context, String urlString) {
        super(context);
        this.urlString = urlString;
    }

    @Override
    protected void onStartLoading() {
        if(reviewsList != null){
            deliverResult(reviewsList);
        }else{
            forceLoad();
        }
    }

    @Override
    public ArrayList<Reviews> loadInBackground() {
        ArrayList<Reviews> reviewsData = new ArrayList<>();
        try {
            String response = NetworkUtils.getJson(urlString);
            JSONArray jsonArray = new JSONObject(response).getJSONArray(JsonKeys.RESULTS_KEY);
            for (int i = 0; i < jsonArray.length(); i++) {
                Reviews reviewsItem = new Reviews();
                JSONObject reviewsObject = jsonArray.getJSONObject(i);
                reviewsItem.setAuthor(reviewsObject.getString(JsonKeys.AUTHOR_KEY));
                reviewsItem.setContent(reviewsObject.getString(JsonKeys.CONTENT_KEY));
                reviewsData.add(reviewsItem);
            }
        }catch (IOException ioEx){
            ioEx.printStackTrace();
            return null;
        }catch(JSONException jex){
            jex.printStackTrace();
            return null;
        }

        return reviewsData;
    }

    @Override
    public void deliverResult(ArrayList<Reviews> data) {
        reviewsList = data;
        super.deliverResult(data);
    }
}
