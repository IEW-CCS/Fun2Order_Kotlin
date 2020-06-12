package com.iew.fun2order.order

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
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
import java.io.ByteArrayOutputStream
import kotlin.collections.ArrayList

class ActivityShowMenuImage : AppCompatActivity() , IAdapterOnClick {


    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    private var MenuImaegByteArray : MutableMap<String,ByteArray?> = mutableMapOf<String,ByteArray?>()
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
        val menuImageList = intent.extras.get("MENU_IMAGES") as ArrayList<String>

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

        val MemoryDBContext = MemoryDatabase(this!!)
        val MenuImageDB = MemoryDBContext.menuImagedao()
        val totalImageCount = menuImageList!!.filter { it != "" }.count()
        var replyImageCount = 0

        MenuImaegByteArray.clear()
        menuImageList.forEach {
          imageURL->
            if (imageURL != "") {
                MenuImaegByteArray.put(imageURL, null)
                var menuImaeg = MenuImageDB.getMenuImageByName(imageURL)
                if (menuImaeg != null) {
                    MenuImaegByteArray.put(imageURL, menuImaeg.image)
                    replyImageCount++
                    if (replyImageCount == totalImageCount) {
                        DisplayImage()
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
                                DisplayImage()
                            }
                        }
                        .addOnFailureListener {
                            replyImageCount++
                            if (replyImageCount == totalImageCount) {
                                DisplayImage()
                            }
                        }
                        .addOnCanceledListener {
                            replyImageCount++
                            if (replyImageCount == totalImageCount) {
                                DisplayImage()
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
    }

    private fun DisplayImage()
    {

        mMenuImages.clear()
        MenuImaegByteArray.forEach {
            val MenuImage = it.value
            if(MenuImage!= null) {
                val bmp = BitmapFactory.decodeByteArray(MenuImage, 0, MenuImage.size)
                mMenuImages.add(bmp)
            }
            else{
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

        mRollPagerViewAdapter.menuImages = mMenuImages
        mRollPagerView!!.setAdapter(mRollPagerViewAdapter)

    }
    override fun onClick(sender: String, pos: Int, type: Int) {
    }
}



