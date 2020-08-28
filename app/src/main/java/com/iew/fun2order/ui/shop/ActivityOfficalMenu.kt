package com.iew.fun2order.ui.shop

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_PROFILE
import kotlinx.android.synthetic.main.activity_offical_menu.*


class ActivityOfficalMenu : AppCompatActivity() {

    private  var  BrandName : String? = null
    private  var  BrandProFile : DETAIL_BRAND_PROFILE? = null
    private  lateinit var navController :NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offical_menu)
        BrandName = intent.extras?.getString("BRAND_NAME")
        BrandProFile = intent.extras?.getParcelable("BRAND_PROFILE") as DETAIL_BRAND_PROFILE

        static_selectBrandName = BrandName ?: ""
        static_selectBrandProFile = BrandProFile ?: null

        navController = findNavController(R.id.nav_brand_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_brand_story, R.id.navigation_brand_news, R.id.navigation_brand_product, R.id.navigation_brand_location,  R.id.brand_navigation
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_brand_view.setupWithNavController(navController)
        setupUIInfo(BrandProFile)
    }


    private fun setupUIInfo(detailBrandProfile : DETAIL_BRAND_PROFILE?)
    {
        if(detailBrandProfile != null)
        {
            val BarColor =detailBrandProfile!!.brandStyle
            if(BarColor != null)
            {
                if(BarColor.tabBarColor!= null)
                {
                    if(BarColor.tabBarColor!!.count() == 3)
                    {
                        val tabR = BarColor!!.tabBarColor!!.get(0).toInt()
                        val tabG = BarColor!!.tabBarColor!!.get(1).toInt()
                        val tabB = BarColor!!.tabBarColor!!.get(2).toInt()
                        nav_brand_view.setBackgroundColor(Color.rgb(tabR, tabG, tabB))
                    }

                }

                if(BarColor.backgroundColor!= null)
                {
                    if(BarColor.backgroundColor!!.count() == 3)
                    {
                        val bgdR = BarColor!!.backgroundColor!!.get(0).toInt()
                        val bgdG = BarColor!!.backgroundColor!!.get(1).toInt()
                        val bgdB = BarColor!!.backgroundColor!!.get(2).toInt()
                        static_backgroundColor = Color.rgb(bgdR, bgdG, bgdB)
                    }

                }


                if(BarColor.textTintColor!= null)
                {
                    if(BarColor.textTintColor!!.count() == 3)
                    {
                        val tintR = BarColor!!.textTintColor!!.get(0).toInt()
                        val tintG = BarColor!!.textTintColor!!.get(1).toInt()
                        val tintB = BarColor!!.textTintColor!!.get(2).toInt()

                        val textColorStates = ColorStateList(
                            arrayOf(
                                intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)
                            ), intArrayOf(
                                Color.rgb(100, 100, 100), Color.rgb(tintR, tintG, tintB)
                            )
                        )

                        nav_brand_view.itemIconTintList = textColorStates
                        nav_brand_view.itemTextColor = textColorStates

                    }
                }
            }
        }
    }

    companion object {
        private var instance: ActivityOfficalMenu? = null
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        private val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(instance!!.applicationContext) }
        private var static_selectBrandName : String = ""
        private var static_selectBrandProFile: DETAIL_BRAND_PROFILE? = null
        private var static_backgroundColor:Int  = Color.rgb(255, 255, 255)
        public fun getBrandName():String
        {
            return static_selectBrandName
        }

        public fun getBrandProfile():DETAIL_BRAND_PROFILE?
        {
            return static_selectBrandProFile
        }

        public fun getBackGroundColor():Int
        {
            return static_backgroundColor
        }
   }
}