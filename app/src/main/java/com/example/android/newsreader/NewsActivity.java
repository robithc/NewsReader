package com.example.android.newsreader;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class NewsActivity extends AppCompatActivity
        implements LoaderCallbacks<List<News>> {

    private static final String LOG_TAG = NewsActivity.class.getName();
    private static final int API_LOADER_ID = 1;
    private final String API_REQUEST_URL =
            "http://content.guardianapis.com/search?order-by=newest&show-tags=contributor&page" +
                    "=1&page-size=30&q=Android&api-key=753788d5-bd1b-4084-8cb8-5a473d5ecdef";
    ProgressBar pbProgressBar;
    private TextView mEmptyStateTextView;
    private NewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pbProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        ListView newsListView = (ListView) findViewById(R.id.list_view);
        mEmptyStateTextView = (TextView) findViewById(R.id.no_internet);
        newsListView.setEmptyView(mEmptyStateTextView);

        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        newsListView.setAdapter(mAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                News currentNews = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                Intent newsIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(newsIntent);
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            LoaderManager loaderManager = getLoaderManager();

            loaderManager.initLoader(API_LOADER_ID, null, this);
        } else {

            pbProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "News Activity onCreateLoader called");
        return new NewsLoader(this, API_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
        pbProgressBar.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.empty_list);
        mAdapter.clear();

        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.clear();
    }


    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}