package com.ravi.popularmovies2.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.ravi.popularmovies2.utils.Constants;

public class Movies implements Parcelable {


    private int id;
    private float voteAverage;
    private String movieName, releaseDate, posterPath, synopsis;

    public Movies(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeFloat(voteAverage);
        parcel.writeString(movieName);
        parcel.writeString(releaseDate);
        parcel.writeString(Constants.IMAGE_BASE_URL + posterPath);
        parcel.writeString(synopsis);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Movies(Parcel in) {
        id = in.readInt();
        voteAverage = in.readFloat();
        movieName = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        synopsis = in.readString();
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Movies> CREATOR = new Parcelable.Creator<Movies>(){

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Movies createFromParcel(Parcel parcel) {
            return new Movies(parcel);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Movies[] newArray(int size) {
            return new Movies[size];
        }
    };

    public int getId() {
        return id;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }
}
