package com.example.android.popmovie.adapter;

import android.content.Context;
import android.media.Image;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.android.popmovie.R;
import com.example.android.popmovie.Trailer;
import com.squareup.picasso.Picasso;

import java.security.acl.LastOwnerException;
import java.util.List;

public class TrailerAdapter extends BaseAdapter{
    private final Context mContext;
    private LayoutInflater mInflater;
    private List<Trailer> mTrailers;

    public TrailerAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Trailer> trailers){
        mTrailers = trailers;
    }

    @Override
    public int getCount() {
        if(mTrailers != null) {
            return mTrailers.size();
        }
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        if(position < 0 || position >= mTrailers.size())
            return null;
        return mTrailers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Trailer trailer = (Trailer) getItem(position);
        ViewHolder viewHolder;

        if(view == null){
            view = mInflater.inflate(R.layout.movie_item_trailer, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        viewHolder = (ViewHolder)view.getTag();
        if(trailer != null) {
            String trailerImagePath = "http://img.youtube.com/vi/" + trailer.getKey() + "/0.jpg";
            Picasso.with(mContext).
                    load(trailerImagePath)
                    .into(viewHolder.imageView);
        }
        return view;
    }

    public static class ViewHolder{
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.trailer_image);
        }
    }
}
