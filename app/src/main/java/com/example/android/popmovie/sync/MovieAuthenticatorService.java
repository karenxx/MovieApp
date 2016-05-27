package com.example.android.popmovie.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MovieAuthenticatorService extends Service {
    public MovieAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        mAuthenticator = new MovieAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
