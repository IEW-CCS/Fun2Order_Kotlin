package com.iew.fun2order.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

fun functionTest(abc : String, callback: (Any?)->Unit) {
    val detailBrandEvent = "/DETAIL_BRAND_EVENT/上宇林"
    val database = Firebase.database
    val myRef = database.getReference(detailBrandEvent)
    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null)
        }
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            callback(dataSnapshot)
        }
    })
}