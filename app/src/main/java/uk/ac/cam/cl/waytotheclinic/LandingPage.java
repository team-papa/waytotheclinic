package uk.ac.cam.cl.waytotheclinic;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;


public class LandingPage  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private String[] places = new String[]{"Belgium", "France", "Frodo", "Germany", "Italy", "Spain"};
    private final int historySize = 3;
    private List<String> history = new ArrayList<>();
    ConstraintLayout top_green_box;
    CustomAutoCompleteTextView search_box;
    DrawerLayout drawer_layout;
    NavigationView nav_view;
    ImageButton menu_button;
    CheckBox check_box;
    TextView check_box_text;
    FloatingActionButton ae_button;
    FloatingActionButton my_location_button;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        top_green_box = findViewById(R.id.top_green_box);
        search_box = findViewById(R.id.search_box);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        menu_button = findViewById(R.id.menu_button);
        check_box = findViewById(R.id.check_box);
        check_box_text = findViewById(R.id.check_box_text);
        ae_button = findViewById(R.id.ae_button);
        my_location_button = findViewById(R.id.my_location_button);

        MapFragment mapFragment = new MapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.map_id, mapFragment);


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


        final List<Map<String, String>> placesList = new ArrayList<Map<String, String>>();
        for(int i=0; i<places.length; i++) {
            placesList.add(placesQueue.poll());
        }

        // Keys used in Hashmap
        final String[] from = { "name","icon"};

        // Ids of views in listview_layout
        final int[] to = { R.id.name, R.id.icon};

        // To make search box drop down align correctly
        search_box.setThreshold(1);
        search_box.setDropDownHeight(dpToPx(300.0F));
        search_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));
        search_box.setDropDownHorizontalOffset(dpToPx(-8.0F));
        search_box.setDropDownVerticalOffset(dpToPx(+10.0F));


        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                search_box.clearFocus();
                search_box.setText(hm.get("name"));
                swipeUp();

                // Add item to recent searches
                addRecentSearch(hm, placesList, history);
                search_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));

                // TODO add action that moves map to selected place hm.get("name")
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
        ActionBarDrawerToggle toggle =  new ActionBarDrawerToggle(
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


        // Make text associated with checkbox clickable
        check_box_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check_box.isChecked()) {
                    check_box.setChecked(false);
                } else {
                    check_box.setChecked(true);
                }
            }
        });


        // Make "my location" button responsive
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.myloc72);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(36.0F), dpToPx(36.0F), true);
        my_location_button.setImageBitmap(bMapScaled);
        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "My location!", Toast.LENGTH_SHORT).show();
                // TODO move map to user's location
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
                                //it was not a tap

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
                                Toast.makeText(getApplicationContext(), "Here's the way to the closest A&E room", Toast.LENGTH_SHORT).show();
                                // TODO show path to nearest AE room

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
    private void addRecentSearch(HashMap<String, String> hm, List<Map<String, String>> placesList, List<String> history) {
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
    public void onBackPressed() {
        drawer_layout = findViewById(R.id.drawer_layout);
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                // TODO Switch map to first floor
                Toast.makeText(getApplicationContext(), "First floor", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_second_floor:
                // TODO Switch map to second floor
                Toast.makeText(getApplicationContext(), "Second floor", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void swipeDown() {
        Toast.makeText(getApplicationContext(), "Down swipe", Toast.LENGTH_SHORT).show();

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
    }

    public void swipeUp() {
        Toast.makeText(getApplicationContext(), "Up swipe", Toast.LENGTH_SHORT).show();

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
    }

    public int dpToPx(Float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
