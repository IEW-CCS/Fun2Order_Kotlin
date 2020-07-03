package com.iew.fun2order.ui.history


import android.annotation.SuppressLint
import android.content.Intent
import android.net.ParseException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.order.JoinOrderActivity
import com.iew.fun2order.order.OrderDetailActivity
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment()  {

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
        adapterHistoryFragMamager = Adapter_HistoryFragMamager(childFragmentManager,requireContext())

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