package com.corochann.androidtvapptutorial.ui;

import android.app.Activity;
import android.os.Bundle;

import com.corochann.androidtvapptutorial.R;

/**
 * Created by corochann on 7/7/2015.
 */
public class ErrorActivity extends Activity {

    private static final String TAG = ErrorActivity.class.getSimpleName();

    private ErrorFragment mErrorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testError();
    }

    private void testError() {
        mErrorFragment = new ErrorFragment();
        getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mErrorFragment).commit();
    }
}