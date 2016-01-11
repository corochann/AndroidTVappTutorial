package com.corochann.androidtvapptutorial.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;

import com.corochann.androidtvapptutorial.R;

/**
 * {@link VerticalGridActivity} loads {@link VerticalGridFragment}
 */
public class VerticalGridActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_grid);
    }
}
