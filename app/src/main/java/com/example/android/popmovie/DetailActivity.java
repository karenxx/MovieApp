package com.example.android.popmovie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class DetailActivity extends AppCompatActivity {
    static final long ERROR_MOVIE_ID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState == null){
            Bundle argument = new Bundle();
            argument.putLong(DetailActivityFragment.DETAIL_MOVIEID_KEY, getIntent().
                    getLongExtra(Intent.EXTRA_TEXT, ERROR_MOVIE_ID));
            DetailActivityFragment detailFragment = new DetailActivityFragment();
            detailFragment.setArguments(argument);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, detailFragment)
                    .commit();
        }
    }
}
