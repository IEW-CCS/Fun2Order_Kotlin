package com.iew.fun2order.ui.my_setup

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.contact.PhoneBase
import kotlinx.android.synthetic.main.activity_add_friend_by_contact.*

class ActivityAddFriendByContact : AppCompatActivity() {


    private val  lstAddFriendCandidate: MutableList<ItemsLV_ContactAddFriend> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend_by_contact)

        intent?.extras?.let {
            val values = it.getParcelableArrayList<PhoneBase>("AddFriendCandidate")
            values?.forEach()
            {   it ->
                lstAddFriendCandidate.add(ItemsLV_ContactAddFriend(it.firebaseUUID!!, it.firebaseTokenID!!, it.name!!,it.firebaseImagePath!! ,"",it.firebaseDisplayName!!,false))
            }
        }

        rcvCandidateMembers.layoutManager = LinearLayoutManager(this)
        rcvCandidateMembers.adapter = AdapterRC_CandidateFriendByContact( this, lstAddFriendCandidate)
        addmemberOK.setOnClickListener{

            val selectList  = lstAddFriendCandidate.filter { it -> it.checked }
            val selectArray = ArrayList(selectList.map { it-> it.UUID })
            val bundle = Bundle()
            bundle.putStringArrayList("AddMembers", selectArray)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        addmemberCancel.setOnClickListener{
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

}