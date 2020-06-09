package com.iew.fun2order.ui.my_setup

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bolts.Task.delay
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.Logon
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityUserProfile
import com.iew.fun2order.db.firebase.USER_PROFILE
import java.util.*

import com.journeyapps.barcodescanner.BarcodeEncoder
import com.tooltip.Tooltip

class RootFragmentProfile() : Fragment(), IAdapterOnClick {

    private lateinit var broadcast: LocalBroadcastManager

    private val  lstProfile: ArrayList<Any> = ArrayList()
    private var  bitmapQRCode : android.graphics.Bitmap? = null

    private var rcvUserProfile : RecyclerView? = null
    private var btnRefresh     : Button? =null
    private var btnLogOut      : Button? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_memberinfo, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            rcvUserProfile = it.findViewById<RecyclerView>(R.id.RecycleVIewUserProfile)
            btnRefresh = it.findViewById<Button>(R.id.Refresh)
            btnLogOut = it.findViewById<Button>(R.id.LogOut)
        }


        broadcast = LocalBroadcastManager.getInstance(context!!)

        lstProfile.clear()
        lstProfile.add("帳號資訊")
        lstProfile.add(ItemsLV_MemberProfile("我的條碼","", "icon_qrcode", true))
        lstProfile.add(ItemsLV_MemberProfile("手機號碼","", "icon_phone", false))

        lstProfile.add("基本資料");
        lstProfile.add(ItemsLV_MemberProfile("姓名", "", "icon_name",true))
        lstProfile.add(ItemsLV_MemberProfile("性別", "", "icon_gender",true))
        lstProfile.add(ItemsLV_MemberProfile("生日", "", "icon_birthday",true))
        lstProfile.add(ItemsLV_MemberProfile("住址", "", "icon_address",true))

        rcvUserProfile!!.layoutManager = LinearLayoutManager(context!!)
        rcvUserProfile!!.adapter = AdapterRC_UserProfile(context!!, lstProfile, this)


        //------ Assign Value -----
        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

            bitmapQRCode = generateQRCode(mAuth.currentUser!!.uid.toString())

            val dbContext = AppDatabase(context!!)
            val profileDB = dbContext.userprofiledao()
            val profile = profileDB.getProfileByID(mAuth.currentUser!!.uid.toString())

            (lstProfile[2] as ItemsLV_MemberProfile).description = profile?.phoneNumber ?: ""
            (lstProfile[4] as ItemsLV_MemberProfile).description = profile?.userName ?: ""
            (lstProfile[5] as ItemsLV_MemberProfile).description = profile?.gender ?: ""
            (lstProfile[6] as ItemsLV_MemberProfile).description = profile?.birthday ?: ""
            (lstProfile[7] as ItemsLV_MemberProfile).description = profile?.address ?: ""

            RecycleViewRefresh()
        }

        btnRefresh?.setOnClickListener{

            val mAuth = FirebaseAuth.getInstance()
            if (mAuth.currentUser != null) {

                val queryPath = "USER_PROFILE/" +  mAuth.currentUser!!.uid.toString()
                val myRef = Firebase.database.getReference(queryPath)

                val displayName =  (lstProfile[4] as ItemsLV_MemberProfile).description ?: ""
                val gender =       (lstProfile[5] as ItemsLV_MemberProfile).description ?: ""
                val birthday =     (lstProfile[6] as ItemsLV_MemberProfile).description ?: ""
                val address =      (lstProfile[7] as ItemsLV_MemberProfile).description ?: ""

                if(displayName != "") {
                    myRef.child("userName").setValue(displayName);
                }

                if(gender != "") {
                    myRef.child("gender").setValue(gender);
                }

                if(birthday != "") {
                    myRef.child("birthday").setValue(birthday);
                }

                if(address != "") {
                    myRef.child("address").setValue(address);
                }

                updateProfile(mAuth.currentUser!!.uid.toString())
                updateTokenID(mAuth.currentUser!!.uid.toString())
                Toast.makeText(activity, "更新資料完成", Toast.LENGTH_LONG).show()
            }
        }

        btnLogOut?.setOnClickListener {
            checkFireBaseSignOut()
        }

    }


    private fun RecycleViewRefresh() {
        rcvUserProfile!!.adapter!!.notifyDataSetChanged()
    }
    private fun generateQRCode(content : String): android.graphics.Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(content, com.google.zxing.BarcodeFormat.QR_CODE, 1024, 1024)
        return bitmap

        /* original method
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, 1024, 1024)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }*/
    }



    private fun memberinfoShowQRCode()
    {
        val image = ImageView(context!!)
        image.setImageBitmap(bitmapQRCode)

        val title = TextView(context!!)
        title.text = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        title.setPadding(10, 10, 10, 10)
        title.gravity = Gravity.CENTER
        title.textSize = 16f

        AlertDialog.Builder(getContext()!!)
            .setCustomTitle(title)
            .setView(image)
            .create()
            .show()
    }


    private fun memberInfoSetName()
    {
        val alert = AlertDialog.Builder(context!!)
        var editTextName: EditText? = null

        with (alert) {
            setTitle("請輸入姓名")
            editTextName = EditText(getContext()!!)
            editTextName!!.hint="變更姓名"
            setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                val name: String = editTextName!!.text.toString()
                if(!name.isNullOrBlank())
                {
                    var changeName = lstProfile[4] as ItemsLV_MemberProfile
                    changeName.description = name
                    lstProfile[4] = changeName

                    //------ Update Display Name to Firebase
                    val mAuth = FirebaseAuth.getInstance()
                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    mAuth.currentUser!!.updateProfile(profileUpdates)

                    RecycleViewRefresh()

                    //----- Notify Display Name Change ---
                    val intent = Intent("profileUpdateMessage")
                    intent.putExtra("displayName", name)
                    broadcast.sendBroadcast(intent)

                }
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = alert.create()
        dialog.setView(editTextName,  50 ,10, 50 , 10)
        dialog.show()
    }


    private fun memberInfoSetGender()
    {
        val imageButtonAction = arrayOf("男性","女性","其他")
        var selectGender : String = imageButtonAction[0]

        AlertDialog.Builder(context!!)
            .setTitle("請選擇性別")
            .setSingleChoiceItems(imageButtonAction, 0, DialogInterface.OnClickListener { _, which -> selectGender = imageButtonAction[which] })
            .setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                val changeName = lstProfile[5] as ItemsLV_MemberProfile
                changeName.description = selectGender
                lstProfile[5] = changeName
                RecycleViewRefresh()

            }
            .setNegativeButton("取消") { dialog, whichButton ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun memberInfoSetBirthday()
    {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context!!, { _, year, month, day ->
            run {
                val selectDate = "${setDateFormat(year, month, day)}"
                val changeName = lstProfile[6] as ItemsLV_MemberProfile
                changeName.description = selectDate
                lstProfile[6] = changeName
                RecycleViewRefresh()

            }
        }, year, month, day).show()
    }

    private fun setDateFormat(year: Int, month: Int, day: Int): String {
        return "${year.toString().padStart(4,'0')}/${(month + 1).toString().padStart(2,'0')}/${day.toString().padStart(2,'0')}"
    }

    private fun memberInfoSetAddress()
    {
        val alert = AlertDialog.Builder(context!!)
        var editTextAddress: EditText? = null

        with (alert) {
            setTitle("請輸入地址")
            editTextAddress = EditText(getContext()!!)
            editTextAddress!!.hint="變更地址"
            setPositiveButton("確定") {
                    dialog, whichButton ->
                dialog.dismiss()
                var address: String = editTextAddress!!.text.toString()
                if(!address.isNullOrBlank())
                {
                    val changeName = lstProfile[7] as ItemsLV_MemberProfile
                    changeName.description = address
                    lstProfile[7] = changeName
                    RecycleViewRefresh()
                }
            }
            setNegativeButton("取消") {
                    dialog, whichButton ->
                dialog.dismiss()
            }
        }

        val dialog = alert.create()
        dialog.setView(editTextAddress,  50 ,10, 50 , 10)
        dialog.show()
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        if (type == 0 && sender == "UserProfile") {

            when (pos) {
                1 -> {
                    memberinfoShowQRCode()
                }
                4 -> {
                    memberInfoSetName()
                }
                5 -> {
                    memberInfoSetGender()
                }
                6 -> {
                    memberInfoSetBirthday()
                }
                7 -> {
                    memberInfoSetAddress()
                }
                else -> { // Note the block

                }
            }

        }
    }

    private fun updateProfile(userID:String)
    {

        //--- GetFireBase and Sync to Local DB
        val queryPath = "USER_PROFILE/$userID"
        val myRef = Firebase.database.getReference(queryPath)

        val dbContext = AppDatabase(context!!)
        val profileDB = dbContext.userprofiledao()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                val entity = profileDB.getProfileByID(userID)
                // Update Profile
                entity.tokenID  =  value?.tokenID ?: ""
                entity.photoURL =  value?.photoURL?: ""
                entity.userName =  value?.userName ?: ""
                entity.gender   =  value?.gender ?: ""
                entity.address  =  value?.address ?: ""
                entity.birthday =  value?.birthday?: ""

                profileDB.updateTodo(entity)
            }

            override fun onCancelled(error: DatabaseError) {
                 Log.w("UpdateProfile", "Failed to read value.", error.toException())
            }
        })
    }


    private fun DeleteProfile(userID:String)
    {

        val queryPath = "USER_PROFILE/$userID"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.removeValue()

    }

    private fun updateTokenID(uuid: String) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val myRef = Firebase.database.getReference("USER_PROFILE/$uuid")
                myRef.child("tokenID").setValue(task.result?.token.toString());
            })
    }


    private fun checkFireBaseSignOut() {
        val alert = AlertDialog.Builder(context!!)
        with(alert) {
            setTitle("確認是否登出使用者")
            setPositiveButton("確定") { dialog, _ ->

                DeleteProfile(FirebaseAuth.getInstance().currentUser!!.uid)

                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName("").build()
                FirebaseAuth.getInstance().currentUser!!.updateProfile(profileUpdates).addOnCompleteListener {
                    FirebaseAuth.getInstance().signOut()
                    //---- Delete FireBase 上面的資料 ----
                    val intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.setClass(context, Logon::class.java)
                    val bundle = Bundle()
                    intent.putExtras(bundle)   // 記得put進去，不然資料不會帶過去哦
                    startActivity(intent)
                    requireActivity().finish()
                    dialog.dismiss()
                }
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }
}
