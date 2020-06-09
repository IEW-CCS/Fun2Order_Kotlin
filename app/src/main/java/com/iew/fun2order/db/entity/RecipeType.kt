package com.iew.fun2order.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(indices = arrayOf(Index(value = ["menu_id","reicpe_type"], unique = true)))
data class RecipeType (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "menu_id")
    var menu_id:String,
    @ColumnInfo(name = "reicpe_type")
    var reicpe_type:String,
    @ColumnInfo(name = "allow_multi")
    var allow_multi:String
): Parcelable