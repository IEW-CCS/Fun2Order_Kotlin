package com.iew.fun2order.brand.location

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.iew.fun2order.R


class PopupAdapter(inflater: LayoutInflater?) : InfoWindowAdapter {
    private var popup: View? = null
    private var inflater: LayoutInflater? = null
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        if (popup == null) {
            popup = inflater?.inflate(R.layout.popup, null)
        }
        val title = popup?.findViewById(R.id.title) as TextView
        title.text = marker.title
        val snippet = popup!!.findViewById(R.id.snippet)as TextView
        snippet.text = marker.snippet
        return popup as View
    }

    init {
        this.inflater = inflater
    }
}