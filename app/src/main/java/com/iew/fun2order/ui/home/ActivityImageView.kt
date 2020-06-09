package com.iew.fun2order.ui.home

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iew.fun2order.MovableImageView
import com.iew.fun2order.R

class ActivityImageView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scalable_image_view)
        supportActionBar?.hide()
        val image_url = intent.getByteArrayExtra("image_url")
        //val bitmapArray: Bitmap = intent.extras.get("image") as Bitmap
        //val imageView : MovableImageView = findViewById<MovableImageView>(R.id.MenuImageName)
        //val bmp = BitmapFactory.decodeByteArray(bitmapArray, 100, bitmapArray.size)
        //imageView.setImageBitmap(bmp)

    }
}