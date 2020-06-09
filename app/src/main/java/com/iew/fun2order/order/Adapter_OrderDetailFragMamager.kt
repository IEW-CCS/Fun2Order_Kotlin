package com.iew.fun2order.order

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU_ORDER

class Adapter_OrderDetailFragMamager(fragmentManager:FragmentManager, var menuorder: USER_MENU_ORDER, var context: Context): FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> RootFragmentOrderStatus(menuorder)
            1 -> RootFragmentOrderStatistics(menuorder)
            else -> RootFragmentOrderMaintain(menuorder)
        }

    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when(position){
            0 -> context.getString(R.string.OrderStatus)
            1 -> context.getString(R.string.OrderStatistics)
            else -> context.getString(R.string.OrderMaintain)

        }
    }

}