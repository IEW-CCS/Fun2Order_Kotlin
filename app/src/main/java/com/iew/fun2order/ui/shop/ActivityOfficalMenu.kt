package com.iew.fun2order.ui.shop

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.iew.fun2order.MainActivity
import com.iew.fun2order.R
import kotlinx.android.synthetic.main.activity_offical_menu.*
import org.json.JSONObject
import java.util.HashMap


class ActivityOfficalMenu : AppCompatActivity() {

    private  var  selectBrandName : String? = null
    private  var  selectBrandImageURL: String? = null
    private  lateinit var navController :NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offical_menu)


        selectBrandName = intent.extras?.getString("BRAND_NAME")
        selectBrandImageURL = intent.extras?.getString("BRAND_IMAGE_URL")
        static_selectBrandName = selectBrandName ?: ""


        navController = findNavController(R.id.nav_brand_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_brand_story, R.id.navigation_brand_news, R.id.navigation_brand_product, R.id.navigation_brand_location,  R.id.brand_navigation
            )
        )

        /*
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.brand_navigation)
        val nameArg = NavArgument.Builder().setDefaultValue("your name").build()
        val mailArg = NavArgument.Builder().setDefaultValue("your email id").build()
        graph.addArgument("your name key", nameArg);
        graph.addArgument("your mail key", mailArg);
        navController.graph = graph*/

        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_brand_view.setupWithNavController(navController)

    }

    companion object {
        private var instance: ActivityOfficalMenu? = null
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        private val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(instance!!.applicationContext) }
        private var static_selectBrandName = ""
        public fun getBrandName():String
        {
            return static_selectBrandName
        }
}
}