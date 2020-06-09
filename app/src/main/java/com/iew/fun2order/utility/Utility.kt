package com.iew.fun2order.utility

import android.view.ViewGroup
import android.widget.ListView
import android.widget.ListAdapter
import android.view.View
import android.app.ActionBar.LayoutParams

object Utility {
    fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter: ListAdapter = listView.getAdapter()
            ?: // pre-condition
            return
        var totalHeight: Int = listView.getPaddingTop() + listView.getPaddingBottom()
        for (i in 0 until listAdapter.getCount()) {
            val listItem: View = listAdapter.getView(i, null, listView)
            if (listItem is ViewGroup) {
                listItem.setLayoutParams(
                    LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    )
                )
            }
            listItem.measure(0, 0)
            totalHeight += listItem.getMeasuredHeight()
        }
        val params: ViewGroup.LayoutParams = listView.getLayoutParams()
        params.height = totalHeight + listView.getDividerHeight() * (listAdapter.getCount() - 1)
        listView.setLayoutParams(params)
    }
}