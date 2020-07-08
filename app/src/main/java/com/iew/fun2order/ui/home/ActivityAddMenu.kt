package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.MenuTypeDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityLocalmage
import com.iew.fun2order.db.entity.entityMeunImage
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.db.firebase.STORE_INFO
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class ActivityAddMenu : AppCompatActivity() {

    private val ACTION_CAMERA_REQUEST_CODE = 100
    private val ACTION_ALBUM_REQUEST_CODE = 200
    private val ACTION_ADD_MENU_PROD_LIST_REQUEST_CODE = 300
    private val ACTION_ADD_RECIPE_REQUEST_CODE = 500
    private val ACTION_ADD_MENU_IMAGE_REQUEST_CODE = 600


    private lateinit var mDBContext: AppDatabase
    private lateinit var mMenuTypeDB: MenuTypeDAO
    private lateinit var mContext : Context

    private var bIsUpdateImage = false
    private var menuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()

    private lateinit var mTextViewMenuPic: TextView
    private lateinit var mDialog : AlertDialog

    private var mbEdit = false

    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    private var mFirebaseUserProfile: USER_PROFILE = USER_PROFILE()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu)
        supportActionBar?.hide()

        bIsUpdateImage = false

        mContext = this@ActivityAddMenu
        mDBContext = AppDatabase(mContext!!)
        mMenuTypeDB = mDBContext.menutyoedao()

        mTextViewMenuPic = findViewById<TextView>(R.id.textViewMenuPic)


        val sEditFlag = intent.extras?.getString("EDIT")
        mFirebaseUserProfile  = intent.extras?.get("USER_PROFILE") as USER_PROFILE

        if(sEditFlag == "Y")
        {
            mbEdit = true
            mFirebaseUserMenu = intent.extras?.get("USER_MENU") as USER_MENU
        }
        else
        {
            val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
            mFirebaseUserMenu = USER_MENU()
            mFirebaseUserMenu.userID = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            mFirebaseUserMenu.menuNumber = FirebaseAuth.getInstance().currentUser!!.uid.toString() + "-MENU-" + timeStamp
        }

        if(mbEdit){

            val editTextMenuID              = findViewById<EditText>(R.id.editTextMenuID)
            val textViewCrMenuType          = findViewById<TextView>(R.id.textViewCrMenuType)
            val editTextMenuDesc            = findViewById<EditText>(R.id.editTextMenuDesc)
            val textViewLocationItemCount   = findViewById<TextView>(R.id.textViewLocationItemCount)
            val textViewProductPriceItemCount= findViewById<TextView>(R.id.textViewProductPriceItemCount)

            editTextMenuID.setText(mFirebaseUserMenu.brandName)
            editTextMenuDesc.setText(mFirebaseUserMenu.menuDescription)
            textViewCrMenuType.text = mFirebaseUserMenu.brandCategory

            setImageButton()

            textViewLocationItemCount.text = "${mFirebaseUserMenu.locations!!.size.toString()} 項";
            textViewProductPriceItemCount.text = "${mFirebaseUserMenu.menuItems!!.size.toString()} 項";


        }

        //---- 以下用不到 ------
        val ImageButtonAction = arrayOf("相機/相簿","取消")
        mTextViewMenuPic.setOnClickListener {
            val Alert =  AlertDialog.Builder(mContext)
                .setTitle("選取照片來源")
                .setItems(ImageButtonAction,  DialogInterface.OnClickListener { dialog, which ->

                    when (which) {
                        0 -> { takeImageFromAlbumWithCropImageLib()}
                        else -> { // Note the block
                          //  Toast.makeText(this, "選取到取消", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .create()
                .show()
        }


        val textViewEditStoreInfo = findViewById<TextView>(R.id.textViewEditStoreInfo)
        textViewEditStoreInfo.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_store_info, null)
            val editTextStoreName        = item.findViewById(R.id.editTextStoreName) as EditText
            val editTextStoreAddress     = item.findViewById(R.id.editTextStoreAddress) as EditText
            val editTextStorePhoneNumber = item.findViewById(R.id.editTextStorePhoneNumber) as EditText
            if(mFirebaseUserMenu.storeInfo != null) {
                editTextStoreName.setText(mFirebaseUserMenu.storeInfo!!.storeName)
                editTextStoreAddress.setText(mFirebaseUserMenu.storeInfo!!.storeAddress)
                editTextStorePhoneNumber.setText(mFirebaseUserMenu.storeInfo!!.storePhoneNumber)
            }
            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                        val storeInfo : STORE_INFO = STORE_INFO()
                        storeInfo.storeName = editTextStoreName.text.toString()
                        storeInfo.storeAddress = editTextStoreAddress.text.toString()
                        storeInfo.storePhoneNumber = editTextStorePhoneNumber.text.toString()
                        mFirebaseUserMenu.storeInfo = storeInfo
                        alertDialog.dismiss()
                }
        }

        //--- 編輯產品項目
        val imageViewProductPriceItemList = findViewById<ImageView>(R.id.imageViewProductPriceItemList)
        imageViewProductPriceItemList.setOnClickListener {
            getProductListOfMenu(mContext)
        }

        // get reference to ImageView
        val textViewProductPriceItem = findViewById<TextView>(R.id.textViewProductPriceItem)
        textViewProductPriceItem.setOnClickListener {
            getProductListOfMenu(mContext)
        }

        // get reference to ImageView
        val textViewProductPriceItemCount = findViewById<TextView>(R.id.textViewProductPriceItemCount)
        textViewProductPriceItemCount.setOnClickListener {
            getProductListOfMenu(mContext)
        }

        //---- 新增產品列表 ----
        val textViewAddProductPrice = findViewById<TextView>(R.id.textViewAddProductPrice)
        textViewAddProductPrice.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_product_price, null)
            val radioGroup =item.findViewById(R.id.radioGroup) as RadioGroup
            val radio1 =item.findViewById(R.id.radioLimit) as RadioButton
            val radio2  =item.findViewById(R.id.radioNoLimit) as RadioButton
            val editLimitCount = item.findViewById(R.id.editLimitCount) as EditText

            //------ Default 設定不限量 ----
            radioGroup.check(radio2.id)
            editLimitCount.visibility = View.INVISIBLE
            editLimitCount.isClickable = false

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
                if(radioButton.text == "限量") {
                    editLimitCount.visibility = View.VISIBLE
                } else {
                    editLimitCount.visibility = View.INVISIBLE
                    editLimitCount.isClickable = false
                }
            }


            val alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setCancelable(false)
                .setPositiveButton("確定",null)
                .setNegativeButton("取消",null)
                .show()

                alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val editTextProduct = item.findViewById(R.id.editTextProduct) as EditText
                    val editTextProductPrice = item.findViewById(R.id.editTextProductPrice) as EditText
                    val textViewProductPriceItemCount = findViewById<TextView>(R.id.textViewProductPriceItemCount)

                    if (TextUtils.isEmpty(editTextProduct.text.trim()))
                    {
                        editTextProduct.requestFocus()
                        editTextProduct.error = "產品名稱不能為空白!"

                    }else {
                        var bFound = false
                        mFirebaseUserMenu.menuItems!!.forEach {
                            if(it.itemName.equals(editTextProduct.text.toString().trim())){
                                bFound = true
                            }
                        }

                        if(bFound){
                            editTextProduct.requestFocus()
                            editTextProduct.error = "產品名稱不能重覆!"

                        }else{
                            if(radio1.isChecked && editLimitCount.text.toString()== "")
                            {
                                editLimitCount.requestFocus()
                                editLimitCount.error = "限量數量必須填寫!"
                            }
                            else {
                                val fdProduct: PRODUCT = PRODUCT()
                                fdProduct.itemName = editTextProduct.text.toString().trim()
                                try {
                                    val parsedInt = editTextProductPrice.text.toString().toInt()
                                    fdProduct.itemPrice = parsedInt
                                    if (radio1.isChecked && editLimitCount.text.toString() != "") {
                                        val limitCount = editLimitCount.text.toString().toInt()
                                        fdProduct.quantityLimitation = limitCount
                                        fdProduct.quantityRemained = limitCount
                                    }
                                    else if(radio2.isChecked)
                                    {
                                        fdProduct.quantityLimitation = null
                                        fdProduct.quantityRemained = null
                                    }

                                } catch (nfe: NumberFormatException) {
                                    fdProduct.itemPrice = 0
                                }

                                fdProduct.sequenceNumber = mFirebaseUserMenu.menuItems!!.size + 1
                                mFirebaseUserMenu.menuItems!!.add(fdProduct)
                                val strProductPriceItemCount = "${mFirebaseUserMenu.menuItems!!.size.toString()} 項"
                                textViewProductPriceItemCount.text = strProductPriceItemCount
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
        }


        //---- 新增品牌分類 ----
        val textViewSelectBrandType = findViewById<TextView>(R.id.textViewSelectBrandType)
        textViewSelectBrandType.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_menu_type, null)
            val array = arrayListOf<String>()
            mFirebaseUserProfile.brandCategoryList!!.forEach(){
                array.add(it)
            }

            var Adapter: ArrayAdapter<String>? = null
            Adapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, array)
            val listView = item.findViewById(R.id.listViewMenuTypeListItems) as ListView
            listView.setAdapter(Adapter)

            for (i in 0 until listView.childCount) {
                (listView.getChildAt(i) as TextView).setTextColor(Color.GREEN)
            }

            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val textViewCrMenuType = findViewById<TextView>(R.id.textViewCrMenuType)
                textViewCrMenuType.text = selectedItem;
                mDialog.dismiss()
            }

            val alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("加入菜單分類", null)
                .setNegativeButton("取消", null)

            mDialog = alertDialog.show();
            mDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText

                    val textViewCrMenuType = findViewById<TextView>(R.id.textViewCrMenuType)

                    if (TextUtils.isEmpty(editTextMenuType.text.toString().trim()))
                    {
                        editTextMenuType.requestFocus()
                        editTextMenuType.error = "類別不能為空白!"
                    }else {
                        textViewCrMenuType.text = editTextMenuType.text.toString();
                        addUserMenuFromFireBase(editTextMenuType.text.toString())
                        mDialog.dismiss()
                    }
                }
        }


        //---- 指定配方 ----
        val textViewAddRecipe = findViewById<TextView>(R.id.textViewAddRecipe)
        textViewAddRecipe.setOnClickListener {
            val bundle = Bundle()
            val I = Intent(mContext, ActivityAddRecipe::class.java)
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_RECIPE_REQUEST_CODE)
        }


        //---- 編輯照片與敘述 ----
        val textViewEditImageDesc = findViewById<TextView>(R.id.textViewEditImageDesc)
        textViewEditImageDesc.setOnClickListener {
            val bundle = Bundle()
            val I = Intent(mContext, ActivityAddMenuImage::class.java)
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_MENU_IMAGE_REQUEST_CODE)
        }


        //---- 照片集合呈現處 ----
        val gridLayoutImageBtnList = findViewById<GridLayout>(R.id.gridLayoutImageBtnList)
        gridLayoutImageBtnList.setOnClickListener {
            val bundle = Bundle()
            val I = Intent(mContext, ActivityAddMenuImage::class.java)
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_MENU_IMAGE_REQUEST_CODE)
        }


        //---- 產生菜單 ----
        val textViewMakeMenu = findViewById<TextView>(R.id.textViewMakeMenu)
        textViewMakeMenu.setOnClickListener {

            val editTextMenuID = findViewById<EditText>(R.id.editTextMenuID)
            if (TextUtils.isEmpty(editTextMenuID.text.toString().trim()))
            {
                editTextMenuID.requestFocus()
                editTextMenuID.error = "品牌名不能為空白!"
            }else {
                createNewMenu()
            }
        }
    }

    private fun takeImageFromAlbumWithCropImageLib() {
        CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val context: Context = this@ActivityAddMenu
        when(requestCode) {
            ACTION_CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    displayImage(data.extras.get("data") as Bitmap)
                }
            }

            ACTION_ALBUM_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val resolver = context!!.contentResolver
                    val bitmap = MediaStore.Images.Media.getBitmap(resolver, data.data)
                    displayImage(bitmap)

                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {

                    val bitmap = BitmapFactory.decodeFile(result.uri.path)
                    displayImage(bitmap)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                }
            }

            ACTION_ALBUM_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val resolver = context!!.contentResolver
                    val bitmap = MediaStore.Images.Media.getBitmap(resolver, data.data)
                    displayImage(bitmap)

                }
            }

            ACTION_ADD_RECIPE_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    mFirebaseUserMenu = data.extras.get("USER_MENU") as USER_MENU
                }
            }

            ACTION_ADD_MENU_PROD_LIST_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    mFirebaseUserMenu = data.extras.get("USER_MENU") as USER_MENU
                    val textViewProductPriceItemCount = findViewById(R.id.textViewProductPriceItemCount) as TextView
                    textViewProductPriceItemCount.setText(mFirebaseUserMenu.menuItems!!.size.toString() + " 項");
                }
            }

            ACTION_ADD_MENU_IMAGE_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    mFirebaseUserMenu = data.extras.get("USER_MENU") as USER_MENU
                    //mbImageModified = data.extras.get("IMAGE_CHG") as Boolean
                    //mMenuImages = data.extras.get("MENU_IMAGES") as MutableList<Bitmap>
                    val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
                    editTextMenuDesc.setText( mFirebaseUserMenu.menuDescription)
                    showUpdateImage()
                    bIsUpdateImage = true
                }
                else
                {
                    bIsUpdateImage = false

                }
            }

            else -> {
                println("no handler onActivityReenter")
            }
        }
    }


    private fun Bitmap.resizeByWidth(width:Int): Bitmap {
        val ratio:Float = this.width.toFloat() / this.height.toFloat()
        val height:Int = Math.round(width / ratio)

        return Bitmap.createScaledBitmap(
            this,
            width-5,
            height-5,
            false
        )
    }

    private fun Bitmap.resizeToFireBaseStorage_MenuInfo(): Bitmap {

        var new_width = 1
        var new_heithg = 1
        val maxWitdh_Height = this.width.coerceAtLeast(this.height)

        if(maxWitdh_Height < 1440)
        {

            return this
        }
        else {

            if (maxWitdh_Height == this.width)
            {

                var ratio:Float = (this.height.toFloat() / this.width.toFloat()).toFloat() ;
                new_width = 1440;
                new_heithg = (1440  * ratio).roundToInt();

            }
            else
            {
                var ratio: Float =  (this.width.toFloat() / this.height.toFloat()).toFloat()  ;
                new_width = (1440  * ratio).roundToInt();
                new_heithg = 1440;

            }
            //val ratio:Float = this.width.toFloat() / this.height.toFloat()
            //val height:Int = Math.round(width / ratio)

            //val width : Int = (this.width.toFloat() / 1440f).roundToInt()
            //val height: Int = (this.height.toFloat() / 1440f).roundToInt()
            // val width : Int = (this.width.toFloat() / 1440f).roundToInt()
            // val height: Int = (this.height.toFloat() / 1440f).roundToInt()

            return Bitmap.createScaledBitmap(
                this,
                new_width,
                new_heithg,
                false
            )
        }
    }

    private fun displayImage(bitmap: Bitmap) {

/*      舊功能現在沒有使用了
        val resizedBitmap = bitmap.resizeByWidth(mMenuImage.layoutParams.height)
        //val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resizedBitmap)
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        //roundedBitmapDrawable.cornerRadius =  (resizedBitmap.width / 2.0).toFloat()
        mMenuBitmap =resizedBitmap
        mMenuImage.setImageDrawable(roundedBitmapDrawable)

 */
    }

    private fun createNewMenu() {
        val userMenu: USER_MENU = mFirebaseUserMenu

        val editTextMenuID = findViewById<EditText>(R.id.editTextMenuID)
        val editTextMenuDesc = findViewById<EditText>(R.id.editTextMenuDesc)
        val textViewCrMenuType = findViewById<TextView>(R.id.textViewCrMenuType)


        val mAuth = FirebaseAuth.getInstance()
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        var userMenuID = userMenu.menuNumber.toString()

        userMenu.brandCategory = textViewCrMenuType.text.toString()
        userMenu.brandName=editTextMenuID.text.toString().replace("\n","")
        userMenu.createTime=timeStamp
        userMenu.menuDescription=editTextMenuDesc.text.toString()
        userMenu.menuImageURL=""
        userMenu.menuNumber=userMenuID
        userMenu.userID=mAuth.currentUser!!.uid
        userMenu.userName=mAuth.currentUser!!.displayName
        userMenu.multiMenuImageURL = mFirebaseUserMenu.multiMenuImageURL

        //------ Upload Image  -------
        if(bIsUpdateImage) {
            uploadImageToFirebase(userMenu.userID!!, userMenu.menuNumber!!, userMenu.multiMenuImageURL!!)
            saveMenuICONToDB(userMenu.userID!!, userMenu.menuNumber!!, userMenu.multiMenuImageURL!!)
            bIsUpdateImage = false
        }

        Firebase.database.reference.child("USER_MENU_INFORMATION").child(mAuth.currentUser!!.uid).child(userMenuID).setValue(userMenu)
            .addOnSuccessListener {
                // Write was successful!
                val bundle = Bundle()
                bundle.putString("Result", "OK")
                val intent = Intent().putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "建立菜單失敗!", Toast.LENGTH_SHORT).show()
            }
    }


    private fun uploadImageToFirebase(MenuUserID:String,  MenuNumber:String, MenuImageURL : MutableList<String>)
    {
        //---- 上傳之前先砍掉所有影像 -----
        if(MenuUserID!= "" && MenuNumber != "") {
            val ImageFolder = "Menu_Image/${MenuUserID}/${MenuNumber}"
            val listRef = Firebase.storage.reference.child(ImageFolder!!)
            listRef.listAll()
                .addOnSuccessListener { listResult ->
                    listResult.prefixes.forEach { prefix ->
                        // All the prefixes under listRef.
                        // You may call listAll() recursively on them.
                    }

                    listResult.items.forEach { item ->
                        item.delete()
                        // All the items under listRef
                    }

                    //--- 砍完以後再新增影像到Firebase 上面-----
                    val MemoryDBContext = MemoryDatabase(this!!)
                    val MenuImageDB = MemoryDBContext.menuImagedao()

                    MenuImageURL.forEach()
                    {
                        imageURL ->
                        val MenuImageObject = MenuImageDB.getMenuImageByName(imageURL)
                        if(MenuImageObject!= null) {
                            val islandRef = Firebase.storage.reference.child(imageURL!!)
                            val uploadTask: UploadTask = islandRef.putBytes(MenuImageObject.image)
                            uploadTask.addOnFailureListener(object : OnFailureListener {
                                override fun onFailure(p0: Exception) {
                                   // Toast.makeText(mContext, "照片上傳失敗: " + imageURL, Toast.LENGTH_SHORT).show()
                                }
                            }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                                  //  Toast.makeText(mContext, "照片上傳成功: " + imageURL, Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        else
                        {
                            val notifyAlert = AlertDialog.Builder(this).create()
                            notifyAlert.setTitle("存取影像錯誤")
                            notifyAlert.setMessage("更新照片資料 ${imageURL} \n資料讀取錯誤!!")
                            notifyAlert.setButton(
                                AlertDialog.BUTTON_POSITIVE,
                                "OK"
                            ) { _, i ->
                            }
                            notifyAlert.show()
                        }
                    }
                    Toast.makeText(mContext, "菜單照片更新完成", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Uh-oh, an error occurred!
                }
        }

    }

    private fun saveMenuICONToDB(MenuUserID:String,  MenuNumber:String, MenuImageURL : MutableList<String>)
    {

        val dbContext = AppDatabase(this)
        val menuICON = dbContext.localImagedao()
        val memoryDBContext = MemoryDatabase(this!!)
        val menuImageDB = memoryDBContext.menuImagedao()
        val iCON_Image = MenuImageURL.firstOrNull()
        if(iCON_Image != null)
        {
            val oldICONImage = menuICON.getMenuImageByName(iCON_Image)
            val MenuImageObject = menuImageDB.getMenuImageByName(iCON_Image)
            if(MenuImageObject!=null)
            {
                if(oldICONImage == null) {
                    menuICON.insertRow(entityLocalmage(null, MenuImageObject.name, "", MenuImageObject.image.clone()!!))
                }
                else
                {
                    oldICONImage.image = MenuImageObject.image.clone()
                    menuICON.updateTodo(oldICONImage)
                }
            }
        }
    }


    private fun uploadImage(bitmap: Bitmap, menu_number:String,  image_name: String) {

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

            val resizedBitmap = bitmap.resizeToFireBaseStorage_MenuInfo()
            //val resizedBitmap = bitmap
            val photoURL : String = "Menu_Image/${mAuth.currentUser!!.uid}/${menu_number}/${image_name}.jpg"

            var islandRef = Firebase.storage.reference.child(photoURL!!)
            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val data: ByteArray = baos.toByteArray()
            val uploadTask: UploadTask = islandRef.putBytes(data)
            uploadTask.addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: Exception) {
                    Toast.makeText(this@ActivityAddMenu, "Upload Image Faild : $image_name", Toast.LENGTH_SHORT).show()
                }

            }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                }

            })

        }

    }

    private fun getProductListOfMenu(context:Context) {
        val bound = Bundle();
        bound.putParcelable("USER_MENU", mFirebaseUserMenu)
        val intent =  Intent(context, ActivityProductPriceList::class.java)
        intent.putExtras(bound);
        startActivityForResult(intent,ACTION_ADD_MENU_PROD_LIST_REQUEST_CODE)
    }



    private fun showUpdateImage() {
        val gridLayoutBtnList = findViewById(com.iew.fun2order.R.id.gridLayoutImageBtnList) as GridLayout
        var metrics = DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics); //抓取螢幕大小的資料
        var width:Int = 0
        var iCnt:Int = 1
        if(mFirebaseUserMenu.multiMenuImageURL!!.size>1){
            width = (metrics.widthPixels-120)/mFirebaseUserMenu.multiMenuImageURL!!.size ;        // 螢幕的寬度/4放近with
            iCnt = mFirebaseUserMenu.multiMenuImageURL!!.size

        }else{
            width = (metrics.widthPixels-120)/1 ;        // 螢幕的寬度/4放近with
        }
        gridLayoutBtnList.removeAllViews()
        gridLayoutBtnList.setColumnCount(iCnt);           // 設定GridLayout有幾行
        gridLayoutBtnList.setRowCount(1);              // 設定GridLayout有幾列


        val MemoryDBContext = MemoryDatabase(this!!)
        val MenuImageDB = MemoryDBContext.menuImagedao()
        mFirebaseUserMenu.multiMenuImageURL!!.forEach {
            if(it != null){

                var menuImaeg = MenuImageDB.getMenuImageByName(it)
                if (menuImaeg != null) {
                    val bm: Bitmap = BitmapFactory.decodeByteArray( menuImaeg.image, 0,  menuImaeg.image!!.size)
                    val b1 = ImageButton(this)
                    b1.setBackgroundResource(R.drawable.corners_rect_gray)
                    b1.setPadding(10, 10, 10, 10)
                    b1.setMinimumWidth(width)
                    b1.setImageBitmap(bm)
                    b1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    gridLayoutBtnList.addView(b1,width,350);
                }
                else{

                    val notifyAlert = AlertDialog.Builder(this).create()
                    notifyAlert.setTitle("存取影像錯誤")
                    notifyAlert.setMessage("更新照片資料 ${it} \n資料讀取錯誤!!")
                    notifyAlert.setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        "OK"
                    ) { dialogInterface, i ->
                    }
                    notifyAlert.show()
                }
            }
        }

        gridLayoutBtnList.children.forEach {
            val container = it as ImageView
            container.setOnClickListener {
                // your click code here
                val bundle = Bundle()
                //bundle.putString("EDIT", "N")
                var I = Intent(this, ActivityAddMenuImage::class.java)
                bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
                I.putExtras(bundle)
                startActivityForResult(I, ACTION_ADD_MENU_IMAGE_REQUEST_CODE)
            }
        }

    }

    private fun setImageButton() {
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


    private fun DisplayMeunImageItem(multiMenuImageURL: MutableList<String>?) {

        var replyImageCount = 0
        var totalImageCount = multiMenuImageURL!!.filter { it != "" }.count()
        var MemoryDBContext = MemoryDatabase(this!!)
        var MenuImageDB = MemoryDBContext.menuImagedao()
        menuImaegByteArray.clear()
        multiMenuImageURL!!.forEach {
            if (it != "") {
                menuImaegByteArray.put(it, null)
                //----- 每次載入畫面都從FireBase抓取一次 -----
                var menuImaeg = MenuImageDB.getMenuImageByName(it)
                if (menuImaeg != null) {
                    MenuImageDB.delete(menuImaeg)
                }
                val islandRef = Firebase.storage.reference.child(it)
                val ONE_MEGABYTE = 1024 * 1024.toLong()
                islandRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener { bytesPrm: ByteArray ->
                        menuImaegByteArray[it] = bytesPrm.clone()
                        try {
                            MenuImageDB.insertRow(entityMeunImage(null, it, "", bytesPrm.clone()!!))
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
        val gridLayoutBtnList = findViewById(com.iew.fun2order.R.id.gridLayoutImageBtnList) as GridLayout
        val MenuImaegExist = menuImaegByteArray.filter { it.value != null }
        val MenuImaegFailed = menuImaegByteArray.filter { it.value == null }

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

        gridLayoutBtnList.children.forEach {
            val container = it as ImageView
            container.setOnClickListener {
                // your click code here
                val bundle = Bundle()
                //bundle.putString("EDIT", "N")
                var I = Intent(this, ActivityAddMenuImage::class.java)
                bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
                I.putExtras(bundle)
                startActivityForResult(I, ACTION_ADD_MENU_IMAGE_REQUEST_CODE)
            }
        }
    }



    private fun addUserMenuFromFireBase(menutype: String)
    {
        val context = this
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    mFirebaseUserProfile = userProfile
                    userProfile.brandCategoryList!!.add(menutype)
                    //createMenuTypeButton(userProfile)
                    dataSnapshot.ref.setValue(userProfile)

                }

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    override fun onBackPressed() {
        //super.onBackPressed()
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("確認動作")
            .setMessage("放棄修改內容?")
            .setPositiveButton(
                "確定"
            ) { dialog, which -> finish() }
            .setNegativeButton("取消", null)
            .show()
    }


}

