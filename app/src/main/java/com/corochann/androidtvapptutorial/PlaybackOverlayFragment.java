package com.corochann.androidtvapptutorial;

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
import android.os.Message;
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
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import junit.framework.Assert;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by corochann on 7/7/2015.
 */
public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {

    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();
    private static final int SIMULATED_BUFFERED_TIME = 10000;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 240;
    private static final boolean SHOW_IMAGE = true;

    private static Context sContext;
    private PlaybackOverlayActivity activity;

    private Movie mSelectedMovie;
    private PlaybackController mPlaybackController;
    private VideoView mVideoView;

    public PlaybackControlsRow getmPlaybackControlsRow() {
        return mPlaybackControlsRow;
    }

    private PlaybackControlsRow mPlaybackControlsRow;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    //private int mCurrentPlaybackState;
    private Handler mHandler;
    private Runnable mRunnable;
    private ArrayList<Movie> mItems = new ArrayList<Movie>();
    private ArrayObjectAdapter mRowsAdapter;

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
    private int mCurrentItem;
    private PicassoPlaybackControlsRowTarget mPlaybackControlsRowTarget;

    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback = new MediaControllerCallback();

    public static PlaybackControlsRowHandler mPlaybackControlsRowHandler = null;
    /* To check if this Fragment is top or not (to decide update UI) */
    private static boolean active = false;

    public static PlaybackOverlayFragment playbackOverlayFragmentInstance;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);




        sContext = getActivity();
        activity = (PlaybackOverlayActivity) getActivity();
        mHandler = new Handler();



        mPlaybackControlsRowHandler = new PlaybackControlsRowHandler();
        mPlaybackController = ((PlaybackOverlayActivity) getActivity()).getmPlaybackController();

        playbackOverlayFragmentInstance = this;

        setBackgroundType(PlaybackOverlayFragment.BG_LIGHT);
        setFadingEnabled(true);

        mItems = MovieProvider.getMovieItems();
        //mCurrentItem = (int) mSelectedMovie.getId() - 1;

        //mPlaybackController.setMovie(mSelectedMovie);
        mPlaybackController.setCurrentItem(mCurrentItem);
        mPlaybackController.setUiHandler(mPlaybackControlsRowHandler);

        mPlaybackController.playPause(true);

        setUpRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.v(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new ItemViewClickedListener());
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
        this.active = true;
        super.onStart();

    }

    @Override
    public void onStop() {
        mPlaybackControlsRowHandler = null;
        this.active = false;

        stopProgressAutomation();
        mRowsAdapter = null;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            if (!getActivity().requestVisibleBehind(true)) {
                // Try to play behind launcher, but if it fails, stop playback.
                mMediaController.getTransportControls().pause();
            }
        } else {
            getActivity().requestVisibleBehind(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoView.suspend();
        mVideoView.setVideoURI(null);
        mPlaybackController.releaseMediaSession();
    }

    private void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }


    public static boolean isActive () {
        return active;
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

                    //togglePlayback(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY);
                } else if (action.getId() == mSkipNextAction.getId()) {
                    /* SkipNext action */
                    mMediaController.getTransportControls().skipToNext();
                    // next(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
                } else if (action.getId() == mSkipPreviousAction.getId()) {
                    /* SkipPrevious action */
                    mMediaController.getTransportControls().skipToPrevious();
                    // prev(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
                } else if (action.getId() == mFastForwardAction.getId()) {
                    /* FastForward action  */
                    mMediaController.getTransportControls().fastForward();
                    // fastForward();
                } else if (action.getId() == mRewindAction.getId()) {
                    /* Rewind action */
                    mMediaController.getTransportControls().rewind();
                    // rewind();
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

    private void fastForward() {
        /* Video control part */
        mMediaController.getTransportControls().fastForward();

        /* UI part */
        int currentTime = mPlaybackController.getPosition();
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
    }

    private void rewind() {
        /* Video control part */
        mMediaController.getTransportControls().rewind();

        /* UI part */
        int currentTime = mPlaybackController.getPosition();
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
    }

    private void next(boolean autoPlay) {
        /* Video control part */
        //mMediaController.getTransportControls().playFromMediaId(Long.toString(mItems.get(mCurrentItem).getId()), bundle);
        mMediaController.getTransportControls().skipToNext();

        /* UI part */
        playbackStateChanged();
        updatePlaybackRow(mCurrentItem);
    }

    private void prev(boolean autoPlay) {
        /* Video control part */
        mMediaController.getTransportControls().skipToPrevious();

        /* UI part */
        playbackStateChanged();
        updatePlaybackRow(mCurrentItem);
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

    public void playbackStateChanged() {

        if (mPlaybackController.getmCurrentPlaybackState() != PlaybackState.STATE_PLAYING) {
            mPlaybackController.setmCurrentPlaybackState(PlaybackState.STATE_PLAYING);
            startProgressAutomation();
            setFadingEnabled(true);
            mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
            notifyChanged(mPlayPauseAction);
        } else if (mPlaybackController.getmCurrentPlaybackState() != PlaybackState.STATE_PAUSED) {
            mPlaybackController.setmCurrentPlaybackState(PlaybackState.STATE_PAUSED);
            stopProgressAutomation();
            //setFadingEnabled(false); // if set to false, PlaybackcontrolsRow will always be on the screen
            mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
            notifyChanged(mPlayPauseAction);
        }

        int currentTime = mPlaybackController.getPosition();
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

    }

    private void togglePlayback(boolean playPause) {
        /* Video control part */
        if (playPause) {
            mMediaController.getTransportControls().play();
        } else {
            mMediaController.getTransportControls().pause();
        }

        /* UI control part */
        playbackStateChanged();
    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0 || getView().getWidth() == 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int updatePeriod = getUpdatePeriod();
                    int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                    int totalTime = mPlaybackControlsRow.getTotalTime();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

                    if (totalTime > 0 && totalTime <= currentTime) {
                        stopProgressAutomation();
                        next(true);
                    } else {
                        mHandler.postDelayed(this, updatePeriod);
                    }
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
        mPlaybackControlsRow = new PlaybackControlsRow(mSelectedMovie);
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

        updatePlaybackRow(mCurrentItem);
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
        Log.d(TAG, "updatePlaybackRow");
        if (mPlaybackControlsRow.getItem() != null) {
            Movie item = (Movie) mPlaybackControlsRow.getItem();
            item.setTitle(mItems.get(index).getTitle());
            item.setStudio(mItems.get(index).getStudio());

            mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
            /* total time is necessary to show video playing time progress bar */
            int duration = (int) Utils.getDuration(mItems.get(index).getVideoUrl());
            Log.i(TAG, "videoUrl: " + mItems.get(index).getVideoUrl());
            Log.i(TAG, "duration = " + duration);
            mPlaybackControlsRow.setTotalTime(duration);
            mPlaybackControlsRow.setCurrentTime(0);
            mPlaybackControlsRow.setBufferedProgress(0);
        }
        if (SHOW_IMAGE) {
            mPlaybackControlsRowTarget = new PicassoPlaybackControlsRowTarget(mPlaybackControlsRow);
            updateVideoImage(mItems.get(mCurrentItem).getCardImageURI());
        }
    }

    /* For cardImage loading to playbackRow */
    public static class PicassoPlaybackControlsRowTarget implements Target {
        PlaybackControlsRow mPlaybackControlsRow;

        public PicassoPlaybackControlsRowTarget(PlaybackControlsRow playbackControlsRow) {
            mPlaybackControlsRow = playbackControlsRow;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(sContext.getResources(), bitmap);
            mPlaybackControlsRow.setImageDrawable(bitmapDrawable);
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
        public void onPlaybackStateChanged(PlaybackState state) {
            Log.d(TAG, "playback state changed: " + state.getState());
            Log.d(TAG, "playback state changed: " + state.toString());
            if (state.getState() == PlaybackState.STATE_PLAYING && mPlaybackController.getmCurrentPlaybackState() != PlaybackState.STATE_PLAYING) {
                mPlaybackController.setmCurrentPlaybackState(PlaybackState.STATE_PLAYING);
                startProgressAutomation();
                setFadingEnabled(true);
                mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
                mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
                notifyChanged(mPlayPauseAction);
            } else if (state.getState() == PlaybackState.STATE_PAUSED && mPlaybackController.getmCurrentPlaybackState() != PlaybackState.STATE_PAUSED) {
                mPlaybackController.setmCurrentPlaybackState(PlaybackState.STATE_PAUSED);
                stopProgressAutomation();
                setFadingEnabled(false);
                mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
                mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
                notifyChanged(mPlayPauseAction);
            }

            int currentTime = (int) state.getPosition();
            mPlaybackControlsRow.setCurrentTime(currentTime);
            mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            Log.d(TAG, "received update of media metadata");
/*
            updateMovieView(
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
                    metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
            );
*/
        }
    }

    /**
     * Handles the message queue from PlaybackController to update UI when user press Media key
     */
    public class PlaybackControlsRowHandler extends Handler {
        private final String TAG = PlaybackControlsRowHandler.class.getSimpleName();

        private PlaybackControlsRowHandler() {
            super(sContext.getMainLooper());
        }

        public PlaybackControlsRowHandler getInstance () {
            if (mPlaybackControlsRowHandler == null) {
                mPlaybackControlsRowHandler = new PlaybackControlsRowHandler();
            }
            return mPlaybackControlsRowHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            Assert.assertNotNull("msg is null!", msg);
            Integer code = msg.what;
            Log.d(TAG, "code: " + code);

            Bundle b = msg.getData();
            if (b == null){
                Log.d(TAG, "this msg has no bundle data...");
            }

            switch(code) {
                case PlaybackController.MSG_PLAY :

                case PlaybackController.MSG_PAUSE :
                default:
                    playbackStateChanged();
                    break;
            }
        }
    }


}

