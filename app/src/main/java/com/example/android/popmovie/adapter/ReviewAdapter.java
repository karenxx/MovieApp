package com.example.android.popmovie.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.popmovie.R;
import com.example.android.popmovie.Review;

import java.util.List;

public class ReviewAdapter extends BaseAdapter {
    private final Context mContext;
    private LayoutInflater mInflater;
    private List<Review> mReviews;

    public ReviewAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Review> reviews) {
        mReviews = reviews;
    }

    @Override
    public int getCount() {
        if (mReviews != null) {
            return mReviews.size();
        } else return 0;
    }

    @Override
    public Object getItem(int position) {
        if (position < 0 || position >= mReviews.size())
            return null;
        return mReviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Review review = (Review) getItem(position);
        ViewHolder viewHolder;
        if (view == null) {
            view = mInflater.inflate(R.layout.movie_item_review, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) view.getTag();
        if (review != null) {
            //String reviewPath = "http://api.themoviedb.org/3/movie" + review.getId() + "/0"
            viewHolder.reviewAuthorView.setText(review.getAuhor());
            viewHolder.reviewContentView.setText(review.getContent());
        }
        return view;
    }

    public static class ViewHolder {
        public final TextView reviewAuthorView;
        public final TextView reviewContentView;

        public ViewHolder(View view) {
            reviewAuthorView = (TextView) view.findViewById(R.id.review_author);
            reviewContentView = (TextView) view.findViewById(R.id.review_content);
        }
    }
}
