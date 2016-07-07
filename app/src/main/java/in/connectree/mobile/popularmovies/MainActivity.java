package in.connectree.mobile.popularmovies;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isDualPane = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (findViewById(R.id.movie_container) != null)
            isDualPane = true;
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                    MainActivityFragment.newInstance(isDualPane))
                    .commit();
        }
        isStoragePermissionGranted();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        switch (PreferenceManager.getDefaultSharedPreferences(this).getInt("sort_order",
                MainActivityFragment.SORT_POPULARITY_DESC)){
            case MainActivityFragment.SORT_POPULARITY_DESC:
                menu.findItem(R.id.menu_item_popularity).setChecked(true);
                break;
            case MainActivityFragment.SORT_RELEASE_DESC:
                menu.findItem(R.id.menu_item_release).setChecked(true);
                break;
            case MainActivityFragment.SORT_RATED_DESC:
                menu.findItem(R.id.menu_item_rating).setChecked(true);
                break;
            case MainActivityFragment.SORT_FAV:
                menu.findItem(R.id.menu_item_favourites).setChecked(true);
                break;
        }
        return true;
    }

    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "Permission is needed to store favourites. Please allow it", Toast.LENGTH_LONG).show();
            //resume tasks needing this permission
        }
    }
}
