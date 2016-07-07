package in.connectree.mobile.popularmovies.databaseProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vidit on 27/06/16.
 */
public class FavouriteMoviesDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FavouriteMovies.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TABLE_MOVIES =
            "CREATE TABLE " + FavouriteMoviesContract.Movies.TABLE_NAME + " (" +
                    FavouriteMoviesContract.Movies._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_MOVIE_ID + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_OVERVIEW + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_BACKDROP + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_POSTER + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_RELEASE_DATE + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Movies.COLUMN_NAME_VOTE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_TABLE_MOVIES =
            "DROP TABLE IF EXISTS " + FavouriteMoviesContract.Movies.TABLE_NAME;

    private static final String SQL_CREATE_TABLE_TRAILERS =
            "CREATE TABLE " + FavouriteMoviesContract.Trailers.TABLE_NAME + " (" +
                    FavouriteMoviesContract.Trailers._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    FavouriteMoviesContract.Trailers.COLUMN_NAME_MOVIE_ID + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Trailers.COLUMN_NAME_KEY + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Trailers.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Trailers.COLUMN_NAME_SITE + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Trailers.COLUMN_NAME_SIZE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_TABLE_TRAILERS =
            "DROP TABLE IF EXISTS " + FavouriteMoviesContract.Trailers.TABLE_NAME;

    private static final String SQL_CREATE_TABLE_REVIEWS =
            "CREATE TABLE " + FavouriteMoviesContract.Reviews.TABLE_NAME + " (" +
                    FavouriteMoviesContract.Reviews._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    FavouriteMoviesContract.Reviews.COLUMN_NAME_MOVIE_ID + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Reviews.COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP +
                    FavouriteMoviesContract.Reviews.COLUMN_NAME_CONTENT + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_TABLE_REVIEWS =
            "DROP TABLE IF EXISTS " + FavouriteMoviesContract.Reviews.TABLE_NAME;

    public FavouriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_MOVIES);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_TRAILERS);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_REVIEWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_MOVIES);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_MOVIES);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_TRAILERS);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_TRAILERS);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_REVIEWS);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_REVIEWS);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
