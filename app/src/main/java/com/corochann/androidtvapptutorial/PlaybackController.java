package com.corochann.androidtvapptutorial;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.util.Log;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

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

    /* Attributes */
    private Activity mActivity;
    private MediaSession mSession;
    private MediaSessionCallback mMediaSessionCallback;
    private VideoView mVideoView;
    private static final ArrayList<Movie> mItems =  MovieProvider.getMovieItems(); // new ArrayList<Movie>();

    /* Global variables */
    private int mCurrentPlaybackState = PlaybackState.STATE_NONE;
    private int mCurrentItem; // index of current item
    private int mPosition = 0;
    private long mStartTimeMillis;
    private long mDuration = -1;

    public int getCurrentPlaybackState() {
        return mCurrentPlaybackState;
    }

    public void setCurrentPlaybackState(int currentPlaybackState) {
        this.mCurrentPlaybackState = currentPlaybackState;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public PlaybackController(Activity activity) {
        mActivity = activity;
        // mVideoView = (VideoView) activity.findViewById(VIDEO_VIEW_RESOURCE_ID);
        createMediaSession(mActivity);
        // mItems = MovieProvider.getMovieItems();
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

    public MediaSessionCallback getMediaSessionCallback() {
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

    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }


    private void updatePlaybackState() {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mCurrentPlaybackState == PlaybackState.STATE_PAUSED || mCurrentPlaybackState == PlaybackState.STATE_NONE) {
            state = PlaybackState.STATE_PAUSED;
        }
        // stateBuilder.setState(state, mPosition, 1.0f);
        stateBuilder.setState(state, getCurrentPosition(), 1.0f);
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

    /**
     * should be called Activity's onDestroy
     */
    public void finishPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView.suspend();
            mVideoView.setVideoURI(null);
        }
        releaseMediaSession();
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
            }
            setPosition(mVideoView.getCurrentPosition());
            mVideoView.pause();

        }

        updatePlaybackState();
    }

    public void fastForward() {
        if (mDuration != -1) {
            // Fast forward 10 seconds.
            setPosition(getCurrentPosition() + (10 * 1000));
            mVideoView.seekTo(mPosition);
        }

    }

    public void rewind() {
        // rewind 10 seconds
        setPosition(getCurrentPosition() - (10 * 1000));
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

    public void updateMetadata() {
        Log.i(TAG, "updateMetadata: getCurrentItem" + getCurrentItem());
        Movie movie = mItems.get(getCurrentItem());
        mDuration = Utils.getDuration(movie.getVideoUrl());
        updateMetadata(movie);
    }

    public void updateMetadata(Movie movie) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

        String title = movie.getTitle().replace("_", " -");

        metadataBuilder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, Long.toString(movie.getId()));
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, movie.getStudio());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, movie.getDescription());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, movie.getCardImageUrl());
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, mDuration);

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, movie.getStudio());

        Glide.with(mActivity)
                .load(Uri.parse(movie.getCardImageUrl()))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {

                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                        mSession.setMetadata(metadataBuilder.build());
                    }
                });
    }


    public void releaseMediaSession() {
        if(mSession != null) {
            mSession.release();
        }
    }

    private class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            playPause(true);
        }

        @Override
        public void onPause() {
            playPause(false);
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
                updateMetadata();
                playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
            } else {
                Log.e(TAG, "onSkipToNext movie is null!");
            }

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
                updateMetadata();
                playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
            } else {
                Log.e(TAG, "onSkipToPrevious movie is null!");
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            mCurrentItem = Integer.parseInt(mediaId);
            Movie movie = mItems.get(mCurrentItem);
            //Movie movie = VideoProvider.getMovieById(mediaId);
            if (movie != null) {
                setVideoPath(movie.getVideoUrl());
                // mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                // updateMetadata(movie);
                updateMetadata();
                playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            setPosition((int) pos);
            mVideoView.seekTo(mPosition);
            updatePlaybackState();
        }

        @Override
        public void onFastForward() {
            fastForward();
        }

        @Override
        public void onRewind() {
            rewind();
        }
    }

}