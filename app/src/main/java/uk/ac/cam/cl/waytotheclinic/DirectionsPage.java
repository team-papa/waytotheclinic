package uk.ac.cam.cl.waytotheclinic;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DirectionsPage extends LandingPage {

    ImageButton back_button;
    AutoCompleteTextView from_box;
    AutoCompleteTextView to_box;
    CheckBox check_box;
    TextView check_box_text;
    ListView instructions_list;
    ConstraintLayout instructions;
    DrawerLayout drawer_layout_dir;

    ConstraintLayout instructions_header;
    MapFragment mapFragment2;


    Map<String, String> extrahm = new HashMap<>();

    int numberInstr = 0;
    int instrLength = 0;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions_page);

        mapFragment2 = (MapFragment) getFragmentManager().getFragment(mapBundle, "map");
        back_button = findViewById(R.id.back_button);
        from_box = findViewById(R.id.from_box);
        to_box = findViewById(R.id.to_box);
        check_box = findViewById(R.id.check_box);
        check_box_text = findViewById(R.id.check_box_text);
        instructions_list = findViewById(R.id.instructions_list);
        instructions = findViewById(R.id.instructions);
        instructions_header = findViewById(R.id.instructions_header);
        drawer_layout_dir = findViewById(R.id.drawer_layout_dir);
        NavigationView nav_view = findViewById(R.id.nav_view_dir);



        // I really wish I could get rid of this but I need syncState()
        ActionBarDrawerToggle toggle =  new ActionBarDrawerToggle(
                this,
                drawer_layout_dir,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer_layout_dir.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);


        // Retrieve value of checkbox
        check_box.setChecked(noStairs);

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
                handlePathBuilding();
            }
        });


        // Make "back" button look nice
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.back_96);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, dpToPx(24.0F), dpToPx(24.0F), true);
        back_button.setImageBitmap(bMapScaled);

        // Make "back" button return to landing page
        back_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Make destination box show the search term
        to_box.setText(toClosestVertex.toString());

        // Enhance the places list of the "from" search box with the user's location
        List<Map<String, String>> enhancedPlacesList = new ArrayList<>(placesList);
        extrahm = new HashMap<>();
        extrahm.put("name", "My location");
        extrahm.put("icon", Integer.toString(R.drawable.target_50));
        // TODO: implement a special vertex for my location
        enhancedPlacesList.add(0, extrahm);


        // Make both search boxes have a list of all the places available
        from_box.setAdapter(new SimpleAdapter(getBaseContext(), enhancedPlacesList, R.layout.autocomplete_layout, from, to));
        from_box.setDropDownHorizontalOffset(dpToPx(-8.0F));
        from_box.setDropDownVerticalOffset(dpToPx(+10.0F));
        from_box.setDropDownHeight(dpToPx(280.0F));
        to_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));
        to_box.setDropDownHorizontalOffset(dpToPx(-8.0F));
        to_box.setDropDownVerticalOffset(dpToPx(+10.0F));
        to_box.setDropDownHeight(dpToPx(240.0F));


        // Nasty.
        View.OnTouchListener touchy = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int from_white_box_width = findViewById(R.id.from_white_box).getWidth(); // to and from white boxes are identical
                        from_box.setDropDownWidth(from_white_box_width);
                        to_box.setDropDownWidth(from_white_box_width);
                    case MotionEvent.ACTION_UP:
                        swipeDownInstructions();
                    default:
                        return false;
                }
            }
        };
        from_box.setOnTouchListener(touchy);
        to_box.setOnTouchListener(touchy);


        // Make search boxes react to choices of user
        AdapterView.OnItemClickListener fromItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                from_box.clearFocus();

                // Add item to recent searches
                if(!hm.get("icon").equals(Integer.toString(R.drawable.target_50))) {
                    addRecentSearch(hm, placesList, history);
                    List<Map<String, String>> enhancedPlacesList = new ArrayList<>(placesList);
                    enhancedPlacesList.add(0, extrahm);
                    from_box.setAdapter(new SimpleAdapter(getBaseContext(), enhancedPlacesList, R.layout.autocomplete_layout, from, to));
                }


                // RICHIE: from label to vertex
                fromClosestVertex = fromLabelToVertex(hm.get("name"));
                from_box.setText(fromClosestVertex.toString());

                handlePathBuilding();

            }
        };
        from_box.setOnItemClickListener(fromItemClickListener);

        AdapterView.OnItemClickListener toItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                to_box.clearFocus();

                // Add item to recent searches
                addRecentSearch(hm, placesList, history);
                to_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));

                // RICHIE: from label to vertex
                toClosestVertex = fromLabelToVertex(hm.get("name"));
                to_box.setText(toClosestVertex.toString());

                handlePathBuilding();
            }
        };
        to_box.setOnItemClickListener(toItemClickListener);



        // Make text based instructions block transition nicely
        instructions.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        View.OnTouchListener swipeListener = new View.OnTouchListener() {
            private Float y1 = 0.0F;
            private Float y2 = 0.0F;
            final Float minSwipeDist = 50.0F;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y1 = motionEvent.getY();
                    case MotionEvent.ACTION_UP:
                        y2 = motionEvent.getY();
                        if (y2 - y1 > minSwipeDist) {
                            swipeDownInstructions();
                        } else if (y1 - y2 > minSwipeDist) {
                            swipeUpInstructions();
                        }
                    default:
                        return false;
                }
            }
        };
        instructions_header.setOnTouchListener(swipeListener);
    }


    public void swipeDownInstructions() {
        ConstraintSet mainConstraintSet = new ConstraintSet();
        mainConstraintSet.clone(instructions);
        mainConstraintSet.connect(R.id.instructions_header, ConstraintSet.BOTTOM, R.id.instructions, ConstraintSet.BOTTOM, dpToPx(0.0F));
        mainConstraintSet.applyTo(instructions);

        // Rotate the two swipe up icons to point upwards
        Animation an = new RotateAnimation(180.0F, 360.0F, dpToPx(8.0F),  dpToPx(8.0F));

        // Set the animation's parameters
        an.setDuration(1000);               // duration in ms
        an.setRepeatCount(0);                // -1 = infinite repeated
        an.setRepeatMode(Animation.REVERSE); // reverses each repeat
        an.setFillAfter(true);               // keep rotation after animation

        // Aply animation to image view
        findViewById(R.id.instructions_swipe_up_left).setAnimation(an);
        findViewById(R.id.instructions_swipe_up_right).setAnimation(an);
    }


    public void swipeUpInstructions() {
        ConstraintSet mainConstraintSet = new ConstraintSet();
        mainConstraintSet.clone(instructions);
        mainConstraintSet.connect(R.id.instructions_header, ConstraintSet.BOTTOM, R.id.instructions, ConstraintSet.BOTTOM, instrLength);
        mainConstraintSet.applyTo(instructions);

        // Rotate the two swipe up icons to point downwards
        Animation an = new RotateAnimation(0.0F, 180.0F, dpToPx(8.0F),  dpToPx(8.0F));

        // Set the animation's parameters
        an.setDuration(1000);               // duration in ms
        an.setRepeatCount(0);                // -1 = infinite repeated
        an.setRepeatMode(Animation.REVERSE); // reverses each repeat
        an.setFillAfter(true);               // keep rotation after animation

        // Aply animation to image view
        findViewById(R.id.instructions_swipe_up_left).setAnimation(an);
        findViewById(R.id.instructions_swipe_up_right).setAnimation(an);
    }


    public void handlePathBuilding() {
        if(fromClosestVertex != null && toClosestVertex != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            // Make swipe up box show
            instructions.setVisibility(View.VISIBLE);

            // Get path in terms of edges and vertices
            PathFinder pf = new PathFinder();
            List<Edge> path = pf.getPath(fromClosestVertex, toClosestVertex, noStairs);
            List<Instruction> textBasedDirections = pf.getTextDirections(path);
            TextInstructionsAdapter instrAdapter = new TextInstructionsAdapter(
                    getApplicationContext(),
                    R.layout.instruction_layout,
                    textBasedDirections);
            instructions_list.setAdapter(instrAdapter);
            numberInstr = textBasedDirections.size();
            instrLength = dpToPx(numberInstr*60.0F);
            if (instrLength > dpToPx(360.0F)) instrLength = dpToPx(360.0F);


            // TODO render path between fromClosestVertex to toClosestVertex

        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer_layout_dir = findViewById(R.id.drawer_layout_dir);

        // Handle side-menu item-clicks
        switch (item.getItemId()) {
            case R.id.nav_first_floor:
                mapFragment.setFloor(1);
                Toast.makeText(getApplicationContext(), "First floor", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_second_floor:
                mapFragment.setFloor(2);
                Toast.makeText(getApplicationContext(), "Second floor", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer_layout_dir.closeDrawer(GravityCompat.START);
        return true;
    }

}
