package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.children
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.MenuTypeDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityMeunImage
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.db.firebase.STORE_INFO
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class ActivityAddMenu : AppCompatActivity() {

    private val ACTION_CAMERA_REQUEST_CODE = 100
    private val ACTION_ALBUM_REQUEST_CODE = 200
    private val ACTION_ADD_MENU_PROD_LIST_REQUEST_CODE = 300
    private val ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE = 400
    private val ACTION_ADD_RECIPE_REQUEST_CODE = 500
    private val ACTION_ADD_MENU_IMAGE_REQUEST_CODE = 600

    private lateinit var mMenuTypeDB: MenuTypeDAO
    //private lateinit var mUserMenuDB: UserMenuDAO
    //private lateinit var mLocationDB: LocationDAO
    //private lateinit var mProductDB: ProductDAO
    private lateinit var mDBContext: AppDatabase
    private var mContext : Context? = null

    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()

    //private lateinit var mMenuImage: ImageButton
    private lateinit var mMenuBitmap : Bitmap
    // var mMenuImages: MutableList<Bitmap> = mutableListOf()
    private lateinit var mTextViewMenuPic: TextView
    private lateinit var  mDialog : AlertDialog
    var mMediaStorageDir: File? = null
    var mMediaStorageReadDir: File? = null
    private var mbEdit = false
    //private lateinit var mUserMenu:UserMenu
    private lateinit var mbutton: Array<Button>
    private var mCount_num: TextView? = null
    //Firebase DB
    private lateinit var mDatabase: DatabaseReference
    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    private var mFirebaseUserProfile: USER_PROFILE = USER_PROFILE()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu)
        supportActionBar?.hide()

        val context: Context = this@ActivityAddMenu

        // [START initialize_database_ref]
        mDatabase = Firebase.database.reference
        mContext = this

        // [END initialize_database_ref]

        mDBContext = AppDatabase(context!!)
        mMenuTypeDB = mDBContext.menutyoedao()
        //mUserMenuDB = mDBContext.usermenudao()
        //mLocationDB = mDBContext.locationdao()
        //mProductDB = mDBContext.productdao()
        //mMenuImage  = findViewById(R.id.imageBtnMenuImage) as ImageButton
        //mMenuImage.requestFocus()
        mTextViewMenuPic = findViewById(R.id.textViewMenuPic) as TextView
        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }

        val sEditFlag = intent.extras.getString("EDIT")
        val sMenuID = intent.extras.getString("MENU_ID")
        mFirebaseUserProfile = intent.extras.get("USER_PROFILE") as USER_PROFILE

        if(sEditFlag.equals("Y")) {
            mbEdit = true
            mFirebaseUserMenu = intent.extras.get("USER_MENU") as USER_MENU

        }else{
            val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
            mFirebaseUserMenu = USER_MENU()
            mFirebaseUserMenu.userID=mAuth.currentUser!!.uid.toString()
            mFirebaseUserMenu.menuNumber=mAuth.currentUser!!.uid.toString() + "-MENU-" + timeStamp
        }
        mMediaStorageDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/Android/data/"
                    + applicationContext.packageName
                    + "/Files"
                    + "/Menu_Image"
                    + "/" + mFirebaseUserMenu.userID
                    + "/" + mFirebaseUserMenu.menuNumber
        )

        mMediaStorageReadDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/Android/data/"
                    + applicationContext.packageName
                    + "/Files/"
        )

        if(mbEdit){
            val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
            val textViewCrMenuType = findViewById(R.id.textViewCrMenuType) as TextView
            val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
            val textViewLocationItemCount = findViewById(R.id.textViewLocationItemCount) as TextView
            val textViewProductPriceItemCount = findViewById(R.id.textViewProductPriceItemCount) as TextView
            editTextMenuID.setText(mFirebaseUserMenu.brandName)
            textViewCrMenuType.setText(mFirebaseUserMenu.brandCategory)
            editTextMenuDesc.setText(mFirebaseUserMenu.menuDescription)
            setImageButton()
            /*
            if(mFirebaseUserMenu.menuImageURL != ""){
                var islandRef = Firebase.storage.reference.child(mFirebaseUserMenu.menuImageURL!!)
                val ONE_MEGABYTE = 1024 * 1024.toLong()
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytesPrm: ByteArray ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    displayImage(bmp)
                }
            }

             */

            //displayImage(BitmapFactory.decodeByteArray(mUserMenu.image, 0, mUserMenu.image.size))
            textViewLocationItemCount.setText(mFirebaseUserMenu.locations!!.size.toString() + " 項");
            textViewProductPriceItemCount.setText(mFirebaseUserMenu.menuItems!!.size.toString()  + " 項");
            /*
             mUserMenu = mUserMenuDB.getMenuByID(sMenuID)
            editTextMenuID.setText(mUserMenu.menu_id)
            textViewCrMenuType.setText(mUserMenu.menu_type)
            editTextMenuDesc.setText(mUserMenu.menu_desc)
            displayImage(BitmapFactory.decodeByteArray(mUserMenu.image, 0, mUserMenu.image.size))
            textViewLocationItemCount.setText(mLocationDB.getLocationByMenuID(editTextMenuID.getText().toString()).count().toString() + " 項");
            textViewProductPriceItemCount.setText(mProductDB.getProductByMenuID(editTextMenuID.getText().toString()).count().toString() + " 項");
             */

        }else{
            /*
            val bitmap  = BitmapFactory.decodeResource(getResources(),R.drawable.image_default_member);
            displayImage(bitmap)

             */
        }
        val ImageButtonAction = arrayOf("相機/相簿","取消")


/*
        mMenuImage.setOnClickListener {
            //Toast.makeText(activity, "TESTING BUTTON CLICK 1", Toast.LENGTH_SHORT).show()

            val Alert =  AlertDialog.Builder(context!!)
                .setTitle("選取照片來源")
                .setItems(ImageButtonAction,  DialogInterface.OnClickListener { dialog, which ->

                    when (which) {
                        0 -> { takeImageFromAlbumWithCropImageLib()}
                        else -> { // Note the block
                            Toast.makeText(this, "選取到取消", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .create()
                .show()
        }
*/
        mTextViewMenuPic.setOnClickListener {
            //Toast.makeText(activity, "TESTING BUTTON CLICK 1", Toast.LENGTH_SHORT).show()

            val Alert =  AlertDialog.Builder(context!!)
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


        val textViewEditStoreInfo = findViewById(R.id.textViewEditStoreInfo) as TextView
        // set on-click listener for TextView
        textViewEditStoreInfo.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_store_info, null)
            var editTextStoreName = item.findViewById(R.id.editTextStoreName) as EditText
            val editTextStoreAddress = item.findViewById(R.id.editTextStoreAddress) as EditText
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
                    var editTextStoreName = item.findViewById(R.id.editTextStoreName) as EditText
                    val editTextStoreAddress = item.findViewById(R.id.editTextStoreAddress) as EditText
                    val editTextStorePhoneNumber = item.findViewById(R.id.editTextStorePhoneNumber) as EditText

/*
                        Toast.makeText(
                            applicationContext,
                            "加入地點:" + editTextLocation.getText().toString(), Toast.LENGTH_SHORT
                        ).show()

 */
                        // Insert DB
                        //val location: Location = Location(null, editTextMenuID.getText().toString(), editTextLocation.getText().toString())
                        //mLocationDB.insertRow(location)
                        //var fdlocation:LOCATION = LOCATION()
                        //fdlocation.location =  editTextLocation.getText().toString()
                        var storeInfo : STORE_INFO = STORE_INFO()
                        storeInfo.storeName = editTextStoreName.getText().toString()
                        storeInfo.storeAddress = editTextStoreAddress.getText().toString()
                        storeInfo.storePhoneNumber = editTextStorePhoneNumber.getText().toString()

                        mFirebaseUserMenu.storeInfo = storeInfo

                        alertDialog.dismiss()

                }
        }

        //ProductPriceList
        // get reference to ImageView
        val imageViewProductPriceItemList = findViewById(R.id.imageViewProductPriceItemList) as ImageView
        // set on-click listener for ImageView
        imageViewProductPriceItemList.setOnClickListener {
            getProductListOfMenu(context)

        }

        // get reference to ImageView
        val textViewProductPriceItem = findViewById(R.id.textViewProductPriceItem) as TextView
        // set on-click listener for ImageView
        textViewProductPriceItem.setOnClickListener {
            getProductListOfMenu(context)

        }

        // get reference to ImageView
        val textViewProductPriceItemCount = findViewById(R.id.textViewProductPriceItemCount) as TextView
        // set on-click listener for ImageView
        textViewProductPriceItemCount.setOnClickListener {
            getProductListOfMenu(context)

        }

        //ProductPriceList
        // get reference to ImageView
        val imageViewLocationItemList = findViewById(R.id.imageViewLocationItemList) as ImageView
        // set on-click listener for ImageView
        imageViewLocationItemList.setOnClickListener {
            getLocationListOfMenu(context)
        }

        val textViewLocationItem = findViewById(R.id.textViewLocationItem) as TextView
        // set on-click listener for ImageView
        textViewLocationItem.setOnClickListener {
            getLocationListOfMenu(context)
        }

        val textViewLocationItemCount = findViewById(R.id.textViewLocationItemCount) as TextView
        // set on-click listener for ImageView
        textViewLocationItemCount.setOnClickListener {
            getLocationListOfMenu(context)
        }


        val textViewAddLocation = findViewById(R.id.textViewAddLocation) as TextView
        // set on-click listener for ImageView
        textViewAddLocation.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_location, null)

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var editTextLocation = item.findViewById(R.id.editTextLocation) as EditText
                    val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
                    val textViewLocationItemCount = findViewById(R.id.textViewLocationItemCount) as TextView

                    if (TextUtils.isEmpty(editTextLocation.text.trim()))
                    {
                        editTextLocation.requestFocus()
                        editTextLocation.error = "地點不能為空白!"
                    }else {
/*
                        Toast.makeText(
                            applicationContext,
                            "加入地點:" + editTextLocation.getText().toString(), Toast.LENGTH_SHORT
                        ).show()

 */
                        // Insert DB
                        //val location: Location = Location(null, editTextMenuID.getText().toString(), editTextLocation.getText().toString())
                        //mLocationDB.insertRow(location)
                        //var fdlocation:LOCATION = LOCATION()
                        //fdlocation.location =  editTextLocation.getText().toString()

                        var bFOund = false
                        mFirebaseUserMenu.locations!!.forEach {
                            if(it.equals(editTextLocation.getText().toString().trim())){
                                bFOund = true
                            }
                        }

                        if(bFOund){
                            editTextLocation.requestFocus()
                            editTextLocation.error = "地點不能重覆!"
                        }else{
                            mFirebaseUserMenu.locations?.add(editTextLocation.getText().toString().toString())
                            //mLocationDB.getLocationByMenuID(editTextMenuID.getText().toString())

                            //textViewLocationItemCount.setText(mLocationDB.getLocationByMenuID(editTextMenuID.getText().toString()).count().toString() + " 項");
                            textViewLocationItemCount.setText(mFirebaseUserMenu.locations!!.size.toString() + " 項");
                            alertDialog.dismiss()
                        }

                    }
                }
        }

        val textViewAddProductPrice = findViewById(R.id.textViewAddProductPrice) as TextView
        // set on-click listener for ImageView
        textViewAddProductPrice.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_product_price, null)

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var editTextProduct = item.findViewById(R.id.editTextProduct) as EditText
                    var editTextProductPrice = item.findViewById(R.id.editTextProductPrice) as EditText
                    val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
                    val textViewProductPriceItemCount = findViewById(R.id.textViewProductPriceItemCount) as TextView

                    if (TextUtils.isEmpty(editTextProduct.text.trim()))
                    {
                        editTextProduct.requestFocus()
                        editTextProduct.error = "產品名稱不能為空白!"
                    }else {

                        //Toast.makeText(applicationContext,
                        //    "加入產品與價格:"+editTextProduct.getText().toString()+":"+editTextProductPrice.getText().toString(), Toast.LENGTH_SHORT).show()
                        // Insert DB
                        //val product: Product = Product(null, editTextMenuID.getText().toString(), editTextProduct.getText().toString(), editTextProductPrice.getText().toString())
                        //mProductDB.insertRow(product)

                        var bFOund = false
                        mFirebaseUserMenu.menuItems!!.forEach {
                            if(it.itemName.equals(editTextProduct.getText().toString().trim())){
                                bFOund = true
                            }
                        }

                        if(bFOund){
                            editTextProduct.requestFocus()
                            editTextProduct.error = "產品名稱不能重覆!"
                        }else{
                            //Firebase
                            var fdproduct : PRODUCT = PRODUCT()
                            fdproduct.itemName =editTextProduct.getText().toString().trim()

                            try {
                                val parsedInt = editTextProductPrice.getText().toString().toInt()
                                fdproduct.itemPrice=parsedInt

                            } catch (nfe: NumberFormatException) {
                                // not a valid int
                                fdproduct.itemPrice=0
                            }

                            fdproduct.sequenceNumber = mFirebaseUserMenu.menuItems!!.size+1
                            //mLocationDB.getLocationByMenuID(editTextMenuID.getText().toString())
                            mFirebaseUserMenu.menuItems!!.add(fdproduct)
                            //textViewProductPriceItemCount.setText(mProductDB.getProductByMenuID(editTextMenuID.getText().toString()).count().toString() + " 項");
                            textViewProductPriceItemCount.setText(mFirebaseUserMenu.menuItems!!.size.toString() + " 項");
                            alertDialog.dismiss()
                        }



                    }
                }
        }

        val textViewSelectBrandType = findViewById(R.id.textViewSelectBrandType) as TextView
        // set on-click listener for ImageView
        textViewSelectBrandType.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_menu_type, null)
            /*
            var menuTypelist = mMenuTypeDB.getMenuTypeslist()
            val array = arrayListOf<String>()
            if (menuTypelist.count() > 0) {

                menuTypelist.forEach()
                {
                    array.add(it.toString())
                }
            }

             */
            val array = arrayListOf<String>()
            mFirebaseUserProfile.brandCategoryList!!.forEach(){
                array.add(it)
            }
            var values = arrayOf(
                "台灣應材",
                "默克",
                "奇美材料"
            )


            var arr_aAdapter: ArrayAdapter<String>? = null

            arr_aAdapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, array)

            var listView = item.findViewById(R.id.listViewMenuTypeListItems) as ListView

            listView!!.setAdapter(arr_aAdapter)
            for (i in 0 until listView.getChildCount()) {
                (listView.getChildAt(i) as TextView).setTextColor(Color.GREEN)
            }

            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                // Get the selected item text from ListView
                val selectedItem = parent.getItemAtPosition(position) as String

                // Display the selected item text on TextView
                Toast.makeText(
                    this,
                    "Your Choose: $selectedItem",
                    Toast.LENGTH_LONG
                ).show()
                val textViewCrMenuType = findViewById(R.id.textViewCrMenuType) as TextView
                textViewCrMenuType.setText(selectedItem);
                mDialog.dismiss()
            }
            //listView.setCacheColorHint(Color.rgb(36, 33, 32));

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("加入菜單分類", null)
                .setNegativeButton("取消", null)
                //.show()
            //val alertDialog = AlertDialog.Builder(this)
            //alertDialog.setView(item)
            //alertDialog.setPositiveButton("加入菜單分類",null)
            //alertDialog.setNegativeButton("取消", null)
            //alertDialog.show()
            mDialog = alertDialog.show();

            mDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText
                    val textViewCrMenuType = findViewById(R.id.textViewCrMenuType) as TextView


                    if (TextUtils.isEmpty(editTextMenuType.text.toString().trim()))
                    {
                        editTextMenuType.requestFocus()
                        editTextMenuType.error = "類別不能為空白!"
                    }else {
                        //Toast.makeText(applicationContext,
                        //    "加入菜單分類:"+editTextMenuType.getText().toString(), Toast.LENGTH_SHORT).show()
                        // Insert DB
                        //val menutype: MenuType = MenuType(null, editTextMenuType.getText().toString())

                        textViewCrMenuType.setText(editTextMenuType.getText().toString());
                        //mMenuTypeDB.insertRow(menutype)
                        addUserMenuFromFireBase(editTextMenuType.getText().toString())
                        mDialog.dismiss()
                    }
                }
        }

        val textViewAddRecipe = findViewById(R.id.textViewAddRecipe) as TextView
        // set on-click listener for textViewMakeMenu
        textViewAddRecipe.setOnClickListener {

            val bundle = Bundle()
            //bundle.putString("EDIT", "N")
            var I = Intent(context, ActivityAddRecipe::class.java)
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_RECIPE_REQUEST_CODE)
        }

        val textViewEditImageDesc = findViewById(R.id.textViewEditImageDesc) as TextView
        // set on-click listener for textViewMakeMenu
        textViewEditImageDesc.setOnClickListener {

            val bundle = Bundle()
            var I = Intent(context, ActivityAddMenuImage::class.java)
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_MENU_IMAGE_REQUEST_CODE)
        }

        val gridLayoutImageBtnList = findViewById(R.id.gridLayoutImageBtnList) as GridLayout
        // set on-click listener for textViewMakeMenu
        gridLayoutImageBtnList.setOnClickListener {

            val bundle = Bundle()
            //bundle.putString("EDIT", "N")
            var I = Intent(context, ActivityAddMenuImage::class.java)
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_MENU_IMAGE_REQUEST_CODE)
        }


        val textViewMakeMenu = findViewById(R.id.textViewMakeMenu) as TextView
        // set on-click listener for textViewMakeMenu
        textViewMakeMenu.setOnClickListener {
            /*
            val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
            val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
            val textViewCrMenuType = findViewById(R.id.textViewCrMenuType) as TextView
            val baos = ByteArrayOutputStream()
            mMenuBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos)

            val usermenu: UserMenu = UserMenu(null, editTextMenuID.getText().toString(), editTextMenuDesc.getText().toString(),
                textViewCrMenuType.getText().toString(), baos.toByteArray())


            if(mbEdit){
                mUserMenu.menu_id=editTextMenuID.getText().toString()
                mUserMenu.menu_desc=editTextMenuDesc.getText().toString()
                mUserMenu.menu_type= textViewCrMenuType.getText().toString()
                mUserMenu.image= baos.toByteArray()
                mUserMenuDB.updateTodo(mUserMenu)
            }else{
                mUserMenuDB.insertRow(usermenu)
            }
*/
            val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
            if (TextUtils.isEmpty(editTextMenuID.text.toString().trim()))
            {
                editTextMenuID.requestFocus()
                editTextMenuID.error = "品牌名不能為空白!"
            }else {

                createNewMenu()
            }
            //val bundle = Bundle()
            //bundle.putString("Result", "OK")
            //val intent = Intent().putExtras(bundle)
            //setResult(Activity.RESULT_OK, intent)
            //finish()
        }
    }

    private fun takeImageFromAlbumWithCropImageLib() {

        CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(this)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")
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

            ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    mFirebaseUserMenu = data.extras.get("USER_MENU") as USER_MENU
                    val textViewLocationItemCount = findViewById(R.id.textViewLocationItemCount) as TextView
                    textViewLocationItemCount.setText(mFirebaseUserMenu.locations!!.size.toString() + " 項");
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

/*
        val resizedBitmap = bitmap.resizeByWidth(mMenuImage.layoutParams.height)
        //val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resizedBitmap)
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        //roundedBitmapDrawable.cornerRadius =  (resizedBitmap.width / 2.0).toFloat()
        mMenuBitmap =resizedBitmap
        mMenuImage.setImageDrawable(roundedBitmapDrawable)

 */
    }

    private fun createNewMenu() {
        var userMenu: USER_MENU = mFirebaseUserMenu

        val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
        val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
        val textViewCrMenuType = findViewById(R.id.textViewCrMenuType) as TextView
        val baos = ByteArrayOutputStream()

        //mMenuBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos)

        //val usermenu: UserMenu = UserMenu(null, editTextMenuID.getText().toString(), editTextMenuDesc.getText().toString(),
        //    textViewCrMenuType.getText().toString(), baos.toByteArray())

/*
        if(mbEdit){
            mUserMenu.menu_id=editTextMenuID.getText().toString()
            mUserMenu.menu_desc=editTextMenuDesc.getText().toString()
            mUserMenu.menu_type= textViewCrMenuType.getText().toString()
            mUserMenu.image= baos.toByteArray()
            mUserMenuDB.updateTodo(mUserMenu)
        }else{
            mUserMenuDB.insertRow(usermenu)
        }
 */

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        //val sdf_Decode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        //var receiveDateTime = sdf_Decode.parse(LocalDateTime.now().toString())
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        var userMenuID =""
        if(mbEdit){
            userMenuID = userMenu.menuNumber.toString()
        }else{
            //userMenuID = mAuth.currentUser!!.uid.toString() + "-MENU-" + timeStamp
            userMenuID = userMenu.menuNumber.toString()
        }

        //Create USER_MENU_ORDER
        userMenu.brandCategory = textViewCrMenuType.getText().toString()
        userMenu.brandName=editTextMenuID.getText().toString().replace("\n","")
        userMenu.createTime=timeStamp
        //var location = LOCATION("FAB1")
        //userMenu.locations.add(location)
        //location.location="FAB2"
        //userMenu.locations.add(location)
        userMenu.menuDescription=editTextMenuDesc.getText().toString()
        userMenu.menuImageURL=""
        //userMenu.menuItems
        userMenu.menuNumber=userMenuID
        //userMenu.menuRecipes
        userMenu.userID=mAuth.currentUser!!.uid
        userMenu.userName=mAuth.currentUser!!.displayName
        userMenu.multiMenuImageURL = mFirebaseUserMenu.multiMenuImageURL

        //------ Upload Image  -------
        uploadImageToFirebase(userMenu.userID!!, userMenu.menuNumber!!, userMenu.multiMenuImageURL!!)

        mDatabase.child("USER_MENU_INFORMATION").child(mAuth.currentUser!!.uid).child(userMenuID).setValue(userMenu)
            .addOnSuccessListener {
                // Write was successful!
                val bundle = Bundle()
                bundle.putString("Result", "OK")
                val intent = Intent().putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
            .addOnFailureListener {
                // Write failed
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
                                    Toast.makeText(mContext, "照片上傳失敗: " + imageURL, Toast.LENGTH_SHORT).show()
                                }
                            }).addOnSuccessListener(object :
                                OnSuccessListener<UploadTask.TaskSnapshot?> {
                                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                                    Toast.makeText(mContext, "照片上傳成功: " + imageURL, Toast.LENGTH_SHORT).show()
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
                            ) { dialogInterface, i ->
                            }
                            notifyAlert.show()

                        }
                    }


                }
                .addOnFailureListener {
                    // Uh-oh, an error occurred!
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
                    Toast.makeText(this@ActivityAddMenu, "Upload Image Faild" + image_name, Toast.LENGTH_SHORT).show()
                }

            }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                    //Toast.makeText(this@ActivityAddMenu, "Upload Image Success" + image_name, Toast.LENGTH_SHORT).show()
                    /*
                    var querypath = "USER_PROFILE/" +  mAuth.currentUser!!.uid.toString()
                    val database = Firebase.database
                    val myRef = database.getReference(querypath)
                    myRef.child("photoURL").setValue(photoURL.toString());
                     */
                }

            })

        }

    }

    private fun getProductListOfMenu(context:Context) {
        val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText
        //var productlist = mProductDB.getProductByMenuID(editTextMenuID.getText().toString())

        val bound = Bundle();
        bound.putString("MENU_ID", editTextMenuID.getText().toString())
        bound.putParcelable("USER_MENU", mFirebaseUserMenu)

        var I =  Intent(context, ActivityProductPriceList::class.java)
        I.putExtras(bound);
        startActivityForResult(I,ACTION_ADD_MENU_PROD_LIST_REQUEST_CODE)
    }

    private fun getLocationListOfMenu(context:Context) {
        val editTextMenuID = findViewById(R.id.editTextMenuID) as EditText

        val array = arrayListOf<String>()
        mFirebaseUserMenu.locations!!.forEach {
            array.add(it)
        }

        val values = arrayOfNulls<String>(array.size)
        array.toArray(values)

        val bound = Bundle();
        bound.putString("TYPE", "LOCATION")
        bound.putStringArray("ItemListData", values)
        bound.putParcelable("USER_MENU", mFirebaseUserMenu)
        var I =  Intent(context, ActivityItemList::class.java)
        I.putExtras(bound);
        startActivityForResult(I,ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE)
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
        // AddImageItem()

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
        MenuImaegByteArray.clear()
        multiMenuImageURL!!.forEach {
            if (it != "") {
                MenuImaegByteArray.put(it, null)
                //----- 每次載入畫面都從FireBase抓取一次 -----
                var menuImaeg = MenuImageDB.getMenuImageByName(it)
                if (menuImaeg != null) {
                    MenuImageDB.delete(menuImaeg)
                }
                val islandRef = Firebase.storage.reference.child(it)
                val ONE_MEGABYTE = 1024 * 1024.toLong()
                islandRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener { bytesPrm: ByteArray ->
                        MenuImaegByteArray[it] = bytesPrm.clone()
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

    private fun saveImageToLocal(bitmap: Bitmap, index: Int):String {

        val filename:String
        //val date = Date(0)
        //val sdf = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        //filename = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        filename = index.toString()
        var final_filename : String = ""
        try
        {
            val path = Environment.getExternalStorageDirectory().toString()
            /*
            val mediaStorageDir = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/Android/data/"
                        + applicationContext.packageName
                        + "/Files"
            )
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    return ""
                }
            }
             */

            var fOut: OutputStream? = null
            var filepath =  "Menu_Image/" + mFirebaseUserMenu.userID + "/" + mFirebaseUserMenu.menuNumber
            final_filename = filename + ".jpg"
            val file = File(mMediaStorageDir,  final_filename)
            fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
            fOut.flush()
            fOut.close()
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName())
            final_filename = filepath + "/" + final_filename
        }
        catch (e:Exception) {
            e.printStackTrace()
            final_filename=""
        }

        return final_filename

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

