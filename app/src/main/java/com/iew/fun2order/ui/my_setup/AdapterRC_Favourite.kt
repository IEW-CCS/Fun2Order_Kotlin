package com.iew.fun2order.ui.my_setup

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.row_setup_memberinfobody.view.*
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityFriendImage
import com.iew.fun2order.db.firebase.USER_PROFILE

class AdapterRC_Favourite(var context: Context, var lstItemsFavourite: List<ItemsLV_Favourite>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_Favourite.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_setup_favouritefriend,  null)
        return ViewHolder(view).listen()
        { pos, type ->
            IAdapterOnClick.onClick("Favourite", pos,type)
        }
    }

    override fun getItemCount(): Int {
        return lstItemsFavourite.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstItemsFavourite[position] )
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()
        fun bindModel(ItemsLV_Favourite: ItemsLV_Favourite) {
            val friendInfo = friendImageDB.getFriendImageByName(ItemsLV_Favourite.Name.toString())
            if(friendInfo !=null)
            {
                itemView.UserName.text = friendInfo.displayname
                var bmp = BitmapFactory.decodeByteArray(friendInfo.image, 0, friendInfo.image.size)
                itemView.UserView.setImageBitmap(bmp)
            }
            else {
                val queryPath = "USER_PROFILE/" + ItemsLV_Favourite.Name.toString()
                val database = Firebase.database
                val myRef = database.getReference(queryPath)
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                        itemView.UserName.text = value?.userName
                        itemView.UserView.setImageDrawable(getImageDrawable(ItemsLV_Favourite.imageName))
                        val photoURL = value?.photoURL
                        if (photoURL != null) {
                            val islandRef = Firebase.storage.reference.child(photoURL)
                            val ONE_MEGABYTE = 1024 * 1024.toLong()
                            islandRef.getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener { bytesPrm: ByteArray ->
                                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                    itemView.UserView.setImageBitmap(bmp)
                                    try {
                                        if (value?.userID != "") {
                                            val friendImage: entityFriendImage =
                                                entityFriendImage(
                                                    null,
                                                    value?.userID,
                                                    value?.userName,
                                                    value?.tokenID,
                                                    bytesPrm
                                                )
                                            friendImageDB.insertRow(friendImage)
                                        }
                                    } catch (ex: Exception) {
                                    }
                                }
                                .addOnFailureListener {
                                }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        // Log.w(TAG, "Failed to read value.", error.toException())
                    }
                })
            }
        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                context.packageName)
            return context.resources.getDrawable(id)
        }
    }
}
