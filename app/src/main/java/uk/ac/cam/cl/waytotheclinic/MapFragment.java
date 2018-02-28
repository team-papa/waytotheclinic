package uk.ac.cam.cl.waytotheclinic;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static android.content.ContentValues.TAG;

public class MapFragment extends Fragment implements OnMapReadyCallback{
    MapView mapView = null;
    GoogleMap googleMap;
    private String[] populatedTiles;
    private int Floor = 1;

    private TileOverlay mapOverlay;
    private TileOverlay pathOverlay;
    private TileOverlay locOverlay;

    private PathTileProvider pathTileProvider;
    private LocTileProvider locTileProvider;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        //region Map Provider
        {
            TileProvider mapTileProvider = new UrlTileProvider(256, 256) {
                @Override
                public URL getTileUrl(int x, int y, int zoom) {
                    Log.d(TAG, "getTileUrl: getting " + x + ", " + y + ", " + zoom);
                    try {
                        if (!tilePopulated(Floor, zoom, x, y))
                            return new URL("http://cjj39.user.srcf.net/WayToTheClinic/blank.png");
                        String url = String.format("http://cjj39.user.srcf.net/WayToTheClinic/TileMap%d/%d/%d/%d.png", Floor, zoom, x, y);
                        return new URL(url);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            mapOverlay = googleMap.addTileOverlay(new TileOverlayOptions().zIndex(1).tileProvider(mapTileProvider));
        }
        //endregion

        //region Path Provider
        pathTileProvider = new PathTileProvider(256, this);
        pathOverlay = googleMap.addTileOverlay(new TileOverlayOptions().zIndex(2).tileProvider(pathTileProvider));
        //endregion

        //region Location Provider
        locTileProvider = new LocTileProvider(256, this);
        locOverlay = googleMap.addTileOverlay(new TileOverlayOptions().zIndex(3).tileProvider(locTileProvider));
        //endregion

        this.setFloor(2);
    }

    private boolean tilePopulated(int f, int z, int x, int y){
        String check = String.format("(%d,%d,%d,%d)", f,z,x,y);
        for(String s : populatedTiles){
            if(s.equals(check))
                return true;
        }
        return false;
    }

    public void setFloor(int floor){
        Floor = floor;
        mapOverlay.clearTileCache();

        //clear path and loc to ensure that path and loc are only shown on correct floors
        pathOverlay.clearTileCache();
        locOverlay.clearTileCache();
        mapView.invalidate();
    }

    public int getFloor(){
        return Floor;
    }

    public void setPath(List<Point> path){
        pathTileProvider.setPath(path);

        //invalidate cache to cause update
        pathOverlay.clearTileCache();
        mapView.invalidate();
    }

    public void setLocation(Point loc){
        locTileProvider.setLocation(loc);

        //invalidate cache to cause update
        locOverlay.clearTileCache();
        mapView.invalidate();
    }

    //region android boilerplate to get mapView working

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

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    Bundle savedInstanceBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View inflatedView = inflater.inflate(R.layout.map_view, container, false);

        Bundle args = getArguments();
        if(args != null)
            Floor = args.getInt("Floor");

        Thread urlLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://cjj39.user.srcf.net/WayToTheClinic/populatedTiles.txt");
                    Scanner s = new Scanner(new BufferedInputStream(url.openStream()));
                    String input = s.nextLine();
                    String[] splitInput = input.split(";");
                    populatedTiles = Arrays.copyOfRange(splitInput, 0, splitInput.length - 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        urlLoader.start();


        MapsInitializer.initialize(getActivity());
        if(mapView == null)
            mapView = inflatedView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceBundle);

        return inflatedView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        savedInstanceBundle = mapViewBundle;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);

        mapView.getMapAsync(this);
        mapView.setBackgroundColor(Color.GRAY);

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
/*        if(mapView == null)
            mapView = getActivity().findViewById(R.id.mapView);
        mapView.onDestroy();*/
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

    public class Point{
        double x;
        double y;
        int floor;

        public Point(double X, double Y, int Floor){
            x = X;
            y = Y;
            floor = Floor;
        }
    }
}
