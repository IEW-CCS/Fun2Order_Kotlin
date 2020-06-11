package com.iew.fun2order.order

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
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_REJECT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import kotlinx.android.synthetic.main.row_notification.view.*
import kotlinx.android.synthetic.main.row_orderdetail_accept.view.*
import kotlinx.android.synthetic.main.row_orderdetail_others.view.*
import kotlinx.android.synthetic.main.row_setup_memberinfobody.view.*


class AdapterRC_OrderDetailStatus(
    var context: Context,
    var lstOrderDetailStatus: List<Any>,
    val IAdapterOnClick: IAdapterOnClick
) : RecyclerView.Adapter<AdapterRC_OrderDetailStatus.BaseViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        return when (viewType) {
            TYPE_ACCEPT -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.row_orderdetail_accept, null)
                AcceptViewHolder(view).listen()
                { pos, type ->
                    IAdapterOnClick.onClick("ACCEPTProdList", pos, type)
                }

            }
            TYPE_OTHERS -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.row_orderdetail_others, null)
                OthersViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }

    }


    override fun getItemCount(): Int {
        return lstOrderDetailStatus.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = lstOrderDetailStatus[position]
        when (holder) {
            is AcceptViewHolder -> holder.bindModel(element as ItemsLV_OrderDetailStatusAccept)
            is OthersViewHolder -> holder.bindModel(element as ItemsLV_OrderDetailStatusOthers)
            else -> throw IllegalArgumentException()
        }
        // holderHeader?.bindModel( LVItems_Favourite[position] )
    }

    override fun getItemViewType(position: Int): Int {
        return when (lstOrderDetailStatus[position]) {
            is ItemsLV_OrderDetailStatusAccept -> TYPE_ACCEPT
            else -> TYPE_OTHERS
        }
    }

    companion object {
        private const val TYPE_ACCEPT = 0
        private const val TYPE_OTHERS = 1
    }

    inner abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindModel(item: T)
    }

    inner class AcceptViewHolder(itemView: View) :
        BaseViewHolder<ItemsLV_OrderDetailStatusAccept>(itemView) {

        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        override fun bindModel(OrderDetailStatusAccept: ItemsLV_OrderDetailStatusAccept) {

            val friendInfo = friendImageDB.getFriendImageByName(OrderDetailStatusAccept.userUUID.toString())
            val queryPath = "USER_PROFILE/" + OrderDetailStatusAccept.userUUID.toString()
            val database = Firebase.database
            val myRef = database.getReference(queryPath)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                    itemView.orderDetail_Accept_UserName.text = value?.userName
                    itemView.orderDetail_Accept_UserView.setImageDrawable(getImageDrawable("image_default_member"))

                    if (friendInfo != null) {
                        val bmp = BitmapFactory.decodeByteArray(friendInfo.image, 0, friendInfo.image.size)
                        itemView.orderDetail_Accept_UserView.setImageBitmap(bmp)
                    } else {

                        val photoURL = value?.photoURL
                        val islandRef = Firebase.storage.reference.child(photoURL!!)
                        val ONE_MEGABYTE = 1024 * 1024.toLong()
                        islandRef.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener { bytesPrm: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                itemView.orderDetail_Accept_UserView.setImageBitmap(bmp)
                                try {
                                    if(value?.userID!= "") {
                                        val friendImage: entityFriendImage = entityFriendImage(
                                            null,
                                            value?.userID,
                                            value?.userName,
                                            bytesPrm
                                        )
                                        friendImageDB.insertRow(friendImage)
                                    }
                                } catch (ex: Exception) {
                                }
                            }
                            .addOnFailureListener {
                                itemView.orderDetail_Accept_UserView.setImageDrawable(
                                    getImageDrawable("image_default_member")
                                )
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


            // set description
            itemView.orderDetail_Accept_Layout.setBackgroundResource(R.drawable.shape_row_orderstatus_accept)
            itemView.orderDetail_Accept_UserLocation.text = OrderDetailStatusAccept.userLocation ?: ""
            itemView.orderDetail_Accept_UserQuantity.text = OrderDetailStatusAccept.quantity
            var userContentItemData: String = "請點擊查看訂單詳細內容"
            if (OrderDetailStatusAccept.userContentProduct.count() < 5) {
                userContentItemData = ""
                var recipeItems = ""
                OrderDetailStatusAccept.userContentProduct.forEach { it ->
                    recipeItems = ""
                    it.menuRecipes!!.forEach {
                        it.recipeItems!!.forEach {
                            if (it.checkedFlag == true) {
                                recipeItems = recipeItems + it.recipeName + " "
                            }
                        }
                    }
                    var referenceItems = "${it.itemName}: ${recipeItems} * ${it.itemQuantity}"
                    if(it.itemComments != "")
                    {
                        referenceItems += " [ ${it.itemComments} ]"
                    }
                    userContentItemData += "${referenceItems}\n"
                }
            }

            itemView.orderDetail_Accept_UserContent.text = userContentItemData.toString()

            itemView.orderDetail_Accept_UserContent.setOnClickListener(View.OnClickListener {
                IAdapterOnClick.onClick("ACCEPTProdList", adapterPosition, 0)
            })


        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(
                imageName, "drawable",
                context.packageName
            )
            return context.resources.getDrawable(id)
        }
    }

    inner class OthersViewHolder(itemView: View) :
        BaseViewHolder<ItemsLV_OrderDetailStatusOthers>(itemView) {

        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()
        override fun bindModel(OrderDetailStatusOthers: ItemsLV_OrderDetailStatusOthers) {

            val friendInfo = friendImageDB.getFriendImageByName(OrderDetailStatusOthers.userUUID.toString())
            val querypath = "USER_PROFILE/" + OrderDetailStatusOthers.userUUID.toString()
            val database = Firebase.database
            val myRef = database.getReference(querypath)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                    itemView.orderDetail_Others_UserName.text = value?.userName
                    itemView.orderDetail_Others_UserView.setImageDrawable(getImageDrawable("image_default_member"))

                    if (friendInfo != null) {
                        val bmp = BitmapFactory.decodeByteArray(friendInfo.image, 0, friendInfo.image.size)
                        itemView.orderDetail_Others_UserView.setImageBitmap(bmp)
                    } else {

                        val photoURL = value?.photoURL
                        val islandRef = Firebase.storage.reference.child(photoURL!!)
                        val ONE_MEGABYTE = 1024 * 1024.toLong()
                        islandRef.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener { bytesPrm: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                itemView.orderDetail_Others_UserView.setImageBitmap(bmp)

                                try {
                                    if(value?.userID!= "") {
                                        val friendImage: entityFriendImage = entityFriendImage(
                                            null,
                                            value?.userID,
                                            value?.userName,
                                            bytesPrm
                                        )
                                        friendImageDB.insertRow(friendImage)
                                    }
                                } catch (ex: Exception) {
                                }
                            }
                            .addOnFailureListener {
                                itemView.orderDetail_Others_UserView.setImageDrawable(
                                    getImageDrawable("image_default_member")
                                )
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            // set description
            when (OrderDetailStatusOthers.userStatus) {
                MENU_ORDER_REPLY_STATUS_WAIT -> {

                    itemView.orderDetail_Others_UserStatus.text = "等待回覆"
                    itemView.orderDetail_Others_layout.setBackgroundResource(R.drawable.shape_row_orderstatus_wait)
                }
                MENU_ORDER_REPLY_STATUS_REJECT -> {
                    itemView.orderDetail_Others_UserStatus.text = "不參加"
                    itemView.orderDetail_Others_layout.setBackgroundResource(R.drawable.shape_row_orderstatus_reject)
                }

                MENU_ORDER_REPLY_STATUS_EXPIRE -> {
                    itemView.orderDetail_Others_UserStatus.text = "逾期未回覆"
                    itemView.orderDetail_Others_layout.setBackgroundResource(R.drawable.shape_row_orderstatus_expire)
                }
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
}



