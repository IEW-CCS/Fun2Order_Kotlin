package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityMeunImage
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.order.ActivityShowMenuImage
import com.iew.fun2order.order.ItemsLV_OrderProduct
import com.iew.fun2order.order.ReferenceOrderActivity
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.ACTION_ADDPRODUCT_CODE
import com.iew.fun2order.utility.ACTION_ADDREFERENCE_CODE
import com.iew.fun2order.utility.ACTION_SHIPPING_CAR_CODE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_join_order_standard.*
import kotlinx.android.synthetic.main.alert_input_customproduct.view.*
import kotlinx.android.synthetic.main.row_detail_productitems_with_carts.view.*

class ActivityJoinOrderStandard : AppCompatActivity(), IAdapterOnClick {

    private var textCartItemCount: TextView? = null
    private var lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()
    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()
    private var selectedMenuLocation: String = ""
    private var selectBrandName: String = ""
    private var menuOrderMessageID: String = ""
    private var provideContactInfo: Boolean = false
    private val lstOrderProducts: ArrayList<ItemsLV_OrderProduct> = ArrayList<ItemsLV_OrderProduct>()
    private var MenuOrderInfoPath = ""
    private lateinit var menuRef: DatabaseReference
    private lateinit var childEventListener: ChildEventListener
    private lateinit var menuInfo: USER_MENU
    private lateinit var menuOrderInfo: USER_MENU_ORDER

    override fun onStart() {
        super.onStart()

        if(MenuOrderInfoPath != "") {
            val MenuItemsPath = "$MenuOrderInfoPath/limitedMenuItems"
            menuRef = Firebase.database.getReference(MenuItemsPath)
            if(menuRef!= null) {
                menuRef.addChildEventListener(childEventListener)

                menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach()
                        {
                            var upload =  it.getValue(PRODUCT::class.java)
                            lstOrderProducts.find { it.itemName == upload!!.itemName }?.itemLimit  = upload!!.quantityRemained.toString()
                            RefreahItemDate()
                        }
                    }
                })
            }
        }
    }


    override fun onStop() {
        super.onStop()

        if(menuRef!= null) {
            menuRef.removeEventListener(childEventListener)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_order_standard)
        supportActionBar?.title = "加入團購單"

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
        menuInfo      = tmpMenuInfo as USER_MENU
        menuOrderInfo = tmpMenuOrderInfo as USER_MENU_ORDER
        val notifyInfo   = tmpNotifyInfo as entityNotification

        MenuOrderInfoPath = "USER_MENU_ORDER/${notifyInfo.orderOwnerID}/${notifyInfo.orderNumber}"
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


        //------ Setup Recycle View ----
        brandItemsTitle.removeAllViews()

        val title = LayoutInflater.from(this).inflate(R.layout.row_detail_productitems_with_carts, null)
        title.itemName.setTextColor(Color.BLUE)
        title.itemName.text = "產品名稱"
        title.itemName.textSize = 16F

        title.itemShoppingCarts.visibility = View.INVISIBLE

        val lp2: TableRow.LayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        lp2.setMargins(20, 0, 0, 0);
        val tbrow = TableRow(this)

        val t1v = TextView(this)
        t1v.text = "價格"
        t1v.setTextColor(Color.BLUE)
        t1v.textSize = 16F
        t1v.width = 20
        t1v.gravity = Gravity.CENTER
        tbrow.addView(t1v, lp2)

        val t2v = TextView(this)
        t2v.text = "餘量"
        t2v.setTextColor(Color.RED)
        t2v.textSize = 16F
        t2v.width = 20
        t2v.gravity = Gravity.CENTER
        tbrow.addView(t2v, lp2)
        title.itemAttribute.addView(tbrow)
        brandItemsTitle.addView(title)

        lstOrderProducts.clear()

        menuInfo.menuItems?.forEach { product ->
            lstOrderProducts.add(
                ItemsLV_OrderProduct(product.itemName!!, product.itemPrice!!.toString(), null, product.sequenceNumber!!.toString())
            )
        }


        rcv_brandItems.layoutManager =  LinearLayoutManager(this)
        rcv_brandItems.adapter = AdapterRC_StandItems_with_carts( this, lstOrderProducts, this)
        rcv_brandItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))





        childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val upload: PRODUCT = p0.getValue(PRODUCT::class.java)!!
                lstOrderProducts.find { it.itemName == upload.itemName }?.itemLimit  = upload.quantityRemained .toString()
                RefreahItemDate()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        }


        customProduct.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_customproduct, null)
            AlertDialog.Builder(this)
                .setView(item)
                .setCancelable(false)
                .setPositiveButton("確定") { dialog, _ ->
                    val selectItems: MENU_PRODUCT = MENU_PRODUCT()
                    selectItems.itemName = item.editItemName.text.toString()
                    selectItems.itemComments = item.editItemComment.text.toString()
                    selectItems.itemQuantity = item.productCount.value
                    val unitPrice = item.editItemPrice.text.toString().toIntOrNull() ?: 0
                    selectItems.itemPrice = unitPrice * item.productCount.value

                    lstSelectedProduct.add(selectItems)
                    setupBadge()
                }
                .setNegativeButton("取消",null)
                .create()
                .show()
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

                    //----  組裝舊的資訊 -----

                    var lstoldProduct: MutableList<MENU_PRODUCT> = mutableListOf()
                    menuOrderInfo.contentItems?.forEach { it ->
                        if(it.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString())
                        {
                            it.orderContent.menuProductItems?.forEach {
                                    MenuProduct ->
                                lstoldProduct.add(MenuProduct.copy())
                            }
                        }
                    }


                    val bundle = Bundle()
                    bundle.putString("SELECT_BRAND", selectBrandName)
                    bundle.putString("SELECT_LOCATION", selectedMenuLocation)
                    bundle.putString("NOTIFY_MESSAGE_ID", menuOrderMessageID)
                    bundle.putBoolean("CONTACT_INFO", provideContactInfo)
                    bundle.putString("MENU_ORDER_PATH", MenuOrderInfoPath)
                    bundle.putParcelableArrayList("SELECT_PRODUCT_LIST", ArrayList(lstSelectedProduct))
                    bundle.putParcelableArrayList("BEFORE_PRODUCT_LIST", ArrayList(lstoldProduct))

                    val intent = Intent(this, ActivityShoppingCartWithLimit::class.java)
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

            ACTION_ADDPRODUCT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {

                    val selectProduct = data?.extras?.get("SelectItem") as MENU_PRODUCT
                    if ((lstSelectedProduct.count() + 1) < 11) {
                        lstSelectedProduct.add(selectProduct)
                        setupBadge()
                    } else {
                        val notifyAlert = AlertDialog.Builder(this).create()
                        notifyAlert.setTitle("通知")
                        notifyAlert.setMessage("訂單產品超過10筆, 無法再新增!!")
                        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK")
                        { dialogInterface, i -> }
                        notifyAlert.show()
                    }
                }
            }


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

    override fun onClick(sender: String, pos: Int, type: Int) {

        val selectProrduct = lstOrderProducts[pos]
        if(menuInfo !=null) {

            val bundle = Bundle()
            var itemLimit = selectProrduct.itemLimit ?: ""

            bundle.putString("SELECT_PRODUCT", selectProrduct.itemName)
            bundle.putString("PRODUCT_PRICE", selectProrduct.itemPrice)
            bundle.putString("LIMIT_COUNT", itemLimit)
            bundle.putParcelable("MENU_INFO", menuInfo.copy())
            var intent = Intent(this, ActivityJoinOrderStandSelectItems::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDPRODUCT_CODE)

        }
        else
        {
            AlertDialog.Builder(this)
                .setTitle("錯誤訊息")
                .setMessage("菜單資訊不存在")
                .setPositiveButton("確定", null)
                .create()
                .show()
        }


    }


    private fun RefreahItemDate()
    {
        if( rcv_brandItems.adapter  != null)
        {
            rcv_brandItems.adapter!!.notifyDataSetChanged()
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