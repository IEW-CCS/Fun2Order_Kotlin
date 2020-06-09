package com.iew.fun2order.ui.my_setup

import androidx.recyclerview.widget.RecyclerView

fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition, 0)
    }

    itemView.setOnLongClickListener {
        event.invoke(adapterPosition, 1)
        true
    }

    return this
}