package com.example.android.popmovie.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.popmovie.R;
import com.example.android.popmovie.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieCursorAdapter.class.getSimpleName();
    private Context mContext;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView movieView = (ImageView) view.findViewById(R.id.list_item_movie_imageview);
        movieView.setLayoutParams(new GridView.LayoutParams(240, 340));

        Picasso.with(mContext).
                load("http://image.tmdb.org/t/p/w185/" + cursor.getString(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER)))
                .fit()
                //.centerCrop()
                .into(movieView);
    }
}
