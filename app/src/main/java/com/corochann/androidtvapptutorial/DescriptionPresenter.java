package com.corochann.androidtvapptutorial;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

public class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private static final String TAG = DescriptionPresenter.class.getSimpleName();

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        viewHolder.getTitle().setText(((Movie) item).getTitle());
        viewHolder.getSubtitle().setText(((Movie) item).getStudio());
    }
}