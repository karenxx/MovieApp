package com.example.android.popmovie;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popmovie.adapter.MovieCursorAdapter;
import com.example.android.popmovie.data.MovieContract;
import com.example.android.popmovie.sync.MovieSyncAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieCursorAdapter mMovieAdapter;
    private static final int MOVIE_LOADER = 0;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY
    };

    static final int COL_MOVIE_TABLE_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_POSTER = 4;
    static final int COL_MOVIE_RELEASE_DATE = 5;
    static final int COL_VOTE_AVERAGE = 6;
    static final int COL_POPULARITY = 7;

    public interface Callback {
        public void onItemSelected(long movieId);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri();
        final Cursor cur = getActivity().getContentResolver().query(movieUri, null, null, null, null);
        mMovieAdapter = new MovieCursorAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridview.setAdapter(mMovieAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    long movieId = cursor.getLong(COL_MOVIE_ID);
                    ((Callback) getActivity()).onItemSelected(movieId);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onSettingChanged() {
        updateMovie();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    private void updateMovie() {
        MovieSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri();

        String preferredSort = Utility.getPreferredSorting(getActivity());
        if (preferredSort.equals(getActivity().getString(R.string.pref_sort_sort_by_ratings))) {
            String sortByRating = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC LIMIT 20";
            return new CursorLoader(getActivity(), movieUri, null, null, null, sortByRating);
        } else if (preferredSort.equals(getActivity().getString(R.string.pref_sort_sort_by_favorite))) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                    getString(R.string.favorite_file_key), Context.MODE_PRIVATE);
            Set<String> favoriteMovies = sharedPreferences.getAll().keySet();
            if(favoriteMovies.size() == 0){
                mMovieAdapter.swapCursor(null);
                return null;
            }
            StringBuilder moviesForSql = new StringBuilder("(");
            for (String movie : favoriteMovies) {
                moviesForSql.append(movie);
                moviesForSql.append(",");
            }
            // remove last ","
            if (moviesForSql.length() != 0) {
                moviesForSql.deleteCharAt(moviesForSql.length() - 1);
            }
            moviesForSql.append(")");
            Log.d(LOG_TAG, moviesForSql.toString());
            return new CursorLoader(getActivity(),
                    movieUri,
                    null,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " in " + moviesForSql.toString(),
                    null,
                    null);
        } else {
            String sortDefault = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC LIMIT 20";
            return new CursorLoader(getActivity(), movieUri, null, null, null, sortDefault);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, data.getCount() + " " + data.getColumnCount());

        mMovieAdapter.swapCursor(data);
        List<Double> list = new ArrayList<>();
        data.moveToFirst();
        while(data.moveToNext()) {
            list.add(data.getDouble(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY)));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
