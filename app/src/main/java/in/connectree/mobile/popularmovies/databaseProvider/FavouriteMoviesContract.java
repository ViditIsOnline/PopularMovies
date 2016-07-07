package in.connectree.mobile.popularmovies.databaseProvider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vidit on 27/06/16.
 */
public final class FavouriteMoviesContract {

    public static final String CONTENT_AUTHORITY = "in.connectree.mobile.popularmovies.DatabaseProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public FavouriteMoviesContract(){}

    public static abstract class Movies implements BaseColumns {

        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_NAME_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_OVERVIEW = "overview";
        public static final String COLUMN_NAME_BACKDROP = "backdrop";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_RELEASE_DATE = "releasedate";
        public static final String COLUMN_NAME_VOTE = "vote";
    }

    public static abstract class Trailers implements BaseColumns {

        public static final String TABLE_NAME = "trailers";
        public static final String COLUMN_NAME_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SITE = "site";
        public static final String COLUMN_NAME_SIZE = "size";

    }

    public static abstract class Reviews implements BaseColumns {

        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_NAME_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_CONTENT = "content";
    }

}
