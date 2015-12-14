package com.corochann.androidtvapptutorial.ui.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;

import com.corochann.androidtvapptutorial.R;

/**
 * Presenter to demonstrate {@link DetailsOverviewRowPresenter}
 */
public class CustomDetailsOverviewRowPresenter extends DetailsOverviewRowPresenter {

    private static final String TAG = CustomDetailsOverviewRowPresenter.class.getSimpleName();

    private Context mContext;

    public CustomDetailsOverviewRowPresenter(Presenter presenter, Context context) {
        super(presenter);
        mContext = context;
    }

    @Override
    protected void onRowViewAttachedToWindow(RowPresenter.ViewHolder vh) {
        Log.v(TAG, "onRowViewAttachedToWindow");
        super.onRowViewAttachedToWindow(vh);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        Log.v(TAG, "onBindRowViewHolder");
        setBackgroundColor(mContext.getResources().getColor(R.color.default_background));
        setStyleLarge(true);
        // It must be called "after" above function call
        super.onBindRowViewHolder(holder, item);
    }

    @Override
    protected void onRowViewExpanded(RowPresenter.ViewHolder vh, boolean expanded) {
        Log.v(TAG, "onRowViewExpanded");
        super.onRowViewExpanded(vh, expanded);
    }
}