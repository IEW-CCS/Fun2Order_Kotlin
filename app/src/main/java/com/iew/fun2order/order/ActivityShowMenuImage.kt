package com.iew.fun2order.order

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.ScalableImageViewActivity
import com.iew.fun2order.db.database.MemoryDatabase
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
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ActivityShowMenuImage : AppCompatActivity() , IAdapterOnClick {


    private var mFirebaseUserMenu: USER_MENU = USER_MENU()

    private lateinit var txtMenuDesc : TextView
    private var mContext: Context? = null
    var mRollPagerView: RollPagerView? = null
    var mMenuImages: MutableList<Bitmap> = mutableListOf()
    var mImgIdx: Int? = null
    var mRollPagerViewAdapter: RollPagerViewAdapter = RollPagerViewAdapter(this)
    private lateinit var mTextViewAddMenuImage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_show_menu_image)
        supportActionBar?.hide()
        mContext = this@ActivityShowMenuImage
        //val projects: Array<String> = intent.extras.getStringArray("ItemListData")

        mFirebaseUserMenu = intent.extras.get("MENU_INFO") as USER_MENU
        var ImageList = intent.extras.get("MENU_IMAGES") as ArrayList<String>

        //firebase
        //mStorage = FirebaseStorage.getInstance();
        mRollPagerView = findViewById<RollPagerView>(R.id.home_rollPagerView)
        txtMenuDesc = findViewById(R.id.menuDesc)

        txtMenuDesc.text = mFirebaseUserMenu.menuDescription ?: ""
        //mRollPagerView.setHintView(null);//隐藏指示器
        //mRollPagerView.setHintView(null);//隐藏指示器

        mRollPagerView!!.setOnItemClickListener(OnItemClickListener { position ->
            mImgIdx = position
            val baos = ByteArrayOutputStream()
            mRollPagerViewAdapter.menuImages[position].compress(
                Bitmap.CompressFormat.JPEG,
                70,
                baos
            )
            val bundle = Bundle()
            bundle.putByteArray("image", baos.toByteArray())
            val intent = Intent(this, ScalableImageViewActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)

        })

        var MemoryDBContext = MemoryDatabase(this!!)
        var MenuImageDB = MemoryDBContext.menuImagedao()

        mMenuImages.clear()
        ImageList.forEach {
            val MenuImage = MenuImageDB.getMenuImageByName(it)
            if(MenuImage!= null) {
                val bmp = BitmapFactory.decodeByteArray(MenuImage.image, 0, MenuImage.image.size)
                mMenuImages.add(bmp)
            }
        }
        displaymRollPageView()
    }

    private fun displaymRollPageView()
    {
        mRollPagerViewAdapter.menuImages = mMenuImages
        mRollPagerView!!.setAdapter(mRollPagerViewAdapter)
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
    }
}



