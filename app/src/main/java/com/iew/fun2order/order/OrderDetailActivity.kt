package com.iew.fun2order.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.iew.fun2order.R

import com.iew.fun2order.db.firebase.USER_MENU_ORDER

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adapterOrderDetailFragMamager: Adapter_OrderDetailFragMamager

    private lateinit var menuOrder: USER_MENU_ORDER
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        supportActionBar?.hide()

        intent?.extras?.let {
            menuOrder = it.getParcelable("menuOrder") as USER_MENU_ORDER
        }

        // adapter
        adapterOrderDetailFragMamager =
            Adapter_OrderDetailFragMamager(
                supportFragmentManager,
                menuOrder,
                this
            )

        // viewPager
        viewPager =  findViewById(R.id.layout_orderDetail_viewPager)
        viewPager.adapter = adapterOrderDetailFragMamager
        viewPager.offscreenPageLimit = 3  //這一行一定要寫

        // tabLayout
        tabLayout = findViewById(R.id.layout_orderDetail_tabLayout)

        // link tabLayout with viewPager
        tabLayout.setupWithViewPager(viewPager)

    }
}
