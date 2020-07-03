package com.iew.fun2order.ui.history

import android.content.Context
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.iew.fun2order.R

class Adapter_HistoryFragMamager(fragmentManager:FragmentManager, var context: Context): FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> RootFragmentOrder_OnGoing()
            else -> RootFragmentOrder_Expired()
        }

    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when(position){
            0 -> context.getString(R.string.OrderOnGoing)
            else -> context.getString(R.string.OrderOnExpired)

        }
    }

}