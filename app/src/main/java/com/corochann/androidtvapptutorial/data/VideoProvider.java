package com.corochann.androidtvapptutorial.data;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.corochann.androidtvapptutorial.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class VideoProvider {

    private static final String TAG = VideoProvider.class.getSimpleName();
    public  static final String VIDEO_LIST_URL = "https://raw.githubusercontent.com/corochann/AndroidTVappTutorial/master/app/src/main/assets/video_lists.json";
    public  static final String PREFIX_URL = "http://corochann.com/wp-content/uploads/2015/11/";

    private static String TAG_ID = "id";
    private static String TAG_MEDIA = "videos";
    private static String TAG_VIDEO_LISTS = "videolists";
    private static String TAG_CATEGORY = "category";
    private static String TAG_STUDIO = "studio";
    private static String TAG_SOURCES = "sources";
    private static String TAG_DESCRIPTION = "description";
    private static String TAG_CARD_THUMB = "card";
    private static String TAG_BACKGROUND = "background";
    private static String TAG_TITLE = "title";

    private static LinkedHashMap<String, List<Movie>> sMovieList;

    private static Resources sResources;

    public static void setContext(Context context) {
        if (null == sResources) {
            sResources = context.getResources();
        }
    }

    /**
     * It may return null when data is not prepared yet by {@link #buildMedia}.
     * Ensure that data is already prepared before call this function.
     * @return
     */
    public static LinkedHashMap<String, List<Movie>> getMedia() {
        return sMovieList;
    }

    /**
     *  ArrayList of movies within specified "category".
     *  If argument is null, then returns all movie list.
     * @param category
     * @return
     */
    public static ArrayList<Movie> getMovieItems(String category) {
        if(sMovieList == null) {
            Log.e(TAG, "sMovieList is not prepared yet!");
            return null;
        } else {
            ArrayList<Movie> movieItems = new ArrayList<>();
            for (Map.Entry<String, List<Movie>> entry : sMovieList.entrySet()) {
                String categoryName = entry.getKey();
                if(category !=null && !category.equals(categoryName)) {
                    continue;
                }
                List<Movie> list = entry.getValue();
                for (int j = 0; j < list.size(); j++) {
                    movieItems.add(list.get(j));
                }
            }
            if(movieItems == null) {
                Log.w(TAG, "No data foud with category: " + category);
            }
            return movieItems;
        }
    }

    public static LinkedHashMap<String, List<Movie>> buildMedia(Context ctx) throws JSONException{
        return buildMedia(ctx, VIDEO_LIST_URL);
    }

    public static LinkedHashMap<String, List<Movie>> buildMedia(Context ctx, String url)
            throws JSONException {
        if (null != sMovieList) {
            return sMovieList;
        }
        sMovieList = new LinkedHashMap<>();
        //sMovieListById = new HashMap<>();

        JSONObject jsonObj = parseUrl(url);

        if (null == jsonObj) {
            Log.e(TAG, "An error occurred fetching videos.");
            return sMovieList;
        }

        JSONArray categories = jsonObj.getJSONArray(TAG_VIDEO_LISTS);

        if (null != categories) {
            final int categoryLength = categories.length();
            Log.d(TAG, "category #: " + categoryLength);
            long id;
            String title;
            String videoUrl;
            String bgImageUrl;
            String cardImageUrl;
            String studio;
            for (int catIdx = 0; catIdx < categoryLength; catIdx++) {
                JSONObject category = categories.getJSONObject(catIdx);
                String categoryName = category.getString(TAG_CATEGORY);
                JSONArray videos = category.getJSONArray(TAG_MEDIA);
                Log.d(TAG,
                        "category: " + catIdx + " Name:" + categoryName + " video length: "
                                + (null != videos ? videos.length() : 0));
                List<Movie> categoryList = new ArrayList<Movie>();
                Movie movie;
                if (null != videos) {
                    for (int vidIdx = 0, vidSize = videos.length(); vidIdx < vidSize; vidIdx++) {
                        JSONObject video = videos.getJSONObject(vidIdx);
                        String description = video.getString(TAG_DESCRIPTION);
                        JSONArray videoUrls = video.getJSONArray(TAG_SOURCES);
                        if (null == videoUrls || videoUrls.length() == 0) {
                            continue;
                        }
                        id = video.getLong(TAG_ID);
                        title = video.getString(TAG_TITLE);
                        videoUrl = PREFIX_URL + getVideoSourceUrl(videoUrls);
                        bgImageUrl = PREFIX_URL + video.getString(TAG_BACKGROUND);
                        cardImageUrl = PREFIX_URL + video.getString(TAG_CARD_THUMB);
                        studio = video.getString(TAG_STUDIO);

                        movie = buildMovieInfo(id, categoryName, title, description, studio,
                                videoUrl, cardImageUrl, bgImageUrl);
                        categoryList.add(movie);
                    }
                    sMovieList.put(categoryName, categoryList);
                }
            }
        }
        return sMovieList;
    }

    private static Movie buildMovieInfo(long id,
                                        String category,
                                        String title,
                                        String description,
                                        String studio,
                                        String videoUrl,
                                        String cardImageUrl,
                                        String bgImageUrl) {
        Movie movie = new Movie();
        movie.setId(id);
        //movie.setId(Movie.getCount());
        //Movie.incrementCount();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCategory(category);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(bgImageUrl);
        movie.setVideoUrl(videoUrl);

        return movie;
    }


    // workaround for partially pre-encoded sample data
    private static String getVideoSourceUrl(final JSONArray videos) throws JSONException {
        try {
            final String url = videos.getString(0);
            return (-1) == url.indexOf('%') ? url : URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new JSONException("Broken VM: no UTF-8");
        }
    }

    protected static JSONObject parseUrl(String urlString) {
        Log.d(TAG, "Parse URL: " + urlString);
        BufferedReader reader = null;

        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
                    //urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "JSON feed closed", e);
                }
            }
        }
    }
}