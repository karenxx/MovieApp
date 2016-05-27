package com.example.android.popmovie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popmovie.adapter.ReviewAdapter;
import com.example.android.popmovie.adapter.TrailerAdapter;
import com.example.android.popmovie.data.MovieContract;
import com.linearlistview.LinearListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    static final String DETAIL_URI = "URI";
    static final String DETAIL_MOVIEID_KEY = "MOVIE_ID";
    private ShareActionProvider mShareActionProvider;
    private SharedPreferences mSharedPreferences;
    private boolean mIsFavorite;
    private MenuItem mActionFavoriteMenu;
    private static final int DETAIL_LOADER = 0;

    private long mMovieId = DetailActivity.ERROR_MOVIE_ID;
    private String mMovieTitle;
    private String mMovieOverview;
    private String mMovieReleaseDate;
    private String mMovieVoteAverage;
    private String mMoviePosterPath;

    private Movie mMovie;
    private Review mReview;
    private Trailer mTrailer;
    private TextView mMovieTitleView;
    private TextView mMovieOverviewView;
    private TextView mMovieReleaseDateView;
    private TextView mMovieVoteAverageView;
    private ImageView mMoviePosterView;
    private LinearListView mMovieTrailerView;
    private LinearListView mMovieReviewView;

    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detailfragment, menu);

        MenuItem action_share = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_share);
        if (mTrailer != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

        mActionFavoriteMenu = menu.findItem(R.id.action_favorite);
        mSharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.favorite_file_key), Context.MODE_PRIVATE);
        mIsFavorite = mSharedPreferences.contains(Long.toString(mMovieId));
        mActionFavoriteMenu.setIcon(mIsFavorite ? R.drawable.abc_btn_rating_star_on_mtrl_alpha
                : R.drawable.abc_btn_rating_star_off_mtrl_alpha);
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + mTrailer.getKey());
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_favorite) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            if (mIsFavorite) {
                mIsFavorite = false;
                editor.remove(Long.toString(mMovieId));
                mActionFavoriteMenu.setIcon(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            } else {
                mIsFavorite = true;
                editor.putString(Long.toString(mMovieId), null);
                mActionFavoriteMenu.setIcon(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
            }
            editor.commit();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = arguments.getLong(DetailActivityFragment.DETAIL_MOVIEID_KEY);
        }
        View rootview = inflater.inflate(R.layout.fragment_detail_activity, container, false);

        mMovieTitleView = (TextView) rootview.findViewById(R.id.detail_title);
        mMovieOverviewView = (TextView) rootview.findViewById(R.id.detail_overview);
        mMovieReleaseDateView = (TextView) rootview.findViewById(R.id.detail_release_date);
        mMovieVoteAverageView = (TextView) rootview.findViewById(R.id.detail_vote_average);
        mMoviePosterView = (ImageView) rootview.findViewById(R.id.detail_image);
        mMovieTrailerView = (LinearListView) rootview.findViewById(R.id.detail_trailers);
        mMovieReviewView = (LinearListView) rootview.findViewById(R.id.detail_reviews);

        mTrailerAdapter = new TrailerAdapter(getActivity());
        mMovieTrailerView.setAdapter(mTrailerAdapter);
        mReviewAdapter = new ReviewAdapter(getActivity());
        mMovieReviewView.setAdapter(mReviewAdapter);

        if (mMovieId != DetailActivity.ERROR_MOVIE_ID) {
            new FetchTrailerTask().execute(mMovieId);
            new FetchReviewTask().execute(mMovieId);
        }
        mMovieTrailerView.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int position, long id) {
                Trailer trailer = (Trailer) mTrailerAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                startActivity(intent);
            }
        });
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMovieId != DetailActivity.ERROR_MOVIE_ID) {
            return new CursorLoader(getActivity(),
                    MovieContract.MovieEntry.buildMovieUri(),
                    null,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                    new String[]{Long.toString(mMovieId)},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader == null || !data.moveToFirst()) {
            return;
        }
        mMovieId = data.getLong(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
        mMovieTitle = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
        mMovieOverview = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW));
        mMoviePosterPath = "http://image.tmdb.org/t/p/w185/" + data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER));
        mMovieReleaseDate = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
        mMovieVoteAverage = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE));

        mMovieTitleView.setText(mMovieTitle);
        mMovieOverviewView.setText(mMovieOverview);
        mMovieReleaseDateView.setText(mMovieReleaseDate);
        mMovieVoteAverageView.setText(mMovieVoteAverage + "/10");
        Picasso.with(getActivity()).
                load(mMoviePosterPath)
                .fit()
                .into(mMoviePosterView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public class FetchTrailerTask extends AsyncTask<Long, Void, List<Trailer>> {
        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        public List<Trailer> getTrailerDataFromJson(String trailerJsonStr) throws JSONException {
            Log.d(LOG_TAG, trailerJsonStr);
            final String OWM_RESULT = "results";
            final String OWM_KEY = "key";
            final String OWM_ID = "id";
            final String OWN_SITE = "site";
            List<Trailer> result = new ArrayList<>();

            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(OWM_RESULT);
            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                if (trailer.getString(OWN_SITE).equals("YouTube")) {
                    String trailerId = trailer.getString(OWM_ID);
                    String trailerKey = trailer.getString(OWM_KEY);
                    result.add(new Trailer(trailerId, trailerKey));
                }
            }
            return result;
        }

        @Override
        protected List<Trailer> doInBackground(Long... params) {
            if (params.length == 0)
                return null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String trailerJSONStr = null;
            try {
                final String TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String APIKEY_PARAM = "api_key";
                Uri buildUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIEDB_API_KEY)
                        .build();
                URL url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                trailerJSONStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
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
            try {
                return getTrailerDataFromJson(trailerJSONStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null && mTrailerAdapter != null) {
                mTrailerAdapter.setData(trailers);
            }
            mTrailerAdapter.notifyDataSetChanged();
            mTrailer = trailers.get(0);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent());
            }
        }
    }

    public class FetchReviewTask extends AsyncTask<Long, Void, List<Review>> {
        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        public List<Review> getReviewDataFromJson(String reviewJsonStr) throws JSONException {
            Log.d(LOG_TAG, reviewJsonStr);
            final String OWM_RESULT = "results";
            final String OWM_ID = "id";
            final String OWM_AUTHOR = "author";
            final String OWM_CONTENT = "content";
            List<Review> result = new ArrayList<>();

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray trailerArray = reviewJson.getJSONArray(OWM_RESULT);
            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject review = trailerArray.getJSONObject(i);
                String reviewId = review.getString(OWM_ID);
                String reviewAuthor = review.getString(OWM_AUTHOR);
                String reviewContent = review.getString(OWM_CONTENT);
                Log.d(LOG_TAG, "fetch " + reviewAuthor + reviewContent);
                result.add(new Review(reviewId, reviewAuthor, reviewContent));
            }
            return result;
        }

        @Override
        protected List<Review> doInBackground(Long... params) {
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String trailerJSONStr = null;
            try {
                final String TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String APIKEY_PARAM = "api_key";
                Uri buildUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIEDB_API_KEY)
                        .build();
                URL url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                trailerJSONStr = buffer.toString();
                Log.v(LOG_TAG, "trailerJSONStr" + trailerJSONStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
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
            try {
                return getReviewDataFromJson(trailerJSONStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null && mReviewAdapter != null) {
                mReviewAdapter.setData(reviews);
            }
            mReviewAdapter.notifyDataSetChanged();
        }
    }
}

