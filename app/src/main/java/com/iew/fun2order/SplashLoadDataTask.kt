package com.iew.fun2order

import android.content.Context
import android.os.AsyncTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityFriend
import com.iew.fun2order.db.firebase.USER_PROFILE


class SplashLoadDataTask(private val callback: LoadDataCallback, val context: Context) : AsyncTask<Void?, Void?, Int>()
{
    override fun onPostExecute(status: Int) {
        super.onPostExecute(status)
        if (status == 0) {
            callback.loaded()
        } else if (status == 1) {
            callback.loadError()
        }
    }

    /**
     * 加载数据回调
     */
    interface LoadDataCallback {
        /**
         * 数据加载完毕
         */
        fun loaded()

        /**
         * 数据加载出错
         */
        fun loadError()
    }

    override fun doInBackground(vararg params: Void?): Int {

        if(FirebaseAuth.getInstance().currentUser != null) {
            downloadFriendList(context)
            downloadSelfProfile(context)
        }
        return 0
    }


    private fun downloadFriendList( context: Context) {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendDB: friendDAO = dbContext.frienddao()
        val queryPath = "USER_PROFILE/$uuid/friendList"
        val myRef = Firebase.database.getReference(queryPath)
        friendDB.deleteall()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val friendUUID = snapshot.getValue(String::class.java)
                    try {
                        val friend: entityFriend = entityFriend(null, friendUUID!!)
                        friendDB.insertRow(friend)
                    } catch (e: Exception) {
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun downloadSelfProfile( context: Context) {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        val dbContext = AppDatabase(context)
        val profileDB = dbContext.userprofiledao()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                val entity = profileDB.getProfileByID(uuid)
                if (entity != null) {
                    // Update Profile
                    entity.tokenID = value?.tokenID ?: ""
                    entity.photoURL = value?.photoURL ?: ""
                    entity.userName = value?.userName ?: ""
                    entity.gender = value?.gender ?: ""
                    entity.address = value?.address ?: ""
                    entity.birthday = value?.birthday ?: ""

                    val photoURL = value?.photoURL ?: ""
                    if (photoURL != "") {
                        val islandRef = Firebase.storage.reference.child(photoURL!!)
                        val ONE_MEGABYTE = (1024 * 1024).toLong()
                        islandRef.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener { bytesPrm: ByteArray ->
                                entity.image = bytesPrm
                                profileDB.updateTodo(entity)
                            }
                            .addOnCanceledListener {
                                profileDB.updateTodo(entity)
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}
