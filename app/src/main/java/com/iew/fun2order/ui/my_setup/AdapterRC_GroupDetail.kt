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
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityFriendImage
import com.iew.fun2order.db.firebase.USER_PROFILE
import kotlinx.android.synthetic.main.row_setup_canditate.view.*
import kotlinx.android.synthetic.main.row_setup_favouritefriend.view.*
import kotlinx.android.synthetic.main.row_setup_favouritefriend.view.UserName
import kotlinx.android.synthetic.main.row_setup_favouritefriend.view.UserView
import kotlinx.android.synthetic.main.row_setup_memberinfobody.view.*

class AdapterRC_GroupDetail(var context: Context, var lstItemsGroupDetail: List<Any>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_GroupDetail.BaseViewHolder<*>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>  {

        return when(viewType)
        {
            TYPE_Items -> {
                val view =  LayoutInflater.from(context).inflate(R.layout.row_setup_favouritefriend,  null)
                ItemsViewHolder(view).listen()
                { pos, type ->
                    IAdapterOnClick.onClick("Group_Detail",pos,type)
                }
            }

            TYPE_Header -> {
                val view =  LayoutInflater.from(context).inflate(R.layout.row_setup_favouritefriend,  null)
                HeaderViewHolder(view).listen()
                { pos, type ->
                    IAdapterOnClick.onClick("Group_Detail",pos,type)
                }
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return lstItemsGroupDetail.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = lstItemsGroupDetail[position]
        when (holder) {
            is ItemsViewHolder -> holder.bindModel(element as ItemsLV_Favourite)
            is HeaderViewHolder -> holder.bindModel(element as ItemsLV_GroupAddMembersItem)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (lstItemsGroupDetail[position]) {
            is ItemsLV_Favourite -> TYPE_Items
            else -> TYPE_Header
        }
    }

    companion object {
        private const val TYPE_Items = 0
        private const val TYPE_Header = 1
    }


    inner abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindModel(item: T)
    }

    inner class ItemsViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Favourite>(itemView) {

        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()
        override fun bindModel(ItemsLV_Favourite: ItemsLV_Favourite) {
            val friendInfo = friendImageDB.getFriendImageByName(ItemsLV_Favourite.Name.toString())
            if (friendInfo != null) {
                itemView.UserName.text = friendInfo.displayname
                val bmp = BitmapFactory.decodeByteArray(friendInfo.image, 0, friendInfo.image.size)
                itemView.UserView.setImageBitmap(bmp)
            } else {
                var queryPath = "USER_PROFILE/" + ItemsLV_Favourite.Name.toString()
                val database = Firebase.database
                val myRef = database.getReference(queryPath)
                // Read from the database
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                        itemView.UserName.text = value?.userName
                        itemView.UserView.setImageDrawable(getImageDrawable(ItemsLV_Favourite.imageName))
                        val photoURL = value?.photoURL
                        if (photoURL != null) {
                            var islandRef = Firebase.storage.reference.child(photoURL)
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
            val id = context.resources.getIdentifier(
                imageName, "drawable",
                context.packageName
            )
            return context.resources.getDrawable(id)
        }
    }

    inner class HeaderViewHolder(itemView: View) :
        BaseViewHolder<ItemsLV_GroupAddMembersItem>(itemView) {

        override fun bindModel(itemsLV_GroupAddMembersItem: ItemsLV_GroupAddMembersItem) {
            itemView.UserName.text = itemsLV_GroupAddMembersItem.Name;
            itemView.UserView.setImageDrawable(getImageDrawable(itemsLV_GroupAddMembersItem.imageName))
        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(
                imageName, "drawable",
                context.packageName
            )
            return context.resources.getDrawable(id)
        }
    }
}



