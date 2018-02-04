package uk.ac.cam.cl.waytotheclinic

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_landing_page.*




class LandingPage : AppCompatActivity() {

    private var mapFragment: MapFragment? = null
    private val PLACES = arrayOf("Belgium", "France", "Italy", "Germany", "Spain")

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
    }


}
