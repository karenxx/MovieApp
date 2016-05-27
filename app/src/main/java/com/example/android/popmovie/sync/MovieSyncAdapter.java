package com.example.android.popmovie.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.popmovie.BuildConfig;
import com.example.android.popmovie.R;
import com.example.android.popmovie.Utility;
import com.example.android.popmovie.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJSONStringStr = null;

        String sortingOrder = Utility.getPreferredSorting(getContext());
        if(sortingOrder.equals(getContext().getString(R.string.pref_sort_sort_by_favorite))) {
            return;
        }
        try {
            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie?";
            final String APIKEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(sortingOrder)
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIEDB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                Log.w(LOG_TAG, "Buffer length is 0.");
                return;
            }
            movieJSONStringStr = buffer.toString();
            getMovieDataFromJson(movieJSONStringStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception" + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    public void getMovieDataFromJson(String movieJsonStr) throws JSONException {
        final String OWM_RESULT = "results";
        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULT);
            ContentValues[] result = new ContentValues[movieArray.length()];

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject currentMovie = movieArray.getJSONObject(i);
                String posterPath = currentMovie.getString("poster_path");
                String movieOverview = currentMovie.getString("overview");
                String releaseDate = currentMovie.getString("release_date");
                long movieId = currentMovie.getLong("id");
                String movieTitle = currentMovie.getString("original_title");
                double movieVoteAverage = currentMovie.getDouble("vote_average");
                double moviePopularity = currentMovie.getDouble("popularity");

                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER, posterPath);
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movieOverview);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieTitle);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, moviePopularity);
                result[i] = movieValues;
            }
//            if (movieArray.length() > 0) {
//                getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTETN_URI, null, null);
//                int insertedCount = getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTETN_URI, result);
//            }

            ContentResolver contentResolver = getContext().getContentResolver();
            Uri uri = MovieContract.MovieEntry.CONTETN_URI;
            for(ContentValues cv : result) {
                long movieId = cv.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                contentResolver.delete(uri, MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?", new String[]{Long.toString(movieId)});
                contentResolver.insert(uri, cv);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        // Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}