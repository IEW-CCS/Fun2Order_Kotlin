package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["uID"], unique = true)))
data class entityUserProfile (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "phoneNumber")
    var phoneNumber:String? = "",
    @ColumnInfo(name = "photoURL")
    var photoURL:String? = "",
    @ColumnInfo(name = "tokenID")
    var tokenID:String? = "",
    @ColumnInfo(name = "uID")
    var uID:String? = "",
    @ColumnInfo(name = "userName")
    var userName:String? = "",
    @ColumnInfo(name = "gender")
    var gender:String? = "",
    @ColumnInfo(name = "address")
    var address:String? = "",
    @ColumnInfo(name = "birthday")
    var birthday:String? = "",
    @ColumnInfo(name = "image")
    var image:ByteArray? = null
)

