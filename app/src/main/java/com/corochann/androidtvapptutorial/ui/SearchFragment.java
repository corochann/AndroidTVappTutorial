package com.corochann.androidtvapptutorial.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.corochann.androidtvapptutorial.model.Movie;
import com.corochann.androidtvapptutorial.data.MovieProvider;
import com.corochann.androidtvapptutorial.common.Utils;
import com.corochann.androidtvapptutorial.ui.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {

    private static final String TAG = SearchFragment.class.getSimpleName();

    private static final int REQUEST_SPEECH = 0x00000010;
    private static final long SEARCH_DELAY_MS = 1000L;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayList<Movie> mItems = MovieProvider.getMovieItems();

    private final Handler mHandler = new Handler();
    private final Runnable mDelayedLoad = new Runnable() {
        @Override
        public void run() {
            loadRows();
        }
    };
    private String mQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        setSearchResultProvider(this);
        setOnItemViewClickedListener(new ItemViewClickedListener());
        if (!Utils.hasPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {
            Log.v(TAG, "no permission RECORD_AUDIO");
            // SpeechRecognitionCallback is not required and if not provided recognition will be handled
            // using internal speech recognizer, in which case you must have RECORD_AUDIO permission
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    Log.v(TAG, "recognizeSpeech");
                    try {
                        startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Cannot find activity for speech recognizer", e);
                    }
                }
            });
        }
    }

    public boolean hasResults() {
        return mRowsAdapter.size() > 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult requestCode=" + requestCode +
                " resultCode=" + resultCode +
                " data=" + data);

        switch (requestCode) {
            case REQUEST_SPEECH:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setSearchQuery(data, true);
                        break;
                }
        }
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        Log.d(TAG, "getResultsAdapter");
        // mRowsAdapter (Search result) has prepared in loadRows method
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery){
        Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        loadQueryWithDelay(newQuery, SEARCH_DELAY_MS);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        // No need to delay(wait) loadQuery, since the query typing has completed.
        loadQueryWithDelay(query, 0);
        return true;
    }

    /**
     * Starts {@link #loadRows()} method after delay.
     * It also cancels previously registered task if it has not yet executed.
     * @param query the word to be searched
     * @param delay the time to wait until loadRows will be executed (milliseconds).
     */
    private void loadQueryWithDelay(String query, long delay) {
        mHandler.removeCallbacks(mDelayedLoad);
        if (!TextUtils.isEmpty(query) && !query.equals("nil")) {
            mQuery = query;
            mHandler.postDelayed(mDelayedLoad, delay);
        }
    }

    /**
     * Searches query specified by mQuery, and sets the result to mRowsAdapter.
     */
    private void loadRows() {
        // offload processing from the UI thread
        new AsyncTask<String, Void, ListRow>() {
            private final String query = mQuery;

            @Override
            protected void onPreExecute() {
                mRowsAdapter.clear();
            }

            @Override
            protected ListRow doInBackground(String... params) {
                final List<Movie> result = new ArrayList<>();
                for (Movie movie : mItems) {
                    // Main logic of search is here.
                    // Just check that "query" is contained in Title or Description or not. (NOTE: excluded studio information here)
                    if (movie.getTitle().toLowerCase(Locale.ENGLISH)
                            .contains(query.toLowerCase(Locale.ENGLISH))
                            || movie.getDescription().toLowerCase(Locale.ENGLISH)
                            .contains(query.toLowerCase(Locale.ENGLISH))) {
                        result.add(movie);
                    }
                }

                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                listRowAdapter.addAll(0, result);
                HeaderItem header = new HeaderItem("Search Results");
                return new ListRow(header, listRowAdapter);
            }

            @Override
            protected void onPostExecute(ListRow listRow) {
                mRowsAdapter.add(listRow);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Movie: " + movie.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            } else {
                Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}