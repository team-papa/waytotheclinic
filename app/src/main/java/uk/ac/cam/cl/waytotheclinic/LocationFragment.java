package uk.ac.cam.cl.waytotheclinic;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.Manifest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LocationFragment extends Fragment {
    private final Context context = this.getContext();
    private final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 2;
    private final String WIFI_DATA_FILE = "wifiModel.dat";  // TODO: create that file

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Location intermediateValue;     // See locationCallback()

    private class LocationTask extends AsyncTask<LocationListener, Void, Location> {
        private LocationListener callback;

        protected Location doInBackground(LocationListener... ll) {
            callback = ll[0];

            try {
                WifiLocater wl = new WifiLocater(callback.getWifiManager());
                wl.loadModel(new File(WIFI_DATA_FILE));

                return wl.getLocation();
            } catch (IOException e) {
                Log.w("LocationFragment", "Failed to read WiFi model from file " + WIFI_DATA_FILE);
                return new Location("Default value");
            }

            // TODO: combine this with GPS and any other sources
        }

        protected void onPostExecute(Location l) {
            locationCallback(false, l);
        }
    }

    public interface LocationListener {
        boolean checkPermissions();
        void requestPermissions();
        WifiManager getWifiManager();
        void startLocationUpdates(LocationRequest lr, LocationCallback lc);
        void updateLocation(Location l);
    }

    private LocationListener callback;

    private Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callback = (LocationListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        callback = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (callback.checkPermissions()) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (callback != null) {
                        new LocationTask().execute(callback);
                    }
                }
            }, 0, 5000);    // Refresh every 5 seconds

            locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locRes) {
                    for (Location l : locRes.getLocations()) {
                        locationCallback(true, l);
                        break;
                    }
                }
            };

            callback.startLocationUpdates(locationRequest, locationCallback);
        } else {
            callback.requestPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        timer.cancel();
    }

    private synchronized void locationCallback(boolean gpsData, Location l) {
        if (!gpsData) {
            intermediateValue = l;
        } else {
            if (intermediateValue != null) {
                // TODO: incorporate WiFi location in return value
//                callback.updateLocation(l);
                intermediateValue = null;
            }
        }
    }
}