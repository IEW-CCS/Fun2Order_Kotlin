package com.iew.fun2order.ui.my_setup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import kotlinx.android.synthetic.main.row_setup_canditate_friend_contact.view.*


class AdapterRC_CandidateFriendByContact(var context: Context, private var lstItemCandidate: List<ItemsLV_ContactAddFriend>) : RecyclerView.Adapter<AdapterRC_CandidateFriendByContact.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_setup_canditate_friend_contact, null)
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
        var requestOptions = RequestOptions()
        fun bindModel(ItemsLV_AddMember: ItemsLV_ContactAddFriend, Position: Int) {
            itemView.SelectFriendName.text = ItemsLV_AddMember.displayName
            itemView.SelectFriendDisplayName.text = ItemsLV_AddMember.ContactName

            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))
            itemView.SelectFriendView.setImageBitmap(null)

            if(ItemsLV_AddMember.imageURL == "") {
                val islandRef = Firebase.storage.reference.child(ItemsLV_AddMember.imagePath)
                islandRef.downloadUrl.addOnSuccessListener { uri ->
                    ItemsLV_AddMember.imageURL = uri.toString()
                    Glide.with(context)
                        .load(uri)
                        .apply(requestOptions)
                        .error(null)
                        .into(itemView.SelectFriendView)
                }
            }
            else
            {
                Glide.with(context)
                    .load(ItemsLV_AddMember.imageURL)
                    .apply(requestOptions)
                    .error(null)
                    .into(itemView.SelectFriendView)
            }

            itemView.SelectCheckBox.tag = Position
            itemView.SelectCheckBox.isChecked = ItemsLV_AddMember.checked
            itemView.SelectCheckBox.setOnClickListener() { view ->
                val checker = view as CheckBox
                ItemsLV_AddMember.checked = checker.isChecked
                notifyItemChanged(adapterPosition)
            }
        }
    }
}
