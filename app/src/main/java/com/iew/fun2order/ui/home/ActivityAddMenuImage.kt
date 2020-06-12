package com.iew.fun2order.ui.home

/*
程式邏輯
1. 尚未系統將ManuInfo 傳進來先整理 ImageURL 照順序整理到 MenuImaegByteArray 中
2. For Each MenuImaegByteArray 利用 URL ID 尋找Image Byte 的檔案
3. 將ImageByte 轉 Bmp Show Image
4. 案OK 離開之前 將Bmp 轉回 Image Byte 存進去DB之中
5. 更新ＭanuInfo裡面照片資訊
 */


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.ScalableImageViewActivity
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityMeunImage
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.ui.home.adapter.RollPagerViewAdapter
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.jude.rollviewpager.OnItemClickListener
import com.jude.rollviewpager.RollPagerView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class ActivityAddMenuImage : AppCompatActivity() , IAdapterOnClick {
    private val ACTION_CAMERA_REQUEST_CODE = 100
    private val ACTION_ALBUM_REQUEST_CODE = 200

    private val avatarCompressQuality = 70
    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    private var mContext : Context? = null

    var mRollPagerView: RollPagerView? = null
    var mMenuImages: MutableList<Bitmap> = mutableListOf()
    var mRollPagerViewAdapter : RollPagerViewAdapter = RollPagerViewAdapter(this)

    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()
    private lateinit var mTextViewAddMenuImage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_add_menu_image)
        supportActionBar?.hide()
        mContext = this@ActivityAddMenuImage
        mFirebaseUserMenu = intent.extras.get("USER_MENU") as USER_MENU

        mRollPagerView = findViewById(com.iew.fun2order.R.id.home_rollPagerView) as RollPagerView
        //mRollPagerView.setHintView(null);//隐藏指示器
        //mRollPagerView.setHintView(null);//隐藏指示器

        mRollPagerView!!.setOnItemClickListener(OnItemClickListener { position ->

            val baos = ByteArrayOutputStream()
            mRollPagerViewAdapter.menuImages[position].compress(Bitmap.CompressFormat.JPEG, 70, baos)

            val bundle = Bundle()
            bundle.putByteArray("image", baos.toByteArray())
            //bundle.put("image", mRollPagerViewAdapter.menuImages[position])
            val intent = Intent(this, ScalableImageViewActivity::class.java)
            //intent.putExtra("image", mRollPagerViewAdapter.menuImages[position])
            intent.putExtras(bundle)
            startActivity(intent)
        })


        //-----  Load Exist Menu Image -------
        val MemoryDBContext = MemoryDatabase(this!!)
        val MenuImageDB = MemoryDBContext.menuImagedao()
        val totalImageCount = mFirebaseUserMenu!!.multiMenuImageURL!!.filter { it != "" }.count()
        var replyImageCount = 0

        mFirebaseUserMenu!!.multiMenuImageURL!!.forEach {
            imageURL->
            if (imageURL != "") {
                MenuImaegByteArray.put(imageURL, null)
                var menuImaeg = MenuImageDB.getMenuImageByName(imageURL)
                if (menuImaeg != null) {
                    MenuImaegByteArray.put(imageURL, menuImaeg.image)
                    replyImageCount++
                    if (replyImageCount == totalImageCount) {
                        displayImage()
                    }
                }
                else {
                    val islandRef = Firebase.storage.reference.child(imageURL)
                    val ONE_MEGABYTE = 1024 * 1024.toLong()
                    islandRef.getBytes(ONE_MEGABYTE)
                        .addOnSuccessListener { bytesPrm: ByteArray ->
                            MenuImaegByteArray.put(imageURL, bytesPrm.clone())

                            try {
                                MenuImageDB.insertRow(
                                    entityMeunImage(
                                        null,
                                        imageURL,
                                        "",
                                        bytesPrm.clone()!!
                                    )
                                )
                            } catch (e: Exception) {
                            }

                            replyImageCount++
                            if (replyImageCount == totalImageCount) {
                                displayImage()
                            }
                        }
                        .addOnFailureListener {
                            replyImageCount++
                            if (replyImageCount == totalImageCount) {
                                displayImage()
                            }
                        }
                        .addOnCanceledListener {
                            replyImageCount++
                            if (replyImageCount == totalImageCount) {
                                displayImage()
                            }
                        }
                }
            }
            else
            {
                val notifyAlert = AlertDialog.Builder(this).create()
                notifyAlert.setTitle("存取影像錯誤")
                notifyAlert.setMessage("照片路徑：${imageURL} \n資料讀取錯誤!!")
                notifyAlert.setButton(
                    AlertDialog.BUTTON_POSITIVE,
                    "OK"
                ) { dialogInterface, i ->
                }
                notifyAlert.show()
            }
        }

        mRollPagerView!!.setAdapter(mRollPagerViewAdapter)

        val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
        editTextMenuDesc.setText(mFirebaseUserMenu.menuDescription)

        mTextViewAddMenuImage = findViewById(com.iew.fun2order.R.id.textViewAddMenuImage) as TextView
        mTextViewAddMenuImage.setOnClickListener {
            //Toast.makeText(activity, "TESTING BUTTON CLICK 1", Toast.LENGTH_SHORT).show()
            if (!checkPermissionREAD_EXTERNAL_STORAGE(this)) {
               return@setOnClickListener
            }
            takeImageFromAlbumWithCropImageLib()
        }

        val btnAddImageOK = findViewById(R.id.btnAddImageOK) as Button
        // set on-click listener for ImageView
        btnAddImageOK.setOnClickListener {

            mFirebaseUserMenu.multiMenuImageURL!!.clear()
            mMenuImages.forEachIndexed { index, bitmap ->
                val imageURL = generatorFileURL(index)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, avatarCompressQuality, baos)
                val data: ByteArray = baos.toByteArray()
                try {
                    val menuImaeg = MenuImageDB.getMenuImageByName(imageURL)
                    if (menuImaeg == null) {
                        MenuImageDB.insertRow(entityMeunImage(null, imageURL, "", data.clone()!!))
                    } else {
                        menuImaeg.image = data.clone()
                        MenuImageDB.updateTodo(menuImaeg)
                    }
                    mFirebaseUserMenu.multiMenuImageURL!!.add(imageURL)
                } catch (ex: Exception) {
                }
            }

            //----- 所有資料政理完以後返回上一頁 -----
            val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
            mFirebaseUserMenu.menuDescription = editTextMenuDesc.getText().toString()
            val bundle = Bundle()
            bundle.putString("Result", "OK")
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        val btnAddImageCancel = findViewById(R.id.btnAddImageCancel) as Button
        // set on-click listener for ImageView
        btnAddImageCancel.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("Result", "NG")
            bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }


    private fun takeImageFromAlbumWithCropImageLib() {
        CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(this)
    }

    private fun displayImage ()
    {
        val MenuImaegExist = MenuImaegByteArray.filter { it.value != null }
        val MenuImaegFailed = MenuImaegByteArray.filter { it.value == null }
        mMenuImages.clear()
        MenuImaegExist.forEach()
        {
            imageURL->
            val bmp = BitmapFactory.decodeByteArray( imageURL.value, 0,  imageURL.value!!.size)
            mMenuImages.add(bmp)
        }
        mRollPagerViewAdapter.menuImages = mMenuImages
        mRollPagerViewAdapter.notifyDataSetChanged()

        MenuImaegFailed.forEach()
        { imageURL ->
            val notifyAlert = AlertDialog.Builder(this).create()
            notifyAlert.setTitle("存取影像錯誤")
            notifyAlert.setMessage("照片路徑：${imageURL.key} \n資料讀取錯誤!!")
            notifyAlert.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "OK"
            ) { dialogInterface, i ->
            }
            notifyAlert.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")
        val context: Context = this@ActivityAddMenuImage

        when(requestCode) {
            ACTION_CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    insertImageToList(data.extras.get("data") as Bitmap)
                }
            }

            ACTION_ALBUM_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val resolver = context!!.contentResolver
                    val bitmap = MediaStore.Images.Media.getBitmap(resolver, data.data)
                    insertImageToList(bitmap)
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeFile(result.uri.path)
                    insertImageToList(bitmap)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

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

            return Bitmap.createScaledBitmap(
                this,
                new_width,
                new_heithg,
                false
            )
        }
    }


    private fun insertImageToList(bitmap: Bitmap) {
        val resizedBitmap = bitmap.resizeToFireBaseStorage_MenuInfo()
        mMenuImages.add(resizedBitmap)
        mRollPagerViewAdapter.menuImages = mMenuImages
        mRollPagerViewAdapter.notifyDataSetChanged()

        //mRollPagerView.notifySubtreeAccessibilityStateChanged()
        if(mMenuImages.size>=3){
            mTextViewAddMenuImage.setEnabled(false)
            mTextViewAddMenuImage.setTextColor(Color.GRAY)
        }else{
            mTextViewAddMenuImage.setEnabled(true)
            mTextViewAddMenuImage.setTextColor(Color.rgb(79,195,247))
        }
    }



    override fun onClick(sender: String, pos: Int, type: Int) {
        val alert = AlertDialog.Builder(this)
        with(alert) {
            setTitle("確認刪除照片")
            setPositiveButton("確定") { dialog, _ ->
                mMenuImages.removeAt(pos)
                mRollPagerViewAdapter.menuImages = mMenuImages
                mRollPagerViewAdapter.notifyDataSetChanged()

                if(mMenuImages.size>=3){
                    mTextViewAddMenuImage.setEnabled(false)
                    mTextViewAddMenuImage.setTextColor(Color.GRAY)
                }else{
                    mTextViewAddMenuImage.setEnabled(true)
                    mTextViewAddMenuImage.setTextColor(Color.rgb(79,195,247))
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


    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    fun checkPermissionREAD_EXTERNAL_STORAGE(
        context:Context):Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M)
        {
            if ((ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED))
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    showDialog("External storage", context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                else
                {
                    ActivityCompat
                        .requestPermissions(
                            context as Activity,
                            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
                return false
            }
            else
            {
                return true
            }
        }
        else
        {
            return true
        }
    }

    fun showDialog(msg:String, context:Context,
                   permission:String) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage(msg + " permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes,
            object: DialogInterface.OnClickListener {
                override fun onClick(dialog:DialogInterface, which:Int) {
                    ActivityCompat.requestPermissions(context as Activity,
                        arrayOf<String>(permission),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
            })
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun generatorFileURL (index: Int) : String
    {
        val filepath =  "Menu_Image/${mFirebaseUserMenu.userID}/${mFirebaseUserMenu.menuNumber}/${index.toString()}.jpg"
        return filepath
    }


    override fun onRequestPermissionsResult(requestCode:Int,
                                   permissions:Array<String>, grantResults:IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // do your stuff
            }
            else
            {
                Toast.makeText(this, "GET_ACCOUNTS Denied",
                    Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions,
                grantResults)
        }
    }
}