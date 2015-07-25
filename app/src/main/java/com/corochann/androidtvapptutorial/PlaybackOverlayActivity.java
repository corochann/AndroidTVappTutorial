package com.corochann.androidtvapptutorial;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import java.util.ArrayList;


public class PlaybackOverlayActivity extends Activity {

    private static final String TAG = PlaybackOverlayActivity.class.getSimpleName();
    public static final String AUTO_PLAY = "auto_play";

    private VideoView mVideoView;
    private ArrayList<Movie> mItems = new ArrayList<Movie>();

    //private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;
    private int mPosition = 0;
    private long mStartTimeMillis;
    private long mDuration = -1;

    private int mCurrentItem;

    private Movie mSelectedMovie;

    public PlaybackController getmPlaybackController() {
        return mPlaybackController;
    }

    private PlaybackController mPlaybackController;

    private Handler mHandler;

    /*
     * List of various states that we can be in
     */
/*
    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, IDLE
    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        /* NOTE: setMediaController (in createMediaSession) must be executed
         * BEFORE inflating Fragment!
         */
        mPlaybackController = new PlaybackController(this);
        setContentView(R.layout.activity_playback_overlay);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mPlaybackController.setVideoView(mVideoView);


        // mHandler = new Handler();

        // mItems = MovieProvider.getMovieItems();
        mSelectedMovie = (Movie) getIntent().getSerializableExtra(DetailsActivity.MOVIE);
        mCurrentItem = (int) mSelectedMovie.getId() - 1;

        mPlaybackController.setMovie(mSelectedMovie);
        mPlaybackController.setCurrentItem(mCurrentItem);
        mPlaybackController.playPause(true);
        // loadViews();
        // playPause(true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
       stopPlayback();
        mVideoView.suspend();
        mVideoView.setVideoURI(null);
        // mSession.release();
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);

        setVideoPath(mSelectedMovie.getVideoUrl());
    }

    public void setVideoPath(String videoUrl) {
        mPlaybackController.setPosition(0);
        mVideoView.setVideoPath(videoUrl);
        mStartTimeMillis = 0;
        mDuration = Utils.getDuration(videoUrl);
    }

    private void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

/*    private void setPosition(int position) {
        if (position > mDuration) {
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
    }*/

/*    public void setPlaybackState(LeanbackPlaybackState playbackState) {
        this.mPlaybackState = playbackState;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback_overlay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            if (!requestVisibleBehind(true)) {
            // Try to play behind launcher, but if it fails, stop playback.
            //playPause(false);
        }
        } else {
            requestVisibleBehind(false);
        }
    }

/*
    public void playPause(boolean doPlay) {
        if (mPlaybackState == LeanbackPlaybackState.IDLE) {
            /*/
/* Callbacks for mVideoView *//*

            setupCallbacks();
        }

        if (doPlay && mPlaybackState != LeanbackPlaybackState.PLAYING) {
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            if (mPosition > 0) {
                mVideoView.seekTo(mPosition);
            }
            mVideoView.start();
            mStartTimeMillis = System.currentTimeMillis();
        } else {
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            int timeElapsedSinceStart = (int) (System.currentTimeMillis() - mStartTimeMillis);
            setPosition(mPosition + timeElapsedSinceStart);
            mVideoView.pause();
        }

        updatePlaybackState();*//*

    }

/*
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

    private void updatePlaybackState() {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mPlaybackState == LeanbackPlaybackState.PAUSED || mPlaybackState == LeanbackPlaybackState.IDLE) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, mPosition, 1.0f);
        // mSession.setPlaybackState(stateBuilder.build());
    }
*/

/*
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
*/

}

