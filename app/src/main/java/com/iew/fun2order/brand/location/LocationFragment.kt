package com.iew.fun2order.brand.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_STORE
import com.iew.fun2order.ui.home.ActivityItemList
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.shop.ActivityOfficalMenu
import com.iew.fun2order.utility.ACTION_LOCATION_REQUEST_CODE
import kotlinx.android.synthetic.main.store_location_fragment.*


class LocationFragment : Fragment(), IAdapterOnClick {


    private lateinit var viewModel: LocationViewModel
    private var storeInfolist : MutableList<DETAIL_BRAND_STORE> = mutableListOf<DETAIL_BRAND_STORE>()
    private var storeInfoSortedlist : MutableList<DETAIL_BRAND_STORE> = mutableListOf<DETAIL_BRAND_STORE>()
    private var oriLocation : Location? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.store_location_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view?.setBackgroundColor(ActivityOfficalMenu.getBackGroundColor())

        if (!hasReadLocationPermission()) {
            if (!requestReadLocationPermission()) {
                locationManager()
            }
        } else {
            locationManager()
        }

        storeInfolist.clear()
        storeInfoSortedlist.clear()

        val brandName = ActivityOfficalMenu.getBrandName()
        if (brandName != "") {
            ProgressDialogUtil.showProgressDialog(context);
            val detailBrandStore = "/DETAIL_BRAND_STORE/$brandName"
            val database = Firebase.database
            val myRef = database.getReference(detailBrandStore)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    ProgressDialogUtil.dismiss()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach()
                    {
                        var storeInfo = it.getValue(DETAIL_BRAND_STORE::class.java)
                        if (storeInfo != null) {
                            storeInfolist.add(storeInfo)
                        }
                    }

                    storeInfolist.sortedBy {storeEntity-> storeEntity.storeID}.forEach {
                        var sortedStore = it.copy()
                        if(sortedStore.storeAddress!= null) {
                            val storePosition = getLocationFromAddress(it.storeAddress!!)
                            sortedStore.storeLongitude = storePosition?.longitude.toString()
                            sortedStore.storeLatitude = storePosition?.latitude.toString()
                        }
                        storeInfoSortedlist.add(sortedStore)
                    }
                    rcv_StoreInfo.adapter!!.notifyDataSetChanged()
                    ProgressDialogUtil.dismiss()
                }
            })
        }

        rcv_StoreInfo.layoutManager = LinearLayoutManager(requireContext())
        rcv_StoreInfo.adapter = AdapterRC_StoreInfo(requireContext(), storeInfoSortedlist, oriLocation,  this)
        rcv_StoreInfo.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        if(type == 0) {
            when (sender) {
                "MAP" -> {
                    val bound = Bundle();
                    bound.putParcelable("STORE_INFO", storeInfoSortedlist[pos])
                    val I = Intent(context, MapsActivity::class.java)
                    I.putExtras(bound);
                    startActivity(I)
                }
                "全部項目" -> {

                }
            }
        }
    }

    private fun getLocationFromAddress( strAddress : String) : GeoPoint?{
        val coder : Geocoder =  Geocoder(context)
        var address :  MutableList<Address> =  mutableListOf<Address>()
        var p1 : GeoPoint? = null

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address != null) {
                val location = address[0]
                p1 = GeoPoint ((location.latitude ),
                    (location.longitude ))
            }
        }
        catch (ex: Exception)
        {
            val exception = ex.message
        }
        return p1
    }


    private fun locationManager(){
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!(isGPSEnabled || isNetworkEnabled))
            Toast.makeText(context, "目前無開啟任何定位功能!!", Toast.LENGTH_SHORT).show()
        else
            try {
                if (isGPSEnabled ) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0L, 0f, locationListener)
                    oriLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        0L, 0f, locationListener)
                    oriLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
            } catch(ex: SecurityException) {
                Toast.makeText(context, "目前因為權限因素無法取得位置!!", Toast.LENGTH_SHORT).show()
            }
    }

    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if(oriLocation == null) {
                oriLocation = location
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

    }


    private fun hasReadLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true
    }


    private fun requestReadLocationPermission(): Boolean{
        //MarshMallow(API-23)之後要在 Runtime 詢問權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val permsRequestCode : Int= ACTION_LOCATION_REQUEST_CODE;
            requestPermissions(perms, permsRequestCode);
            return true;
        }
        return false;
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTION_LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager()
                    rcv_StoreInfo.adapter = null
                    rcv_StoreInfo.adapter = AdapterRC_StoreInfo(requireContext(), storeInfoSortedlist,oriLocation,  this)
                }
            }
        }
    }
}