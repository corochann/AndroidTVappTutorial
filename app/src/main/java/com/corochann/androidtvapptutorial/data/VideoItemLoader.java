package com.corochann.androidtvapptutorial.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.corochann.androidtvapptutorial.model.Movie;

import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Loader class which prepares Movie class data
 */
public class VideoItemLoader extends AsyncTaskLoader<LinkedHashMap<String, List<Movie>>> {

    private static final String TAG = VideoItemLoader.class.getSimpleName();
    public static final int VIDEO_ITEM_LOADER_ID = 0;

    LinkedHashMap<String, List<Movie>> mData;

    public VideoItemLoader(Context context) {
        super(context);
    }

    @Override
    public LinkedHashMap<String, List<Movie>> loadInBackground() {
        Log.d(TAG, "loadInBackground");

        /*
         * Executed in background thread.
         * Prepare data here, it may take long time (Database access, URL connection, etc).
         * return value is used in onLoadFinished() method in Activity/Fragment's LoaderCallbacks.
         */
        //LinkedHashMap<String, List<Movie>> videoLists = prepareData();
        LinkedHashMap<String, List<Movie>> videoLists = null;
        try {
            videoLists = VideoProvider.buildMedia(getContext(), VideoProvider.VIDEO_LIST_URL);
        } catch (JSONException e) {
            Log.e(TAG, "buildMedia failed", e);
            //cancelLoad();
        }
        return videoLists;
    }

    @Override
    public void deliverResult(LinkedHashMap<String, List<Movie>> data) {
        Log.d(TAG, "deliverResult");

        LinkedHashMap<String, List<Movie>> oldData = mData;
        mData = data;

        if(isStarted()){
            Log.d(TAG, "isStarted true");
            super.deliverResult(data);
        }

        if(oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    private void releaseResources(LinkedHashMap<String, List<Movie>> data) {
        Log.d(TAG, "releaseResources");
        // For a simple List, there is nothing to do. For something like a Cursor, we
        // would close it in this method. All resources associated with the Loader
        // should be released here.
        data = null;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading");
        if (mData != null) {
            Log.d(TAG, "mData remaining");
            deliverResult(mData);
        } else {
            Log.d(TAG, "mData is null, forceLoad");
            forceLoad();
        }
        //super.onStartLoading();

    }

    @Override
    protected void onStopLoading() {
        Log.d(TAG, "onStopLoading");
        //super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset");
        super.onReset();
    }

    @Override
    public void onCanceled(LinkedHashMap<String, List<Movie>> data) {
        Log.d(TAG, "onCanceled");
        super.onCanceled(data);
    }

    @Override
    protected boolean onCancelLoad() {
        return super.onCancelLoad();
    }

    private LinkedHashMap<String, List<Movie>> prepareData() {
        LinkedHashMap<String, List<Movie>> videoLists = new LinkedHashMap<>();
        List<Movie> videoList = MovieProvider.getMovieItems();
        videoLists.put("category 1", videoList);
        videoLists.put("category 2", videoList);
        videoLists.put("category 3", videoList);
        return videoLists;
    }
}