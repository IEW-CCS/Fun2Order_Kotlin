package com.iew.fun2order.ui.shop

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.SUGGEST_MENU_INFO
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_suggest.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ActivitySuggest : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    var suggestImage : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)
        supportActionBar?.title = "建議品牌"

        addSuggestBrandImage.setOnClickListener {
            takeImageFromAlbumWithCropImageLib()
        }


        submitSuggestBrand.setOnClickListener {

            if(suggestImage == null)
            {
                AlertDialog.Builder(this)
                    .setTitle("錯誤訊息")
                    .setMessage("請先上傳推薦品牌菜單影像")
                    .setPositiveButton("確定",null)
                    .create()
                    .show()


            }
            else if (editSuggestBrandName.text.toString() == "")
            {
                AlertDialog.Builder(this)
                    .setTitle("錯誤訊息")
                    .setMessage("請先輸入推薦品牌名稱")
                    .setPositiveButton("確定",null)
                    .create()
                    .show()
            }
            else
            {
                val resizedBitmap = suggestImage!!.resizeToFireBaseStorage_MenuInfo()
                val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
                var mAuth = FirebaseAuth.getInstance()
                if (mAuth.currentUser != null) {

                    ProgressDialogUtil.showProgressDialog(this);
                    val photoURL : String = "SUGGESTION_BRAND_IMAGE/${timeStamp}/${editSuggestBrandName.text.toString()}.jpg"
                    var islandRef = Firebase.storage.reference.child(photoURL!!)
                    val baos = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                    val data: ByteArray = baos.toByteArray()
                    val uploadTask: UploadTask = islandRef.putBytes(data)
                    uploadTask.addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(p0: Exception) {

                            ProgressDialogUtil.dismiss()
                            AlertDialog.Builder(this@ActivitySuggest)
                                .setTitle("錯誤訊息")
                                .setMessage("建議品牌影像上傳失敗")
                                .setPositiveButton("確定",null)
                                .create()
                                .show()
                        }

                    }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                        override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                            ProgressDialogUtil.dismiss()
                            var suggest = SUGGEST_MENU_INFO(photoURL,editSuggestBrandName.text.toString(),timeStamp,mAuth.currentUser!!.uid)
                            val suggestPath = "SUGGESTION_MENU_INFORMATION/$timeStamp"
                            val myRef = Firebase.database.getReference(suggestPath)
                            val brandName = editSuggestBrandName.text.toString()
                            val illegalch = charArrayOf('.', '#', '$', '[', ']')
                            for (i in illegalch) {
                                brandName.replace(i.toString(),"",false)
                            }
                            myRef.child(brandName).setValue(suggest);

                            AlertDialog.Builder(this@ActivitySuggest)
                                .setTitle("訊息")
                                .setMessage("建議品牌影像上傳成功")
                                .setPositiveButton("確定") { dialog, _ ->
                                    finish()
                                }
                                .create()
                                .show()
                        }
                    })
                }




            }

        }
    }

    private fun takeImageFromAlbumWithCropImageLib() {
        CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")
        val context: Context = this
        when(requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeFile(result.uri.path)
                    suggestImage = bitmap.copy(bitmap.config, bitmap.isMutable)
                    showBrandImage.setImageBitmap(bitmap)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                }
            }

            else -> {
                println("no handler onActivityReenter")
            }
        }
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
}