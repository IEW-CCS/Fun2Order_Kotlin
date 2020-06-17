package com.iew.fun2order

import android.app.AlertDialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

object ProgressDialogUtil {
    private var mAlertDialog: AlertDialog? = null

    fun showProgressDialog(context: Context?) {
        if (mAlertDialog == null) {
            mAlertDialog = AlertDialog.Builder(context, R.style.CustomProgressDialog).create()
        }
        val loadView: View =
            LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog_view, null)
        mAlertDialog!!.setView(loadView, 0, 0, 0, 0)
        mAlertDialog!!.setCanceledOnTouchOutside(false)
        val tvTip: TextView = loadView.findViewById(R.id.tvTip)
        tvTip.text = "載入中..."
        mAlertDialog!!.show()
    }

    fun showProgressDialog(context: Context?, tip: String?) {
        var tip = tip
        if (TextUtils.isEmpty(tip)) {
            tip = "載入中..."
        }
        if (mAlertDialog == null) {
            mAlertDialog = AlertDialog.Builder(context, R.style.CustomProgressDialog).create()
        }
        val loadView: View = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog_view, null)
        mAlertDialog!!.setView(loadView, 0, 0, 0, 0)
        mAlertDialog!!.setCanceledOnTouchOutside(false)
        val tvTip: TextView = loadView.findViewById(R.id.tvTip)
        tvTip.text = tip
        mAlertDialog!!.show()
    }

    fun dismiss() {
        if (mAlertDialog != null && mAlertDialog!!.isShowing()) {
            mAlertDialog!!.dismiss()
            mAlertDialog = null
        }
    }
}