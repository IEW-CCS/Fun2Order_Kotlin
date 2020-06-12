package com.iew.fun2order

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class Splash : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)
        val secondsDelayed = 1
        Handler().postDelayed(Runnable {
            val mAuth = FirebaseAuth.getInstance()
            if (mAuth.currentUser != null) {
                val newIntent = Intent()
                newIntent.setClass(this@Splash, Logon::class.java)
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
        }, secondsDelayed*1000L)
    }
}