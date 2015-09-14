package com.corochann.androidtvapptutorial.recommendation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.corochann.androidtvapptutorial.R;
import com.corochann.androidtvapptutorial.model.Movie;
import com.corochann.androidtvapptutorial.ui.DetailsActivity;
import com.squareup.picasso.Picasso;

import java.net.URI;

public class RecommendationFactory {

    private static final String TAG = RecommendationFactory.class.getSimpleName();
    private static final int CARD_WIDTH = 500;
    private static final int CARD_HEIGHT = 500;
    private static final int BACKGROUND_WIDTH = 1920;
    private static final int BACKGROUND_HEIGHT = 1080;

    private Context mContext;
    private NotificationManager mNotificationManager;

    public RecommendationFactory(Context context) {
        mContext = context;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }


    public void recommend(int id, Movie movie) {
        recommend(id, movie, NotificationCompat.PRIORITY_DEFAULT);
    }

    /**
     * create a notification for recommending item of Movie class
     * @param movie
     */
    public void recommend(final int id, final Movie movie, final int priority) {
        Log.i(TAG, "recommend");
        /* Run in background thread, since bitmap loading must be done in background */
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "recommendation in progress");
                Bitmap backgroundBitmap = prepareBitmap(movie.getCardImageUrl(), BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
                Bitmap cardImageBitmap = prepareBitmap(movie.getCardImageUrl(), CARD_WIDTH, CARD_HEIGHT);
                PendingIntent intent = buildPendingIntent(movie, id);

                RecommendationBuilder recommendationBuilder = new RecommendationBuilder(mContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setBackground(backgroundBitmap)
                        .setId(id)
                        .setPriority(priority)
                        .setTitle(movie.getTitle())
                        .setDescription(movie.getDescription())
                        .setIntent(intent)
                        .setBitmap(cardImageBitmap)
                        .setFastLaneColor(mContext.getResources().getColor(R.color.fastlane_background));
                Notification recommendNotification = recommendationBuilder.build();
                mNotificationManager.notify(id, recommendNotification);
            }}).start();
    }

    /**
     * prepare bitmap from URL string
     * @param url
     * @return
     */
    public Bitmap prepareBitmap(String url, int width, int height) {
        Bitmap bitmap = null;
        try {
            URI uri = new URI(url);
            bitmap = Picasso.with(mContext)
                    .load(uri.toString())
                    .resize(width, height)
                    .get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return bitmap;
    }

    private PendingIntent buildPendingIntent(Movie movie, int id) {
        Intent detailsIntent = new Intent(mContext, DetailsActivity.class);
        detailsIntent.putExtra(DetailsActivity.MOVIE, movie);
        detailsIntent.putExtra(DetailsActivity.NOTIFICATION_ID, id);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(DetailsActivity.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailsIntent.setAction(Long.toString(movie.getId()));

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
