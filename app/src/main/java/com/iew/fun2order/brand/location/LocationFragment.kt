package com.iew.fun2order.brand.location

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.MainActivity
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_STORE
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.STORE_INFO
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.home.ActivityItemList
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.shop.ActivityOfficalMenu
import com.iew.fun2order.ui.shop.ActivitySetupDetailOrder
import com.iew.fun2order.ui.shop.ActivitySetupDetailOrderNext
import com.iew.fun2order.utility.*
import kotlinx.android.synthetic.main.store_location_fragment.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


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
                "ACTION" -> {
                    val storeName = storeInfoSortedlist[pos].storeName ?: ""
                    val storeAddress = storeInfoSortedlist[pos].storeAddress?: ""
                    val storePhoneNumber = storeInfoSortedlist[pos].storePhoneNumber?: ""
                    val deliveryService =  storeInfoSortedlist[pos].deliveryServiceFlag  ?: false
                    val menuNumber =  storeInfoSortedlist[pos].storeMenuNumber
                    val brandName = ActivityOfficalMenu.getBrandName()

                    if(brandName == null)
                    {
                        val notifyAlert = AlertDialog.Builder(requireContext()).create()
                        notifyAlert.setTitle("錯誤")
                        notifyAlert.setMessage("品牌名稱不存在\n無法訂購")
                        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, i ->
                            dialog.dismiss()
                        }
                        notifyAlert.show()
                    }
                    else
                    {

                        val buttonActions = arrayOf("揪團訂購", "自己訂購")
                        AlertDialog.Builder(requireContext())
                            .setTitle("請選擇訂購方式")
                            .setItems(buttonActions, DialogInterface.OnClickListener { dialog, which ->
                                var selfOrderFlag = false
                                dialog.dismiss()
                                when (which) {
                                    0 -> {
                                        selfOrderFlag = false
                                    }
                                    1 -> {
                                        selfOrderFlag = true
                                    }
                                }

                                if (menuNumber != "") {
                                    val bundle = Bundle()
                                    bundle.putString("BRAND_NAME", brandName)
                                    bundle.putString("BRAND_MENU_NUMBER", menuNumber)
                                    bundle.putString("STORE_NAME", storeName)
                                    bundle.putString("STORE_ADDRESS", storeAddress)
                                    bundle.putString("STORE_PHONE_NUMBER", storePhoneNumber)
                                    bundle.putBoolean("SELF_ORDER", selfOrderFlag)
                                    bundle.putBoolean("DELIVERY_SERVICE", deliveryService)

                                    var intent = Intent(requireContext(), ActivityCoworkOrderInfo::class.java)
                                    intent.putExtras(bundle)
                                    startActivity(intent)

                                }
                                else {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("通知訊息")
                                        .setMessage("品牌菜單資訊不存在!!\n無法揪團!!")
                                        .setPositiveButton("確定", null)
                                        .create()
                                        .show()
                                }

                            })
                            .setNegativeButton("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                    }
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
        else {
            try {
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L, 0f, locationListener
                    )
                    oriLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L, 0f, locationListener
                    )
                    oriLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
            } catch (ex: SecurityException) {
                Toast.makeText(context, "目前因為權限因素無法取得位置!!", Toast.LENGTH_SHORT).show()
            }
            if(oriLocation == null)
            {
                Toast.makeText(context, "無法取得當前位置!!", Toast.LENGTH_SHORT).show()
            }
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
                    rcv_StoreInfo.adapter = AdapterRC_StoreInfo(requireContext(), storeInfoSortedlist, oriLocation,  this)
                }
            }
        }
    }


    private fun createSelfOrder(brandName: String, brandMenuNumber: String, storeName: String,storeAddress: String,storePhoneNumber: String)
    {

        val userMenuOrder: USER_MENU_ORDER = USER_MENU_ORDER()
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())

        val dueDayTime = Calendar.getInstance()
        //---- Default 一天的時間
        dueDayTime.add(Calendar.DAY_OF_YEAR,1)

        val dueTimeStamp = DATATIMEFORMAT_NORMAL.format(dueDayTime.time)

        if (brandName == null || brandMenuNumber == null) {
            val notifyAlert = AlertDialog.Builder(requireContext()).create()
            notifyAlert.setTitle("錯誤通知")
            notifyAlert.setMessage("品牌資訊有誤!! 無法產生訂單。")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK")
            { _, _ -> }
            notifyAlert.show()
            return
        }

        //Create USER_MENU_ORDER
        userMenuOrder.brandName = brandName
        userMenuOrder.createTime = timeStamp
        userMenuOrder.menuNumber = brandMenuNumber
        userMenuOrder.dueTime = dueTimeStamp
        userMenuOrder.locations = null
        userMenuOrder.orderNumber = "M$timeStamp"
        userMenuOrder.orderOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
        userMenuOrder.orderOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
        userMenuOrder.orderStatus = ORDER_STATUS_INIT
        userMenuOrder.orderTotalPrice = 0
        userMenuOrder.orderTotalQuantity = 0
        userMenuOrder.orderType = "F"
        userMenuOrder.needContactInfoFlag = false

        val storeInfo : STORE_INFO = STORE_INFO()
        storeInfo.storeName = storeName ?: ""
        storeInfo.storeAddress = storeAddress ?: ""
        storeInfo.storePhoneNumber = storePhoneNumber ?: ""
        userMenuOrder.storeInfo= storeInfo

        //----- 自己訂購只填寫自己的資訊 -----
        val orderMember: ORDER_MEMBER = com.iew.fun2order.db.firebase.ORDER_MEMBER()
        orderMember.memberID = FirebaseAuth.getInstance().currentUser!!.uid
        orderMember.memberTokenID = com.iew.fun2order.MainActivity.localtokenID
        orderMember.orderOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
        orderMember.orderContent.createTime = timeStamp
        orderMember.orderContent.itemFinalPrice = 0
        orderMember.orderContent.itemOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
        orderMember.orderContent.itemOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
        orderMember.orderContent.itemQuantity = 0
        orderMember.orderContent.itemSinglePrice = 0
        orderMember.orderContent.location = ""
        orderMember.orderContent.orderNumber = userMenuOrder.orderNumber
        orderMember.orderContent.payCheckedFlag = false
        orderMember.orderContent.payNumber = 0
        orderMember.orderContent.payTime = ""
        orderMember.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_WAIT
        orderMember.orderContent.ostype = "Android"
        userMenuOrder.contentItems!!.add(orderMember)

        Firebase.database.reference.child("USER_MENU_ORDER")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(userMenuOrder.orderNumber.toString()).setValue(userMenuOrder)
            .addOnSuccessListener {
                sendOrderReqFcmMessage (userMenuOrder, "自己訂購發起的訂單")
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.setClass(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "團購單建立失敗!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessageDetailOrderFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, msChuGroupDetailMsg:String)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        var title = "團購邀請"
        var body = if (msChuGroupDetailMsg == "") {
            "由 ${userMenuOrder.orderOwnerName} 發起的團購邀請，請點擊通知以查看詳細資訊。"
        } else {
            "由 ${userMenuOrder.orderOwnerName} 的團購邀請 : \n$msChuGroupDetailMsg。"
        }

        val self = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)   //Enter your notification message

        notificationBody.put("messageID", "")      //Enter
        notificationBody.put("messageTitle", title)   //Enter
        notificationBody.put("messageBody", body)    //Enter
        notificationBody.put("notificationType", NOTIFICATION_TYPE_ACTION_JOIN_ORDER )   //Enter
        notificationBody.put("receiveTime", timeStamp)   //Enter
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
        notificationBody.put("dueTime",    userMenuOrder.dueTime ?: "")   //Enter  20200515 addition
        notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())   //Enter
        notificationBody.put("messageDetail", msChuGroupDetailMsg?: "")   //Enter
        notificationBody.put("isRead", "N")   //Enter
        notificationBody.put("replyStatus", "")   //Enter
        notificationBody.put("replyTime", "")   //Enter

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
    }

    private fun sendOrderReqFcmMessage(userMenuOrder: USER_MENU_ORDER, msChuGroupDetailMsg:String ) {
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        ProgressDialogUtil.showProgressDialog(requireContext(),"處理中");
        val androidType =  userMenuOrder.contentItems?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" && it.memberTokenID != ""}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }
        sendMessageDetailOrderFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp, msChuGroupDetailMsg)
        ProgressDialogUtil.dismiss()
    }


}