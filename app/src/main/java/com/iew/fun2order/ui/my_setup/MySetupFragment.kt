package com.iew.fun2order.ui.my_setup

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager

import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

import com.iew.fun2order.R
import com.iew.fun2order.utility.*
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityGroup
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.tooltip.Tooltip
import java.io.ByteArrayOutputStream
import java.util.ArrayList

class MySetupFragment : Fragment() {


    private lateinit var tabitem_friend : View
    private lateinit var tabitem_group : View
    private lateinit var tabLayout:TabLayout
    private lateinit var viewPager:ViewPager
    private lateinit var userUUID: TextView
    private lateinit var adapterMySetupFragMamager: Adapter_MySetupFragMamager

    private lateinit var mySetupViewModel: MySetupViewModel

    private lateinit var userImage: ImageButton
    private lateinit var userName: TextView

    private val avatarMaxSize :Int = 1024
    private val avatarSize :Int = 120
    private val avatarCompressQuality = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mySetupViewModel =
            ViewModelProviders.of(this).get(MySetupViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_setup, container, false)

        //------  Get Component ------
        userUUID  = root.findViewById(R.id.mySetup_UserUUID)
        userName  = root.findViewById(R.id.mySetup_UserName)
        userImage = root.findViewById(R.id.mySetup_UserImage)
        userImage.setOnClickListener {
            takeImageFromAlbumWithCropImageLib()
        }

        // adapter
        adapterMySetupFragMamager = Adapter_MySetupFragMamager(childFragmentManager,context!!)

        // viewPager
        viewPager = root.findViewById(R.id.mySetup_viewPager)
        viewPager.adapter = adapterMySetupFragMamager

        // tabLayout
        tabLayout = root.findViewById(R.id.mySetup_tabLayout)

        // link tabLayout with viewPager
        tabLayout.setupWithViewPager(viewPager)


        tabitem_friend = root.findViewById(R.id.view_friend)
        tabitem_group = root.findViewById(R.id.view_group)

        setUpToolTips()

        return root
    }


    fun setUpToolTips()
    {
        val dbContext = AppDatabase(context!!)
        val memoryContext = MemoryDatabase(context!!)
        val groupDB       = dbContext.groupdao()
        val friendDB      = memoryContext.frienddao()
        val friendlist = friendDB.getFriendslist()
        if(friendlist.count() == 0) {

            val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("share", AppCompatActivity.MODE_PRIVATE);
            val editor= sharedPreferences.edit();
            val tooltipAddFriend: Boolean = sharedPreferences.getBoolean("tooltipAddFriend", true);

            if(tooltipAddFriend) {
                val tooltip: Tooltip = Tooltip.Builder(tabitem_friend)
                    .setText("按此加入好友")
                    .setDismissOnClick(true)
                    .setCancelable(true)
                    .setCornerRadius(20f)
                    .setBackgroundColor(resources.getColor(R.color.blue))
                    .setTextColor(resources.getColor(R.color.white))
                    .setOnDismissListener {
                        editor.putBoolean("tooltipAddFriend", false);
                        editor.apply();
                    }
                    .show()
            }
        }

        groupDB.getAllGroup().observe(viewLifecycleOwner, Observer {
            var list = it as ArrayList<entityGroup>
            if(friendlist.count() > 0 && list.count()==0) {

                val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("share", AppCompatActivity.MODE_PRIVATE);
                val editor= sharedPreferences.edit();
                val tooltipAddGroup: Boolean = sharedPreferences.getBoolean("tooltipAddGroup", true);

                if(tooltipAddGroup) {
                    val tooltip2: Tooltip = Tooltip.Builder(tabitem_group)
                        .setText("按此加入群組")
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setCornerRadius(20f)
                        .setBackgroundColor(resources.getColor(R.color.blue))
                        .setTextColor(resources.getColor(R.color.white))
                        .setOnDismissListener {
                            editor.putBoolean("tooltipAddGroup", false);
                            editor.apply();
                        }
                        .show()
                }
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            //------  FireBase Get Information -----
            var mAuth = FirebaseAuth.getInstance()
            if (mAuth.currentUser != null) {
                //----- 直接從DB取資料 -----
                var dbContext = AppDatabase(context!!)
                var profileDB = dbContext.userprofiledao()
                var profile = profileDB.getProfileByID(mAuth.currentUser!!.uid.toString())
                if (profile != null) {
                    userUUID.text = profile.uID ?: ""
                    userName.text = profile.userName ?: ""

                    if(profile.image !=null)
                    {
                        val bmp = BitmapFactory.decodeByteArray(profile.image, 0, profile.image!!.size)
                        displayImage(bmp)
                    }
                    else {
                        val photoURL = profile.photoURL ?: ""
                        if (photoURL != "") {
                            val islandRef = Firebase.storage.reference.child(photoURL!!)
                            val ONE_MEGABYTE = (avatarMaxSize * avatarMaxSize).toLong()
                            islandRef.getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener { bytesPrm: ByteArray ->
                                    val bmp =
                                        BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                    displayImage(bmp)
                                }
                        }
                    }
                }
            }
    }



    private fun displayImage(bitmap: Bitmap) {

        try {
            val resizedBitmap = bitmap.resizeByWidth(userImage.layoutParams.height)
            val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resizedBitmap)
            roundedBitmapDrawable.cornerRadius =  (resizedBitmap.width / 2.0).toFloat()  //弄成圓形
            userImage.setImageDrawable(roundedBitmapDrawable)
        }
        catch (e: Exception)
        {

        }


    }



    private fun uploadImage(bitmap: Bitmap) {
        uploadImage2FireBase(bitmap)
    }


    private fun uploadImage2FireBase(bitmap: Bitmap) {
        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

            val resizedBitmap = bitmap.resizeByWidth(avatarSize)
            val photoURL : String = "UserProfile_Photo/${mAuth.currentUser!!.uid}.png"

            var islandRef = Firebase.storage.reference.child(photoURL!!)

            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, avatarCompressQuality, baos)
            val data: ByteArray = baos.toByteArray()
            val uploadTask: UploadTask = islandRef.putBytes(data)
            uploadTask.addOnFailureListener {
                Log.e("UploadImage2FireBase", "Avatar Upload Failed : " + it.message)
                Toast.makeText(activity, "Upload Image Failed", Toast.LENGTH_SHORT).show()

            }.addOnSuccessListener {
                var querypath = "USER_PROFILE/" +  mAuth.currentUser!!.uid.toString()
                val database = Firebase.database
                val myRef = database.getReference(querypath)
                myRef.child("photoURL").setValue(photoURL.toString());
                Toast.makeText(activity, "更新圖像成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun takeImageFromCameraWithIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, ACTION_CAMERA_REQUEST_CODE)
    }

    private fun takeImageFromAlbumWithIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ACTION_ALBUM_REQUEST_CODE)
    }

    private fun takeImageFromAlbumWithCropImageLib() {
        CropImage.activity().setAspectRatio(1,1).setCropShape(CropImageView.CropShape.RECTANGLE) .start(context!!, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // println("收到 result code $requestCode")

        when(requestCode) {
            ACTION_CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    displayImage(data.extras.get("data") as Bitmap)
                    uploadImage(data.extras.get("data") as Bitmap)
                }
            }

            ACTION_ALBUM_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val resolver = context!!.contentResolver
                    val bitmap = MediaStore.Images.Media.getBitmap(resolver, data.data)
                    displayImage(bitmap)
                    uploadImage(bitmap)
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeFile(result.uri.path)
                    displayImage(bitmap)
                    uploadImage(bitmap)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                     Log.w("CropImage", "Failed to CROP Image.")
                }
            }
            else -> {
                println("no handler onActivityReenter")
            }
        }
    }

    private fun Bitmap.resizeByWidth(width:Int):Bitmap{
        val ratio:Float = this.width.toFloat() / this.height.toFloat()
        val height:Int = Math.round(width / ratio)
        return Bitmap.createScaledBitmap(
            this,
            width,
            height,
            false
        )
    }

    private var messageReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val receiveDisplayName = p1?.getStringExtra("displayName")
            if(receiveDisplayName!= null) {
                userName.text = receiveDisplayName
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(messageReceiver, IntentFilter("profileUpdateMessage"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(messageReceiver)
    }

}