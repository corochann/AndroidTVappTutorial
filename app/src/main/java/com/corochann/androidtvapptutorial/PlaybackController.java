package com.corochann.androidtvapptutorial;

import android.app.Activity;
import android.drm.DrmStore;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.util.Log;
import android.widget.VideoView;

import java.util.ArrayList;

/**
 * Created by corochann on 24/7/2015.
 * PlaybackController
 * - owns MediaSession
 * - owns VideoView specified from activity
 * and
 * - manages Movielist
 * - handles media button action
 */
public class PlaybackController {

    /* Constants */
    private static final String TAG = PlaybackController.class.getSimpleName();
    private static final String MEDIA_SESSION_TAG = "AndroidTVappTutorialSession";

    public static final int MSG_STOP = 0;
    public static final int MSG_PAUSE = 1;
    public static final int MSG_PLAY = 2;
    public static final int MSG_REWIND = 3;
    public static final int MSG_SKIP_TO_PREVIOUS = 4;
    public static final int MSG_SKIP_TO_NEXT = 5;
    public static final int MSG_FAST_FORWARD = 6;
    public static final int MSG_SET_RATING = 7;
    public static final int MSG_SEEK_TO = 8;
    public static final int MSG_PLAY_PAUSE = 9;
    public static final int MSG_PLAY_FROM_MEDIA_ID = 10;
    public static final int MSG_PLAY_FROM_SEARCH = 11;
    public static final int MSG_SKIP_TO_QUEUE_ITEM = 12;



    // private static final int VIDEO_VIEW_RESOURCE_ID = R.id.videoView;

/*private static PlaybackController mPlaybackController = null;*/
    /* Attributes */
    private Activity mActivity;
    private MediaSession mSession;
    private MediaSessionCallback mMediaSessionCallback;
    private Handler mUiHandler; // to update UI of Activity/Fragment
    private VideoView mVideoView;
    private ArrayList<Movie> mItems = new ArrayList<Movie>();

    public int getmCurrentPlaybackState() {
        return mCurrentPlaybackState;
    }

    public void setmCurrentPlaybackState(int mCurrentPlaybackState) {
        this.mCurrentPlaybackState = mCurrentPlaybackState;
    }

    /* Global variables */
    private int mCurrentPlaybackState = PlaybackState.STATE_NONE;
    private int mPosition = 0;
    private long mStartTimeMillis;

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public long getmDuration() {
        return mDuration;
    }

    private long mDuration = -1;

    public int getmCurrentItem() {
        return mCurrentItem;
    }

    private int mCurrentItem; // index of current item


    public PlaybackController(Activity activity) {
        mActivity = activity;
        // mVideoView = (VideoView) activity.findViewById(VIDEO_VIEW_RESOURCE_ID);
        createMediaSession(activity);
        mItems = MovieProvider.getMovieItems();
    }

    private void createMediaSession(Activity activity) {
        if (mSession == null) {
            mSession = new MediaSession(activity, MEDIA_SESSION_TAG);
            mMediaSessionCallback = new MediaSessionCallback();
            mSession.setCallback(mMediaSessionCallback);
            mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mSession.setActive(true);
            activity.setMediaController(new MediaController(activity, mSession.getSessionToken()));
        }
    }

    public MediaSessionCallback getmMediaSessionCallback() {
        return mMediaSessionCallback;
    }


    public void setVideoView (VideoView videoView) {
        mVideoView = videoView;

        /* Callbacks for mVideoView */
        setupCallbacks();

    }

    public void setMovie (Movie movie) {
        // Log.v(TAG, "setMovie: " + movie.toString());
        mVideoView.setVideoPath(movie.getVideoUrl());
    }


    public void setUiHandler (Handler handler) {
        mUiHandler = handler;
    }

    public void setVideoPath(String videoUrl) {
        setPosition(0);
        mVideoView.setVideoPath(videoUrl);
        mStartTimeMillis = 0;
        mDuration = Utils.getDuration(videoUrl);
    }

    public void setPosition(int position) {
        if (position > mDuration) {
            Log.d(TAG, "position: " + position + ", mDuration: " + mDuration);
            mPosition = (int) mDuration;
        } else if (position < 0) {
            mPosition = 0;
            mStartTimeMillis = System.currentTimeMillis();
        } else {
            mPosition = position;
        }
        mStartTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "position set to " + mPosition);
    }

    public int getPosition() {
        return mPosition;
    }


    private void updatePlaybackState() {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mCurrentPlaybackState == PlaybackState.STATE_PAUSED || mCurrentPlaybackState == PlaybackState.STATE_NONE) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, mPosition, 1.0f);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PAUSE |
                PlaybackState.ACTION_PLAY_PAUSE |
                PlaybackState.ACTION_REWIND |
                PlaybackState.ACTION_FAST_FORWARD |
                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                PlaybackState.ACTION_SKIP_TO_NEXT |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;
        return actions;
    }

    public void playPause(boolean doPlay) {

        if (mCurrentPlaybackState == PlaybackState.STATE_NONE) {
            /* Callbacks for mVideoView */
            setupCallbacks();
        }

        //if (doPlay && mCurrentPlaybackState != PlaybackState.STATE_PLAYING) {
        if (doPlay) { // Play
            Log.d(TAG, "playPause: play");
            if(mCurrentPlaybackState == PlaybackState.STATE_PLAYING) {
                /* if current state is already playing, do nothing */
                return;
            } else {
                mCurrentPlaybackState = PlaybackState.STATE_PLAYING;
                if (mPosition > 0) {
                    mVideoView.seekTo(mPosition);
                }
                mVideoView.seekTo(mPosition);
                mVideoView.start();
                mStartTimeMillis = System.currentTimeMillis();
            }
        } else { // Pause
            Log.d(TAG, "playPause: pause");
            if(mCurrentPlaybackState == PlaybackState.STATE_PAUSED) {
                /* if current state is already paused, do nothing */
                return;
            } else {
                mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                int timeElapsedSinceStart = (int) (System.currentTimeMillis() - mStartTimeMillis);
                Log.d(TAG, "timeElapsedSinceStart" + timeElapsedSinceStart);
                setPosition(mPosition + timeElapsedSinceStart);
                mVideoView.pause();
            }
        }

        updatePlaybackState();
    }

    public void fastForward() {
        if (mDuration != -1) {
            // Fast forward 10 seconds.
            setPosition(mVideoView.getCurrentPosition() + (10 * 1000));
            mVideoView.seekTo(mPosition);
        }

    }

    public void rewind() {
        // rewind 10 seconds
        setPosition(mVideoView.getCurrentPosition() - (10 * 1000));
        mVideoView.seekTo(mPosition);
    }

    public int getBufferPercentage() {
        return mVideoView.getBufferPercentage();
    }

    public int calcBufferedTime(int currentTime) {
        int bufferedTime;
        bufferedTime = currentTime + (int) ((mDuration - currentTime) * getBufferPercentage()) / 100;
        return bufferedTime;
    }

    private void setupCallbacks() {

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoView.stopPlayback();
                mCurrentPlaybackState = PlaybackState.STATE_NONE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mCurrentPlaybackState == PlaybackState.STATE_PLAYING) {
                    mVideoView.start();
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mCurrentPlaybackState = PlaybackState.STATE_NONE;
            }
        });
    }

    public void setCurrentItem(int currentItem) {
        Log.v(TAG, "setCurrentItem: " + currentItem);
        this.mCurrentItem = currentItem;
    }

    public void releaseMediaSession() {
        mSession.release();
    }

    private class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            playPause(true);

            Message msg = Message.obtain();
            msg.what = MSG_PLAY;
            mUiHandler.sendMessage(msg);
            /*if (PlaybackOverlayFragment.isActive()) {
                PlaybackOverlayFragment.playbackOverlayFragmentInstance.playbackStateChanged();
            }
*/
        }

        @Override
        public void onPause() {
            playPause(false);

            Message msg = Message.obtain();
            msg.what = MSG_PAUSE;
            mUiHandler.sendMessage(msg);
        }

        @Override
        public void onSkipToNext() {
            if (++mCurrentItem >= mItems.size()) { // Current Item is set to next here
                mCurrentItem = 0;
            }

            Movie movie = mItems.get(mCurrentItem);
            //Movie movie = VideoProvider.getMovieById(mediaId);
            if (movie != null) {
                setVideoPath(movie.getVideoUrl());
                //mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                //updateMetadata(movie);
                playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
            }

/*
            if (PlaybackOverlayFragment.isActive()) {
                PlaybackOverlayFragment.playbackOverlayFragmentInstance.playbackStateChanged();
                PlaybackOverlayFragment.playbackOverlayFragmentInstance.updatePlaybackRow(mCurrentItem);
                //updatePlaybackRow(mCurrentItem);
            }
*/

            Message msg = Message.obtain();
            msg.what = MSG_SKIP_TO_NEXT;
            mUiHandler.sendMessage(msg);
        }


        @Override
        public void onSkipToPrevious() {
            if (--mCurrentItem < 0) { // Current Item is set to previous here
                mCurrentItem = mItems.size()-1;
            }

            Movie movie = mItems.get(mCurrentItem);
            //Movie movie = VideoProvider.getMovieById(mediaId);
            if (movie != null) {
                setVideoPath(movie.getVideoUrl());
                //mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                //updateMetadata(movie);
                playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
            }

/*            if (PlaybackOverlayFragment.isActive()) {
                PlaybackOverlayFragment.playbackOverlayFragmentInstance.playbackStateChanged();
                PlaybackOverlayFragment.playbackOverlayFragmentInstance.updatePlaybackRow(mCurrentItem);
            }*/

            Message msg = Message.obtain();
            msg.what = MSG_SKIP_TO_PREVIOUS;
            mUiHandler.sendMessage(msg);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Movie movie = mItems.get(Integer.parseInt(mediaId));
            //Movie movie = VideoProvider.getMovieById(mediaId);
            if (movie != null) {
                setVideoPath(movie.getVideoUrl());
                mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                //updateMetadata(movie);
                //playPause(extras.getBoolean(AUTO_PLAY));
            }
        }

        @Override
        public void onSeekTo(long pos) {
            setPosition((int) pos);
            mVideoView.seekTo(mPosition);
            updatePlaybackState();

            Message msg = Message.obtain();
            msg.what = MSG_SEEK_TO;
            mUiHandler.sendMessage(msg);
        }

        @Override
        public void onFastForward() {
            fastForward();

            Message msg = Message.obtain();
            msg.what = MSG_FAST_FORWARD;
            mUiHandler.sendMessage(msg);
        }

        @Override
        public void onRewind() {
            rewind();

            Message msg = Message.obtain();
            msg.what = MSG_REWIND;
            mUiHandler.sendMessage(msg);
        }
    }

}