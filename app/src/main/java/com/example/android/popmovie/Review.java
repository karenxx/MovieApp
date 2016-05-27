package com.example.android.popmovie;

public class Review {
    public final String mId;
    public final String mAuhor;
    public final String mContent;

    public Review(String id, String author, String content) {
        mId = id;
        mAuhor = author;
        mContent = content;
    }

    public String getId() {
        return mId;
    }

    public String getAuhor() {
        return mAuhor;
    }

    public String getContent() {
        return mContent;
    }
}
