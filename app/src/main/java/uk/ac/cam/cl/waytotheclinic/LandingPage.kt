package uk.ac.cam.cl.waytotheclinic

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_landing_page.*
import android.widget.Toast
import android.support.constraint.ConstraintSet
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.ImageViewCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import kotlinx.android.synthetic.main.app_landing_page.*
import android.view.inputmethod.InputMethodManager


class LandingPage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mapFragment: MapFragment? = null
    private val PLACES = arrayOf("Belgium", "Frodo", "France", "Italy", "Germany", "Spain")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        mapFragment = Fragment.instantiate(this@LandingPage, MapFragment::class.java.getName()) as MapFragment?

        supportFragmentManager.beginTransaction().replace(R.id.map_id, mapFragment)

        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, PLACES)
        val textView = findViewById(R.id.places_list) as AutoCompleteTextView
        textView.setAdapter<ArrayAdapter<String>>(adapter)



        menu_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0)
                if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    drawer_layout.openDrawer(GravityCompat.START)
                }
            }
        })


        drawer_layout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View?, slideOffset: Float) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0)
            }
            override fun onDrawerClosed(drawerView: View?) {}
        })



        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            top_green_box.getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGING);
        }


        var y1: Float = 0.0F
        var y2: Float = 0.0F
        var minSwipeDist: Float = 50.0F
        val constraintSet = ConstraintSet()
        top_green_box.setOnClickListener(null);
        val swipeListener = View.OnTouchListener { view, motionEvent ->
            when (motionEvent.getAction()) {

                MotionEvent.ACTION_DOWN -> {
                    y1 = motionEvent.getY()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    y2 = motionEvent.getY()

                    if (y2 - y1 > minSwipeDist) {
                        Toast.makeText(this@LandingPage, "Down swipe", Toast.LENGTH_SHORT).show()
                        val params = top_green_box.getLayoutParams()
                        params.height = dpToPx(300.0F)
                        top_green_box.setLayoutParams(params)

                        //menu_button.setColorFilter(R.color.colorWhite)
                        ImageViewCompat.setImageTintList(menu_button, ColorStateList.valueOf(ContextCompat.getColor
                        (this@LandingPage, R.color.colorWhite)));

                        constraintSet.clone(top_green_box)
                        constraintSet.connect(R.id.places_list, ConstraintSet.START, R.id.top_white_box, ConstraintSet.START, dpToPx(8.0F))
                        constraintSet.applyTo(top_green_box)
                    } else if (y1 - y2 > minSwipeDist) {
                        Toast.makeText(this@LandingPage, "Up swipe", Toast.LENGTH_SHORT).show()
                        val params = top_green_box.getLayoutParams()
                        params.height = dpToPx(60.0F)
                        top_green_box.setLayoutParams(params)


                        ImageViewCompat.setImageTintList(menu_button, ColorStateList.valueOf(ContextCompat.getColor
                        (this@LandingPage, R.color.colorDarkGreen)));

                        constraintSet.clone(top_green_box)
                        constraintSet.connect(R.id.places_list, ConstraintSet.START, R.id.menu_button, ConstraintSet.END, dpToPx(12.0F))
                        constraintSet.applyTo(top_green_box)

                    }
                    true
                }
            }
            false
        }

        top_green_box.setOnTouchListener(swipeListener);
        menu_button.setOnTouchListener(swipeListener);
        places_list.setOnTouchListener(swipeListener);


        places_list.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(view: View, hasFocus: Boolean) {
                if(!hasFocus) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        })
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu) : Boolean {
        val inflater = getMenuInflater() as MenuInflater
        inflater.inflate(R.menu.activity_draw_drawer, menu)
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_first_floor -> {
                // TODO Switch map to first floor
                Toast.makeText(this@LandingPage, "First floor", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_second_floor -> {
                // TODO Switch map to second floor
                Toast.makeText(this@LandingPage, "Second floor", Toast.LENGTH_SHORT).show()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun dpToPx(value: Float) : Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().displayMetrics).toInt()
    }

}
