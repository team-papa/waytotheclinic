package uk.ac.cam.cl.waytotheclinic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationLibrary {
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public int test = 1;
    private static Location locationData;

    public static Location getLocation(AppCompatActivity current) {
        if (ContextCompat.checkSelfPermission(current,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(current);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(current, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                locationData = location;
                            }
                        }
                    });
        }
        return locationData;
    }

    public static void changeSettings() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
    }
}
