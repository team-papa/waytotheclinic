package uk.ac.cam.cl.waytotheclinic;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static uk.ac.cam.cl.waytotheclinic.MapFragment.fromLatLngToPoint;
import static uk.ac.cam.cl.waytotheclinic.VertexComparator.ManhattanDistance2D;

public class LandingPage  extends AppCompatActivity implements LocationFragment.LocationListener, NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener {

    // UI Components
    ConstraintLayout main_layout;
    ConstraintLayout top_green_box;
    CustomAutoCompleteTextView search_box;
    DrawerLayout drawer_layout;
    ImageButton menu_button;
    private MapFragment map_fragment;
    CheckBox check_box;
    TextView check_box_text;
    FloatingActionButton ae_button;
    FloatingActionButton my_location_button;
    ConstraintLayout bottom_white_box;
    ConstraintLayout directions;
    TextView search_term;


    // Lift/stairs checkbox
    static boolean noStairs = true;

    // List of all the places, passed around between classes. Necessary for adapters in search bars.
    static List<Map<String, String>> placesList = new ArrayList<>();

    // History of recent searches
    static List<String> history = new ArrayList<>();

    // Number of recent searches that we remember
    final int historySize = 3;

    // Keys used in Hashmap
    final String[] from = { "name","icon"};

    // Ids of views in listview_layout
    final int[] to = { R.id.name, R.id.icon};

    // Source and destination vertices
    static Vertex fromClosestVertex;
    static Vertex toClosestVertex;
    static String searchString;

    public static MapFragment.Point myLocation = new MapFragment.Point(26, 98.6,2);;
    private Marker myLocationMarker;
    public static MapFragment.Point clickedLocation;
    public static Marker clickedLocationMarker;

    // Location related
    private final String LOCATION_FRAGMENT_TAG = "location-fragment";
    private final int LOCATION_PERMISSIONS = 1;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mStepCounter;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private Float mBearing;
    LinkedList<Float> mBearingQueue;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        map_fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
        main_layout = findViewById(R.id.main_layout);
        top_green_box = findViewById(R.id.top_green_box);
        search_box = findViewById(R.id.search_box);
        drawer_layout = findViewById(R.id.drawer_layout);
        NavigationView nav_view = findViewById(R.id.nav_view);
        menu_button = findViewById(R.id.menu_button);
        check_box = findViewById(R.id.check_box);
        check_box_text = findViewById(R.id.check_box_text);
        ae_button = findViewById(R.id.ae_button);
        my_location_button = findViewById(R.id.my_location_button);
        bottom_white_box = findViewById(R.id.bottom_white_box);
        directions = findViewById(R.id.directions);
        search_term = findViewById(R.id.search_term);

        // Generate all the searchable labels on the map. Call them places.
        Set<String> places = LocationsProvider.generateLocations(getApplicationContext());
        for(String label: places) {
            Log.d("Label", label);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> recentSearches = preferences.getStringSet("RecentSearches", null);
        if (recentSearches != null) {
            history.clear();
            history.addAll(recentSearches);
            Collections.sort(history);
        }

        PriorityQueue<Map<String, String>> placesQueue = new PriorityQueue<>(200,
                new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> m1, Map<String, String> m2) {
                        String icon1 = m1.get("icon");
                        String icon2 = m2.get("icon");
                        if(icon1.equals(Integer.toString(R.drawable.history_50)) &&
                                icon2.equals(Integer.toString(R.drawable.marker_50))) {
                            return -1;
                        } else if(icon2.equals(Integer.toString(R.drawable.history_50)) &&
                                icon1.equals(Integer.toString(R.drawable.marker_50))) {
                            return +1;
                        } else {
                            String name1 = m1.get("name");
                            String name2 = m2.get("name");
                            return name1.compareTo(name2);
                        }
                    }
                }
        );

        for(String place: places) {
            Map<String, String> hm = new HashMap<>();
            hm.put("name", place);
            if(history.contains(place)) {
                hm.put("icon", Integer.toString(R.drawable.history_50));
            } else {
                hm.put("icon", Integer.toString(R.drawable.marker_50));
            }
            placesQueue.add(hm);
        }

        if(placesList.isEmpty()) {
            for (String place : places) {
                placesList.add(placesQueue.poll());
            }
        }

        // Keys used in Hashmap
        final String[] from = { "name","icon"};

        // Ids of views in listview_layout
        final int[] to = { R.id.name, R.id.icon};

        LocationFragment locationFragment = new LocationFragment();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(locationFragment, LOCATION_FRAGMENT_TAG).commit();

        // To make search box drop down align correctly
        search_box.setThreshold(1);
        search_box.setDropDownHeight(dpToPx(150.0F));
        search_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));
        search_box.setDropDownHorizontalOffset(dpToPx(-8.0F));
        search_box.setDropDownVerticalOffset(dpToPx(+10.0F));


        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                search_box.clearFocus();
                swipeUp();

                // Add item to recent searches
                addRecentSearch(hm, placesList, history);
                search_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));

                searchString = hm.get("name");
                Log.d("searchString", searchString);

                // RICHIE: from label to vertex
                toClosestVertex = getClosestMatchingVertex(searchString);
                search_box.setText(searchString);

                // Add action that moves map to selected place hm.get("name") (which is now closestVertex)
                clickedLocation = MapFragment.getPointFromVertex(toClosestVertex, 2, 960);
                LatLng latLng = MapFragment.fromPointToLatLng(clickedLocation);
                if (clickedLocationMarker == null) {
                    MarkerOptions op = new MarkerOptions();
                    op.position(latLng);
                    Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.clicked_loc_marker);
                    Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(32.0F), dpToPx(32.0F), true);
                    op.icon(BitmapDescriptorFactory.fromBitmap(bMapScaled));
                    clickedLocationMarker = MapFragment.googleMap.addMarker(op);
                } else {
                    clickedLocationMarker.setPosition(latLng);
                }


                // Move camera to focus on destination
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 1);
                MapFragment.googleMap.animateCamera(cameraUpdate);

                // Make bottom bar containing ->DIRECTIONS button appear
                bottom_white_box.setVisibility(View.VISIBLE);
                ae_button.setVisibility(View.INVISIBLE);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(main_layout);
                constraintSet.connect(R.id.my_location_button, ConstraintSet.BOTTOM, R.id.bottom_white_box, ConstraintSet.TOP, dpToPx(16.0F));
                constraintSet.applyTo(main_layout);

                search_term.setText(searchString);
                directions.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getBaseContext(), DirectionsPage.class);
                        startActivity(intent);
                    }
                });
            }
        };
        search_box.setOnItemClickListener(itemClickListener);



        // On click, the menu button opens the side menu and closes the keyboard (if open)
        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    drawer_layout.openDrawer(GravityCompat.START);
                }
            }
        });


        // When opening the side menu, close keyboard. This handles the case of swipe opening.
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerOpened(View drawerView) {}

            @Override
            public void onDrawerClosed(View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });


        // I really wish I could get rid of this but I need syncState()
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer_layout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);


        // Implementation of smooth sliding transition for the green box containing the search bar.
        top_green_box.setOnClickListener(null);
        top_green_box.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        View.OnTouchListener swipeListener = new View.OnTouchListener() {
            private Float y1 = 0.0F;
            private Float y2 = 0.0F;
            final Float minSwipeDist = 50.0F;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y1 = motionEvent.getY();

                        int top_white_box_width_1 = findViewById(R.id.top_white_box).getWidth();
                        search_box.setDropDownWidth(top_white_box_width_1);
                    case MotionEvent.ACTION_UP:
                        y2 = motionEvent.getY();
                        if (y2 - y1 > minSwipeDist) {
                            swipeDown();
                        } else if (y1 - y2 > minSwipeDist) {
                           swipeUp();
                        }
                    default:
                        return false;
                }
            }
        };

        // Apply above listener to multiple elements
        top_green_box.setOnTouchListener(swipeListener);
        menu_button.setOnTouchListener(swipeListener);
        search_box.setOnTouchListener(swipeListener);


        // Closes keyboard when search bar not focused
        search_box.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });


        // Make text associated with checkbox clickable + change noStairs accordingly
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check_box.isChecked()) {
                    check_box.setChecked(false);
                } else {
                    check_box.setChecked(true);
                }
            }
        };
        check_box_text.setOnClickListener(onClickListener);

        check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                noStairs = b;
            }
        });

        // Make "my location" button responsive
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.myloc);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(24.0F), dpToPx(24.0F), true);
        my_location_button.setImageBitmap(bMapScaled);
        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "My location!", Toast.LENGTH_SHORT).show();
                LatLng latLng = new LatLng(myLocation.x, myLocation.y);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 1);
                if (myLocationMarker == null) {
                    MarkerOptions op = new MarkerOptions();
                    op.position(latLng);
                    Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.mylocmap);
                    Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(32.0F), dpToPx(32.0F), true);
                    op.icon(BitmapDescriptorFactory.fromBitmap(bMapScaled));
                    myLocationMarker = MapFragment.googleMap.addMarker(op);
                } else {
                    myLocationMarker.setPosition(latLng);
                }
                map_fragment.googleMap.animateCamera(cameraUpdate);
            }
        });


        // Make AE button responsive
        final long timeoutBetweenTaps = 8 * ViewConfiguration.getTapTimeout();
        ae_button.setOnTouchListener(new View.OnTouchListener() {
                Handler handler = new Handler();
                int numberOfTaps = 0;
                long lastTapTimeMs = 0;
                long touchDownMs = 0;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchDownMs = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                            handler.removeCallbacksAndMessages(null);

                            if ((System.currentTimeMillis() - touchDownMs) > timeoutBetweenTaps) {
                                // It was not a tap
                                numberOfTaps = 0;
                                lastTapTimeMs = 0;
                                break;
                            }

                            if (numberOfTaps > 0
                                    && (System.currentTimeMillis() - lastTapTimeMs) < timeoutBetweenTaps) {
                                numberOfTaps += 1;
                                if (numberOfTaps == 2)
                                    Toast.makeText(getApplicationContext(), "1 more tap", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "2 more taps", Toast.LENGTH_SHORT).show();
                                numberOfTaps = 1;
                            }

                            lastTapTimeMs = System.currentTimeMillis();

                            if (numberOfTaps == 3) {
                                Toast.makeText(getApplicationContext(), "Here's the way to Accident & Emergency ", Toast.LENGTH_SHORT).show();
                                // TODO show path to nearest AE room
                                Intent intent = new Intent(getBaseContext(), DirectionsPage.class);
                                searchString = "A and E";
                                intent.putExtra("ae", true);
                                startActivity(intent);


                            } else if (numberOfTaps == 2) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Tap faster!", Toast.LENGTH_SHORT).show();
                                    }
                                }, timeoutBetweenTaps);
                            }
                    }
                    return true;
                }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mBearingQueue = new LinkedList<>();
        mBearingQueue.offer(new Float(0.0));
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }


    @Override
    public void onStop() {
        // Remember the recent searches
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> recentSearches = new TreeSet<>();
        recentSearches.addAll(history);
        editor.putStringSet("RecentSearches", recentSearches);
        editor.apply();

        super.onStop();
    }

    // Once a search is made, the location is added to the history of searches list (of predetermined,
    // limited capacity), and introduced at the top of @placesList, with a new icon
    public void addRecentSearch(HashMap<String, String> hm, List<Map<String, String>> placesList, List<String> history) {
        placesList.remove(hm);
        hm.put("icon", Integer.toString(R.drawable.history_50));
        placesList.add(0, hm);
        String name = hm.get("name");
        if(history.contains(name)) {
            history.remove(name);
        }
        history.add(0, name);

        if(history.size() > historySize) {
            String normalSearchName = placesList.get(historySize).get("name");
            history.remove(historySize);

            // In placesList we have historySize many items with clock icon, and we need to reinsert
            // the entry normalSearchName, but with a marker icon. Need to take care of lexicographic order
            // So we should do pairwise comparisons with each item in placesList, starting with
            // the first one with marker icon, i.e. placesList.get(historySize+1), because at
            // placesList.get(historySize) we have our normalSearchName entry

            int index = historySize;
            String currentName = placesList.get(historySize + 1).get("name");
            while(normalSearchName.compareTo(currentName) >= 0) {
                index += 1;
                if(placesList.size()-1 == index) {
                    // Need to add entry at the end of the list
                   break;
                } else {
                    currentName = placesList.get(index+1).get("name");
                }
            }
            Map<String, String> normalHm = placesList.get(historySize);
            normalHm.put("icon", Integer.toString(R.drawable.marker_50));
            placesList.remove(normalHm);
            placesList.add(index, normalHm);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions,
                                            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        Fragment frg = getSupportFragmentManager().findFragmentByTag(LOCATION_FRAGMENT_TAG);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.attach(frg);
        ft.detach(frg);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        drawer_layout = findViewById(R.id.drawer_layout);
        DrawerLayout drawer_layout_dir = findViewById(R.id.drawer_layout_dir);
        bottom_white_box = findViewById(R.id.bottom_white_box);
        search_box = findViewById(R.id.search_box);
        if (drawer_layout != null && drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else if  (drawer_layout_dir != null && drawer_layout_dir.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_dir.closeDrawer(GravityCompat.START);
        } else if(bottom_white_box != null && bottom_white_box.getVisibility() == View.VISIBLE) {
            bottom_white_box.setVisibility(View.INVISIBLE);
            ae_button.setVisibility(View.VISIBLE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(main_layout);
            constraintSet.connect(R.id.my_location_button, ConstraintSet.BOTTOM, R.id.bottom_white_box, ConstraintSet.TOP, dpToPx(8.0F));
            constraintSet.applyTo(main_layout);
            search_box.setText("");
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        check_box = findViewById(R.id.check_box);
        check_box.setChecked(noStairs);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_draw_drawer, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer_layout = findViewById(R.id.drawer_layout);

        // Handle side-menu item-clicks
        switch (item.getItemId()) {
            case R.id.nav_first_floor:
                map_fragment.setFloor(1);
                Toast.makeText(getApplicationContext(), "First floor", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_second_floor:
                map_fragment.setFloor(2);
                Toast.makeText(getApplicationContext(), "Second floor", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_third_floor:
                map_fragment.setFloor(3);
                Toast.makeText(getApplicationContext(), "Third floor", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void swipeDown() {
        ConstraintSet constraintSet = new ConstraintSet();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) top_green_box.getLayoutParams();
        params.height = dpToPx(300.0F);
        top_green_box.setLayoutParams(params);

        // Changing menu button color from green to white
        ImageViewCompat.setImageTintList(menu_button,
                ColorStateList.valueOf(ContextCompat.getColor(
                        getApplicationContext(), R.color.colorWhite)));

        constraintSet.clone(top_green_box);
        constraintSet.connect(R.id.search_box, ConstraintSet.START, R.id.top_white_box, ConstraintSet.START, dpToPx(8.0F));
        constraintSet.applyTo(top_green_box);
        search_box.setDropDownHorizontalOffset(dpToPx(-8.0F));
        search_box.clearFocus();
        search_box.setDropDownHeight(dpToPx(150.0F));

    }

    public void swipeUp() {
        ConstraintSet constraintSet = new ConstraintSet();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) top_green_box.getLayoutParams();
        params.height = dpToPx(60.0F);
        top_green_box.setLayoutParams(params);

        // Change menu button color from white to green
        ImageViewCompat.setImageTintList(menu_button,
                ColorStateList.valueOf(ContextCompat.getColor(
                        getApplicationContext(), R.color.colorDarkGreen)));

        constraintSet.clone(top_green_box);
        constraintSet.connect(R.id.search_box, ConstraintSet.START, R.id.menu_button, ConstraintSet.END, dpToPx(12.0F));
        constraintSet.applyTo(top_green_box);

        search_box.setDropDownHorizontalOffset(dpToPx(-50.0F));
        search_box.setDropDownHeight(dpToPx(280.0F));
    }


    public Vertex getClosestMatchingVertex(String searchTerm) {
        HashSet<Vertex> vertexSet = (HashSet<Vertex>) LocationsProvider.generateVertices(getApplicationContext());
        HashMap<Vertex,Vertex> vertexMap = new HashMap<>();

        for(Vertex vertex : vertexSet) {
            vertexMap.put(vertex,vertex);
        }

        MapFragment.Point myLocationPoint = MapFragment.fromLatLngToPoint(new LatLng(myLocation.x, myLocation.y));
        Vertex myLocationVertex = getNearestVertex(myLocationPoint.x, myLocationPoint.y, 2,960,vertexMap);

        if(searchTerm.equals("My location")) {
            LatLng latLng = new LatLng(myLocation.x, myLocation.y);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 1);
            if (myLocationMarker == null) {
                MarkerOptions op = new MarkerOptions();
                op.position(latLng);
                Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.mylocmap);
                Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(32.0F), dpToPx(32.0F), true);
                op.icon(BitmapDescriptorFactory.fromBitmap(bMapScaled));
                myLocationMarker = MapFragment.googleMap.addMarker(op);
            } else {
                myLocationMarker.setPosition(latLng);
            }
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment_dir)).googleMap.animateCamera(cameraUpdate);
            return myLocationVertex;
        }

        HashMap<Vertex, String> bestVertices = new HashMap<>();
        int bestLCS = 1; // set best LCS to 1, so we ignore vertices that have 0 match

        ArrayList<String> searchArray = new ArrayList<>(
                Arrays.asList(searchTerm.toLowerCase().split(" ")));

        for (Vertex v : vertexSet) {

            for (String label : v.getLabels()) {
                if(label != null) {
                    ArrayList<String> labelArray = new ArrayList<>(
                            Arrays.asList(label.toLowerCase().split(" ")));

                    int currLCS = new LongestCommonSubsequence<String>
                            (searchArray, labelArray).getLCS().size();

                    if (currLCS == bestLCS) {
                        bestVertices.put(v, label);
                    } else if (currLCS > bestLCS) {
                        bestVertices = new HashMap<>();
                        bestLCS = currLCS;
                        bestVertices.put(v, label);
                    }
                }
            }
        }

        // check if no vertices match
        if (bestVertices.size() > 0) {

            // choose closest one vertex
            int closest = Integer.MAX_VALUE;
            Vertex closestVertex = null;
            for (Map.Entry<Vertex, String> entry : bestVertices.entrySet()) {
                Vertex v = entry.getKey();
                String label = entry.getValue();

                int currDistance = VertexComparator.manhattanDistance(myLocationVertex, v);
                System.out.format("%s %s %d\n", v, label, currDistance);

                if (currDistance < closest) {
                    closest = currDistance;
                    closestVertex = v;
                }
            }

            System.out.println("Best vertex: " + closestVertex);

            return closestVertex;
        } else {
            System.out.println("No vertices found. Search for something else?");
        }

        return null;
    }

    public int dpToPx(Float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    // Methods below here are called by LocationFragment, part of the interface LocationFragment.LocationListener

    @Override
    public boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermissions() {
        ActivityCompat.requestPermissions(this,
            new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                          Manifest.permission.ACCESS_WIFI_STATE},
            LOCATION_PERMISSIONS);
    }

    // TODO for Alex: fix these before pushing to develop

    @Override
    public WifiManager getWifiManager() {
//        return (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        return null;
    }

    @Override
    public void startLocationUpdates(LocationRequest lr, LocationCallback lc) {
//        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(lr, lc, null);
    }

    @Override
    public void updateLocation(Location l) {

        //myLocation = l;
        Log.i("waytotheclinic", "waytotheclinic location updated: " + l.toString());

        double x = (l.getLatitude() - 52.173154) / (52.175751 - 52.173154) * (902 - 176) + 176;
        double y = (l.getLongitude() - 0.138020) / (0.143265 - 0.138020) * (562 - 362) + 562;
        // Floor number is altitude when we have data from WiFi
        // int floor = (int) l.getAltitude();

        MapFragment.Point p = new MapFragment.Point(x, y, 0);
        //map_fragment.setLocation(p);
        //map_fragment.setLocation(new MapFragment.Point(26, 98.6, 0));
    }

    // floor is -1 indexed
    // xd and yd are in [0,1]
    public static Vertex getNearestVertex(double xd, double yd, int floor,
                                          double squareSideLength, Map<Vertex, Vertex> vMap) {
        int nearestX = (int) (xd * squareSideLength);
        int nearestY = (int) (yd * squareSideLength);

        Vertex touched = new Vertex(nearestX, nearestY, floor);

        Vertex candidate = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Vertex v : vMap.keySet()) {
            Log.d("v", Integer.toString(v.getZ()));
            Log.d("touch", Integer.toString(touched.getZ()));
            // Only consider it if they are on the same floor
            if (v.getZ()+2 != touched.getZ()) {
                continue;
            }

            if (ManhattanDistance2D(touched, v) < bestDistance) {
                candidate = v;
                bestDistance = ManhattanDistance2D(touched, v);
            }
        }
        return candidate;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        }
        else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }
        /*else if (event.sensor == mStepCounter) {
            for(Float fl : event.values) {
                Log.d("values: ", fl.toString());
            }
        }*/
        updateOrientationAngles();
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        // "mOrientationAngles" now has up-to-date information.

        mBearing = mOrientationAngles[0];
        Boolean check = hasChangedDirection();
        //Log.d("Angle: ", mBearing.toString());
        //Log.d("Changed: ", check.toString());
    }

    public boolean hasChangedDirection() {
        if(mBearingQueue.size() >= 100) {
            mBearingQueue.poll();
        }
        Float oldDirection = mBearingQueue.peekFirst();
        if(Math.abs(mBearing - oldDirection) > 1) {
            mBearingQueue.clear();
            mBearingQueue.offer(mBearing);
            return true;
        }
        else {
            mBearingQueue.offer(mBearing);
            return false;
        }
    }
}
