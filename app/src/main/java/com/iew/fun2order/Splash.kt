package com.iew.fun2order

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.iew.fun2order.SplashLoadDataTask.LoadDataCallback


class Splash : AppCompatActivity(), LoadDataCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)
        // 启动加载应用数据任务类
        val task = SplashLoadDataTask(this, this)
        task.execute()
    }

    /**
     * 跳转界面
     */
    private fun jump() {
        Handler().postDelayed({ // 此处可以根据版本号进行一些判断，再跳转到相应的界面

            val newIntent = Intent()
            newIntent.setClass(this@Splash, Logon::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.extras != null) {
                newIntent.putExtras(intent!!.extras!!)   // 如果有帶入Notification Info 就帶進去Main畫面處理
            } else {
                newIntent.putExtras(Bundle())            //沒有就帶空的值進去
            }
            startActivity(newIntent)
            finish()
        }, 1000) // 停留时间500ms
    }

    /**
     * 数据加载完成
     */
    override fun loaded() {
        jump()
    }

    /**
     * 数据加载出错
     */
    override fun loadError() {
        // 进行出错处理
        // ...
        jump()
    }

    override fun onBackPressed() {
        // Splash界面不允许使用back键
    }
}



