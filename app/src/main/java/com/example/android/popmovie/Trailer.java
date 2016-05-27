package com.example.android.popmovie;

public class Trailer {
    public final String mId;
    public final String mKey;

    public Trailer(String id, String key){
        mId = id;
        mKey = key;
    }
    public String getId() {
        return mId;
    }
    public String getKey(){
        return mKey;
    }
}
