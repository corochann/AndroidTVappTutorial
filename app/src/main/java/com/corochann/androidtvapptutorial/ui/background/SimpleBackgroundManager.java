package com.corochann.androidtvapptutorial.ui.background;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.app.BackgroundManager;
import android.util.DisplayMetrics;

import com.corochann.androidtvapptutorial.R;


/**
 * Created by corochann on 3/7/2015.
 */
public class SimpleBackgroundManager {

    private static final String TAG = SimpleBackgroundManager.class.getSimpleName();

    private final int DEFAULT_BACKGROUND_RES_ID = R.drawable.default_background;
    private static Drawable mDefaultBackground;

    private Activity mActivity;
    private BackgroundManager mBackgroundManager;

    public SimpleBackgroundManager(Activity activity) {
        mActivity = activity;
        mDefaultBackground = activity.getDrawable(DEFAULT_BACKGROUND_RES_ID);
        mBackgroundManager = BackgroundManager.getInstance(activity);
        mBackgroundManager.attach(activity.getWindow());
        activity.getWindowManager().getDefaultDisplay().getMetrics(new DisplayMetrics());
    }

    public void updateBackground(Drawable drawable) {
        mBackgroundManager.setDrawable(drawable);
    }

    public void clearBackground() {
        mBackgroundManager.setDrawable(mDefaultBackground);
    }

}