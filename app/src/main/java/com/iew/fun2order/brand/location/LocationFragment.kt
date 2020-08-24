package com.iew.fun2order.brand.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iew.fun2order.R
import com.iew.fun2order.ui.shop.ActivityOfficalMenu

class LocationFragment : Fragment() {


    private lateinit var viewModel: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.store_location_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val abc = ActivityOfficalMenu.getBrandName()
        var abcc = 123
    }

}