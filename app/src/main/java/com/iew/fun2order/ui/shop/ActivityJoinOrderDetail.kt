package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_PROFILE
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFORMATION
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.order.ReferenceOrderActivity
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.ACTION_ADDPRODUCT_CODE
import com.iew.fun2order.utility.ACTION_ADDREFERENCE_CODE
import com.iew.fun2order.utility.ACTION_SHIPPING_CAR_CODE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_join_order_detail.*
import kotlinx.android.synthetic.main.activity_join_order_detail.brandItemsTitle
import kotlinx.android.synthetic.main.activity_join_order_detail.rcv_brandItems
import kotlinx.android.synthetic.main.activity_join_order_detail.segmentedItemCategory
import kotlinx.android.synthetic.main.alert_input_customproduct.view.*
import kotlinx.android.synthetic.main.row_detail_productitems_with_carts.view.*


class ActivityJoinOrderDetail : AppCompatActivity(), IAdapterOnClick {

    private var textCartItemCount: TextView? = null
    private var lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()

    private  var  selectBrandName : String = ""
    private  var  selectBrandMenuNumber: String = ""
    private  var  selectedMenuLocation: String = ""
    private  var  menuOrderMessageID: String = ""

    private  var  provideContactInfo: Boolean = false

    private  var  menuExist:Boolean = false
    private  lateinit var detailMenuInfo : DETAIL_MENU_INFORMATION
    private  var productItemInfo : MutableList<ItemsLV_Products> = mutableListOf()
    private  var productPriceSequence : MutableList<String> = mutableListOf()


    private  lateinit var  selectProductCategory : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_order_detail)
        supportActionBar?.title = "加入團購單"
        menuExist = false

        val tmpMenuOrderInfo = intent.extras?.get("MenuOrderInfo")
        menuOrderMessageID = intent.extras?.getString("NotifyMessageID","") ?: ""

        if(tmpMenuOrderInfo == null)
        {
           AlertDialog.Builder(this)
                .setTitle("錯誤")
                .setMessage("訂單資訊錯誤.")
                .setPositiveButton("確定"){ dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .show()
            return
        }

        val menuOrderInfo = tmpMenuOrderInfo as USER_MENU_ORDER
        selectBrandName = menuOrderInfo.brandName.toString()
        selectBrandMenuNumber = menuOrderInfo.menuNumber.toString()
        provideContactInfo = menuOrderInfo.needContactInfoFlag ?: false

        //---Setup location -----
        SegmentedGroupLocation.removeAllViews()
        menuOrderInfo?.locations?.forEach { location ->
            addRadioButton(LayoutInflater.from(this), SegmentedGroupLocation, location.toString())
        }

        if(SegmentedGroupLocation.childCount == 0)
        {
            linearLayoutGroupLoc.visibility  = View.GONE
        }

        SegmentedGroupLocation!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            selectedMenuLocation = radioButton.text.toString()
        })

        joinOrderReference.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("MenuOrder", menuOrderInfo)
            val intent = Intent(this, ReferenceOrderActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDREFERENCE_CODE)
        }


        segmentedItemCategory.removeAllViews()
        productItemInfo.clear()
        productPriceSequence.clear()

        segmentedItemCategory.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            selectProductCategory = radioButton.text.toString()

            if(detailMenuInfo != null && selectProductCategory != "")
            {
                //----- Clear -------
                productItemInfo.clear()
                productPriceSequence.clear()

                //----- Setup Title Bar ---
                brandItemsTitle.removeAllViews()
                val title = LayoutInflater.from(this).inflate(R.layout.row_detail_productitems_with_carts, null)
                title.itemName.setTextColor(Color.BLUE)
                title.itemName.text = "品名"
                title.itemName.textSize = 16F

                title.itemShoppingCarts.visibility = View.INVISIBLE

                val lp2:TableRow.LayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                lp2.setMargins(20, 0, 0, 0);

                val tbrow = TableRow(this)
                val selectProductCategory = detailMenuInfo.productCategory?.firstOrNull { it -> it.categoryName == selectProductCategory}
                if(selectProductCategory!= null)
                {
                    selectProductCategory.priceTemplate?.recipeList?.sortedBy { it.itemSequence }?.forEach {

                        val t1v = TextView(this)
                        t1v.text =  it.itemName
                        t1v.setTextColor(Color.BLACK)
                        t1v.textSize = 16F
                        t1v.width = 20
                        t1v.gravity = Gravity.CENTER
                        t1v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                        tbrow.addView(t1v,lp2)

                        productPriceSequence.add( it.itemName)
                    }

                    title.itemAttribute.addView(tbrow)
                    brandItemsTitle.addView(title)

                    //---------- Setup Body ------
                    selectProductCategory.productItems?.forEach {
                        productItemInfo.add(ItemsLV_Products(it.productName,it.priceList, selectProductCategory.priceTemplate?.standAloneProduct))
                    }
                    rcv_brandItems.adapter?.notifyDataSetChanged()
                }
            }
        })

        joinOrderBrandName.text = selectBrandName
        downloadBrandProfileInfoFromFireBase(selectBrandName)

        rcv_brandItems.layoutManager =  LinearLayoutManager(this)
        rcv_brandItems.adapter = AdapterRC_Items_with_carts( this, productItemInfo,productPriceSequence, this)
        rcv_brandItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        loadSelfProduct(menuOrderInfo)

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
                        AlertDialog.Builder(this@ActivityJoinOrderDetail)
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

    override fun onClick(sender: String, pos: Int, type: Int) {

        val selectProrduct = productItemInfo[pos]
        if(detailMenuInfo !=null) {

            val bundle = Bundle()
            bundle.putString("SELECT_CATEGORY", selectProductCategory)
            bundle.putString("SELECT_PRODUCT", selectProrduct.Name)
            bundle.putParcelable("DETAIL_MENU_INFO",detailMenuInfo)
            var intent = Intent(this, ActivityJoinOrderDetailSelectItems::class.java)
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

    private fun addRadioButton(inflater: LayoutInflater, group: SegmentedGroup, btnName: String) {

        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
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