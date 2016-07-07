package in.connectree.mobile.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.connectree.mobile.popularmovies.adapter.MoviesAdapter;
import in.connectree.mobile.popularmovies.databaseProvider.FavouriteMoviesContract;
import in.connectree.mobile.popularmovies.model.CachedStringRequest;
import in.connectree.mobile.popularmovies.model.EndlessRecyclerViewScrollListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final int SORT_RELEASE_DESC = 0;
    public static final int SORT_POPULARITY_DESC = 1;
    public static final int SORT_RATED_DESC = 2;
    public static final int SORT_FAV = 3;
    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private static final String SAVED_POSITION = "saved_position";
    private static final String ARG_PARAM1 = "dual_pane";
    public static JSONArray movies_result = new JSONArray();
    private SharedPreferences sharedPreferences;
    private View rootView;
    private RecyclerView recyclerView;
    private MoviesAdapter moviesAdapter;
    private ProgressBar progressBar;
    private GridLayoutManager mLayoutManager;
    private boolean isDualPane;
    private boolean isFavouritesChecked = false;

    public MainActivityFragment() {
    }

    public static MainActivityFragment newInstance(boolean isDualPane) {
        MainActivityFragment fragment = new MainActivityFragment();
        //Put arguments to the fragment passed in this method
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isDualPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_POSITION, mLayoutManager.findFirstVisibleItemPosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            int i = savedInstanceState.getInt(SAVED_POSITION);
            mLayoutManager.scrollToPosition(i);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isDualPane = getArguments().getBoolean(ARG_PARAM1);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mLayoutManager = new GridLayoutManager(rootView.getContext(),
                    getResources().getInteger(R.integer.number_of_columns));

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_main);
        recyclerView.setLayoutManager(mLayoutManager);
        moviesAdapter = new MoviesAdapter(getContext(), getActivity(), isDualPane);
        recyclerView.setAdapter(moviesAdapter);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                makeRequest(page + 1, sharedPreferences.getInt("sort_order", SORT_POPULARITY_DESC));
            }
        });

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar_main);

        if (savedInstanceState == null) {
            if (sharedPreferences.getInt("sort_order", SORT_POPULARITY_DESC) == SORT_FAV &&
                    movies_result.length() == 0)
                makeDatabaseRequest();
            else
                makeRequest(1, sharedPreferences.getInt("sort_order", SORT_POPULARITY_DESC));
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        isFavouritesChecked = menu.findItem(R.id.menu_item_favourites).isChecked();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_item_popularity:
                if (!item.isChecked()){
                    moviesAdapter.notifyDataSetChanged();
                    movies_result = new JSONArray();
                    makeRequest(1, SORT_POPULARITY_DESC);
                    item.setChecked(true);
                    sharedPreferences.edit().putInt("sort_order",SORT_POPULARITY_DESC).apply();
                }
                return true;
            case R.id.menu_item_release:
                if (!item.isChecked()){
                    moviesAdapter.notifyDataSetChanged();
                    movies_result = new JSONArray();
                    makeRequest(1, SORT_RELEASE_DESC);
                    item.setChecked(true);
                    sharedPreferences.edit().putInt("sort_order",SORT_RELEASE_DESC).apply();
                }
                return true;
            case R.id.menu_item_rating:
                if (!item.isChecked()){
                    moviesAdapter.notifyDataSetChanged();
                    movies_result = new JSONArray();
                    makeRequest(1, SORT_RATED_DESC);
                    item.setChecked(true);
                    sharedPreferences.edit().putInt("sort_order",SORT_RATED_DESC).apply();
                }
                return true;
            case R.id.menu_item_favourites:
                if (!item.isChecked()){
                    moviesAdapter.notifyDataSetChanged();
                    movies_result = new JSONArray();
                    makeDatabaseRequest();
                    item.setChecked(true);
                    sharedPreferences.edit().putInt("sort_order",SORT_FAV).apply();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeRequest(int page, int sortBy) {

        String sort = "popularity.desc";
        switch (sortBy){
            case SORT_RELEASE_DESC :
                sort = "release_date.desc";
                break;
            case SORT_RATED_DESC :
                sort = "vote_average.desc";
                break;
            case SORT_FAV:
                return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        String url = Uri.parse(ApplicationClass.DISCOVER_URL).buildUpon()
                .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY)
                .appendQueryParameter("page", String.valueOf(page))
                .appendQueryParameter("sort_by", sort)
                .build().toString();

        CachedStringRequest cachedStringRequest= new CachedStringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray("results");
                    for (int i=0; i < jsonArray.length(); i++)
                        movies_result.put(jsonArray.get(i));

                    moviesAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, movies_result.toString());
//                for all data at once
//                moviesAdapter.notifyDataSetChanged();
//                for data in range
//                moviesAdapter.notifyItemRangeInserted(moviesAdapter.getItemCount(), movies_result.length() - 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                }, 1000);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if (error instanceof NoConnectionError)
                    Toast.makeText(getContext(), "No Internet Connection.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                }, 1000);
            }
        });
        ApplicationClass.getInstance().addToRequestQueue(cachedStringRequest);
    }

    private void makeDatabaseRequest() {
        new OpenDatabase().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFavouritesChecked)
            makeDatabaseRequest();
    }

    private class OpenDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Cursor cursor = getContext().getContentResolver().
                    query(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                            appendPath(FavouriteMoviesContract.Movies.TABLE_NAME).build(),
                            null,
                            null,
                            null,
                            null,
                            null);
            try {
                movies_result = new JSONArray();
                while (cursor.moveToNext()) {
                    JSONObject jsonObject= new JSONObject();
                    try {
                        jsonObject.put("poster_path",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_POSTER)));
                        jsonObject.put("overview",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_OVERVIEW)));
                        jsonObject.put("release_date",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_RELEASE_DATE)));
                        jsonObject.put("id",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID)));
                        jsonObject.put("title",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_TITLE)));
                        jsonObject.put("backdrop_path",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_BACKDROP)));
                        jsonObject.put("vote_average",cursor.getString(cursor.getColumnIndexOrThrow(
                                FavouriteMoviesContract.Movies.COLUMN_NAME_VOTE)));

                        movies_result.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            moviesAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}
