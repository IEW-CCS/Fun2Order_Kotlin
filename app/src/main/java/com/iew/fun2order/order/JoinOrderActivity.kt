package com.iew.fun2order.order

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
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
import com.google.firebase.database.*
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
import kotlin.collections.ArrayList


class JoinOrderActivity : AppCompatActivity(), IAdapterOnClick {

    private var boolChangeOrder :Boolean = false
    private var mInterstitialAd: InterstitialAd? = null
    private val lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()
    private var mFirebaseUserMenu: USER_MENU? = null
    private var mFirebaseUserMenuPath: String = ""
    private var mFirebaseUserMenuOrder: USER_MENU_ORDER? = null
    private var mFirebaseUserMenuOrderPath: String = ""

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

    private val lstLimitedProductList: MutableList<PRODUCT> = mutableListOf()
    private var basicCellHeight : Int = 202
    private var selfSelectmenuLocation: String = ""
    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()
    private var brandImageStream: ByteArray? = null

    private lateinit var menuRef: DatabaseReference
    private lateinit var childEventListener: ChildEventListener

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    protected fun exitByBackKey() {
        if(boolChangeOrder) {
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
        if(mFirebaseUserMenuOrderPath != "") {
            val MenuItemsPath = "$mFirebaseUserMenuOrderPath/limitedMenuItems"
            menuRef = Firebase.database.getReference(MenuItemsPath)
            if(menuRef!= null) {
                menuRef.addChildEventListener(childEventListener)
                menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        lstLimitedProductList.clear()
                        dataSnapshot.children.forEach()
                        {
                            var upload =  it.getValue(PRODUCT::class.java)
                            if (upload != null) {
                                var addPRODUCT = PRODUCT()
                                addPRODUCT.sequenceNumber = upload.sequenceNumber
                                addPRODUCT.itemPrice = upload.itemPrice
                                addPRODUCT.itemName = upload.itemName
                                addPRODUCT.quantityLimitation = upload.quantityLimitation
                                addPRODUCT.quantityRemained = upload.quantityRemained
                                lstLimitedProductList.add(addPRODUCT)
                            }
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
        txtBrandName        = findViewById(R.id.joinOrderBrandName)
        layOrderReference   = findViewById(R.id.joinOrderReference)
        layOrderAddProduct  = findViewById(R.id.joinOrderAddProduct)
        layOrderSubmit      = findViewById(R.id.joinOrderSubmit)
        rcvSelectedProduct  = findViewById(R.id.rcv_joinOrderSelectedList)
        mSegmentedGroupLocation = findViewById(R.id.SegmentedGroupLocation)
        gridLayoutBtnList       = findViewById(R.id.gridLayoutImageBtnList)
        txtjoinOrderDesc        = findViewById(R.id.joinOrderDesc)
        txtjoinOrderShowDetail  = findViewById(R.id.joinOrderShowDetail)
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
            mFirebaseUserMenuOrderPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}"
            loadSelfOrder(menuOrderOwnerID, menuOrderNumber)

            val menuPath = "USER_MENU_INFORMATION/${values.orderOwnerID}/${values.menuNumber}"
            mFirebaseUserMenuPath = menuPath
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
                                    null,
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
            bundle.putString("MenuOrderInfoPath", mFirebaseUserMenuOrderPath)
            bundle.putParcelableArrayList("productList", lstOrderProducts)
            bundle.putParcelableArrayList("recipeList", lstOrderRecipes)
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDPRODUCT_CODE)
        }

        //----- 選擇完畢上傳FireBase ------
        layOrderSubmit.setOnClickListener {
            if (lstSelectedProduct.count() > 0) {
                if ((mSegmentedGroupLocation.childCount > 0 && menuLocation != "") || mSegmentedGroupLocation.childCount == 0) {

                    val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}/contentItems"
                    val database = Firebase.database
                    val myRef = database.getReference(menuPath)
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            dataSnapshot.children.forEach()
                            { it ->
                                val users = it.getValue(ORDER_MEMBER::class.java)
                                if (users!!.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString()) {

                                       //--- 1. 檢查限量 ---
                                    val limitProduct = CheckLimited( users.orderContent.menuProductItems,lstSelectedProduct.toMutableList())
                                    val overLimit = limitProduct.filter { it.itemQuantity!!  < 0 }

                                    if(overLimit.count() == 0) {
                                        updateLimit(limitProduct)
                                        users.orderContent.menuProductItems = lstSelectedProduct.toMutableList()
                                        users.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_ACCEPT
                                        users.orderContent.createTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                                        users.orderContent.location = menuLocation
                                        users.orderContent.itemOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
                                        var itemQuantity: Int = 0
                                        lstSelectedProduct.forEach { menuProducts ->
                                            if (menuProducts.itemQuantity != null) {
                                                itemQuantity += menuProducts.itemQuantity!!
                                            }
                                        }
                                        users.orderContent.itemQuantity = itemQuantity
                                        it.ref.setValue(users)

                                        // 2. ------ 更新LocalDB Status -------
                                        var orderNumber = ""
                                        val notificationDB = AppDatabase(this@JoinOrderActivity!!).notificationdao()
                                        val currentNotify = notificationDB.getNotifybyMsgID(menuOrderMessageID)
                                        if(currentNotify!= null) {
                                            orderNumber = currentNotify.orderNumber
                                            if (currentNotify.messageID == menuOrderMessageID) {
                                                currentNotify.replyStatus = MENU_ORDER_REPLY_STATUS_ACCEPT
                                                currentNotify.replyTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                                                try {
                                                    notificationDB.update(currentNotify)
                                                    updateOrderStatusToLocalDB(orderNumber, MENU_ORDER_REPLY_STATUS_ACCEPT)
                                                } catch (e: Exception) {
                                                }
                                            }
                                        }

                                        // 3. ----- 使否填寫聯絡資訊 -----
                                        val needContactInfoFlag = mFirebaseUserMenuOrder?.needContactInfoFlag ?: false
                                        if (needContactInfoFlag) {
                                            val item = LayoutInflater.from(this@JoinOrderActivity).inflate(R.layout.alert_input_contact_information, null)
                                            var editTextName = item.findViewById(R.id.editTextName) as EditText
                                            var editTextPhone = item.findViewById(R.id.editTextPhone) as EditText
                                            var editTextAddress = item.findViewById(R.id.editTextAddress) as EditText

                                            val dbContext = AppDatabase(this@JoinOrderActivity)
                                            val profileDB = dbContext.userprofiledao()
                                            val entity = profileDB.getProfileByID(FirebaseAuth.getInstance().currentUser!!.uid.toString())

                                            if(entity!= null) {
                                                editTextName.text = Editable.Factory.getInstance().newEditable(entity.userName ?: "")
                                                editTextPhone.text = Editable.Factory.getInstance().newEditable(entity.phoneNumber ?: "")
                                                editTextAddress.text = Editable.Factory.getInstance().newEditable(entity.address ?: "")
                                            }

                                            if(users.orderContent.userContactInfo != null)
                                            {
                                                val tmpContact= users.orderContent.userContactInfo!!
                                                editTextName.text = Editable.Factory.getInstance().newEditable(tmpContact.userName ?: "")
                                                editTextPhone.text = Editable.Factory.getInstance().newEditable(tmpContact.userPhoneNumber ?: "")
                                                editTextAddress.text = Editable.Factory.getInstance().newEditable(tmpContact.userAddress ?: "")
                                            }

                                            var alertDialog = AlertDialog.Builder(this@JoinOrderActivity)
                                                    .setView(item)
                                                    .setCancelable(false)
                                                    .setPositiveButton("確定") { dialog, _ ->
                                                        var contactInfo = CONTENT_CONTACTINFO()
                                                        contactInfo.userName = editTextName.text.trim().toString()
                                                        contactInfo.userPhoneNumber  = editTextPhone.text.trim().toString()
                                                        contactInfo.userAddress  = editTextAddress.text.trim().toString()
                                                        users.orderContent.userContactInfo = contactInfo
                                                        it.ref.setValue(users)
                                                        finish()
                                                        dialog.dismiss()

                                                    }
                                                    .setNegativeButton("暫不提供") { dialog, _ ->

                                                        users.orderContent.userContactInfo = null
                                                        it.ref.setValue(users)
                                                        finish()
                                                        dialog.dismiss()
                                                    }
                                                    .create()
                                                    .show()
                                        }
                                        else
                                        {
                                            finish()
                                        }
                                    }
                                    else
                                    {

                                        // overLimit.count() > 0  確認限量資訊
                                        var overLimitList :String = ""
                                        overLimit.forEach()
                                        {
                                            overLimitProduct ->
                                            overLimitList += "[${overLimitProduct.itemName}]\n"
                                        }
                                        val Alert = AlertDialog.Builder(this@JoinOrderActivity).create()
                                        Alert.setTitle("錯誤")
                                        Alert.setMessage("以下產品超過已經可以購買的數量:\n${overLimitList}")
                                        Alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _,_ -> }
                                        Alert.show()
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                } else {

                    val Alert = AlertDialog.Builder(this).create()
                    Alert.setTitle("錯誤")
                    Alert.setMessage("請選擇地點 !!")
                    Alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _,_ -> }
                    Alert.show()
                }

            } else {
                val Alert = AlertDialog.Builder(this).create()
                Alert.setTitle("錯誤")
                Alert.setMessage("請至少選擇一個產品 !!")
                Alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _,_ -> }
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
                    basicCellHeight = view.measuredHeight
                }
                else if (state.itemCount == 0 && lstSelectedProduct.count() > 0)
                {
                    realHeight = (basicCellHeight * lstSelectedProduct.count()) + 20
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



        childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val upload: PRODUCT = p0.getValue(PRODUCT::class.java)!!
                if(upload!= null) {
                    lstLimitedProductList.find { it.itemName == upload.itemName }?.quantityRemained = upload.quantityRemained
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        }

    }


    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        if (lstSelectedProduct.count() > 1) {
            rcvSelectedProduct.layoutParams.height = 0
        }
        return super.onCreateView(name, context, attrs)
    }

    private fun updateLimit(  updateLimitProductItems: MutableList<MENU_PRODUCT>?)
    {
        if(mFirebaseUserMenuOrderPath != "") {
            val MenuItemsPath = "$mFirebaseUserMenuOrderPath/limitedMenuItems"
            menuRef = Firebase.database.getReference(MenuItemsPath)
            if(menuRef!= null) {
                menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach()
                        {
                            DataSnap->
                            var upload =  DataSnap.getValue(PRODUCT::class.java)
                            if (upload != null) {
                                val updateItem = updateLimitProductItems?.firstOrNull { it.itemName == upload.itemName }
                                if(updateItem != null)
                                {
                                    if( upload.quantityRemained != updateItem.itemQuantity) {
                                        upload.quantityRemained = updateItem.itemQuantity
                                        DataSnap.ref.setValue(upload)
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }




    private fun CheckLimited( oldProductItems: MutableList<MENU_PRODUCT>?, newProductItems: MutableList<MENU_PRODUCT>?) : MutableList<MENU_PRODUCT>
    {
        var replyProductItems: MutableList<MENU_PRODUCT> = mutableListOf()
        if(lstLimitedProductList.count() != 0) {

            var oldProductItemKey: MutableList<String> = mutableListOf()
            var newProductItemKey: MutableList<String> = mutableListOf()
            if (oldProductItems != null) {
                oldProductItemKey = oldProductItems.mapNotNull { it.itemName }.toMutableList()
            }
            if (newProductItems != null) {
                newProductItemKey = newProductItems.mapNotNull { it.itemName }.toMutableList()
            }
            val limitProductItemKey = lstLimitedProductList.mapNotNull { it.itemName }
            var mergeItems = (oldProductItemKey.union(newProductItemKey)).intersect(limitProductItemKey)
            mergeItems.forEach()
            { mergeKey ->

                var oldDate = oldProductItems?.filter { it.itemName == mergeKey }
                val sumOld = oldDate?.sumBy { it.itemQuantity!! } ?: 0

                var newDate = newProductItems?.filter { it.itemName == mergeKey }
                val sumNew = newDate?.sumBy { it.itemQuantity!! } ?: 0

                var diff = sumNew - sumOld
                var limitItem = lstLimitedProductList.firstOrNull { it.itemName == mergeKey }

                if (limitItem != null) {
                    if (limitItem.quantityRemained != null) {
                        val item = MENU_PRODUCT()
                        item.itemName = limitItem.itemName
                        var items_quantity = limitItem.quantityRemained!! - diff
                        item.itemQuantity = items_quantity
                        replyProductItems.add(item)
                    }
                }
            }
        }
        return replyProductItems
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
