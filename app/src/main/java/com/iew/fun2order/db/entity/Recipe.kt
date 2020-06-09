package com.iew.fun2order.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(indices = arrayOf(Index(value = ["menu_id","reicpe_type","recipe"], unique = true)))
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "menu_id")
    var menu_id:String,
    @ColumnInfo(name = "reicpe_type")
    var reicpe_type:String,
    @ColumnInfo(name = "reicpe")
    var reicpe:String
): Parcelable