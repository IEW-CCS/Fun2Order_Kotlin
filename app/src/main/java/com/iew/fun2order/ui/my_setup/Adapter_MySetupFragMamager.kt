package com.iew.fun2order.ui.my_setup

import android.content.Context
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.iew.fun2order.R

class Adapter_MySetupFragMamager(fragmentManager:FragmentManager, var context: Context): FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return RootFragmentProfile()
            1 -> return RootFragmentFavourite()
            else -> return RootFragmentGroup()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        when(position){
            0 -> return context.getString(R.string.Member)
            1 -> return context.getString(R.string.Favority)
            else -> return context.getString(R.string.Group)
        }
    }
}