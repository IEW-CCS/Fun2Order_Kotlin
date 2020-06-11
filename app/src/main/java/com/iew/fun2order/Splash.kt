package com.iew.fun2order

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class Splash : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)
        val secondsDelayed = 1
        Handler().postDelayed(Runnable {
            startActivity(Intent(this@Splash, Logon::class.java))
            finish()
        }, secondsDelayed*1000L)
    }
}