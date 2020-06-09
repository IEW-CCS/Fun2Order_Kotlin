package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["menu_id","location"], unique = true)))
data class Location (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "menu_id")
    var menu_id:String,
    @ColumnInfo(name = "location")
    var location:String

)