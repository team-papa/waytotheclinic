package uk.ac.cam.cl.waytotheclinic

import android.Manifest
import android.animation.LayoutTransition
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_landing_page.*
import android.support.constraint.ConstraintSet
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.widget.*
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener


class LandingPage : AppCompatActivity() {

    private var mapFragment: MapFragment? = null
    private val PLACES = arrayOf("Belgium", "France", "Italy", "Germany", "Spain")
    private val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1
    private var mCurrentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        mapFragment = Fragment.instantiate(this@LandingPage, MapFragment::class.java.getName()) as MapFragment?

        supportFragmentManager.beginTransaction().replace(R.id.map_id, mapFragment)

        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, PLACES)
        val textView = findViewById(R.id.places_list) as AutoCompleteTextView
        textView.setAdapter<ArrayAdapter<String>>(adapter)



        menu_button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View) {
                val params = top_green_box.getLayoutParams()
                params.height = 500
                top_green_box.setLayoutParams(params)
            }
        })


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            top_green_box.getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGING);
        }

        var y1: Float = 0.0F
        var y2: Float = 0.0F
        var minSwipeDist: Float = 50.0F
        val constraintSet = ConstraintSet()
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
            true
        }

        top_green_box.setOnTouchListener(swipeListener);
        menu_button.setOnTouchListener(swipeListener);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            var mFusedLocationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                        if (location != null) {
                            mCurrentLocation = location
                            val latitude = findViewById(R.id.latitudeText) as TextView
                            latitude.setText("Latitude: ${location.latitude.toString()}")
                            val longitude = findViewById(R.id.longitudeText) as TextView
                            longitude.setText("Longitude: ${location.longitude.toString()}")

                        }
                    })
        }
        else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var mFusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                if (location != null) {
                    mCurrentLocation = location
                    val latitude = findViewById(R.id.latitudeText) as TextView
                    latitude.setText("Latitude: ${location.latitude.toString()}")
                    val longitude = findViewById(R.id.longitudeText) as TextView
                    longitude.setText("Longitude: ${location.longitude.toString()}")
                }
            })
        }
    }

    fun dpToPx(value: Float) : Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().displayMetrics).toInt()
    }

}
