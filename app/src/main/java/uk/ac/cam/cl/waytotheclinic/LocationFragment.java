package uk.ac.cam.cl.waytotheclinic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationFragment extends Fragment {

    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location locationData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    public Location getLocation(AppCompatActivity current) {
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
}
