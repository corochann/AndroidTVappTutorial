package com.corochann.androidtvapptutorial.ui.presenter;

import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowPresenter;

/**
 * Presenter to demonstrate {@link FullWidthDetailsOverviewRowPresenter}
 */
public class CustomFullWidthDetailsOverviewRowPresenter extends FullWidthDetailsOverviewRowPresenter {

    private static final String TAG = CustomFullWidthDetailsOverviewRowPresenter.class.getSimpleName();

    public CustomFullWidthDetailsOverviewRowPresenter(Presenter presenter) {
        super(presenter);
    }

    @Override
    protected void onRowViewAttachedToWindow(RowPresenter.ViewHolder vh) {
        super.onRowViewAttachedToWindow(vh);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
    }

    @Override
    protected void onLayoutOverviewFrame(ViewHolder viewHolder, int oldState, boolean logoChanged) {
        /* Please try selecting either one. */
        //setState(viewHolder, FullWidthDetailsOverviewRowPresenter.STATE_SMALL);
        //setState(viewHolder, FullWidthDetailsOverviewRowPresenter.STATE_FULL);
        setState(viewHolder, FullWidthDetailsOverviewRowPresenter.STATE_HALF);  // Default behavior

        super.onLayoutOverviewFrame(viewHolder, oldState, logoChanged);
    }
}