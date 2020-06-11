package com.iew.fun2order.order

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.row_orderdetail_others.view.*
import kotlinx.android.synthetic.main.row_ordermaintain.view.*
import kotlinx.android.synthetic.main.row_setup_memberinfobody.view.*
import java.text.SimpleDateFormat


class AdapterRC_OrderMaintain(
    var context: Context,
    var lstItemOrderMaintain: List<ItemsLV_OrderMaintain>,
    val IAdapterOnClick: IAdapterOnClick
) : RecyclerView.Adapter<AdapterRC_OrderMaintain.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_ordermaintain, null)

        return ViewHolder(view).listen()
        { pos, type ->
            IAdapterOnClick.onClick("detail", pos, type)
        }
    }

    override fun getItemCount(): Int {
        return lstItemOrderMaintain.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindModel(lstItemOrderMaintain[position], position)
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(
                imageName, "drawable",
                context.packageName
            )
            return context.resources.getDrawable(id)
        }

        @SuppressLint("SimpleDateFormat")
        fun bindModel(orderMaintain: ItemsLV_OrderMaintain, position: Int) {

            val friendInfo = friendImageDB.getFriendImageByName(orderMaintain.userUUID.toString())


            itemView.orderMaintain_Edit.tag = position

            val queryPath = "USER_PROFILE/" + orderMaintain.userUUID.toString()
            val database = Firebase.database
            val myRef = database.getReference(queryPath)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                    itemView.orderMaintain_UserName.text = value?.userName
                    itemView.orderMaintain_UserView.setImageDrawable(getImageDrawable("image_default_member"))

                    if (friendInfo != null) {
                        val bmp = BitmapFactory.decodeByteArray(friendInfo.image, 0, friendInfo.image.size)
                        itemView.orderMaintain_UserView.setImageBitmap(bmp)
                    } else {

                        val photoURL = value?.photoURL
                        var islandRef = Firebase.storage.reference.child(photoURL!!)
                        val ONE_MEGABYTE = 1024 * 1024.toLong()
                        islandRef.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener { bytesPrm: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                itemView.orderMaintain_UserView.setImageBitmap(bmp)

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
                                itemView.orderMaintain_UserView.setImageDrawable(getImageDrawable("image_default_member"))
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

            var userContentItemData: String = "請點擊查看訂單詳細內容"
            if(orderMaintain.userContentProduct.count()<5) {
                userContentItemData = ""
                orderMaintain.userContentProduct.forEach {
                    val referenceItems = "${it.itemName}*${it.itemQuantity} "
                    userContentItemData += "$referenceItems"
                }
            }

            itemView.orderMaintain_UserContent.text = userContentItemData
            itemView.orderMaintain_Edit.setOnClickListener(View.OnClickListener {
                IAdapterOnClick.onClick("edit", it.tag as Int, 0)
            })


            if (orderMaintain.payCheckFlag) {
                itemView.orderMaintain_PayStatusTitle.text = "付款日"
                itemView.orderMaintain_PayNumberTitle.text = "付款金額"

                val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
                val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

                if (orderMaintain.payTime != "") {
                    val startDateTime = sdfDecode.parse(orderMaintain.payTime)
                    val formatStartDatetime = sdfEncode.format(startDateTime).toString()
                    itemView.orderMaintain_PayStatus.text = formatStartDatetime
                } else {

                    itemView.orderMaintain_PayStatus.text = ""
                }
                itemView.orderMaintain_PayNumber.text = orderMaintain.payNumber.toString()
            } else {

                itemView.orderMaintain_PayStatusTitle.text = "尚未付款"
                itemView.orderMaintain_PayNumberTitle.text = ""
                itemView.orderMaintain_PayStatus.text = ""
                itemView.orderMaintain_PayNumber.text = ""
            }
        }
    }
}







