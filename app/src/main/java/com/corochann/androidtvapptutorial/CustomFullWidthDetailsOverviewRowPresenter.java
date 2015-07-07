package com.corochann.androidtvapptutorial;

import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowPresenter;

/**
 * Created by 5004121605 on 6/7/2015.
 */
public class CustomFullWidthDetailsOverviewRowPresenter extends FullWidthDetailsOverviewRowPresenter {

    private static final String TAG = CustomFullWidthDetailsOverviewRowPresenter.class.getSimpleName();

    CustomFullWidthDetailsOverviewRowPresenter(Presenter presenter) {
        super(presenter);
    }
    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        this.setState((ViewHolder) holder, FullWidthDetailsOverviewRowPresenter.STATE_SMALL);
    }
}