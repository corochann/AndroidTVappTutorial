package com.corochann.androidtvapptutorial;

import java.util.ArrayList;

/**
 * Created by corochann on 21/7/2015.
 */
public class MovieProvider {

    private static final String TAG = MovieProvider.class.getSimpleName();

    private static ArrayList<Movie> mItems = null;
    
    private MovieProvider() {}

    public static ArrayList<Movie> getMovieItems() {
        if(mItems == null) {
            mItems = new ArrayList<Movie>();

            Movie movie1 = new Movie();
            movie1.setId(1);
            movie1.setTitle("Title1");
            movie1.setStudio("studio1");
            movie1.setDescription("description1");
            movie1.setCardImageUrl("http://heimkehrend.raindrop.jp/kl-hacker/wp-content/uploads/2014/08/DSC02580.jpg");
            //movie1.setVideoUrl("http://corochann.com/wp-content/uploads/2015/07/MVI_0949.mp4");
            movie1.setVideoUrl("http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4");
            mItems.add(movie1);

            Movie movie2 = new Movie();
            movie2.setId(2);
            movie2.setTitle("Title2");
            movie2.setStudio("studio2");
            movie2.setDescription("description2");
            movie2.setCardImageUrl("http://heimkehrend.raindrop.jp/kl-hacker/wp-content/uploads/2014/08/DSC02630.jpg");
            //movie2.setVideoUrl("http://corochann.com/wp-content/uploads/2015/07/MVI_0962.mp4");
            movie2.setVideoUrl("http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4");
            mItems.add(movie2);

            Movie movie3 = new Movie();
            movie3.setId(3);
            movie3.setTitle("Title3");
            movie3.setStudio("studio3");
            movie3.setDescription("description3");
            movie3.setCardImageUrl("http://heimkehrend.raindrop.jp/kl-hacker/wp-content/uploads/2014/08/DSC02529.jpg");
            movie3.setVideoUrl("http://corochann.com/wp-content/uploads/2015/07/MVI_1112.mp4");
            mItems.add(movie3);
        }
        return mItems;
    }

}