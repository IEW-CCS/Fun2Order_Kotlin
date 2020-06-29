package com.iew.fun2order.ui.notifications

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.tabs.TabLayout
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.order.JoinOrderActivity
import com.iew.fun2order.ui.history.Adapter_HistoryFragMamager
import com.iew.fun2order.ui.history.HistoryViewModel
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.*
import java.text.SimpleDateFormat
import java.util.*


class NotificationsFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adapterNotifyFragMamager: Adapter_NotifyFragMamager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        // adapter
        adapterNotifyFragMamager = Adapter_NotifyFragMamager(childFragmentManager,requireContext())

        // viewPager
        viewPager = root.findViewById(R.id.layout_notify_viewPager)
        viewPager.adapter = adapterNotifyFragMamager

        // tabLayout
        tabLayout = root.findViewById(R.id.layout_notify_tabLayout)

        // link tabLayout with viewPager
        tabLayout.setupWithViewPager(viewPager)
        return root
    }
}