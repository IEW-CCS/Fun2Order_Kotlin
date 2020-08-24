package com.iew.fun2order.ui.my_setup

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.iew.fun2order.R
import com.iew.fun2order.utility.ACTION_ALBUM_REQUEST_CODE
import com.iew.fun2order.utility.ACTION_CAMERA_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_group.*
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class ActivityAddGroup : AppCompatActivity(), IAdapterCheckBOXChanged {


    private val IMAGE_SIZE = 160

    private lateinit var imgBtnGroupImage: ImageButton
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button
    private lateinit var edxGroupName : EditText
    private lateinit var edxGroupDesc: EditText
    private lateinit var bmpGroupBitmap : Bitmap
    private val  lstCandidate: MutableList<ItemsLV_Canditate> = mutableListOf()

    private var gruopid:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        supportActionBar?.hide()

        val buttonActions = arrayOf("相機/相簿","取消")
        imgBtnGroupImage = findViewById(R.id.AddGroupImage)
        btnAdd           = findViewById(R.id.AddGroupOK)
        btnCancel        = findViewById(R.id.AddGroupCancel)
        edxGroupName     = findViewById(R.id.GroupName)
        edxGroupDesc     = findViewById(R.id.GroupDesc)

        if (this.intent.extras != null) {
            intent?.extras?.let {
                gruopid                = it.getString("GroupID") ?: ""
                val groupName: String? = it.getString("GroupName") ?: ""
                val groupDesc: String? = it.getString("GroupDesc")  ?: ""
                val groupImg: ByteArray? = it.getByteArray("GroupImage") ?: null

                val friendList = it.getStringArrayList("FriendList") ?: null
                val groupMemberList = it.getStringArrayList("GroupMemberList") ?: null

                edxGroupName.setText(groupName)
                edxGroupDesc.setText(groupDesc)

                if (groupImg != null) {
                    bmpGroupBitmap = BitmapFactory.decodeByteArray(groupImg, 0, groupImg.size)
                    imgBtnGroupImage.setImageBitmap(bmpGroupBitmap)
                }
                else
                {
                    val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.image_default_group)
                    bmpGroupBitmap = bitmap.resizeByWidth(IMAGE_SIZE)
                    imgBtnGroupImage.setImageBitmap(bmpGroupBitmap)
                }

                lstCandidate.clear()
                friendList?.forEach()
                {   it ->
                    lstCandidate.add(ItemsLV_Canditate(it, "image1","", "","",false))
                }

                groupMemberList?.forEach()
                {
                    it ->
                    val existMember = lstCandidate.filter{member -> member.Name == it}
                    existMember?.forEach()
                    {
                        items ->
                        items.checked = true
                    }
                }

                RecycleViewAddGroupMemberList.layoutManager = LinearLayoutManager(this)
                RecycleViewAddGroupMemberList.adapter = AdapterRC_Candidate( this, lstCandidate, this)
            }
        } else {

            val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.image_default_group)
            bmpGroupBitmap = bitmap.resizeByWidth(IMAGE_SIZE)
            imgBtnGroupImage.setImageBitmap(bmpGroupBitmap)
        }

        btnAdd.setOnClickListener{
            val value = edxGroupName.text.toString().trim()
            if(value.isEmpty())
            {
                Toast.makeText(this, "請輸入群組名稱!!",Toast.LENGTH_SHORT).show()
            }
            else
            {
                if(value == "")
                {
                    Toast.makeText(this, "請輸入群組名稱!!",Toast.LENGTH_SHORT).show()
                }
                else {

                    val baos = ByteArrayOutputStream()
                    bmpGroupBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos)

                    val addFriendArray = arrayListOf<String>()
                    lstCandidate.forEach {
                            it -> if(it.checked) { addFriendArray.add(it.Name) }
                    }


                    val bundle = Bundle()
                    bundle.putString("GroupID", gruopid)
                    bundle.putString("GroupName", value);
                    bundle.putString("GroupDesc", edxGroupDesc.text.toString().trim());
                    bundle.putByteArray("GroupImage", baos.toByteArray())
                    bundle.putStringArrayList("AddFriendMembers", addFriendArray)

                    val intent = Intent().putExtras(bundle)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }

        btnCancel.setOnClickListener{
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        imgBtnGroupImage.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("選取照片來源")
                .setItems(buttonActions,  DialogInterface.OnClickListener { _, which ->
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
    }

    private fun takeImageFromAlbumWithCropImageLib() {
        CropImage.activity().setAspectRatio(1,1).setCropShape(CropImageView.CropShape.RECTANGLE).start(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")

        when(requestCode) {
            ACTION_CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    displayImage(data.extras!!.get("data") as Bitmap)
                }
            }
            ACTION_ALBUM_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val resolver = this.contentResolver
                    val bitmap = MediaStore.Images.Media.getBitmap(resolver, data.data!!)
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
            else -> {
                println("no handler onActivityReenter")
            }
        }
    }

    private fun Bitmap.resizeByWidth(width:Int):Bitmap{
        val ratio:Float = this.width.toFloat() / this.height.toFloat()
        val height:Int = (width / ratio).roundToInt()
        return Bitmap.createScaledBitmap(
            this,
            width,
            height,
            false
        )
    }

    private fun displayImage(bitmap: Bitmap) {
        bmpGroupBitmap = bitmap.resizeByWidth(IMAGE_SIZE)
        imgBtnGroupImage.setImageBitmap(bmpGroupBitmap)
    }

    override fun onChanged(SelectPosition: Int, checked: Boolean) {
        lstCandidate[SelectPosition].checked = checked
        RecycleViewAddGroupMemberList.adapter!!.notifyDataSetChanged()
    }

}
