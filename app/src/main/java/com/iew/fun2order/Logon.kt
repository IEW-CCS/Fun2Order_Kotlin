package com.iew.fun2order

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_logon_main.*

class Logon  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logon_main)
        supportActionBar?.hide()

        // 傳統寫法
        // val btnGuest = findViewById(R.id.btnGuest) as Button

        btnGuest.visibility= View.GONE
        // 按下按鈕 觸發事件
        btnGuest.setOnClickListener {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setClass(this@Logon, MainActivity::class.java)
            val bundle = Bundle()
            //bundle.putString("height",h.getText().toString());
            //bundle.putString("width",w.getText().toString());
            intent.putExtras(bundle)   // 記得put進去，不然資料不會帶過去哦
            startActivity(intent)
            this.finish()
        }

        // 按下按鈕 觸發事件
        btnVerifyPhone.setOnClickListener{
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setClass(this@Logon, VerifyPhone::class.java)
            startActivity(intent)
            this.finish()
        }


        //----- 專門用來判斷第一次執行 如果第一次執行先SignOut 避免奇怪錯誤----
        val sharedPreferences : SharedPreferences = this.getSharedPreferences("share",MODE_PRIVATE);
        val isFirstRun: Boolean = sharedPreferences.getBoolean("isFirstRun", true);
        val editor= sharedPreferences.edit();
        if(isFirstRun){
            if(FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
            }
            editor.apply();
        }

        //----- 如果已經登入了 就直接跳轉畫面Main Activity ----
        val mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            val newIntent = Intent()
            newIntent.setClass(this@Logon, MainActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.extras != null) {
                newIntent.putExtras(intent!!.extras!!)   // 如果有帶入Notification Info 就帶進去Main畫面處理
            }
            else
            {
                newIntent.putExtras(Bundle())            //沒有就帶空的值進去
            }
            startActivity(newIntent)
            this.finish()
        }
    }
}