package com.iew.fun2order.ui.my_setup

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
import kotlinx.android.synthetic.main.row_setup_memberinfobody.view.*

class AdapterRC_Candidate(
    var context: Context,
    private var lstItemCandidate: List<ItemsLV_Canditate>,
    val adapterCheckBox: IAdapterCheckBOXChanged
) : RecyclerView.Adapter<AdapterRC_Candidate.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_setup_canditate, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lstItemCandidate.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(lstItemCandidate[position], position)
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()
        fun bindModel(ItemsLV_AddMember: ItemsLV_Canditate, Position: Int) {

            val friendInfo = friendImageDB.getFriendImageByName(ItemsLV_AddMember.Name.toString())
            if(friendInfo != null)
            {
                itemView.SelectFriendName.text = friendInfo.displayname
                ItemsLV_AddMember.tokenid = friendInfo.tokenID
                val bmp = BitmapFactory.decodeByteArray(friendInfo.image, 0, friendInfo.image.size)
                itemView.SelectFriendView.setImageBitmap(bmp)
            }
            else {

                val queryPath = "USER_PROFILE/" + ItemsLV_AddMember.Name.toString()
                val database = Firebase.database
                val myRef = database.getReference(queryPath)
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                        itemView.SelectFriendName.text = value?.userName
                        itemView.SelectFriendView.setImageDrawable(getImageDrawable(ItemsLV_AddMember.imageName))
                        ItemsLV_AddMember.tokenid = value?.tokenID.toString()
                        val photoURL = value?.photoURL
                        if (photoURL != null) {
                            val islandRef = Firebase.storage.reference.child(photoURL)
                            val ONE_MEGABYTE = 1024 * 1024.toLong()
                            islandRef.getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener { bytesPrm: ByteArray ->
                                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                    itemView.SelectFriendView.setImageBitmap(bmp)
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
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            itemView.SelectCheckBox.tag = Position
            itemView.SelectCheckBox.isChecked = ItemsLV_AddMember.checked
            itemView.SelectCheckBox.setOnClickListener() { view ->
                val checker = view as CheckBox
                val selectedPosition = checker.tag as Int
                adapterCheckBox.onChanged(selectedPosition, checker.isChecked)
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
