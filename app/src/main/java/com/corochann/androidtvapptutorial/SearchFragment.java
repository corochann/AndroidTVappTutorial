package com.corochann.androidtvapptutorial;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.util.Log;

/**
 * Created by corochann on 2/9/2015.
 */
public class SearchFragment extends android.support.v17.leanback.app.SearchFragment {

    private static final String TAG = SearchFragment.class.getSimpleName();

    private static final int REQUEST_SPEECH = 0x00000010;
    private ArrayObjectAdapter mRowsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        //setSearchResultProvider(this);
        //setOnItemViewClickedListener(new ItemViewClickedListener());
        if (!Utils.hasPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {
            // SpeechRecognitionCallback is not required and if not provided recognition will be handled
            // using internal speech recognizer, in which case you must have RECORD_AUDIO permission
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    Log.v(TAG, "recognizeSpeech");
                    try {
                        startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Cannot find activity for speech recognizer", e);
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}