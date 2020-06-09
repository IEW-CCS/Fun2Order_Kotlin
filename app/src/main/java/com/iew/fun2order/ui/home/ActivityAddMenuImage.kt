package com.iew.fun2order.ui.home

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
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iew.fun2order.R
import com.iew.fun2order.ScalableImageViewActivity
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.ui.home.adapter.RollPagerViewAdapter
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.jude.rollviewpager.OnItemClickListener
import com.jude.rollviewpager.RollPagerView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ActivityAddMenuImage : AppCompatActivity() , IAdapterOnClick {
    private val ACTION_CAMERA_REQUEST_CODE = 100
    private val ACTION_ALBUM_REQUEST_CODE = 200

    var listView: ListView? = null

    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    private var mContext : Context? = null
    var mRollPagerView: RollPagerView? = null
    var mMenuImages: MutableList<Bitmap> = mutableListOf()
    var mImgIdx: Int? = null
    var mMediaStorageDir: File? = null
    var mMediaStorageReadDir: File? = null
    var mRollPagerViewAdapter : RollPagerViewAdapter = RollPagerViewAdapter(this)
    private lateinit var mTextViewAddMenuImage: TextView
    var mImageTitles =
        arrayOf("嵐山", "車折神社", "清水寺", "直指庵", "圓光寺", "高台寺", "北野天滿宮", "高雄神護寺")
    var mImageDescription = arrayOf(
        "京都的「嵐山」是無人不知無人不曉的日本代表性賞楓名所。既是國家級史蹟，又是國家指定名勝景點的「嵐山」，其楓紅時期的景致宛如一幅絕美的畫作。非常推薦大家搭乘超有人氣的「嵯峨野遊覽小火車」，從各種角度來賞楓!",
        "擁有大國主神社、弁天神社、藝能神社等3間境內神社的「車折神社」，是相當有名的藝人神社，來此參拜的藝人絡繹不絕。此外，神社周圍寫滿一整面藝人姓名的木柵欄也相當有名。這間「車折神社」不但四季的美景獲得好評，更是許多人會前來造訪的賞楓景點!",
        "提到京都觀光景點絕對少不了「清水寺」的紅葉，能從樹木上方眺望，感受不同於以往的觀賞樂趣。此外，於11月中旬~12月初會舉行「秋季夜間特別參拜」，夜晚打上燈光的紅葉可說是絶景，相當有值得一看的價值!",
        "京都的私房賞楓景點「直指庵」為淨土宗的寺院，不但觀賞期間較長，被寂靜所包圍的景觀也充滿著浪漫的氛圍。「直指庵」內有一尊「愛逢地藏」像，因此也是相當有人氣的祈求良緣景點!",
        "「圓光寺」是臨濟宗南禪寺派的寺院。能在寺院內的池泉回遊式庭園「十牛之庭」與枯山水式庭園「奔龍庭」欣賞秋季的楓紅，對比鮮明的繽紛色彩非常美麗，因此獲得好評。樹葉從11 月中旬開始變色，11月下旬則進入觀賞的最佳時期。",
        "距離「八坂神社」不遠處有一個有名的賞楓地點「高台寺」，每年總會吸引大批遊客前來造訪。此處的池泉回遊式庭園據說是由豐臣秀吉之妻「寧寧」所建造的，每到紅葉變色的季節，美麗的庭園與「高台寺」相互輝映，形成饒富逸趣的景致。",
        "以「春梅名所」而聞名的「北野天滿宮」，近年期間限定開放參觀的「史跡御土居的紅葉苑」成為最受矚目的新賞楓景點。約250棵楓樹在夜晚打上燈光後的景色可以說是觀賞重點!11月下旬〜12月上旬為最佳觀賞期，觀光客絡繹不絕相當熱鬧!",
        "京都郊區的踏青地點「高雄神護寺」一帶，與梅畑槙尾町的「西明寺」、梅畑栂尾町的「高山寺」並稱「三尾」，是自古以來便為人所知的紅葉名所。特別是人氣紅葉名所「高雄神護寺」內的五大堂被紅葉所包圍的景 觀更是必看重點，夜晚點燈後更顯絕美!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_add_menu_image)
        supportActionBar?.hide()

        mContext = this@ActivityAddMenuImage


        //val projects: Array<String> = intent.extras.getStringArray("ItemListData")
        mFirebaseUserMenu = intent.extras.get("USER_MENU") as USER_MENU

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
        //Menu_Image/IuVgttLJ19ULXzojeaGgd3Rwh2D2/IuVgttLJ19ULXzojeaGgd3Rwh2D2-MENU-20200601124418467/
        if (! mMediaStorageDir!!.exists()){
            mMediaStorageDir!!.mkdirs()
        }

        //firebase
        //mStorage = FirebaseStorage.getInstance();
        mRollPagerView = findViewById(com.iew.fun2order.R.id.home_rollPagerView) as RollPagerView
        //mRollPagerView.setHintView(null);//隐藏指示器
        //mRollPagerView.setHintView(null);//隐藏指示器

        mRollPagerView!!.setOnItemClickListener(OnItemClickListener { position ->
            mImgIdx = position
            val baos = ByteArrayOutputStream()
            mRollPagerViewAdapter.menuImages[position].compress(Bitmap.CompressFormat.JPEG, 70, baos)

            val bundle = Bundle()
            bundle.putByteArray("image", baos.toByteArray())
            //bundle.put("image", mRollPagerViewAdapter.menuImages[position])
            val intent = Intent(this, ScalableImageViewActivity::class.java)
            //intent.putExtra("image", mRollPagerViewAdapter.menuImages[position])
            intent.putExtras(bundle)
            startActivity(intent)

            //val bs = ByteArrayOutputStream()
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs)

            //val i = Intent(this, ScalableImageViewActivity::class.java)
            //i.putExtra(EXTRA_BTYEARRAY, bs.toByteArray())
        })

        /*
        mRollPagerView!!.setOn(OnLongClickListener { position ->

            val baos = ByteArrayOutputStream()
            mRollPagerViewAdapter.menuImages[position].compress(Bitmap.CompressFormat.PNG, 50, baos)

            val bundle = Bundle()
            bundle.putByteArray("image", baos.toByteArray())
            val intent = Intent(this, ScalableImageViewActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)

        })

         */
        mMenuImages.clear()
        mFirebaseUserMenu.multiMenuImageURL!!.forEach {
            val file = File(mMediaStorageReadDir.toString() +"/"+ it.toString())
            if (file.exists())
            {
                //val option = BitmapFactory.Options()
                //option.inJustDecodeBounds = true
                //option.inPurgeable = true
                val bm:Bitmap = BitmapFactory.decodeFile(file.absolutePath)

                mMenuImages.add(bm)
                mRollPagerViewAdapter.menuImages = mMenuImages
            }
        }

        mRollPagerView!!.setAdapter(mRollPagerViewAdapter)

        val editTextMenuDesc = findViewById(R.id.editTextMenuDesc) as EditText
        editTextMenuDesc.setText(mFirebaseUserMenu.menuDescription)

        mTextViewAddMenuImage = findViewById(com.iew.fun2order.R.id.textViewAddMenuImage) as TextView
        val ImageButtonAction = arrayOf("相機/相簿","取消")
        mTextViewAddMenuImage.setOnClickListener {
            //Toast.makeText(activity, "TESTING BUTTON CLICK 1", Toast.LENGTH_SHORT).show()
            if (!checkPermissionREAD_EXTERNAL_STORAGE(this)) {
               return@setOnClickListener
            }
            val Alert =  AlertDialog.Builder(mContext!!)
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

        val btnAddImageOK = findViewById(R.id.btnAddImageOK) as Button
        // set on-click listener for ImageView
        btnAddImageOK.setOnClickListener {
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

    override fun onBackPressed() {

/*
        val bundle = Bundle()
        bundle.putString("Result", "OK")
        bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
        val intent = Intent().putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
        finish()
 */
        super.onBackPressed()
    }

    private fun takeImageFromAlbumWithCropImageLib() {

        CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")
        val context: Context = this@ActivityAddMenuImage

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
        var filename:String=""
        val resizedBitmap = bitmap.resizeToFireBaseStorage_MenuInfo()
        //resizedBitmap.compress(Bitmap.CompressFormat.PNG, 50)
        //val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resizedBitmap)
        //val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        //roundedBitmapDrawable.cornerRadius =  (resizedBitmap.width / 2.0).toFloat()
        //mMenuBitmap =resizedBitmap
        mMenuImages.add(resizedBitmap)
        mRollPagerViewAdapter.menuImages = mMenuImages
        mRollPagerView!!.setAdapter(mRollPagerViewAdapter)
        //mRollPagerView.notifySubtreeAccessibilityStateChanged()
        if(mMenuImages.size>=3){
            mTextViewAddMenuImage.setEnabled(false)
            mTextViewAddMenuImage.setTextColor(Color.GRAY)
        }else{
            mTextViewAddMenuImage.setEnabled(true)
            mTextViewAddMenuImage.setTextColor(Color.rgb(79,195,247))
        }

        filename = saveImageToLocal(resizedBitmap)
        if(filename !=""){
            mFirebaseUserMenu.multiMenuImageURL!!.add(filename)
        }

    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        val alert = AlertDialog.Builder(this)
        with(alert) {
            setTitle("確認刪除照片")
            setPositiveButton("確定") { dialog, _ ->
                mRollPagerViewAdapter.menuImages.removeAt(pos)
                mRollPagerView!!.setAdapter(mRollPagerViewAdapter)
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()

    }

    private fun saveImageToLocal(bitmap: Bitmap):String {

        val filename:String
        //val date = Date(0)
        //val sdf = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        filename = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
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
            final_filename = "MenuImage-"+filename + ".jpg"
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