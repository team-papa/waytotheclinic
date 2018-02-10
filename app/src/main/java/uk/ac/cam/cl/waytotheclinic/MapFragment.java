package uk.ac.cam.cl.waytotheclinic;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.MapView;

/**
 * Created by Chris on 10/02/2018.
 */

public class MapFragment extends Fragment{
    MapView mapView;
    Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mapView = activity.findViewById(R.id.mapView);
    }
}
