package uk.ac.cam.cl.waytotheclinic;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static uk.ac.cam.cl.waytotheclinic.LandingPage.searchString;

public class MapFragment extends Fragment implements OnMapReadyCallback{
    MapView mapView = null;
    static GoogleMap googleMap;
    private String[] populatedTiles;
    private int Floor = 1;

    private TileOverlay mapOverlay;
    private TileOverlay pathOverlay;
    private TileOverlay locOverlay;

    private PathTileProvider pathTileProvider;
    private LocTileProvider locTileProvider;


    /**
     * This method is called once the map is available to be written to
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        //region Map Provider
        {
            TileProvider mapTileProvider = new UrlTileProvider(256, 256) {
                @Override
                public URL getTileUrl(int x, int y, int zoom) {
                    try {
                        //if the tile is blank just use the blank tile
                        if (!tilePopulated(Floor, zoom, x, y))
                            return getCachedFileOrGetRemote("blank.png");

                        //Otherwise return the correct tile
                        return getCachedFileOrGetRemote(String.format("TileMap%d/%d/%d/%d.png", Floor, zoom, x, y));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                private String baseTilePath = (getActivity().getExternalFilesDir(null).getAbsolutePath()) + "/TileStore/";
//                private String remoteTilePath = "http://cjj39.user.srcf.net/WayToTheClinic/";
                private String remoteTilePath = "https://s3.eu-west-2.amazonaws.com/waytoclinic/Finalised+Maps/";

                public URL getCachedFileOrGetRemote(String path) throws MalformedURLException {
                    File local = new File(baseTilePath + path);
                    if(!local.exists()) {
                        //load data from server then write to local
                        URL remoteURL = new URL(remoteTilePath + path);

                        try {
                            BufferedInputStream in = new BufferedInputStream(
                                    remoteURL.openStream());

                            local.getParentFile().mkdirs();

                            BufferedOutputStream out = new BufferedOutputStream(
                                    new FileOutputStream(local));

                            byte[] buffer = new byte[128];
                            for(int read; (read = in.read(buffer)) > 0; ){
                                out.write(buffer, 0, read);
                            }

                            in.close();
                            out.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return new URL("file://" + baseTilePath + path);
                }
            };
            mapOverlay = googleMap.addTileOverlay(new TileOverlayOptions().zIndex(1).tileProvider(mapTileProvider));
        }
        //endregion

        //region Path Provider
        pathTileProvider = new PathTileProvider(256, this);
        pathOverlay = googleMap.addTileOverlay(new TileOverlayOptions().zIndex(2).tileProvider(pathTileProvider));
        //endregion
        Log.d("pathTileProvider", Boolean.toString(pathTileProvider != null));

        //region Location Provider
        locTileProvider = new LocTileProvider(256, this);
        locOverlay = googleMap.addTileOverlay(new TileOverlayOptions().zIndex(3).tileProvider(locTileProvider));
        //endregion

        // Set initial floor to floor 2.
        this.setFloor(2);


        // When the map is pressed, a position in longitude/latitude for the click is returned.
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                if(getActivity().findViewById(R.id.bottom_white_box) != null) {
                    setLocation(latLng);
                }
            }
        });
    }

    public void setLocation(LatLng latLng){

        locTileProvider.setLocation(new Point(latLng.latitude,latLng.longitude,0));

        if (LandingPage.clickedLocationMarker == null) {
            MarkerOptions op = new MarkerOptions();
            op.position(latLng);
            Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.clicked_loc_marker);
            Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(32.0F), dpToPx(32.0F), true);
            op.icon(BitmapDescriptorFactory.fromBitmap(bMapScaled));
            LandingPage.clickedLocationMarker = googleMap.addMarker(op);
        } else {
            LandingPage.clickedLocationMarker.setPosition(latLng);
        }

        // We currently have a LatLng in lat/long coordinates. We must convert this to a Point in map coordinates [0,1]
        Point mapLoc = fromLatLngToPoint(latLng);


        // Call getNearestVertex
        Context context = getActivity().getApplicationContext();
        HashSet<Vertex> vertexSet = (HashSet<Vertex>) LocationsProvider.generateVertices(context);
        HashMap<Vertex,Vertex> vertexMap = new HashMap<>();

        for(Vertex vertex : vertexSet) {
            vertexMap.put(vertex,vertex);
        }

        Vertex closest = LandingPage.getNearestVertex(mapLoc.x,mapLoc.y,getFloor(),960,vertexMap);

        ArrayList<String> labelsList = closest.getLabels();
        if(labelsList != null && labelsList.size() != 0) {
            String topLabel = labelsList.get(0);
            Log.d("Closest Point:", topLabel.toString());

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 1);
            googleMap.animateCamera(cameraUpdate);

            searchString = topLabel;
            LandingPage.toClosestVertex = closest;

            ConstraintLayout bottom_white_box = getActivity().findViewById(R.id.bottom_white_box);
            ConstraintLayout main_layout = getActivity().findViewById(R.id.main_layout);
            ConstraintLayout directions = getActivity().findViewById(R.id.directions);
            TextView search_term = getActivity().findViewById(R.id.search_term);
            FloatingActionButton ae_button = getActivity().findViewById(R.id.ae_button);

            // Make bottom bar containing ->DIRECTIONS button appear
            bottom_white_box.setVisibility(View.VISIBLE);
            ae_button.setVisibility(View.INVISIBLE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(main_layout);
            constraintSet.connect(R.id.my_location_button, ConstraintSet.BOTTOM, R.id.bottom_white_box, ConstraintSet.TOP, dpToPx(16.0F));
            constraintSet.applyTo(main_layout);

            // Close keyboard
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

            search_term.setText(searchString);
            directions.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getBaseContext(), DirectionsPage.class);
                    startActivity(intent);
                }
            });
        }

        //invalidate cache to cause update
        locOverlay.clearTileCache();
        mapView.invalidate();
    }

    public static Point fromLatLngToPoint(LatLng latLng) {
        Double x = (latLng.longitude + 180) / 360;
        Double y = ((1 - Math.log(Math.tan(latLng.latitude * Math.PI / 180) + 1 / Math.cos(latLng.latitude * Math.PI / 180)) / Math.PI) / 2);
        return new Point(x, y, 0);
    };

    public static LatLng fromPointToLatLng(Point point){
        Double lng = point.x * 360 - 180;
        Double n = Math.PI - 2 * Math.PI * point.y;
        Double lat = (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
        return new LatLng(lat, lng);
    };


    //checks whether the tile is in the populated tiles list
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

    public List<Edge> truncatePath(List<Edge> edgesToSkipPast,
                                   List<Edge> edgePath, int instructionNumber) {

        ArrayList<Edge> remainingPath = new ArrayList<>();

        if (instructionNumber == 0) return remainingPath;

        Edge edgeToSkipPast = edgesToSkipPast.get(instructionNumber - 1);

        int i = 0;
        while (!edgePath.get(i).equals(edgeToSkipPast)) {
            i++;
        }

        i++; // skip past it too

        // put rest in remainingPath and return it
        while (i < edgePath.size()) {
            remainingPath.add(edgePath.get(i));
        }


        return remainingPath;
    }

    public void setPath(List<Point> path){
        Log.d("pathTileProviderBroke", Boolean.toString(pathTileProvider != null));
        pathTileProvider.setPath(path);

        //invalidate cache to cause update
        pathOverlay.clearTileCache();
        mapView.invalidate();
    }

    public void setPath(List<Edge> edgePath, double imgSize){
        if(pathOverlay != null) {
//            pathOverlay.clearTileCache();
        }

        final int edgeZOffset = 2;

        List<Point> result = new ArrayList<>();
        if(edgePath.size() == 0)
            return;

        Vertex pre = edgePath.get(0).getInVertex();
        result.add(getPointFromVertex(pre, edgeZOffset, imgSize));
        for(Edge edge : edgePath){
            Vertex post = edge.getOutVertex();
            result.add(getPointFromVertex(post, edgeZOffset, imgSize));
        }

        setPath(result);
    }

    public static Point getPointFromVertex(Vertex v, int ZOffset, double imgSize){
        double x = ((double)v.getX())/imgSize;
        double y = ((double)v.getY())/imgSize;
        int floor = v.getZ() + ZOffset;
        return new Point(x, y, floor);
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
//                    url = new URL("http://cjj39.user.srcf.net/WayToTheClinic/populatedTiles.txt");
                    url = new URL("https://s3.eu-west-2.amazonaws.com/waytoclinic/Finalised+Maps/populatedTiles.txt");
                    Scanner s = new Scanner(new BufferedInputStream(url.openStream()));
                    String input = s.nextLine();
                    String[] splitInput = input.split(";");
                    populatedTiles = Arrays.copyOfRange(splitInput, 0, splitInput.length - 1);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchElementException e) {
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

    public static class Point{
        double x;
        double y;
        int floor;

        public Point(double X, double Y, int Floor){
            x = X;
            y = Y;
            floor = Floor;
        }
    }

    public int dpToPx(Float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
