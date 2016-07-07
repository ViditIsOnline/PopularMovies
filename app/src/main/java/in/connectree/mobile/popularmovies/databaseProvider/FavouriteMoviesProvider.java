package in.connectree.mobile.popularmovies.databaseProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by vidit on 30/06/16.
 */
public class FavouriteMoviesProvider extends ContentProvider {

    static final int MOVIES = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int TRAILERS = 102;
    static final int REVIEWS = 103;
    // The URI Matcher used by this content provider.
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private FavouriteMoviesDbHelper favouriteMoviesDbHelper;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavouriteMoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, FavouriteMoviesContract.Movies.TABLE_NAME, MOVIES);
        matcher.addURI(authority, FavouriteMoviesContract.Movies.TABLE_NAME + "/*", MOVIE_WITH_ID);
        matcher.addURI(authority, FavouriteMoviesContract.Trailers.TABLE_NAME + "/*", TRAILERS);
        matcher.addURI(authority, FavouriteMoviesContract.Reviews.TABLE_NAME + "/*", REVIEWS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        favouriteMoviesDbHelper = new FavouriteMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;

        switch (uriMatcher.match(uri)){
            case MOVIES:
                retCursor = favouriteMoviesDbHelper.getReadableDatabase().query(
                        FavouriteMoviesContract.Movies.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_WITH_ID:
                retCursor = favouriteMoviesDbHelper.getReadableDatabase().query(
                        FavouriteMoviesContract.Movies.TABLE_NAME,
                        projection,
                        FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRAILERS:
                retCursor = favouriteMoviesDbHelper.getReadableDatabase().query(
                        FavouriteMoviesContract.Trailers.TABLE_NAME,
                        projection,
                        FavouriteMoviesContract.Trailers.COLUMN_NAME_MOVIE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case REVIEWS:
                retCursor = favouriteMoviesDbHelper.getReadableDatabase().query(
                        FavouriteMoviesContract.Reviews.TABLE_NAME,
                        projection,
                        FavouriteMoviesContract.Reviews.COLUMN_NAME_MOVIE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case MOVIES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        FavouriteMoviesContract.CONTENT_AUTHORITY + "/" + FavouriteMoviesContract.Movies.TABLE_NAME;
            case MOVIE_WITH_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        FavouriteMoviesContract.CONTENT_AUTHORITY + "/" + FavouriteMoviesContract.Movies.TABLE_NAME;
            case TRAILERS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        FavouriteMoviesContract.CONTENT_AUTHORITY + "/" + FavouriteMoviesContract.Trailers.TABLE_NAME;
            case REVIEWS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        FavouriteMoviesContract.CONTENT_AUTHORITY + "/" + FavouriteMoviesContract.Reviews.TABLE_NAME;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = favouriteMoviesDbHelper.getWritableDatabase();
        Uri retUri;
        switch (uriMatcher.match(uri)){
            case MOVIES:
                retUri = ContentUris.withAppendedId(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                        appendPath(FavouriteMoviesContract.Movies.TABLE_NAME).build(),
                        db.insert(FavouriteMoviesContract.Movies.TABLE_NAME,null,contentValues));
                break;
            case TRAILERS:
                retUri = ContentUris.withAppendedId(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                appendPath(FavouriteMoviesContract.Trailers.TABLE_NAME).build(),
                        db.insert(FavouriteMoviesContract.Trailers.TABLE_NAME,null,contentValues));
                break;
            case REVIEWS:
                retUri = ContentUris.withAppendedId(FavouriteMoviesContract.BASE_CONTENT_URI.buildUpon().
                                appendPath(FavouriteMoviesContract.Reviews.TABLE_NAME).build(),
                        db.insert(FavouriteMoviesContract.Reviews.TABLE_NAME,null,contentValues));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = favouriteMoviesDbHelper.getWritableDatabase();
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (uriMatcher.match(uri)) {
            case MOVIES:
                rowsDeleted = db.delete(
                        FavouriteMoviesContract.Movies.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                rowsDeleted = db.delete(
                        FavouriteMoviesContract.Trailers.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(
                        FavouriteMoviesContract.Reviews.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = favouriteMoviesDbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriMatcher.match(uri)) {
            case MOVIES:
                rowsUpdated = db.update(FavouriteMoviesContract.Movies.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRAILERS:
                rowsUpdated = db.update(FavouriteMoviesContract.Trailers.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(FavouriteMoviesContract.Reviews.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
