package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.MainActivity
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.order.AdapterRC_SelectedProduct
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import kotlinx.android.synthetic.main.activity_shopping_cart.*
import kotlinx.android.synthetic.main.alert_input_customproduct.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityShoppingCartWithLimit : AppCompatActivity() , IAdapterOnClick {

    private lateinit var  lstSelectedProduct: MutableList<MENU_PRODUCT>
    private val lstLimitedProductList: MutableList<PRODUCT> = mutableListOf()

    private var mFirebaseUserMenuOrderPath: String = ""
    private lateinit var menuRef: DatabaseReference
    private lateinit var childEventListener: ChildEventListener

    override fun onStart() {
        super.onStart()

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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    protected fun exitByBackKey() {

        val notifyAlert = AlertDialog.Builder(this).create()
        notifyAlert.setTitle("提示訊息")
        notifyAlert.setTitle("確定要回到上一頁離開嗎？")
        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "確定") { _, i ->

            val bundle = Bundle()
            bundle.putParcelableArrayList("SELECT_PRODUCT_LIST",ArrayList(lstSelectedProduct))
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()

        }
        notifyAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消") { _, i ->
        }
        notifyAlert.show()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)
        supportActionBar?.title = "購物車內容"

        val tmpBrandName= intent.extras?.getString ("SELECT_BRAND","")
        val productList = intent.extras?.get("SELECT_PRODUCT_LIST") as ArrayList<MENU_PRODUCT>
        val provideContactInfo = intent.extras?.getBoolean("CONTACT_INFO",false) ?: false
        val menuOrderMessageID = intent.extras?.getString("NOTIFY_MESSAGE_ID","") ?: ""
        val selectLocation = intent.extras?.getString("SELECT_LOCATION","") ?: ""
        val beforeProductList = intent.extras?.get("BEFORE_PRODUCT_LIST") as ArrayList<MENU_PRODUCT>
        mFirebaseUserMenuOrderPath = intent.extras?.getString("MENU_ORDER_PATH","") ?: ""


        lstSelectedProduct = productList.toMutableList()

        BrandName.text = tmpBrandName
        rcv_selectedProduct.layoutManager = LinearLayoutManager(this)
        rcv_selectedProduct.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcv_selectedProduct.adapter = AdapterRC_SelectedProduct(this, lstSelectedProduct, this)

        btnSubmit.setOnClickListener {

            //-----Check Limit  ------
            val limitProduct = CheckLimited( beforeProductList.toMutableList(),lstSelectedProduct.toMutableList())
            val overLimit = limitProduct.filter { it.itemQuantity!!  < 0 }
            if(overLimit.count() == 0) {
                val notifyAlert = AlertDialog.Builder(this).create()
                notifyAlert.setTitle("提示訊息")
                notifyAlert.setTitle("確定要完成訂單嗎？")
                notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "確定") { _, i ->

                    updateLimit(limitProduct)
                    upOrderToFireBase(menuOrderMessageID, selectLocation,provideContactInfo )

                    //  ------ 更新LocalDB Status -------
                    if(menuOrderMessageID != "") {
                        var orderNumber = ""
                        val notificationDB = AppDatabase(this).notificationdao()
                        val currentNotify = notificationDB.getNotifybyMsgID(menuOrderMessageID)
                        if (currentNotify != null) {
                            orderNumber = currentNotify.orderNumber
                            if (currentNotify.messageID == menuOrderMessageID) {
                                currentNotify.replyStatus = MENU_ORDER_REPLY_STATUS_ACCEPT
                                currentNotify.replyTime = DATATIMEFORMAT_NORMAL.format(Date())
                                try {
                                    notificationDB.update(currentNotify)
                                    updateOrderStatusToLocalDB(orderNumber, MENU_ORDER_REPLY_STATUS_ACCEPT)
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                }
                notifyAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消") { _, i ->
                }
                notifyAlert.show()
            }
            else
            {

                var overLimitList :String = ""
                overLimit.forEach()
                {
                    overLimitProduct ->
                    overLimitList += "[${overLimitProduct.itemName}]\n"
                }
                val Alert = AlertDialog.Builder(this).create()
                Alert.setTitle("錯誤")
                Alert.setMessage("以下產品超過已經可以購買的數量:\n${overLimitList}")
                Alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _,_ -> }
                Alert.show()

            }
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

    override fun onClick(sender: String, pos: Int, type: Int) {
        if (type == 1) {
            checkRemoveSelectedProduct(pos)
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

    private fun finishOrder()
    {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()

    }

    private fun checkRemoveSelectedProduct(position: Int) {
        val alert = AlertDialog.Builder(this)
        with(alert) {
            setTitle("確認刪除所選擇的產品")
            setMessage(lstSelectedProduct[position].itemName + "\n" + lstSelectedProduct[position].itemComments)
            setPositiveButton("確定") { dialog, _ ->
                lstSelectedProduct.removeAt(position)

                if(rcv_selectedProduct.adapter != null) {
                    rcv_selectedProduct.adapter!!.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }


   private fun updateOrderStatusToLocalDB(orderNumber: String, replyStatus: String) {
    val notificationDB = AppDatabase(this ).notificationdao()
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


    private fun upOrderToFireBase(tmpMenuOrderMessageID:String, tmpSelectLocation:String, tmpProvideContactInfo : Boolean)
    {
        val notificationDB = AppDatabase(this).notificationdao()
        val currentNotify = notificationDB.getNotifybyMsgID(tmpMenuOrderMessageID)
        if (currentNotify != null) {

            val menuPath = "USER_MENU_ORDER/${currentNotify.orderOwnerID}/${currentNotify.orderNumber}/contentItems"
            val database = Firebase.database
            val myRef = database.getReference(menuPath)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(dataSnapshotProduct: DataSnapshot) {
                    dataSnapshotProduct.children.forEach()
                    { it ->
                        val users = it.getValue(ORDER_MEMBER::class.java)
                        if (users!!.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString()) {

                            val finalProduct = lstSelectedProduct.toMutableList()
                            finalProduct.forEach {
                                product ->
                                product.menuRecipes?.forEach {
                                    recipe ->
                                    recipe.recipeItems?.removeIf { it -> it.checkedFlag == false }
                                }

                                product.menuRecipes?.removeIf{ recipes-> recipes.recipeItems?.count() == 0 }
                            }


                            users.orderContent.menuProductItems = finalProduct
                            users.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_ACCEPT
                            users.orderContent.createTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                            users.orderContent.location = tmpSelectLocation
                            users.orderContent.itemOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
                            var itemQuantity: Int = 0
                            lstSelectedProduct.forEach { menuProducts ->
                                if (menuProducts.itemQuantity != null) {
                                    itemQuantity += menuProducts.itemQuantity!!
                                }
                            }
                            users.orderContent.itemQuantity = itemQuantity
                            it.ref.setValue(users)

                            // 2. ----- 使否填寫聯絡資訊 -----
                            if (tmpProvideContactInfo) {
                                val item = LayoutInflater.from(this@ActivityShoppingCartWithLimit).inflate(R.layout.alert_input_contact_information, null)
                                val editTextName = item.findViewById(R.id.editTextName) as EditText
                                val editTextPhone = item.findViewById(R.id.editTextPhone) as EditText
                                val editTextAddress = item.findViewById(R.id.editTextAddress) as EditText

                                val dbContext = AppDatabase(this@ActivityShoppingCartWithLimit)
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

                                AlertDialog.Builder(this@ActivityShoppingCartWithLimit)
                                    .setView(item)
                                    .setCancelable(false)
                                    .setPositiveButton("確定") { dialog, _ ->
                                        var contactInfo = CONTENT_CONTACTINFO()
                                        contactInfo.userName = editTextName.text.trim().toString()
                                        contactInfo.userPhoneNumber  = editTextPhone.text.trim().toString()
                                        contactInfo.userAddress  = editTextAddress.text.trim().toString()
                                        users.orderContent.userContactInfo = contactInfo

                                        it.ref.setValue(users)
                                        finishOrder()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("暫不提供") { dialog, _ ->
                                        users.orderContent.userContactInfo = null
                                        it.ref.setValue(users)
                                        finishOrder()
                                        dialog.dismiss()
                                    }
                                    .create()
                                    .show()
                            }
                            else
                            {
                                finishOrder()
                            }
                        }
                    }
                }
            })
        }
    }
}