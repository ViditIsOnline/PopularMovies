package in.connectree.mobile.popularmovies;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import in.connectree.mobile.popularmovies.adapter.ReviewsAdapter;
import in.connectree.mobile.popularmovies.adapter.TrailersAdapter;
import in.connectree.mobile.popularmovies.databaseProvider.FavouriteMoviesContract;
import in.connectree.mobile.popularmovies.model.CachedStringRequest;
import in.connectree.mobile.popularmovies.model.DividerItemDecoration;

/**
 * Created by vidit on 16/02/16.
 */
public class DetailActivityFragment extends Fragment {

    public static final String TAG = DetailActivityFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "position";
    private static final String ARG_PARAM2 = "dual_pane";
    private static final String ARG_PARAM3 = "favourites_checked";
    public static JSONArray trailers_result = new JSONArray();
    public static JSONArray reviews_result = new JSONArray();
    private static int REQUEST_TRAILERS = 1;
    private static int REQUEST_REVIEWS = 2;
    private View rootView;
    private ImageView posterImageView;
    private ImageView toolbarImageView;
    private TextView yearTextView, ratingTextView, plotTextView, nameTextView;
    private int mPosition;
    private JSONObject movie;
    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;
    private RecyclerView trailersRecyclerView, reviewsRecyclerView;
    private LinearLayoutManager mLayoutManager1, mLayoutManager2;
    private FloatingActionButton likeButton;
    private boolean exists;
    private Bitmap bmPoster, bmBackdrop;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private boolean mIsDualPane;
    private boolean isFavouritesChecked = false;

    public DetailActivityFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position Parameter 1.
     * @param isDualPane Parameter 2.
     * @return A new instance of fragment DetailActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailActivityFragment newInstance(int position, boolean isDualPane) {
        DetailActivityFragment fragment = new DetailActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        args.putBoolean(ARG_PARAM2, isDualPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(ARG_PARAM1);
            mIsDualPane = getArguments().getBoolean(ARG_PARAM2);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menu_item_share).setVisible(true);
        if (mIsDualPane)
            isFavouritesChecked = menu.findItem(R.id.menu_item_favourites).isChecked();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                try {
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" +
                            trailers_result.getJSONObject(0).getString("key"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        collapsingToolbarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.collapsing_toolbar_layout);

        posterImageView = (ImageView) rootView.findViewById(R.id.image_view_poster);
        toolbarImageView = (ImageView) getActivity().findViewById(R.id.image_view_toolbar);

        yearTextView = (TextView) rootView.findViewById(R.id.text_view_year);
        ratingTextView = (TextView) rootView.findViewById(R.id.text_view_rating);
        plotTextView = (TextView) rootView.findViewById(R.id.text_view_plot);
        likeButton = (FloatingActionButton) getActivity().findViewById(R.id.fab_like);
        nameTextView = (TextView) rootView.findViewById(R.id.text_view_name);

        if (mIsDualPane)
            likeButton.setVisibility(View.VISIBLE);
        makeDatabaseRequest();

        try {
            movie = MainActivityFragment.movies_result.getJSONObject(mPosition);
            if (mIsDualPane)
                nameTextView.setText(movie.getString("title"));
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy");
            yearTextView.setText(outputDateFormat.format(
                    inputDateFormat.parse(movie.getString("release_date"))));
            ratingTextView.setText(movie.getString("vote_average") + "/10");
            plotTextView.setText(movie.getString("overview"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        trailersRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_trailers);

        mLayoutManager1 = new LinearLayoutManager(rootView.getContext());
        trailersRecyclerView.setLayoutManager(mLayoutManager1);

        trailersAdapter = new TrailersAdapter(getContext());
        trailersRecyclerView.setAdapter(trailersAdapter);
        trailersRecyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        reviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_reviews);

        mLayoutManager2 = new LinearLayoutManager(getContext());
        reviewsRecyclerView.setLayoutManager(mLayoutManager2);

        reviewsAdapter = new ReviewsAdapter(getContext());
        reviewsRecyclerView.setAdapter(reviewsAdapter);
        reviewsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exists)
                    new DeleteEntries().execute();
                else
                    new InsertEntries().execute();
            }
        });

        return rootView;
    }

    private void makeRequest(String category, final int type) {

        String url = null;
        try {
            url = Uri.parse(ApplicationClass.MOVIE_URL).buildUpon().appendPath(movie.getString("id"))
                    .appendPath(category)
                    .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CachedStringRequest cachedStringRequest = new CachedStringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray("results");
                    if (type == REQUEST_TRAILERS) {
                        trailers_result = jsonArray;
                        trailersAdapter.notifyDataSetChanged();
                    } else if (type == REQUEST_REVIEWS) {
                        reviews_result = jsonArray;
                        reviewsAdapter.notifyDataSetChanged();
                    }
                    Log.d(TAG, jsonArray.toString());
                    //trailersAdapter.notifyItemInserted(trailers_result.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                for all data at once
//                moviesAdapter.notifyDataSetChanged();
//                for data in range
//                moviesAdapter.notifyItemRangeInserted(moviesAdapter.getItemCount(), movies_result.length() - 1);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getContext(), "No Internet Connection.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        ApplicationClass.getInstance().addToRequestQueue(cachedStringRequest);
    }

    private void makeDatabaseRequest() {
        new CheckDatabase().execute();
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void applyPalette(Palette palette) {
        int primary = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        collapsingToolbarLayout.setContentScrimColor(palette.getDarkVibrantColor(primary));
        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkVibrantColor(primary));
    }

    private class CheckDatabase extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                String selection = FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID + " = ?";
                String[] selectionArgs = null;
                try {
                    selectionArgs = new String[]{String.valueOf(movie.getString("id"))};
                } catch (JSONException e) {
                    this.cancel(true);
                    e.printStackTrace();
                }
                Cursor cursor = getContext().getContentResolver().
                        query(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                        appendPath(FavouriteMoviesContract.Movies.TABLE_NAME).build(),
                                null,
                                selection,
                                selectionArgs,
                                null,
                                null);

                try {
                    if (cursor.moveToNext()) {
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            } catch (Exception e) {
                this.cancel(true);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                likeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_thumbs_down));
                exists = true;
                new GetTrailers().execute();
                new GetReviews().execute();

                File root = new File(Environment.getExternalStorageDirectory() + File.separator +
                        "PopularMovies" + File.separator + "images" + File.separator);

                try {
                    Glide.with(rootView.getContext()).load(new File(root, movie.getString("id") + "poster.png")).
                            into(posterImageView);
                    if (!mIsDualPane)
                        Glide.with(rootView.getContext()).load(new File(root, movie.getString("id") + "backdrop.png")).
                                asBitmap().
                                listener(new RequestListener<File, Bitmap>() {
                                    @Override
                                    public boolean onException(Exception e, File model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, File model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                            public void onGenerated(Palette palette) {
                                                applyPalette(palette);
                                            }
                                        });
                                        return false;
                                    }
                                }).
                                into(toolbarImageView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                likeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_thumbs_up));
                exists = false;
                try {
                    Glide.with(rootView.getContext()).load(ApplicationClass.IMAGE_BASE_URL +
                            movie.getString("poster_path")).into(posterImageView);
                    if (!mIsDualPane)
                        Glide.with(rootView.getContext()).load(ApplicationClass.IMAGE_BASE_URL +
                                movie.getString("backdrop_path")).
                                asBitmap().
                                listener(new RequestListener<String, Bitmap>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                            public void onGenerated(Palette palette) {
                                                applyPalette(palette);
                                            }
                                        });
                                        return false;
                                    }
                                }).
                                into(toolbarImageView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                makeRequest("videos", REQUEST_TRAILERS);
                makeRequest("reviews", REQUEST_REVIEWS);
            }
        }
    }

    private class GetTrailers extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Cursor cursor = null;
                try {
                    cursor = getContext().getContentResolver().
                            query(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                            appendPath(FavouriteMoviesContract.Trailers.TABLE_NAME).
                                            appendPath(String.valueOf(movie.getString("id"))).build(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                trailers_result = new JSONArray();
                try {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("key", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Trailers.COLUMN_NAME_KEY)));
                            jsonObject.put("name", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Trailers.COLUMN_NAME_NAME)));
                            jsonObject.put("site", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Trailers.COLUMN_NAME_SITE)));
                            jsonObject.put("size", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Trailers.COLUMN_NAME_SIZE)));
                            trailers_result.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    cursor.close();
                }
            } catch (Exception e) {
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            trailersAdapter.notifyDataSetChanged();
        }
    }

    private class GetReviews extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Cursor cursor = null;
                try {
                    cursor = getContext().getContentResolver().
                            query(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                            appendPath(FavouriteMoviesContract.Reviews.TABLE_NAME).
                                            appendPath(String.valueOf(movie.getString("id"))).build(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                reviews_result = new JSONArray();
                try {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("author", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Reviews.COLUMN_NAME_AUTHOR)));
                            jsonObject.put("content", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Reviews.COLUMN_NAME_CONTENT)));
                            reviews_result.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    cursor.close();
                }
            } catch (Exception e) {
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            reviewsAdapter.notifyDataSetChanged();
        }
    }

    private class InsertEntries extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            posterImageView.buildDrawingCache();
            bmPoster = posterImageView.getDrawingCache();
            if (!mIsDualPane) {
                toolbarImageView.buildDrawingCache();
                bmBackdrop = toolbarImageView.getDrawingCache();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ContentValues values = new ContentValues();
                try {
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_BACKDROP, movie.getString("backdrop_path"));
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID, movie.getString("id"));
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_OVERVIEW, movie.getString("overview"));
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_POSTER, movie.getString("poster_path"));
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_RELEASE_DATE, movie.getString("release_date"));
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_TITLE, movie.getString("title"));
                    values.put(FavouriteMoviesContract.Movies.COLUMN_NAME_VOTE, movie.getString("vote_average"));
                    getContext().getContentResolver().insert(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                            appendPath(FavouriteMoviesContract.Movies.TABLE_NAME).build(), values);

                    for (int i = 0; i < trailers_result.length(); i++) {
                        JSONObject jsonObject = trailers_result.getJSONObject(i);
                        ContentValues trailerValues = new ContentValues();
                        trailerValues.put(FavouriteMoviesContract.Trailers.COLUMN_NAME_KEY, jsonObject.getString("key"));
                        trailerValues.put(FavouriteMoviesContract.Trailers.COLUMN_NAME_MOVIE_ID, movie.getString("id"));
                        trailerValues.put(FavouriteMoviesContract.Trailers.COLUMN_NAME_NAME, jsonObject.getString("name"));
                        trailerValues.put(FavouriteMoviesContract.Trailers.COLUMN_NAME_SITE, jsonObject.getString("site"));
                        trailerValues.put(FavouriteMoviesContract.Trailers.COLUMN_NAME_SIZE, jsonObject.getString("size"));
                        getContext().getContentResolver().insert(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                appendPath(FavouriteMoviesContract.Trailers.TABLE_NAME).appendPath(movie.getString("id")).
                                build(), trailerValues);
                    }
                    if (isStoragePermissionGranted()) {
                        try {
                            File root = new File(Environment.getExternalStorageDirectory() + File.separator +
                                    "PopularMovies" + File.separator + "images" + File.separator);
                            root.mkdirs();
                            File imageDir = new File(root, movie.getString("id") + "poster.png");
                            FileOutputStream fOut = new FileOutputStream(imageDir);
                            bmPoster.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                            fOut.flush();
                            fOut.close();

                            if (!mIsDualPane) {
                                imageDir = new File(root, movie.getString("id") + "backdrop.png");
                                fOut = new FileOutputStream(imageDir);
                                bmBackdrop.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                fOut.flush();
                                fOut.close();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    for (int i = 0; i < reviews_result.length(); i++) {
                        JSONObject jsonObject = reviews_result.getJSONObject(i);
                        ContentValues reviewValues = new ContentValues();
                        reviewValues.put(FavouriteMoviesContract.Reviews.COLUMN_NAME_AUTHOR, jsonObject.getString("author"));
                        reviewValues.put(FavouriteMoviesContract.Reviews.COLUMN_NAME_MOVIE_ID, movie.getString("id"));
                        reviewValues.put(FavouriteMoviesContract.Reviews.COLUMN_NAME_CONTENT, jsonObject.getString("content"));
                        getContext().getContentResolver().insert(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                appendPath(FavouriteMoviesContract.Reviews.TABLE_NAME).appendPath(movie.getString("id")).
                                build(), reviewValues);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Snackbar snackbar = Snackbar.make(rootView, "Liked", Snackbar.LENGTH_SHORT);
            snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackbar.show();
            likeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_thumbs_down));
            exists = true;
            if (isFavouritesChecked)
                new RefreshMoviesRecyclerView().execute(true);
        }
    }

    private class DeleteEntries extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String[] selectionArgs = null;
                try {
                    selectionArgs = new String[]{String.valueOf(movie.getString("id"))};
                    String selection = FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID + " = ?";
                    getContext().getContentResolver().delete(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                            appendPath(FavouriteMoviesContract.Movies.TABLE_NAME).build(), selection, selectionArgs);
                    selection = FavouriteMoviesContract.Trailers.COLUMN_NAME_MOVIE_ID + " = ?";
                    getContext().getContentResolver().delete(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                            appendPath(FavouriteMoviesContract.Trailers.TABLE_NAME).appendPath(movie.getString("id")).
                            build(), selection, selectionArgs);
                    selection = FavouriteMoviesContract.Reviews.COLUMN_NAME_MOVIE_ID + " = ?";
                    getContext().getContentResolver().delete(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                            appendPath(FavouriteMoviesContract.Reviews.TABLE_NAME).appendPath(movie.getString("id")).
                            build(), selection, selectionArgs);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Snackbar snackbar = Snackbar.make(rootView, "Unliked", Snackbar.LENGTH_SHORT);
            snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackbar.show();
            likeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_thumbs_up));
            exists = false;
            if (isFavouritesChecked)
                new RefreshMoviesRecyclerView().execute(false);
        }
    }

    private class RefreshMoviesRecyclerView extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... aBoolean) {

            try {
                Cursor cursor = getContext().getContentResolver().
                        query(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                        appendPath(FavouriteMoviesContract.Movies.TABLE_NAME).build(),
                                null,
                                null,
                                null,
                                null,
                                null);
                try {
                    MainActivityFragment.movies_result = new JSONArray();
                    while (cursor.moveToNext()) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("poster_path", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_POSTER)));
                            jsonObject.put("overview", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_OVERVIEW)));
                            jsonObject.put("release_date", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_RELEASE_DATE)));
                            jsonObject.put("id", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID)));
                            jsonObject.put("title", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_TITLE)));
                            jsonObject.put("backdrop_path", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_BACKDROP)));
                            jsonObject.put("vote_average", cursor.getString(cursor.getColumnIndexOrThrow(
                                    FavouriteMoviesContract.Movies.COLUMN_NAME_VOTE)));

                            MainActivityFragment.movies_result.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    cursor.close();
                }
                return aBoolean[0];

            } catch (Exception e) {
                this.cancel(true);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (mIsDualPane) {
                RecyclerView.Adapter adapter = ((RecyclerView) getActivity().findViewById(R.id.recycler_view_main)).getAdapter();
                adapter.notifyDataSetChanged();
            }

        }
    }
}
