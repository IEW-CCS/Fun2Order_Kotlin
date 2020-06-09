package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["menu_id"], unique = true)))
data class UserMenu (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "menu_id")
    var menu_id:String,
    @ColumnInfo(name = "menu_desc")
    var menu_desc:String,
    @ColumnInfo(name = "menu_type")
    var menu_type:String,
    @ColumnInfo(name = "image")
    var image:ByteArray
)