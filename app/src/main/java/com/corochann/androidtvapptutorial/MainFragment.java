package com.corochann.androidtvapptutorial;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.util.Log;

/**
 * Created by corochann on 2015/06/28.
 */
public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
}
