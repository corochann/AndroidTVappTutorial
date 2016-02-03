package com.corochann.androidtvapptutorial.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;

import com.corochann.androidtvapptutorial.data.VideoProvider;
import com.corochann.androidtvapptutorial.model.Movie;
import com.corochann.androidtvapptutorial.ui.background.PicassoBackgroundManager;
import com.corochann.androidtvapptutorial.ui.presenter.CardPresenter;

import org.json.JSONException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * VerticalGridFragment shows contents with vertical alignment
 */
public class VerticalGridFragment extends android.support.v17.leanback.app.VerticalGridFragment {

    private static final String TAG = VerticalGridFragment.class.getSimpleName();
    private static final int NUM_COLUMNS = 4;

    private LinkedHashMap<String, List<Movie>> mVideoLists = null;
    private ArrayObjectAdapter mAdapter;

    private PicassoBackgroundManager picassoBackgroundManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        picassoBackgroundManager = new PicassoBackgroundManager(getActivity());

        setTitle("VerticalGridFragment");
        //setBadgeDrawable(getResources().getDrawable(R.drawable.app_icon_your_company));

        setupFragment();
        setupEventListeners();

        // it will move current focus to specified position. Comment out it to see the behavior.
        // setSelectedPosition(5);
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new CardPresenter());

        /* Add movie items */
        try {
            mVideoLists = VideoProvider.buildMedia(getActivity());
        } catch (JSONException e) {
            Log.e(TAG, e.toString(), e);
        }
        if(mVideoLists != null) {
            for (int i = 0; i < 3; i++) { // This loop is to for increasing the number of contents. not necessary.
                for (Map.Entry<String, List<Movie>> entry : mVideoLists.entrySet()) {
                    // String categoryName = entry.getKey();
                    List<Movie> list = entry.getValue();
                    for (int j = 0; j < list.size(); j++) {
                        Movie movie = list.get(j);
                        mAdapter.add(movie);
                    }
                }
            }
        }
        setAdapter(mAdapter);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                Movie movie = (Movie) item;

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);
                getActivity().startActivity(intent);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if( item != null ){
                picassoBackgroundManager.updateBackgroundWithDelay(((Movie) item).getBackgroundImageUrl());
            } else {
                Log.w(TAG, "item is null");
            }

        }
    }
}