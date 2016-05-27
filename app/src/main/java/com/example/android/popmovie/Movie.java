package com.example.android.popmovie;

public class Movie {
    public final String mPosterPath;
    public final String mOverview;
    public final String mReleaseDate;
    public final long mId;
    public final String mTitle;
    public final double mPopularity;
    public final double mVoteAverage;

    public static final String KEY_POSTERPATH = "poster_path";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_RELEASE_DATE = "release_date";
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_POPULARITY = "popularity";
    public static final String KEY_VOTE_AVERAGE = "vote_average";


    public Movie(String posterPath, String overview, String releaseDate, long id,
                 String title, double popularity, double voteAverage){
        mPosterPath = "http://image.tmdb.org/t/p/w185/" + posterPath;
        mOverview = overview;
        mReleaseDate = releaseDate;
        mId = id;
        mTitle = title;
        mPopularity = popularity;
        mVoteAverage = voteAverage;
    }

    public long getId() {
        return mId;
    }
    public String getPosterPath() {
        return mPosterPath;
    }
    public String getOverview() {
        return mOverview;
    }
    public String getReleaseDate() {
        return mReleaseDate;
    }
    public String getTitle() {
        return mTitle;
    }
    public double getPopularity() {
        return mPopularity;
    }
    public double getVoteAverage() {
        return mVoteAverage;
    }

}
