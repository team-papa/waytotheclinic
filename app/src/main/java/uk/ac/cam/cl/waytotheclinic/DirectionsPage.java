package uk.ac.cam.cl.waytotheclinic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DirectionsPage extends LandingPage {

    ImageButton back_button;
    AutoCompleteTextView from_box;
    AutoCompleteTextView to_box;

    Map<String, String> extrahm = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions_page);

        back_button = findViewById(R.id.back_button);
        from_box = findViewById(R.id.from_box);
        to_box = findViewById(R.id.to_box);

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
        to_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));
        to_box.setDropDownHorizontalOffset(dpToPx(-8.0F));
        to_box.setDropDownVerticalOffset(dpToPx(+10.0F));


        // Nasty.
        View.OnTouchListener touchy = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int from_white_box_width = findViewById(R.id.from_white_box).getWidth(); // to and from white boxes are identical
                        Toast.makeText(getApplicationContext(), "FROM WHITE BOX WIDTH " + Integer.toString(from_white_box_width), Toast.LENGTH_SHORT).show();
                        from_box.setDropDownWidth(from_white_box_width);
                        to_box.setDropDownWidth(from_white_box_width);
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
                from_box.setText(hm.get("name"));

                // Add item to recent searches
                if(!hm.get("icon").equals(Integer.toString(R.drawable.target_50))) {
                    addRecentSearch(hm, placesList, history);
                    List<Map<String, String>> enhancedPlacesList = new ArrayList<>(placesList);
                    enhancedPlacesList.add(0, extrahm);
                    from_box.setAdapter(new SimpleAdapter(getBaseContext(), enhancedPlacesList, R.layout.autocomplete_layout, from, to));
                }


                // RICHIE: from label to vertex
                fromClosestVertex = fromLabelToVertex(hm.get("name"));

                if(fromClosestVertex != null && toClosestVertex != null) {
                    Toast.makeText(getApplicationContext(), "1 from" + fromClosestVertex.toString() + " to " + toClosestVertex.toString(), Toast.LENGTH_SHORT).show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    // TODO render path between fromClosestVertex to toClosestVertex

                }
            }
        };
        from_box.setOnItemClickListener(fromItemClickListener);

        AdapterView.OnItemClickListener toItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                to_box.clearFocus();
                to_box.setText(hm.get("name"));

                // Add item to recent searches
                addRecentSearch(hm, placesList, history);
                to_box.setAdapter(new SimpleAdapter(getBaseContext(), placesList, R.layout.autocomplete_layout, from, to));

                // RICHIE: from label to vertex
                toClosestVertex = fromLabelToVertex(hm.get("name"));

                if(fromClosestVertex != null && toClosestVertex != null) {
                    Toast.makeText(getApplicationContext(), "2 from" + fromClosestVertex.toString() + " to " + toClosestVertex.toString(), Toast.LENGTH_SHORT).show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    // TODO render path between fromClosestVertex to toClosestVertex
                }
            }
        };
        to_box.setOnItemClickListener(toItemClickListener);


    }
}
