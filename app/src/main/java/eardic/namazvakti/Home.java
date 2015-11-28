package eardic.namazvakti;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;

import java.util.List;
import java.util.Locale;

import eardic.namazvakti.list.FetchTimesTask;
import eardic.namazvakti.utils.Internet;
import eardic.namazvakti.utils.Localization;
import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.LocationManager;

public class Home extends Activity implements
        LeftMenuFragment.NavigationDrawerCallbacks {
    private final Handler fragmentHandler = new Handler();
    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private LeftMenuFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get lang choice from shared pref and set app lang
        Localization localization = new Localization(this);
        localization.InitLocale();

        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (LeftMenuFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_times);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // Update prayer times if internet is available
        AsyncTask<Void, Void, Void> updateTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                updateTimes();
                return null;
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            updateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            updateTask.execute();


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // Create azan sound if not exists
//                try {
//                    Log.d("SoundFile", "Sound file manager started...");
//
//                    // Create media directory if not exists
//                    File mediaDir = new File("/storage/emulated/0/media");
//                    if(!mediaDir.exists()){
//                        mediaDir.mkdir();
//                    }
//
//                    // Create sound file
//                    String fileName = getString(R.string.azan_sound_file);
//                    File file = new File("/storage/emulated/0/media/" + fileName);
//                    // Set permissions
//                    file.setExecutable(true);
//                    file.setReadable(true);
//                    file.setWritable(true);
//                    // If file not exists then create
//                    if (!file.exists()) {
//                        // Copy file from assets to media dir
//                        InputStream inputStream = getResources().openRawResource(R.raw.ezan);
//                        FileOutputStream fo = new FileOutputStream(file);
//                        int data;
//                        byte[] buffer = new byte[4096];
//                        int read;
//                        while ((read = inputStream.read(buffer)) != -1) {
//                            fo.write(buffer, 0, read);
//                        }
//                        fo.close();
//                        inputStream.close();
//                        // Start media scanner on created file.
//                        Log.d("SoundFile", "Created file.");
////                        MediaScannerConnection.scanFile(
////                                getApplicationContext(),
////                                new String[]{file.getAbsolutePath()},
////                                null,
////                                new MediaScannerConnection.OnScanCompletedListener() {
////                                    @Override
////                                    public void onScanCompleted(String path, Uri uri) {
////                                        Log.v("EzanVakti", "File " + path + " was scanned seccessfully: " + uri);
////                                    }
////                                });
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }).start();
    }

    private boolean updateTimes() {
        if (Internet.hasInternetConnection(this)) {
            try {
                LocationManager locMan = new LocationManager(this);
                List<Location> locs = locMan.fetchLocations();
                if (!locs.isEmpty()) {
                    Location loc = locs.get(0);
                    loc.setPrayerTimes(new FetchTimesTask(loc, new Locale(getString(R.string.locale))).execute().get());
                    if (!loc.getPrayerTimes().getPrayerTimes().isEmpty()) {
                        return locMan.saveLocation(loc);
                    } else {
                        Log.v("UpdateTimes",
                                "Times couldn't be fetched. Update canceled.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Fragment f = getFragmentManager().findFragmentByTag(
                LocationFragment.TAG);
        if (f != null) {
            LocationFragment locF = (LocationFragment) f;
            locF.switchList();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        final int pos = position;
        fragmentHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final FragmentManager fragmentManager = getFragmentManager();
                switch (pos) {
                    case 0:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new HomeFragment()).commit();
                        mTitle = getString(R.string.title_times);
                        break;
                    case 1:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new MonthlyTimesFragment())
                                .commit();
                        mTitle = getString(R.string.title_ml_times);
                        break;
                    case 2:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new CompassFragment())
                                .commit();
                        mTitle = getString(R.string.title_compass);
                        break;
                    case 3:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new SettingsFragment())
                                .commit();
                        mTitle = getString(R.string.title_settings);
                        break;
                    case 4:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new AboutFragment()).commit();
                        mTitle = getString(R.string.title_about);
                        break;
                }
            }
        }, 300);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
        return super.onCreateOptionsMenu(menu);
    }
}
