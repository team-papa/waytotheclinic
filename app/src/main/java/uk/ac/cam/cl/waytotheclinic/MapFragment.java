package uk.ac.cam.cl.waytotheclinic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback{
    MapView mapView = null;
    GoogleMap googleMap;
    public TileProvider mapTileProvider;
    public TileProvider pathTileProvider;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.map_view, container, false);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        Bitmap map = ((BitmapDrawable)ContextCompat.getDrawable(getContext(), R.drawable.test_map)).getBitmap();
        mapTileProvider = new MapTileProvider(map, googleMap);
        googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mapTileProvider).zIndex(1));

        Bitmap testPath = Bitmap.createBitmap(map.getWidth(), map.getHeight(), map.getConfig());
        List<Pair> points = new ArrayList<>();
        {
            String testCoordString = getString(R.string.test_path);
            for(String s : testCoordString.split(";")){
                String temp = s.substring(1, s.length() - 1);
                String[] vals = temp.split(", ");
                points.add(new Pair(Integer.decode(vals[0]), Integer.decode(vals[1])));
            }
        }
        createPathFromString(testPath, points);
        pathTileProvider = new MapTileProvider(testPath, googleMap);
        googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(pathTileProvider).zIndex(2));
    }

    public static void createPathFromString(Bitmap map, List<Pair> coords){
        Canvas canvas = new Canvas(map);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setARGB(255, 255, 0, 0);
        for(Pair p : coords){
            canvas.drawPoint(p.a, p.b, paint);
        }
        map.prepareToDraw();
    }

    //region android boilerplate to get mapView working
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);

        mapView.getMapAsync(this);
        mapView.setBackgroundColor(Color.BLACK);

        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);
        mapView.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);
        mapView.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);
        mapView.onLowMemory();
    }

    //endregion

    private class Pair{
        int a;
        int b;
        public Pair(int A, int B){a = A; b = B;}
    }
}
