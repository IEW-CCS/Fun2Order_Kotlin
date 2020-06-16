package com.iew.fun2order.order

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityMeunImage
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.ACTION_ADDPRODUCT_CODE
import com.iew.fun2order.utility.ACTION_ADDREFERENCE_CODE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import info.hoang8f.android.segmented.SegmentedGroup
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList


class JoinOrderActivity : AppCompatActivity(), IAdapterOnClick {

    private var boolChangeOrder :Boolean = false
    private var mInterstitialAd: InterstitialAd? = null
    private val lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()
    private var mFirebaseUserMenu: USER_MENU? = null
    private var mFirebaseUserMenuOrder: USER_MENU_ORDER? = null

    private lateinit var txtBrandName: TextView
    private lateinit var layOrderReference: LinearLayout
    private lateinit var layOrderAddProduct: LinearLayout
    private lateinit var layOrderSubmit: LinearLayout
    private lateinit var menuOrderOwnerID: String
    private lateinit var menuOrderNumber: String
    private lateinit var menuLocation: String

    private lateinit var rcvSelectedProduct: RecyclerView
    private lateinit var menuOrderMessageID: String
    private lateinit var mSegmentedGroupLocation: SegmentedGroup

    private lateinit var gridLayoutBtnList: GridLayout
    private lateinit var txtjoinOrderDesc: TextView
    private lateinit var txtjoinOrderShowDetail: TextView

    private var selfSelectmenuLocation: String = ""

    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()
    private var brandImageStream: ByteArray? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    protected fun exitByBackKey() {

        if(boolChangeOrder == true) {
            val notifyAlert = AlertDialog.Builder(this@JoinOrderActivity).create()
            notifyAlert.setTitle("提示訊息")
            notifyAlert.setTitle("訂購單已更動, 你確定要離開嗎？")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "確定") { _, i ->
                finish()
            }
            notifyAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消") { _, i ->
            }
            notifyAlert.show()
        }
        else
        {
            finish()
        }
    }


    override fun onStart() {
        super.onStart()
        if(rcvSelectedProduct.adapter!= null) {
            rcvSelectedProduct.adapter!!.notifyDataSetChanged()
        }
    }

    private fun requestNewInterstitial() {
        val adRequest: AdRequest = AdRequest.Builder().build()
        mInterstitialAd!!.loadAd(adRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_order)
        supportActionBar?.hide()
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = this.getString(R.string.interstitial_ad_unit_id)
        requestNewInterstitial()

        mInterstitialAd!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mInterstitialAd!!.show()
            }
        }

        menuLocation = ""
        txtBrandName = findViewById(R.id.joinOrderBrandName)
        layOrderReference = findViewById(R.id.joinOrderReference)
        layOrderAddProduct = findViewById(R.id.joinOrderAddProduct)
        layOrderSubmit = findViewById(R.id.joinOrderSubmit)
        rcvSelectedProduct = findViewById(R.id.rcv_joinOrderSelectedList)
        mSegmentedGroupLocation = findViewById(R.id.SegmentedGroupLocation)
        gridLayoutBtnList = findViewById(R.id.gridLayoutImageBtnList)
        txtjoinOrderDesc = findViewById(R.id.joinOrderDesc)
        txtjoinOrderShowDetail = findViewById(R.id.joinOrderShowDetail)
        lstSelectedProduct.clear()

        val mInflater: LayoutInflater? = LayoutInflater.from(this);
        val lstOrderProducts: ArrayList<ItemsLV_OrderProduct> = ArrayList<ItemsLV_OrderProduct>()
        val lstOrderRecipes: ArrayList<RECIPE> = ArrayList<RECIPE>()

        intent?.extras?.let {

            val values = it.getParcelable("InviteOrderInfo") as entityNotification
            menuOrderOwnerID   = values.orderOwnerID
            menuOrderNumber    = values.orderNumber
            menuOrderMessageID = values.messageID

            //預先載入自己選擇的產品
            loadSelfOrder(menuOrderOwnerID, menuOrderNumber)

            val menuPath = "USER_MENU_INFORMATION/${values.orderOwnerID}/${values.menuNumber}"
            val database = Firebase.database
            val myRef = database.getReference(menuPath)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mFirebaseUserMenu = dataSnapshot.getValue(USER_MENU::class.java)

                    if (mFirebaseUserMenu != null) {
                        txtBrandName.text = mFirebaseUserMenu?.brandName
                        mSegmentedGroupLocation.removeAllViews()
                        mFirebaseUserMenu?.locations?.forEach { location ->
                            addRadioButton(
                                mInflater!!,
                                mSegmentedGroupLocation,
                                location.toString()
                            )
                        }
                        mFirebaseUserMenu?.menuItems?.forEach { product ->
                            lstOrderProducts.add(
                                ItemsLV_OrderProduct(
                                    product.itemName!!,
                                    product.itemPrice!!.toString(),
                                    product.sequenceNumber!!.toString()
                                )
                            )
                        }
                        mFirebaseUserMenu?.menuRecipes?.forEach { recipe ->
                            lstOrderRecipes.add(
                                RECIPE(
                                    recipe.allowedMultiFlag,
                                    recipe.recipeCategory,
                                    recipe.recipeItems!!.toMutableList(),
                                    recipe.sequenceNumber
                                )
                            )
                        }


                        //---- Load Default Location -----
                        if (selfSelectmenuLocation != "") {
                            var segmentedDefaultSetting: RadioButton
                            if (mSegmentedGroupLocation.childCount > 0) {
                                for (i in 0 until mSegmentedGroupLocation.childCount) {
                                    segmentedDefaultSetting =
                                        mSegmentedGroupLocation.getChildAt(i) as RadioButton
                                    if (segmentedDefaultSetting.text == selfSelectmenuLocation) {
                                        segmentedDefaultSetting.isChecked = true
                                    }
                                }
                            }
                        }

                        txtjoinOrderDesc.text = mFirebaseUserMenu!!.menuDescription
                        if(mFirebaseUserMenu!!.multiMenuImageURL!!.count() !=0) {
                            DisplayMeunImageItem(mFirebaseUserMenu!!.multiMenuImageURL)
                        }
                        else if (mFirebaseUserMenu!!.menuImageURL != null )
                        {
                            val menuImage: MutableList<String>? = mutableListOf()
                            menuImage!!.add(mFirebaseUserMenu!!.menuImageURL!!)
                            DisplayMeunImageItem(menuImage)
                        }
                    }

                    else
                    {
                        val notifyAlert = AlertDialog.Builder(this@JoinOrderActivity).create()
                        notifyAlert.setTitle("訊息通知")
                        notifyAlert.setCancelable(false)
                        notifyAlert.setMessage("產品資訊不存在\n請聯繫訂單發起人: ${values.orderOwnerName} \n品牌名稱: ${values.brandName} ")
                        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                            finish()
                        }
                        notifyAlert.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    mFirebaseUserMenu = null

                    val notifyAlert = AlertDialog.Builder(this@JoinOrderActivity).create()
                    notifyAlert.setTitle("訊息通知")
                    notifyAlert.setCancelable(false)
                    notifyAlert.setMessage("產品資訊讀取錯誤, 請再次一次")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                        finish()
                    }
                    notifyAlert.show()
                }
            })
        }



        //---- User Select Location  -----
        mSegmentedGroupLocation!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            menuLocation = radioButton.text.toString()
            boolChangeOrder  = true
        })

        //----  看看別人訂了什麼 -----
        layOrderReference.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("MenuOrderOwnerID", menuOrderOwnerID)
            bundle.putString("MenuOrderNumber", menuOrderNumber)
            bundle.putParcelable("MenuOrder", mFirebaseUserMenuOrder)
            val intent = Intent(this, ReferenceOrderActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDREFERENCE_CODE)
        }

        //----- 選擇產品列表 ------
        layOrderAddProduct.setOnClickListener {

            val bundle = Bundle()
            bundle.putParcelableArrayList("productList", lstOrderProducts)
            bundle.putParcelableArrayList("recipeList", lstOrderRecipes)
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDPRODUCT_CODE)
        }

        //----- 選擇完畢上傳FireBase ------
        layOrderSubmit.setOnClickListener {
            // Location is Optional  可能是空白值
            if (lstSelectedProduct.count() > 0) {
                if ((mSegmentedGroupLocation.childCount > 0 && menuLocation != "") || mSegmentedGroupLocation.childCount == 0) {
                    //1. ------- 上傳FireBase -----------
                    val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}/contentItems"
                    val database = Firebase.database
                    val myRef = database.getReference(menuPath)
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            dataSnapshot.children.forEach()
                            {
                                val users = it.getValue(ORDER_MEMBER::class.java)
                                if (users!!.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString()) {

                                    users.orderContent.menuProductItems =
                                        lstSelectedProduct.toMutableList()
                                    users.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_ACCEPT
                                    users.orderContent.createTime =
                                        SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                                    users.orderContent.location = menuLocation
                                    users.orderContent.itemOwnerName =
                                        FirebaseAuth.getInstance().currentUser!!.displayName

                                    var itemQuantity: Int = 0
                                    lstSelectedProduct.forEach { menuProducts ->
                                        if (menuProducts.itemQuantity != null) {
                                            itemQuantity += menuProducts.itemQuantity!!
                                        }
                                    }
                                    users.orderContent.itemQuantity = itemQuantity
                                    it.ref.setValue(users)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                    // 2. ------ 更新LocalDB Status -------
                    var orderNumber = ""
                    val notificationDB = AppDatabase(this!!).notificationdao()
                    val currentNotify = notificationDB.getNotifybyMsgID(menuOrderMessageID)
                    orderNumber = currentNotify.orderNumber ?: ""
                    if (currentNotify != null && currentNotify?.messageID == menuOrderMessageID) {
                        currentNotify.replyStatus = MENU_ORDER_REPLY_STATUS_ACCEPT
                        currentNotify.replyTime =
                            SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())

                        try {
                            notificationDB.update(currentNotify)
                            updateOrderStatusToLocalDB(orderNumber, MENU_ORDER_REPLY_STATUS_ACCEPT)
                        } catch (e: Exception) {

                        }

                    }


                    finish()
                } else {

                    val Alert = AlertDialog.Builder(this).create()
                    Alert.setTitle("錯誤")
                    Alert.setMessage("請選擇地點 !!")
                    Alert.setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        "OK"
                    ) { dialogInterface, i ->
                    }
                    Alert.show()
                }

            } else {
                val Alert = AlertDialog.Builder(this).create()
                Alert.setTitle("錯誤")
                Alert.setMessage("請至少選擇一個產品 !!")
                Alert.setButton(
                    AlertDialog.BUTTON_POSITIVE,
                    "OK"
                ) { dialogInterface, i ->
                }
                Alert.show()
            }
        }

        val LayoutManagement: LinearLayoutManager = object : LinearLayoutManager(this) {
            override fun onMeasure(
                recycler: RecyclerView.Recycler,
                state: RecyclerView.State,
                widthSpec: Int,
                heightSpec: Int
            ) {
                super.onMeasure(recycler, state, widthSpec, heightSpec)
                var realHeight = View.MeasureSpec.getSize(heightSpec);
                val realWidth = View.MeasureSpec.getSize(widthSpec);
                if (state.itemCount > 0 && lstSelectedProduct.count() > 0) {
                    val view: View = recycler.getViewForPosition(0)
                    measureChild(view, widthSpec, heightSpec)
                    realHeight = (view.measuredHeight * lstSelectedProduct.count()) + 20
                }

                // 額外加20 代表吃掉Marge top down height
                setMeasuredDimension(realWidth, realHeight)
            }
        }

        rcvSelectedProduct.layoutManager = LayoutManagement
        rcvSelectedProduct.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        rcvSelectedProduct.adapter =
            AdapterRC_SelectedProduct(
                this,
                lstSelectedProduct,
                this
            )

        txtjoinOrderShowDetail.setOnClickListener {
            val imageList: ArrayList<String> = ArrayList<String>(MenuImaegByteArray.keys)
            val bundle = Bundle()
            val I = Intent(this, ActivityShowMenuImage::class.java)
            bundle.putParcelable("MENU_INFO", mFirebaseUserMenu)
            bundle.putStringArrayList("MENU_IMAGES", imageList)
            I.putExtras(bundle)
            startActivity(I)
        }

        gridLayoutBtnList.setOnClickListener {
            val imageList: ArrayList<String> = ArrayList<String>(MenuImaegByteArray.keys)
            val bundle = Bundle()
            val I = Intent(this, ActivityShowMenuImage::class.java)
            bundle.putParcelable("MENU_INFO", mFirebaseUserMenu)
            bundle.putStringArrayList("MENU_IMAGES", imageList)
            I.putExtras(bundle)
            startActivity(I)
        }

        txtjoinOrderDesc.setOnClickListener {
            val imageList: ArrayList<String> = ArrayList<String>(MenuImaegByteArray.keys)
            val bundle = Bundle()
            val I = Intent(this, ActivityShowMenuImage::class.java)
            bundle.putParcelable("MENU_INFO", mFirebaseUserMenu)
            bundle.putStringArrayList("MENU_IMAGES", imageList)
            I.putExtras(bundle)
            startActivity(I)
        }

    }


    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        if (lstSelectedProduct.count() > 1) {
            rcvSelectedProduct.layoutParams.height = 0
        }
        return super.onCreateView(name, context, attrs)
    }


    private fun DisplayMeunImageItem(multiMenuImageURL: MutableList<String>?) {

        var replyImageCount = 0
        var totalImageCount = multiMenuImageURL!!.filter { it != "" }.count()
        var MemoryDBContext = MemoryDatabase(this!!)
        var MenuImageDB = MemoryDBContext.menuImagedao()

        MenuImaegByteArray.clear()

        multiMenuImageURL!!.forEach {
            if (it != "") {
                MenuImaegByteArray.put(it, null)
                var menuImaeg = MenuImageDB.getMenuImageByName(it)
                if (menuImaeg != null) {
                    MenuImageDB.delete(menuImaeg)
                }
                val islandRef = Firebase.storage.reference.child(it)
                val ONE_MEGABYTE = 1024 * 1024.toLong()
                islandRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener { bytesPrm: ByteArray ->
                        brandImageStream = bytesPrm.clone()
                        MenuImaegByteArray[it] = brandImageStream

                        try {
                            MenuImageDB.insertRow(entityMeunImage(null, it, "", brandImageStream!!))
                        } catch (e: Exception) {
                        }

                        replyImageCount++
                        if (replyImageCount == totalImageCount) {
                            DisplayImage()
                        }
                    }
                    .addOnFailureListener {
                        replyImageCount++
                        if (replyImageCount == totalImageCount) {
                            DisplayImage()
                        }
                    }
            }
        }
    }

    private fun DisplayImage() {
        var width: Int = 0
        var iCnt: Int = 1

        val MenuImaegExist = MenuImaegByteArray.filter { it.value != null }
        val MenuImaegFailed = MenuImaegByteArray.filter { it.value == null }

        // val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
        if (MenuImaegExist.count() > 1) {
            width = (gridLayoutBtnList.width) / MenuImaegExist.count(); // 螢幕的寬度/4放近with
            iCnt = MenuImaegExist.count()
        } else {
            width = (gridLayoutBtnList.width) / 1;        // 螢幕的寬度/4放近with
        }

        gridLayoutBtnList.removeAllViews()
        gridLayoutBtnList.columnCount = iCnt;           // 設定GridLayout有幾行
        gridLayoutBtnList.rowCount = 1;              // 設定GridLayout有幾列

        MenuImaegExist.forEach()
        {
            if(it.value != null) {
                val b1 = ImageView(this)
                val bmp = BitmapFactory.decodeByteArray(it.value, 0, it.value!!.size)
                b1.setBackgroundResource(R.drawable.corners_rect_gray)
                b1.setPadding(10, 10, 10, 10)
                b1.setMinimumWidth(width)
                b1.setImageBitmap(bmp)
                b1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                gridLayoutBtnList.addView(b1, width, 320);
            }
        }

        MenuImaegFailed.forEach()
        {

            val notifyAlert = AlertDialog.Builder(this).create()
            notifyAlert.setTitle("存取影像錯誤")
            notifyAlert.setMessage("照片路徑：${it.key} \n資料讀取錯誤!!")
            notifyAlert.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "OK"
            ) { dialogInterface, i ->
            }
            notifyAlert.show()

        }
    }

    private fun addRadioButton(inflater: LayoutInflater, group: SegmentedGroup, btnName: String) {

        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }


    private fun updateOrderStatusToLocalDB(orderNumber: String, replyStatus: String) {

        val notificationDB = AppDatabase(this).notificationdao()
        if (orderNumber != "") {
            val orderMessages = notificationDB.getNotifybyOrderNo(orderNumber)
            orderMessages.forEach()
            {
                it.replyStatus = replyStatus
                it.replyTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                notificationDB.update(it)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ACTION_ADDPRODUCT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {

                    val selectProduct = data?.extras?.get("SelectItem") as MENU_PRODUCT
                    if ((lstSelectedProduct.count() + 1) < 11) {
                        lstSelectedProduct.add(selectProduct)
                        rcvSelectedProduct.adapter!!.notifyDataSetChanged()
                        boolChangeOrder  = true
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
                        rcvSelectedProduct.adapter!!.notifyDataSetChanged()
                        boolChangeOrder  = true
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
            else -> {
                println("no handler onActivityReenter")
            }
        }

    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        if (type == 1) {
            checkRemoveSelectedProduct(pos)
        }
    }

    private fun checkRemoveSelectedProduct(position: Int) {
        val alert = AlertDialog.Builder(this)
        with(alert) {
            setTitle("確認刪除所選擇的產品")
            setMessage(lstSelectedProduct[position].itemName + "\n" + lstSelectedProduct[position].itemComments)
            setPositiveButton("確定") { dialog, _ ->
                lstSelectedProduct.removeAt(position)
                rcvSelectedProduct.adapter!!.notifyDataSetChanged()
                dialog.dismiss()
                boolChangeOrder  = true
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }

    private fun loadSelfOrder(menuOrderOwnerID: String, menuOrderNumber: String) {
        val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mFirebaseUserMenuOrder = dataSnapshot.getValue(USER_MENU_ORDER::class.java)
                mFirebaseUserMenuOrder?.contentItems?.forEach { orderMember ->
                    if (orderMember.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString() && orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                        val refProductItems = orderMember.orderContent.menuProductItems?.toMutableList() ?: mutableListOf()
                        val refLocation = orderMember.orderContent.location
                        refProductItems.forEach()
                        {
                            lstSelectedProduct.add(it)
                        }

                        if(rcvSelectedProduct.adapter!= null) {
                            rcvSelectedProduct.adapter!!.notifyDataSetChanged()
                        }


                        selfSelectmenuLocation = refLocation ?: ""
                        if (refLocation != null) {
                            var segmentedDefaultSetting: RadioButton
                            if (mSegmentedGroupLocation.childCount > 0) {
                                for (i in 0 until mSegmentedGroupLocation.childCount) {
                                    segmentedDefaultSetting =
                                        mSegmentedGroupLocation.getChildAt(i) as RadioButton
                                    if (segmentedDefaultSetting.text == refLocation) {
                                        segmentedDefaultSetting.isChecked = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
}
