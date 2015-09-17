package com.corochann.androidtvapptutorial.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * SpinnerFragment shows spinning progressbar to notify user that
 * application is processing something (while downloading, or preparing sth etc.)
 *
 * Example of usage in AsyncTask
 * + Start showing: OnPreExecute
 *         mSpinnerFragment = new SpinnerFragment();
 *         getFragmentManager().beginTransaction().add(R.id.some_view_group, mSpinnerFragment).commit();
 * + Stop showing: OnPostExecute
 *         getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
 */
public class SpinnerFragment extends Fragment {

    private static final String TAG = SpinnerFragment.class.getSimpleName();

    private static final int SPINNER_WIDTH = 100;
    private static final int SPINNER_HEIGHT = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ProgressBar progressBar = new ProgressBar(container.getContext());
        if (container instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(SPINNER_WIDTH, SPINNER_HEIGHT, Gravity.CENTER);
            progressBar.setLayoutParams(layoutParams);
        }
        return progressBar;
    }
}