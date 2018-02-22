package uk.ac.cam.cl.waytotheclinic;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.Manifest;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LocationFragment extends Fragment {
	private final Activity context = this.getActivity();
	private final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 2;
	private final String WIFI_DATA_FILE = "wifiModel.dat";	// TODO: create that file

	private class LocationTask extends AsyncTask<Void, Void, Location> {
		protected Location doInBackground(Void... v) {
			try {
				WifiLocater wl = new WifiLocater(context);
				wl.loadModel(new File(WIFI_DATA_FILE));

				return wl.getLocation();
			} catch (IOException e) {
				Log.w("LocationFragment", "Failed to read WiFi model from file " + WIFI_DATA_FILE);
				return new Location("Default value");
			}

			// TODO: combine this with GPS and any other sources
		}
	}

	private Timer timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					new LocationTask().execute();
					// TODO: do something with the return value
				}
			}, 0, 5000);	// Refresh every 5 seconds (review this)
		} else {
			ActivityCompat.requestPermissions(context,
				new String[] {Manifest.permission.ACCESS_WIFI_STATE},
				MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		timer.cancel();
	}
}