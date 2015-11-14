package com.corochann.androidtvapptutorial.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.corochann.androidtvapptutorial.data.VideoItemLoader;
import com.corochann.androidtvapptutorial.model.Movie;
import com.corochann.androidtvapptutorial.data.MovieProvider;
import com.corochann.androidtvapptutorial.ui.background.PicassoBackgroundManager;
import com.corochann.androidtvapptutorial.R;
import com.corochann.androidtvapptutorial.recommendation.RecommendationFactory;
import com.corochann.androidtvapptutorial.ui.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by corochann on 2015/06/28.
 */
public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private ArrayObjectAdapter mRowsAdapter;

    /* Grid row item settings */
    private static final int GRID_ITEM_WIDTH = 300;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final String GRID_STRING_ERROR_FRAGMENT = "ErrorFragment";
    private static final String GRID_STRING_GUIDED_STEP_FRAGMENT = "GuidedStepFragment";
    private static final String GRID_STRING_RECOMMENDATION = "Recommendation";
    private static final String GRID_STRING_SPINNER = "Spinner";

    private static PicassoBackgroundManager picassoBackgroundManager = null;

    ArrayList<Movie> mItems = null; //MovieProvider.getMovieItems();
    private static int recommendationCounter = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();

        loadRows();
        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(VideoItemLoader.VIDEO_ITEM_LOADER_ID, null, new MainFragmentLoaderCallbacks());


        setupEventListeners();

        picassoBackgroundManager = new PicassoBackgroundManager(getActivity());
        picassoBackgroundManager.updateBackgroundWithDelay();
        //picassoBackgroundManager.updateBackgroundWithDelay("http://heimkehrend.raindrop.jp/kl-hacker/wp-content/uploads/2014/10/RIMG0656.jpg");
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
        setOnItemViewClickedListener(new ItemViewClickedListener());

        // Existence of this method make In-app search icon visible
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            // each time the item is clicked, code inside here will be executed.
            Log.d(TAG, "onItemClicked: item = " + item.toString());
            if (item instanceof Movie) {
                Movie movie = (Movie) item;

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);
                getActivity().startActivity(intent);
            } else if (item instanceof String){
                if (item == GRID_STRING_ERROR_FRAGMENT) {
                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);
                } else if (item == GRID_STRING_GUIDED_STEP_FRAGMENT) {
                    Intent intent = new Intent(getActivity(), GuidedStepActivity.class);
                    startActivity(intent);
                } else if (item == GRID_STRING_RECOMMENDATION) {
                    Log.v(TAG, "onClick recommendation. counter " + recommendationCounter);
                    RecommendationFactory recommendationFactory = new RecommendationFactory(getActivity().getApplicationContext());
                    if(mItems != null){
                        Movie movie = mItems.get(recommendationCounter % mItems.size());
                        recommendationFactory.recommend(recommendationCounter, movie, NotificationCompat.PRIORITY_HIGH);
                        Toast.makeText(getActivity(), "Recommendation sent (item " + recommendationCounter +")", Toast.LENGTH_SHORT).show();
                        recommendationCounter++;
                    } else {
                        Toast.makeText(getActivity(), "Recommendation unsuccessful (video data not prepared yet)", Toast.LENGTH_SHORT).show();
                    }
                } else if (item == GRID_STRING_SPINNER) {
                    // Show SpinnerFragment, while backgroundtask is executed
                    new ShowSpinnerTask().execute();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            // each time the item is selected, code inside here will be executed.
            if (item instanceof String) {                    // GridItemPresenter
                picassoBackgroundManager.updateBackgroundWithDelay();
            } else if (item instanceof Movie) {              // CardPresenter
                picassoBackgroundManager.updateBackgroundWithDelay(((Movie) item).getBackgroundImageUrl());
            }
        }
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.videos_by_google_banner));
        setTitle("Hello Android TV!"); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }



    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        /* GridItemPresenter */
        HeaderItem gridItemPresenterHeader = new HeaderItem(0, "GridItemPresenter");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(GRID_STRING_ERROR_FRAGMENT);
        gridRowAdapter.add(GRID_STRING_GUIDED_STEP_FRAGMENT);
        gridRowAdapter.add(GRID_STRING_RECOMMENDATION);
        gridRowAdapter.add(GRID_STRING_SPINNER);
        mRowsAdapter.add(new ListRow(gridItemPresenterHeader, gridRowAdapter));

        /* CardPresenter */
/*
        HeaderItem cardPresenterHeader = new HeaderItem(1, "CardPresenter");
        CardPresenter cardPresenter = new CardPresenter();
        ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);

        for (Movie movie : mItems) {
            cardRowAdapter.add(movie);
        }

        mRowsAdapter.add(new ListRow(cardPresenterHeader, cardRowAdapter));
*/

        /* Set */
        setAdapter(mRowsAdapter);
    }

    private class MainFragmentLoaderCallbacks implements LoaderManager.LoaderCallbacks<LinkedHashMap<String, List<Movie>>> {
        @Override
        public Loader<LinkedHashMap<String, List<Movie>>> onCreateLoader(int id, Bundle args) {
            /* Create new Loader */
            Log.d(TAG, "onCreateLoader");
            if(id == VideoItemLoader.VIDEO_ITEM_LOADER_ID) {
                Log.d(TAG, "create VideoItemLoader");
                //return new VideoItemLoader(getActivity());
                return new VideoItemLoader(getActivity().getApplicationContext());
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<LinkedHashMap<String, List<Movie>>> loader, LinkedHashMap<String, List<Movie>> data) {
            Log.d(TAG, "onLoadFinished");
            /* Loader data has prepared. Start updating UI here */
            switch (loader.getId()) {
                case VideoItemLoader.VIDEO_ITEM_LOADER_ID:
                    Log.d(TAG, "VideoLists UI update");

                    /* Hold data reference to use it for recommendation */
                    mItems = new ArrayList<Movie>();

                    mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

                    int index = 0;
        /* GridItemPresenter */
                    HeaderItem gridItemPresenterHeader = new HeaderItem(index, "GridItemPresenter");
                    index++;

                    GridItemPresenter mGridPresenter = new GridItemPresenter();
                    ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
                    gridRowAdapter.add(GRID_STRING_ERROR_FRAGMENT);
                    gridRowAdapter.add(GRID_STRING_GUIDED_STEP_FRAGMENT);
                    gridRowAdapter.add(GRID_STRING_RECOMMENDATION);
                    gridRowAdapter.add(GRID_STRING_SPINNER);
                    mRowsAdapter.add(new ListRow(gridItemPresenterHeader, gridRowAdapter));

        /* CardPresenter */
                    CardPresenter cardPresenter = new CardPresenter();

                    if (null != data) {
                        for (Map.Entry<String, List<Movie>> entry : data.entrySet()) {
                            ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
                            List<Movie> list = entry.getValue();

                            for (int j = 0; j < list.size(); j++) {
                                Movie movie = list.get(j);
                                cardRowAdapter.add(movie);
                                mItems.add(movie);           // Add movie reference for recommendation purpose.
                            }
                            HeaderItem header = new HeaderItem(index, entry.getKey());
                            index++;
                            mRowsAdapter.add(new ListRow(header, cardRowAdapter));
                        }
                    } else {
                        Log.e(TAG, "An error occurred fetching videos");
                    }
        /* Set */
                    setAdapter(mRowsAdapter);
            }
        }

        @Override
        public void onLoaderReset(Loader<LinkedHashMap<String, List<Movie>>> loader) {
            Log.d(TAG, "onLoadReset");
            /* When it is called, Loader data is now unavailable due to some reason. */

        }


    }

    /**
     * from AOSP sample source code
     * GridItemPresenter class. Show TextView with item type String.
     */
    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }
    }

    private class ShowSpinnerTask extends AsyncTask<Void, Void, Void> {
        SpinnerFragment mSpinnerFragment;

        @Override
        protected void onPreExecute() {
            mSpinnerFragment = new SpinnerFragment();
            getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSpinnerFragment).commit();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Do some background process here.
            // It just waits 5 sec in this Tutorial
            SystemClock.sleep(5000);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
        }
    }
}
