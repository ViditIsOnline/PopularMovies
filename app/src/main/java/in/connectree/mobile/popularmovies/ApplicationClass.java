package in.connectree.mobile.popularmovies;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.target.ViewTarget;

/**
 * Created by vidit on 23/01/16.
 */
public class ApplicationClass extends Application {

    public static final String TAG = ApplicationClass.class.getSimpleName();

    public static String DISCOVER_URL = "http://api.themoviedb.org/3/discover/movie";
    public static String MOVIE_URL = "http://api.themoviedb.org/3/movie";
    public static String THUMBNAIL_URL = "http://img.youtube.com/vi/";
    public static String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    private RequestQueue mRequestQueue;

    private static ApplicationClass instance;

    public ApplicationClass(){}

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ViewTarget.setTagId(R.id.glide_tag);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static ApplicationClass getInstance() {
        return instance;
    }
}
