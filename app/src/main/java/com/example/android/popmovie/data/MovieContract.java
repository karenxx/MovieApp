package com.example.android.popmovie.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movie database
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.popmovie";
    public static final Uri BASE_CONTETN_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";


    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTETN_URI =
                BASE_CONTETN_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER = "movie_poster";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_POPULARITY = "popularity";

        public static Uri buildMovieUri() {
            return CONTETN_URI;
        }

        public static Uri buildMovieByIdUri(long id){
            return CONTETN_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static long getMovieIdfromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

}
