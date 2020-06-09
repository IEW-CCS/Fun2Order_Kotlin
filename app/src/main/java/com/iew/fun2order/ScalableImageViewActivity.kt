package com.iew.fun2order

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class ScalableImageViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scalable_image_view)
        supportActionBar?.hide()
        //val bitmapArray = intent.getByteArrayExtra("image")
        //val bitmapArray:Bitmap = intent.extras.get("image") as Bitmap
        val imageView : MovableImageView = findViewById<MovableImageView>(R.id.ScalableBrandName)
        //val bmp = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)

        val b = intent.getByteArrayExtra("image")
        val bmp = BitmapFactory.decodeByteArray(b, 0, b.size)
        imageView.setImageBitmap(bmp)
        //imageView.setImageBitmap(bitmapArray)

        var gestureDetector = GestureDetector(this, SingleTapConfirm())
        //val imageButton = findViewById<View>(R.id.img) as ImageButton

        imageView.setOnTouchListener(OnTouchListener { arg0, arg1 ->
            if (gestureDetector.onTouchEvent(arg1)) {
                // single tap
                val bundle = Bundle()
                bundle.putString("Result", "OK")
                //bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
                val intent = Intent().putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                finish()
                return@OnTouchListener true
            } else {
                // your code for move and drag
            }
            false
        })


    }

    private class SingleTapConfirm : SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }
    }
}
