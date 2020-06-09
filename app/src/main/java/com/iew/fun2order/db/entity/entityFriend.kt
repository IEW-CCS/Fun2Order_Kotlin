package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["friend"], unique = true)))
data class entityFriend (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "friend")
    val friend:String

)