package com.iew.fun2order.ui.history


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.iew.fun2order.R

class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adapterHistoryFragMamager: Adapter_HistoryFragMamager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)


        // adapter
        adapterHistoryFragMamager = Adapter_HistoryFragMamager(childFragmentManager,context!!)

        // viewPager
        viewPager = root.findViewById(R.id.layout_history_viewPager)
        viewPager.adapter = adapterHistoryFragMamager

        // tabLayout
        tabLayout = root.findViewById(R.id.layout_history_tabLayout)

        // link tabLayout with viewPager
        tabLayout.setupWithViewPager(viewPager)
        return root
    }
}