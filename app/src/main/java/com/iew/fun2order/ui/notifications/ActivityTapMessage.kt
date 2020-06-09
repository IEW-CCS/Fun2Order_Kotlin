package com.iew.fun2order.ui.notifications

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.iew.fun2order.R
import com.iew.fun2order.db.entity.entityNotification
import kotlinx.android.synthetic.main.activity_tap_message.*
import java.text.ParseException
import java.text.SimpleDateFormat

class ActivityTapMessage : AppCompatActivity() {

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tap_message)

        supportActionBar?.hide()

        val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

        groupbuy_Title.text = ""
        txtDrafter.text = ""
        txtStarttime.text = ""
        txtEndtime.text = ""
        txtBrand.text = ""
        txtAttendance.text = ""
        txtDescription.text = ""

        intent?.extras?.let {

            val values = it.getParcelable("Notification") as entityNotification
            groupbuy_Title.text = "訊息內容"
            txtDrafter.text = values.orderOwnerName ?: ""
            txtBrand.text = values.brandName ?: ""
            txtAttendance.text = values.attendedMemberCount ?: ""
            txtDescription.text = values.messageDetail ?: ""

            try {
                val receiveDateTime = sdfDecode.parse(values.receiveTime)
                txtStarttime.text = sdfEncode.format(receiveDateTime).toString()
            } catch (ex: ParseException) {
                Log.v("Exception", ex.localizedMessage)
            }

            try {
                val dueDateTime = sdfDecode.parse(values.dueTime)
                txtEndtime.text = sdfEncode.format(dueDateTime).toString()
            } catch (ex: ParseException) {
                Log.v("Exception", ex.localizedMessage)
                txtEndtime.text = "無逾期時間"
            }
        }
    }
}
