package eardic.namazvakti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import eardic.namazvakti.utils.QiblaFinder;

/**
 * A placeholder fragment containing a simple view.
 */
@SuppressLint("InflateParams")
public class CompassFragment extends Fragment implements SensorEventListener {
    private static final int WIFI_SHOW_TIME = 15000;
    // define the display assembly compass picture
    private ImageView compassImage, qabeImage;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    private TextView angleHeader, angleText, locStatus, noteText,
            calibrationLow, wifiMessage;
    private AtomicReference<Location> currLocAtomic = new AtomicReference<Location>();
    private LocationManager locationManager;
    private Timer timer = new Timer();
    private TimerTask wifiShowTask;

    public CompassFragment() {
        this.wifiShowTask = new TimerTask() {
            @Override
            public void run() {
                Activity act = getActivity();
                if (act != null) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wifiMessage.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getMenuInflater().inflate(R.menu.compass, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private MenuInflater getMenuInflater() {
        return getActivity().getMenuInflater();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_compass_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog
            // layout
            builder.setView(
                    inflater.inflate(R.layout.compass_info_dialog, null))
                    .setPositiveButton(getActivity().getString(R.string.okey), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
            return true;
        } else if (item.getItemId() == R.id.action_compass_refresh) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                    .replace(R.id.container, new CompassFragment()).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compass, container,
                false);

        setHasOptionsMenu(true);

        // our compass compassImage
        compassImage = (ImageView) rootView.findViewById(R.id.imageViewCompass);
        qabeImage = (ImageView) rootView.findViewById(R.id.qabeImage);

        // Error text
        // TextView that will tell the user what degree is he heading
        angleHeader = (TextView) rootView.findViewById(R.id.angleHeader);
        angleText = (TextView) rootView.findViewById(R.id.qiblaAngleText);
        calibrationLow = (TextView) rootView.findViewById(R.id.compassCalibration);
        locStatus = (TextView) rootView.findViewById(R.id.compassLocSatus);
        noteText = (TextView) rootView.findViewById(R.id.compassNote);
        wifiMessage = (TextView) rootView.findViewById(R.id.wifiMessage);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getActivity().getSystemService(
                Activity.SENSOR_SERVICE);

        initLocationService();
        initOrientationSensor();

        return rootView;
    }

    private void initOrientationSensor() {
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void showWaitForLocationMessage() {
        // Waiting for Location message is shown only.
        locStatus.setVisibility(View.VISIBLE);
        locStatus.setText(getString(R.string.finding_loc_msg));
        angleText.setVisibility(View.INVISIBLE);
        angleHeader.setVisibility(View.INVISIBLE);
        compassImage.setVisibility(View.INVISIBLE);
        qabeImage.setVisibility(View.INVISIBLE);
        noteText.setVisibility(View.INVISIBLE);
        calibrationLow.setVisibility(View.INVISIBLE);
    }

    private void showCompass() {
        // Show compass hide location waiting message
        locStatus.setVisibility(View.INVISIBLE);
        compassImage.setVisibility(View.VISIBLE);
        qabeImage.setVisibility(View.VISIBLE);
        angleText.setVisibility(View.VISIBLE);
        angleHeader.setVisibility(View.VISIBLE);
    }

    private void initLocationService() {
        try {

            timer.schedule(wifiShowTask, WIFI_SHOW_TIME);
            showWaitForLocationMessage();

            // Acquire a reference to the system Location Manager
            this.locationManager = (LocationManager) getActivity()
                    .getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Save location to use in onSensorChanged
                    currLocAtomic.set(location);
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            // Register the listener with the Location Manager to receive
            // location updates
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 300000, locationListener);
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 300000, locationListener);
            } else {//No location service is enabled, so redirect to location settings

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.loc_service_error);  // GPS not found
                builder.setMessage(R.string.loc_service_must_be_enabled); // Want to enable?
                builder.setNegativeButton(R.string.close, null);
                builder.setPositiveButton(R.string.loc_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
                builder.create().show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        mSensorManager.unregisterListener(this);
        Log.d("CompassDestroy", "Unregistered listeners and canceled timers");
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
        compassImage.setImageDrawable(null);
        qabeImage.setImageDrawable(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        initOrientationSensor();
    }

    private void checkRollandPitch(float pitch, float roll) {
        if (Math.abs(roll) > 20 || Math.abs(pitch) > 15) {
            noteText.setVisibility(View.VISIBLE);
        } else {
            noteText.setVisibility(View.INVISIBLE);
        }
    }

    private void checkAccuracy(int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                calibrationLow.setVisibility(View.VISIBLE);
                break;
            default:
                calibrationLow.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Location currLoc = currLocAtomic.get();
            if (currLoc != null) {
                timer.cancel();
                wifiMessage.setVisibility(View.INVISIBLE);

                showCompass();

                checkRollandPitch(event.values[1], event.values[2]);
                checkAccuracy(event.accuracy);

                // get the angle around the z-axis rotated
                float degree = Math.round(event.values[0]);

                // create a rotation animation (reverse turn degree degrees)
                RotateAnimation ra = new RotateAnimation(currentDegree,
                        -degree, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setFillEnabled(true);
                ra.setFillAfter(true);

                float degreeQibla = (float) QiblaFinder.getDegrees(
                        currLoc.getLatitude(), currLoc.getLongitude(),
                        21.4225f, 39.8261f, Math.round(degree));

                angleText.setText(Integer.toString(Math.round(degreeQibla))
                        + "\u00b0");

                if (Math.abs(degreeQibla) < 10) {
                    qabeImage.setImageDrawable(getResources().getDrawable(
                            R.drawable.kabe_right));
                } else {
                    qabeImage.setImageDrawable(getResources().getDrawable(
                            R.drawable.kabe_wrong));
                }

                // create a rotation animation (reverse turn degree degrees)
                RotateAnimation raq = new RotateAnimation(degreeQibla,
                        degreeQibla, Animation.RELATIVE_TO_PARENT, 0.07f,// Right
                        // little
                        Animation.RELATIVE_TO_PARENT, 0.485f);// Circle Width
                raq.setFillEnabled(true);
                raq.setFillAfter(true);

                // Start the animation
                compassImage.startAnimation(ra);
                qabeImage.startAnimation(raq);
                currentDegree = -degree;
            } else {
                showWaitForLocationMessage();
            }
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("Compass", "Accuracy changed to : " + accuracy);
    }

}