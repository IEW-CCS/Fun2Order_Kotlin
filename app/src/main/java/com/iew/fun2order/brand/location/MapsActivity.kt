package com.iew.fun2order.brand.location

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_STORE


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var storeInfo: DETAIL_BRAND_STORE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        storeInfo = intent.extras?.getParcelable<DETAIL_BRAND_STORE>("STORE_INFO")  ?: null



    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val storeLatitude = storeInfo?.storeLatitude?.toDoubleOrNull()
        val storeLongitude = storeInfo?.storeLongitude?.toDoubleOrNull()

        if(storeLatitude!= null && storeLongitude != null )
        {
            val storeLocation = LatLng(storeLatitude, storeLongitude)
            val storeName = storeInfo?.storeName ?: ""
            val storePhone = "電話: ${storeInfo?.storePhoneNumber}"
            val storeAddress = "地址: ${storeInfo?.storeAddress}"

            val storeInformation = "${storePhone}\r\n${storeAddress}"

            mMap.setInfoWindowAdapter(PopupAdapter(layoutInflater));
            val melbourne = mMap.addMarker(MarkerOptions().position(storeLocation).title(storeName).snippet(storeInformation))
            melbourne?.showInfoWindow()

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation,15F))

        }

    }
}
