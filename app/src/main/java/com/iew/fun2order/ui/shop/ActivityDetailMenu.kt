package com.iew.fun2order.ui.shop

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.iew.fun2order.MainActivity
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_PROFILE
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFORMATION
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import com.iew.fun2order.utility.NOTIFICATION_TYPE_ACTION_JOIN_ORDER
import com.iew.fun2order.utility.ORDER_STATUS_INIT
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_detail_menu.*
import kotlinx.android.synthetic.main.activity_detail_menu.brandName
import kotlinx.android.synthetic.main.row_detail_productitems.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


/*
     object to json
        var gson = Gson()
        var jsonString = gson.toJson(TestModel(1,"Test"))
     json to Object
        var jsonString = """{"id":1,"description":"Test"}""";
        var testModel = gson.fromJson(jsonString, TestModel::class.java)

      firebase json to Object
          val objectclass = dataSnapshot.getValue(Any::class.java)
          val json = Gson().toJson(objectclass)
         val brandProfile = Gson().fromJson(json, DETAIL_BRAND_PROFILE::class.java)
 */

class ActivityDetailMenu : AppCompatActivity() {


    private  var  selectBrandName : String? = null
    private  var  selectBrandImageURL: String? = null
    private  var  selectBrandMenuNumber: String? = null
    private  var  menuExist:Boolean = false
    private  lateinit var detailMenuInfo : DETAIL_MENU_INFORMATION
    private  var productItemInfo : MutableList<ItemsLV_Products> = mutableListOf()
    private  var productPriceSequence : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)
        supportActionBar?.title = "品牌詳細資料"


        menuExist = false
        selectBrandName = intent.extras?.getString("BRAND_NAME")
        selectBrandImageURL = intent.extras?.getString("BRAND_IMAGE_URL")

        val request: AdRequest = AdRequest.Builder().build()
        adView.loadAd(request)
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(errorCode: Int) {
                adView.visibility = View.GONE
            }
        }


        segmentedItemCategory.removeAllViews()
        productItemInfo.clear()
        productPriceSequence.clear()

        segmentedItemCategory.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            val selectItemCategory = radioButton.text.toString()


            if (detailMenuInfo != null && selectItemCategory != "") {
                //----- Clear -------
                productItemInfo.clear()
                productPriceSequence.clear()

                //----- Setup Title Bar ---
                brandItemsTitle.removeAllViews()
                val title =
                    LayoutInflater.from(this).inflate(R.layout.row_detail_producttitle, null)
                title.itemName.setTextColor(Color.BLUE)
                title.itemName.text = "品名"
                title.itemName.textSize = 16F

                val lp2: TableRow.LayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT
                );
                lp2.setMargins(20, 0, 0, 0);

                val tbrow = TableRow(this)
                val selectProductCategory =
                    detailMenuInfo.productCategory?.firstOrNull { it -> it.categoryName == selectItemCategory }
                if (selectProductCategory != null) {
                    selectProductCategory.priceTemplate?.recipeList?.sortedBy { it.itemSequence }
                        ?.forEach {

                            val t1v = TextView(this)
                            t1v.text = it.itemName
                            t1v.setTextColor(Color.BLACK)
                            t1v.textSize = 16F
                            t1v.width = 20
                            t1v.gravity = Gravity.CENTER
                            t1v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                            tbrow.addView(t1v, lp2)

                            productPriceSequence.add(it.itemName)
                        }

                    title.itemAttribute.addView(tbrow)
                    brandItemsTitle.addView(title)


                    //---------- Setup Body ------
                    selectProductCategory.productItems?.forEach {

                        val productDesc = it.productDescription ?: ""
                        productItemInfo.add(
                            ItemsLV_Products(
                                it.productName,
                                it.priceList,
                                selectProductCategory?.priceTemplate.standAloneProduct,
                                productDesc
                            )
                        )
                    }
                    rcv_brandItems.adapter?.notifyDataSetChanged()
                }
            }
        })

        brandName.text = selectBrandName
        downloadBrandImageFromFireBaseGlide(selectBrandImageURL)
        downloadBrandProfileInfoFromFireBase(selectBrandName)

        rcv_brandItems.layoutManager = LinearLayoutManager(this)
        rcv_brandItems.adapter = AdapterRC_Items(this, productItemInfo, productPriceSequence)
        rcv_brandItems.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        btnGroupBuyInvite.setOnClickListener {

            //----- 20200817 MBY Chris ------
            val buttonActions = arrayOf("揪團訂購", "自己訂購")
            AlertDialog.Builder(this!!)
                .setTitle("請選擇訂購方式")
                .setItems(buttonActions, DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> {
                            dialog.dismiss()
                            //---- 團購設定 -----
                            if (menuExist == true) {
                                val bundle = Bundle()
                                bundle.putString("BRAND_NAME", selectBrandName)
                                bundle.putString("BRAND_MENU_NUMBER", selectBrandMenuNumber)
                                var intent = Intent(this, ActivitySetupDetailOrder::class.java)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            } else {
                                AlertDialog.Builder(this@ActivityDetailMenu)
                                    .setTitle("通知訊息")
                                    .setMessage("$selectBrandMenuNumber 資訊不存在!!\n無法揪團!!")
                                    .setPositiveButton("確定", null)
                                    .create()
                                    .show()

                            }
                        }
                        1 -> {
                            dialog.dismiss()
                            if (menuExist == true) {
                                createSelfOrder()
                            }
                            else {
                                AlertDialog.Builder(this@ActivityDetailMenu)
                                    .setTitle("通知訊息")
                                    .setMessage("$selectBrandMenuNumber 資訊不存在!!\n無法揪團!!")
                                    .setPositiveButton("確定", null)
                                    .create()
                                    .show()
                            }
                        }
                        else -> {
                            dialog.dismiss()
                        }
                    }
                })
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()


        }

    }


    private fun createSelfOrder()
    {

        val userMenuOrder: USER_MENU_ORDER = USER_MENU_ORDER()
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())

        val dueDayTime = Calendar.getInstance()
        //---- Default 一天的時間
        dueDayTime.add(Calendar.DAY_OF_YEAR,1)

        val dueTimeStamp = DATATIMEFORMAT_NORMAL.format(dueDayTime.time)

        if (selectBrandName == null || selectBrandMenuNumber == null) {
            val notifyAlert = AlertDialog.Builder(this).create()
            notifyAlert.setTitle("錯誤通知")
            notifyAlert.setMessage("品牌資訊有誤!! 無法產生訂單。")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK")
            { _, _ -> }
            notifyAlert.show()
            return
        }

        //Create USER_MENU_ORDER
        userMenuOrder.brandName = selectBrandName
        userMenuOrder.createTime = timeStamp
        userMenuOrder.menuNumber = selectBrandMenuNumber
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
                sendFcmMessage(userMenuOrder, "自己訂購發起的訂單")
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.setClass(this, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "團購單建立失敗!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadBrandImageFromFireBase(ImageURL:String?)
    {
        if (ImageURL != null) {
            val islandRef = Firebase.storage.reference.child(ImageURL)
            val ONE_MEGABYTE = 1024 * 1024.toLong()
            islandRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener { bytesPrm: ByteArray ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    brandImage.setImageBitmap(bmp)
                }
                .addOnFailureListener {
                }
        }
    }


    private fun downloadBrandImageFromFireBaseGlide(ImageURL:String?)
    {
        brandImage.setImageBitmap(null)
        if (ImageURL != null) {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))
                Glide.with(this)
                    .load(ImageURL)
                    .apply(requestOptions)
                    .error(null)
                    .into(brandImage)
        }
    }

    private fun downloadBrandProfileInfoFromFireBase(BrandName:String?)
    {
        if (BrandName != null) {
            val detailProfileURL = "/DETAIL_BRAND_PROFILE/$BrandName"
            val database = Firebase.database
            val myRef = database.getReference(detailProfileURL)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val brandProfile = dataSnapshot.getValue(DETAIL_BRAND_PROFILE::class.java)
                    if(brandProfile!= null) {
                        selectBrandMenuNumber = brandProfile!!.menuNumber
                        downloadBrandDetailMenuInfoFromFireBase(brandProfile!!.menuNumber)
                    }
                }
            })
        }
    }


    private fun downloadBrandDetailMenuInfoFromFireBase(DetailMenuNumber:String?)
    {
        if (DetailMenuNumber != null) {
            val detailMenuURL = "/DETAIL_MENU_INFORMATION/$DetailMenuNumber"
            val database = Firebase.database
            val myRef = database.getReference(detailMenuURL)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val objectClass = dataSnapshot.getValue(Any::class.java)
                    if(objectClass == null)
                    {
                        AlertDialog.Builder(this@ActivityDetailMenu)
                            .setTitle("通知訊息")
                            .setMessage("$DetailMenuNumber 資訊不存在!!")
                            .setPositiveButton("確定",null)
                            .create()
                            .show()
                    }
                    else {

                        val json = Gson().toJson(objectClass)
                        try {
                            detailMenuInfo = Gson().fromJson(json, DETAIL_MENU_INFORMATION::class.java)
                            setupUIInfo()
                            menuExist = true
                        } catch (ex: Exception) {
                        }
                    }
                }
            })


        }
    }

    private fun setupUIInfo()
    {
          if(detailMenuInfo != null)
          {
               val category = detailMenuInfo.productCategory?.map { it.categoryName }?.toList()
               category?.forEach {
                   addButtonToSegment(layoutInflater, segmentedItemCategory,it)
              }

              if (segmentedItemCategory.childCount > 0) {
                  val default = segmentedItemCategory.getChildAt(0) as RadioButton
                  default.isChecked = true
              }
          }
    }


    private fun addButtonToSegment(inflater: LayoutInflater, group: SegmentedGroup, btnName: String)
    {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }



    private fun sendMessageBrandOrderFCMWithIOS (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, msChuGroupDetailMsg:String)
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
        notification.put("notification", notificationHeader)
        notification.put("data", notificationBody)


        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
    }

    private fun sendMessageBrandOrderFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, msChuGroupDetailMsg:String)
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

    private fun sendFcmMessage(userMenuOrder: USER_MENU_ORDER, msChuGroupDetailMsg:String ) {
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())

        ProgressDialogUtil.showProgressDialog(this,"處理中");

        //-------Notification List 拆開成Android and IOS -----
        val iosType =  userMenuOrder.contentItems?.filter { it -> (it.orderContent.ostype ?: "iOS"  == "iOS" || it.orderContent.ostype ?: "iOS"  == "") && it.memberTokenID != ""}
        val androidType =  userMenuOrder.contentItems?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" && it.memberTokenID != ""}
        val iosTypeList = iosType?.map { it -> it.memberTokenID !!}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }

        sendMessageBrandOrderFCMWithIOS (iosTypeList!!,userMenuOrder,  timeStamp, msChuGroupDetailMsg)
        sendMessageBrandOrderFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp, msChuGroupDetailMsg)

        ProgressDialogUtil.dismiss()
    }
}
