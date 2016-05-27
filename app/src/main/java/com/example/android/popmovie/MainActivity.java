package com.example.android.popmovie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.popmovie.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mSortSetting;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mSortSetting = Utility.getPreferredSorting(this);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
        MovieSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortSetting = Utility.getPreferredSorting(this);
        if (sortSetting != null && !sortSetting.equals(mSortSetting)) {
            MainActivityFragment mainFragment = (MainActivityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.movie_fragment);
            if (mainFragment != null) {
                mainFragment.onSettingChanged();
            }
//            DetailActivityFragment detailActivityFragment = (DetailActivityFragment) getSupportFragmentManager()
//                    .findFragmentByTag(DETAILFRAGMENT_TAG);
//            if(detailActivityFragment != null){
//                detailActivityFragment.onSettingChanged(sortSetting);
//            }
            mSortSetting = sortSetting;
        }
    }

    @Override
    public void onItemSelected(long movieId) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putLong(DetailActivityFragment.DETAIL_MOVIEID_KEY, movieId);
            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailActivityFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, movieId);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
