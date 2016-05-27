package com.example.android.popmovie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.popmovie.data.MovieContract.MovieEntry;

/**
 * Manages a local database for movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movie.db";
    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "("
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                + MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_POSTER + " TEXT, "
                + MovieEntry.COLUMN_OVERVIEW + " TEXT, "
                + MovieEntry.COLUMN_RELEASE_DATE + " TEXT, "
                + MovieEntry.COLUMN_POPULARITY + " REAL, "
                + MovieEntry.COLUMN_VOTE_AVERAGE + " REAL);";
       db.execSQL(SQL_CREATE_MOVIE_TABLE);
     }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
