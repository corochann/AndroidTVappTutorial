package com.corochann.androidtvapptutorial.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;

import com.corochann.androidtvapptutorial.data.VideoProvider;
import com.corochann.androidtvapptutorial.model.Movie;
import com.corochann.androidtvapptutorial.data.MovieProvider;
import com.corochann.androidtvapptutorial.common.PlaybackController;
import com.corochann.androidtvapptutorial.common.Utils;
import com.corochann.androidtvapptutorial.ui.presenter.CardPresenter;
import com.corochann.androidtvapptutorial.ui.presenter.DescriptionPresenter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by corochann on 7/7/2015.
 */
public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {

    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int VIDEO_PLAY_FINISHED_MARGIN = 100; // used to check video playback has finished or not.
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 240;
    private static final boolean SHOW_IMAGE = true;
    private static Context sContext;
    private PlaybackOverlayActivity activity;

    //private Movie mSelectedMovie;
    private PlaybackController mPlaybackController;
    private PlaybackControlsRow mPlaybackControlsRow;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private Handler mHandler;
    private Runnable mRunnable;
    private ArrayList<Movie> mItems = new ArrayList<Movie>();
    private ArrayObjectAdapter mRowsAdapter;

    private Movie mSelectedMovie;
    String mCategoryName;

    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.RepeatAction mRepeatAction;
    private PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
    private PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
    private PlaybackControlsRow.ShuffleAction mShuffleAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.HighQualityAction mHighQualityAction;
    private PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    private PlaybackControlsRow.MoreActions mMoreActions;
    //private int mCurrentItem;
    private PicassoPlaybackControlsRowTarget mPlaybackControlsRowTarget;

    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback = new MediaControllerCallback();

    public PlaybackControlsRow getPlaybackControlsRow() {
        return mPlaybackControlsRow;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        sContext = getActivity();
        activity = (PlaybackOverlayActivity) getActivity();
        mHandler = new Handler();

        mSelectedMovie = getActivity().getIntent().getParcelableExtra(DetailsActivity.MOVIE);
        // TODO: temporal workaround, each category has separated by 100 for now.
        int currentItemIndex = (int)mSelectedMovie.getId() % 100;
        mCategoryName = mSelectedMovie.getCategory();
        mItems = VideoProvider.getMovieItems(mCategoryName);

        mPlaybackController = activity.getPlaybackController();
        mPlaybackController.setPlaylist(currentItemIndex, mItems);
        Log.i(TAG, "currentItemIndex: " + currentItemIndex);

        setBackgroundType(PlaybackOverlayFragment.BG_LIGHT);
        setFadingEnabled(true);

        setUpRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.v(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new ItemViewClickedListener());

        mMediaController.getTransportControls().play();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMediaController = getActivity().getMediaController();
        Log.d(TAG, "register callback of mediaController");
        if(mMediaController == null){
            Log.e(TAG, "mMediaController is null");
        }
        mMediaController.registerCallback(mMediaControllerCallback);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDetach() {
        if (mMediaController != null) {
            Log.d(TAG, "unregister callback of mediaController");
            mMediaController.unregisterCallback(mMediaControllerCallback);
        }
        super.onDetach();
    }

    @Override
    public void onStart() {
        startProgressAutomation();
        super.onStart();

    }

    @Override
    public void onStop() {
        mRowsAdapter = null;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        stopProgressAutomation();
        super.onDestroy();
    }

    private void setUpRows() {
        ClassPresenterSelector ps = new ClassPresenterSelector();

        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        //playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new DetailsDescriptionPresenter());
        playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new DescriptionPresenter());

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        /*
         * Add PlaybackControlsRow to mRowsAdapter, which makes video control UI.
         * PlaybackControlsRow is supposed to be first Row of mRowsAdapter.
         */
        addPlaybackControlsRow();
        /* add ListRow to second row of mRowsAdapter */
        addOtherRows();

        /* onClick */
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
                    /* PlayPause action */
                    if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY) {
                        mMediaController.getTransportControls().play();
                    } else if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE) {
                        mMediaController.getTransportControls().pause();
                    }
                } else if (action.getId() == mSkipNextAction.getId()) {
                    /* SkipNext action */
                    mMediaController.getTransportControls().skipToNext();
                } else if (action.getId() == mSkipPreviousAction.getId()) {
                    /* SkipPrevious action */
                    mMediaController.getTransportControls().skipToPrevious();
                } else if (action.getId() == mFastForwardAction.getId()) {
                    /* FastForward action  */
                    mMediaController.getTransportControls().fastForward();
                } else if (action.getId() == mRewindAction.getId()) {
                    /* Rewind action */
                    mMediaController.getTransportControls().rewind();
                }
                if (action instanceof PlaybackControlsRow.MultiAction) {
                    /* Following action is subclass of MultiAction
                     * - PlayPauseAction
                     * - FastForwardAction
                     * - RewindAction
                     * - ThumbsAction
                     * - RepeatAction
                     * - ShuffleAction
                     * - HighQualityAction
                     * - ClosedCaptioningAction
                     */
                    notifyChanged(action);

                    /* Change icon */
                    if (action instanceof PlaybackControlsRow.ThumbsUpAction ||
                            action instanceof PlaybackControlsRow.ThumbsDownAction ||
                            action instanceof PlaybackControlsRow.RepeatAction ||
                            action instanceof PlaybackControlsRow.ShuffleAction ||
                            action instanceof PlaybackControlsRow.HighQualityAction ||
                            action instanceof PlaybackControlsRow.ClosedCaptioningAction) {
                        ((PlaybackControlsRow.MultiAction) action).nextIndex();
                    }
                }
            }
        });

        setAdapter(mRowsAdapter);

    }


    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0 || getView().getWidth() == 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        Log.d(TAG, "startProgressAutomation");
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int updatePeriod = getUpdatePeriod();

                    //int currentTime = mPlaybackControlsRow.getCurrentTime(); // + updatePeriod;
                    int currentTime = mPlaybackController.getCurrentPosition();
                    int totalTime = mPlaybackControlsRow.getTotalTime();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    mPlaybackControlsRow.setBufferedProgress(mPlaybackController.calcBufferedTime(currentTime));

                    //mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
                    //Log.v(TAG, "currenttime " + currentTime);
                    //Log.v(TAG, "totalTime " + totalTime);
                    //Log.v(TAG, "updatePeriod " + updatePeriod);
                    //Log.v(TAG, "BufferPercentage " + mPlaybackController.getBufferPercentage());
                    //Log.v(TAG, "duration: " + mPlaybackController.getDuration());
                    //Log.v(TAG, "bufferedtime " + mPlaybackController.calcBufferedTime(currentTime));

                    if (totalTime > 0 && totalTime <= currentTime + VIDEO_PLAY_FINISHED_MARGIN) {
                        // stopProgressAutomation();
                        mMediaController.getTransportControls().skipToNext();
                        //next(true);
                    } else {
                        // mHandler.postDelayed(this, updatePeriod);
                    }
                    mHandler.postDelayed(this, updatePeriod);
                }
            };
            mHandler.postDelayed(mRunnable, getUpdatePeriod());
        }
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
    }

    private void addPlaybackControlsRow() {
        /* movieItem data must have newly allocated area, do not refer existing movie (ex. mItems.get(0))
           since this item will be overwritten later */
        Movie movieItem = new Movie();
        mPlaybackControlsRow = new PlaybackControlsRow(movieItem);
        mRowsAdapter.add(mPlaybackControlsRow);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);
        
        Activity activity = getActivity();
        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(activity);
        mRepeatAction = new PlaybackControlsRow.RepeatAction(activity);
        mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(activity);
        mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(activity);
        mShuffleAction = new PlaybackControlsRow.ShuffleAction(activity);
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(activity);
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(activity);
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(activity);
        mRewindAction = new PlaybackControlsRow.RewindAction(activity);
        mHighQualityAction = new PlaybackControlsRow.HighQualityAction(activity);
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(activity);
        mMoreActions = new PlaybackControlsRow.MoreActions(activity);

        /* PrimaryAction setting */
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);

        /* SecondaryAction setting */
        mSecondaryActionsAdapter.add(mThumbsUpAction);
        mSecondaryActionsAdapter.add(mThumbsDownAction);
        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        mSecondaryActionsAdapter.add(mHighQualityAction);
        mSecondaryActionsAdapter.add(mClosedCaptioningAction);
        mSecondaryActionsAdapter.add(mMoreActions);

        updatePlaybackRow(mPlaybackController.getCurrentItem());
        mPlaybackController.updateMetadata();
    }

    private void addOtherRows() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for(Movie movie : mItems) {
            listRowAdapter.add(movie);
        }

        HeaderItem header = new HeaderItem(0, "Other Movies");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    public void updatePlaybackRow(int index) {
        Log.d(TAG, "updatePlaybackRow index: " + index);
        if (mPlaybackControlsRow.getItem() != null) {
            Movie item = (Movie) mPlaybackControlsRow.getItem();
            item.setTitle(mItems.get(index).getTitle());
            item.setStudio(mItems.get(index).getStudio());

            mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
            /* total time is necessary to show video playing time progress bar */
            int duration = (int) Utils.getDuration(mItems.get(index).getVideoUrl());
            mPlaybackController.setDuration(duration);
            Log.i(TAG, "videoUrl: " + mItems.get(index).getVideoUrl());
            Log.i(TAG, "duration = " + duration);
            mPlaybackControlsRow.setTotalTime(duration);
            mPlaybackControlsRow.setCurrentTime(0);
            mPlaybackControlsRow.setBufferedProgress(0);
        } else {
            Log.e(TAG, "mPlaybackControlsRow.getItem is null!");
        }
        if (SHOW_IMAGE) {
            mPlaybackControlsRowTarget = new PicassoPlaybackControlsRowTarget(mPlaybackControlsRow);
            updateVideoImage(mItems.get(index).getCardImageURI());
        }
    }

    private void updateMovieView(String title, String studio, String cardImageUrl, long duration) {
        Log.d(TAG, "updateMovieView");

        if (mPlaybackControlsRow.getItem() != null) {
            Movie item = (Movie) mPlaybackControlsRow.getItem();
            item.setTitle(title);
            item.setStudio(studio);
        } else {
            Log.e(TAG, "mPlaybackControlsRow.getItem is null!");
        }
        mPlaybackControlsRow.setTotalTime((int) duration);
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());

        // Show the video card image if there is enough room in the UI for it.
        // If you have many primary actions, you may not have enough room.
        if (SHOW_IMAGE) {
            mPlaybackControlsRowTarget = new PicassoPlaybackControlsRowTarget(mPlaybackControlsRow);
            updateVideoImage(cardImageUrl);
        }
    }

    /* For cardImage loading to playbackRow */
    public class PicassoPlaybackControlsRowTarget implements Target {
        PlaybackControlsRow mPlaybackControlsRow;

        public PicassoPlaybackControlsRowTarget(PlaybackControlsRow playbackControlsRow) {
            mPlaybackControlsRow = playbackControlsRow;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(sContext.getResources(), bitmap);
            mPlaybackControlsRow.setImageDrawable(bitmapDrawable);
            mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mPlaybackControlsRow.setImageDrawable(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }

    protected void updateVideoImage(URI uri) {
        Picasso.with(sContext)
                .load(uri.toString())
                .resize(Utils.convertDpToPixel(sContext, CARD_WIDTH),
                        Utils.convertDpToPixel(sContext, CARD_HEIGHT))
                .into(mPlaybackControlsRowTarget);
        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
    }

    protected void updateVideoImage(String url) {
        try {
            URI uri = new URI(url);
            updateVideoImage(uri);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(final PlaybackState state) {
            Log.d(TAG, "playback state changed: " + state.getState());
            Log.d(TAG, "playback state changed: " + state.toString());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (state.getState() == PlaybackState.STATE_PLAYING) {
                        mPlaybackController.setCurrentPlaybackState(PlaybackState.STATE_PLAYING);
                        startProgressAutomation();
                        // setFadingEnabled(false);
                        mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
                        mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
                        notifyChanged(mPlayPauseAction);
                    } else if (state.getState() == PlaybackState.STATE_PAUSED) {
                        mPlaybackController.setCurrentPlaybackState(PlaybackState.STATE_PAUSED);
                        // stopProgressAutomation();
                        // setFadingEnabled(false);
                        mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
                        mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
                        notifyChanged(mPlayPauseAction);
                    }

                    int currentTime = (int) state.getPosition();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    // mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
                    mPlaybackControlsRow.setBufferedProgress(mPlaybackController.calcBufferedTime(currentTime));

                }
            });
        }

        @Override
        public void onMetadataChanged(final MediaMetadata metadata) {
            Log.d(TAG, "received update of media metadata");
                    updateMovieView(
                            metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
                            metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                            metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
                            metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
                    );
        }
    }

}

