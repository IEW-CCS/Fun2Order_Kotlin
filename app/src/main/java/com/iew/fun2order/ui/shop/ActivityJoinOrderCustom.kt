package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityMeunImage
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.order.ActivityShowMenuImage
import com.iew.fun2order.order.ReferenceOrderActivity
import com.iew.fun2order.utility.ACTION_ADDPRODUCT_CODE
import com.iew.fun2order.utility.ACTION_ADDREFERENCE_CODE
import com.iew.fun2order.utility.ACTION_SHIPPING_CAR_CODE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_join_order_custom.*
import kotlinx.android.synthetic.main.activity_join_order_custom.SegmentedGroupLocation
import kotlinx.android.synthetic.main.activity_join_order_custom.joinOrderReference
import kotlinx.android.synthetic.main.activity_join_order_custom.linearLayoutGroupLoc
import kotlinx.android.synthetic.main.activity_join_order_detail.*
import kotlinx.android.synthetic.main.alert_input_customproduct.view.*


class ActivityJoinOrderCustom : AppCompatActivity() {

    private var textCartItemCount: TextView? = null
    private var lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()
    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()
    private var selectedMenuLocation: String = ""
    private var selectBrandName: String = ""
    private var menuOrderMessageID: String = ""
    private var provideContactInfo: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_order_custom)
        supportActionBar?.title = "加入團購單"

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = this.getString(R.string.interstitial_ad_unit_id)
        requestNewInterstitial()

        mInterstitialAd!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mInterstitialAd!!.show()
            }
        }

        val tmpMenuOrderInfo = intent.extras?.get("InviteOrderInfo")
        val tmpMenuInfo      = intent.extras?.get("InviteMenuInfo")
        val tmpNotifyInfo   = intent.extras?.get("InviteNotifyInfo")

        if(tmpMenuOrderInfo == null)
        {
            showAlertWithDataCheck("錯誤","訂單資訊錯誤.")
            return
        }
        else if(tmpMenuInfo == null)
        {
            showAlertWithDataCheck("錯誤","產品資訊錯誤.")
            return
        }

        else if(tmpNotifyInfo == null)
        {
            showAlertWithDataCheck("錯誤","系統資訊錯誤.")
            return
        }


        //---------- 真正邏輯部分 ---------

        val menuOrderInfo = tmpMenuOrderInfo as USER_MENU_ORDER
        val menuInfo      = tmpMenuInfo as USER_MENU
        val notifyInfo   = tmpNotifyInfo as entityNotification

        provideContactInfo =  menuOrderInfo.needContactInfoFlag ?: false
        menuOrderMessageID = notifyInfo.messageID
        selectBrandName = menuInfo.brandName ?: ""
        
        brandName.text = menuInfo.brandName

        //---Setup location -----
        SegmentedGroupLocation.removeAllViews()
        menuOrderInfo?.locations?.forEach { location ->
            addRadioButton(LayoutInflater.from(this), SegmentedGroupLocation, location.toString())
        }

        if(SegmentedGroupLocation.childCount == 0)
        {
            linearLayoutGroupLoc.visibility = View.GONE
        }

        SegmentedGroupLocation!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            selectedMenuLocation = radioButton.text.toString()
        })


        //---  PreLoad Menu Image  ----
        preLoadMenuImage(menuInfo)

        //--- PreLoad Selected Product---
        loadSelfProduct(menuOrderInfo)

        btnAddToShoppingCar.setOnClickListener {

            val selectItems: MENU_PRODUCT = MENU_PRODUCT()
            selectItems.itemName = editItemName.text.toString()
            selectItems.itemComments = editItemComment.text.toString()
            selectItems.itemQuantity = productCount.value
            val unitPrice = editItemPrice.text.toString().toIntOrNull() ?: 0
            selectItems.itemPrice = unitPrice * productCount.value

            lstSelectedProduct.add(selectItems)
            setupBadge()

            //---- 清空資料 ----
            editItemName.text = Editable.Factory.getInstance().newEditable("")
            editItemPrice.text = Editable.Factory.getInstance().newEditable("")
            editItemComment.text = Editable.Factory.getInstance().newEditable("")
            productCount.value = 1

            AlertDialog.Builder(this)
                .setTitle("通知")
                .setMessage("已經將產品加入購物車!!")
                .setPositiveButton("確定",null)
                .show()

        }

        btnDetailInfo.setOnClickListener {
            MenuImaegByteArray.clear()
            if(menuInfo.multiMenuImageURL?.count() !=0) {
                menuInfo.multiMenuImageURL!!.forEach {
                    MenuImaegByteArray.put(it,null)
                }
            }
            else if (menuInfo.menuImageURL != null )
            {
                MenuImaegByteArray.put(menuInfo.menuImageURL!!,null)
            }
            val imageList: ArrayList<String> = ArrayList<String>(MenuImaegByteArray.keys)
            val bundle = Bundle()
            val I = Intent(this, ActivityShowMenuImage::class.java)
            bundle.putParcelable("MENU_INFO", menuInfo)
            bundle.putStringArrayList("MENU_IMAGES", imageList)
            I.putExtras(bundle)
            startActivity(I)
        }

        joinOrderReference.setOnClickListener {

            val bundle = Bundle()
            bundle.putParcelable("MenuOrder", menuOrderInfo)
            val intent = Intent(this, ReferenceOrderActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDREFERENCE_CODE)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val menuItem: MenuItem = menu.findItem(R.id.action_cart)
        val actionView: View = menuItem.getActionView()
        textCartItemCount = actionView.findViewById(R.id.cart_badge)
        setupBadge()
        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_cart -> {

                if(selectedMenuLocation == "" && SegmentedGroupLocation.childCount > 0) {
                    val Alert = AlertDialog.Builder(this).create()
                    Alert.setTitle("錯誤")
                    Alert.setMessage("請選擇地點 !!")
                    Alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ -> }
                    Alert.show()
                }
                else {

                    val bundle = Bundle()

                    bundle.putString("SELECT_BRAND", selectBrandName)
                    bundle.putString("SELECT_LOCATION", selectedMenuLocation)
                    bundle.putString("NOTIFY_MESSAGE_ID", menuOrderMessageID)
                    bundle.putBoolean("CONTACT_INFO", provideContactInfo)
                    bundle.putParcelableArrayList("SELECT_PRODUCT_LIST", ArrayList(lstSelectedProduct))

                    val intent = Intent(this, ActivityShoppingCart::class.java)
                    intent.putExtras(bundle)
                    startActivityForResult(intent, ACTION_SHIPPING_CAR_CODE)
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBadge() {
        if (textCartItemCount != null) {
            if (lstSelectedProduct.count() == 0) {
                if (textCartItemCount!!.visibility != View.GONE) {
                    textCartItemCount!!.visibility = View.GONE
                }
            } else {
                textCartItemCount!!.text = Math.min(lstSelectedProduct.count(), 99).toString()
                if (textCartItemCount!!.visibility != View.VISIBLE) {
                    textCartItemCount!!.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun requestNewInterstitial() {
        val adRequest: AdRequest = AdRequest.Builder().build()
        mInterstitialAd!!.loadAd(adRequest)
    }

    private fun showAlertWithDataCheck(title:String, message:String)
    {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("確定"){ dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun addRadioButton(inflater: LayoutInflater, group: SegmentedGroup, btnName: String) {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_ADDREFERENCE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val referenceProduct =
                        data?.getParcelableArrayListExtra<MENU_PRODUCT>("referenceProducts")
                    val afterrefProductCount = referenceProduct?.count() ?: 0
                    if (lstSelectedProduct.count().toInt() + afterrefProductCount < 11) {
                        referenceProduct!!.forEach()
                        {
                            lstSelectedProduct.add(it.copy())
                        }
                        setupBadge()

                    } else {
                        val notifyAlert = AlertDialog.Builder(this).create()
                        notifyAlert.setTitle("通知")
                        notifyAlert.setMessage("訂單產品超過10筆, 無法再新增!!")
                        notifyAlert.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "OK"
                        ) { dialogInterface, i ->
                        }
                        notifyAlert.show()
                    }
                }
            }

            ACTION_SHIPPING_CAR_CODE ->
            {
                if (resultCode == Activity.RESULT_OK) {

                    val selectProductList = data?.extras?.get("SELECT_PRODUCT_LIST") as ArrayList<MENU_PRODUCT>
                    lstSelectedProduct.clear()
                    lstSelectedProduct = selectProductList.toMutableList()
                    setupBadge()
                }
            }
            else -> {
                println("no handler onActivityReenter")
            }
        }
    }

    private fun preLoadMenuImage(tmpMenuInfo: USER_MENU) {
        val MemoryDBContext = MemoryDatabase(this!!)
        val MenuImageDB = MemoryDBContext.menuImagedao()
        MenuImaegByteArray.clear()
        if (tmpMenuInfo.multiMenuImageURL?.count() != 0) {
            tmpMenuInfo.multiMenuImageURL!!.forEach {
                MenuImaegByteArray.put(it, null)
            }
        } else if (tmpMenuInfo.menuImageURL != null) {
            MenuImaegByteArray.put(tmpMenuInfo.menuImageURL!!, null)
        }
        val imageList: ArrayList<String> = ArrayList<String>(MenuImaegByteArray.keys)
        imageList?.forEach { imageURL ->
            if (imageURL != "") {
                val islandRef = Firebase.storage.reference.child(imageURL)
                val ONE_MEGABYTE = 1024 * 1024.toLong()
                islandRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener { bytesPrm: ByteArray ->
                        try {
                            MenuImageDB.insertRow(entityMeunImage(null, imageURL, "", bytesPrm.clone()!!))
                        } catch (e: Exception) {
                        }
                    }
            }
        }
    }

    private fun loadSelfProduct(UserMenuOrder:USER_MENU_ORDER)
    {
        UserMenuOrder?.contentItems?.forEach { orderMember ->
            if (orderMember.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString() && orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                val refProductItems = orderMember.orderContent.menuProductItems?.toMutableList() ?: mutableListOf()
                val refLocation = orderMember.orderContent.location

                refProductItems.forEach()
                {
                    lstSelectedProduct.add(it)
                }

                selectedMenuLocation = refLocation ?: ""
                if (refLocation != null) {
                    var segmentedDefaultSetting: RadioButton
                    if (SegmentedGroupLocation.childCount > 0) {
                        for (i in 0 until SegmentedGroupLocation.childCount) {
                            segmentedDefaultSetting = SegmentedGroupLocation.getChildAt(i) as RadioButton
                            if (segmentedDefaultSetting.text == refLocation) {
                                segmentedDefaultSetting.isChecked = true
                            }
                        }
                    }
                }

                setupBadge()
            }
        }
    }
}