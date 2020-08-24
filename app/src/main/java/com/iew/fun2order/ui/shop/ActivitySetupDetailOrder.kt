package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.groupDAO
import com.iew.fun2order.db.dao.group_detailDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.entity.entityGroup_detail
import com.iew.fun2order.ui.my_setup.*
import com.iew.fun2order.utility.ACTION_ADD_GROUP_REQUEST_CODE
import com.iew.fun2order.utility.ACTION_ADD_MEMBER_REQUEST_CODE
import com.iew.fun2order.utility.ACTION_MODIFY_GROUP_REQUEST_CODE
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import kotlinx.android.synthetic.main.activity_setup_detail_order.*
import java.util.*

class ActivitySetupDetailOrder : AppCompatActivity(), IAdapterOnClick, IAdapterCheckBOXChanged {

    private var  listGroup: MutableList<ItemsLV_Group> = mutableListOf()
    private val  listCandidate: MutableList<ItemsLV_Canditate> = mutableListOf()
    private  var selectGroupID: String = ""
    private  var selectGroupName: String = ""
    private lateinit var addGroupICON : Bitmap

    private lateinit var groupDB : groupDAO
    private lateinit var groupdetailDB : group_detailDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_detail_order)
        addGroupICON = BitmapFactory.decodeResource(this.resources,R.drawable.icon_add_group)

        supportActionBar?.title = "邀請好友"

        val selectBrandName = intent.extras?.getString("BRAND_NAME")
        val selectBrandMenuNumber = intent.extras?.getString("BRAND_MENU_NUMBER")

        brandName.text = selectBrandName

        recyclerViewGroupList!!.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL ,false)
        recyclerViewGroupList!!.adapter = AdapterRC_Group( this, listGroup , this)

        recyclerViewGroupMemberList!!.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.VERTICAL ,false)
        recyclerViewGroupMemberList!!.adapter = AdapterRC_Candidate(this, listCandidate, this)


        btnNext.setOnClickListener {

            val checkedList = listCandidate.filter { it -> it.checked }
          //  val tokenIDList = checkedList.map { it -> it.tokenid }

            val bundle = Bundle()
            bundle.putString("BRAND_NAME", selectBrandName)
            bundle.putString("BRAND_MENU_NUMBER", selectBrandMenuNumber)
            bundle.putParcelableArrayList("INVITE_TOKEN_ID", ArrayList(checkedList))

            val intent = Intent(this, ActivitySetupDetailOrderNext::class.java)
            intent.putExtras(bundle)
            startActivity(intent)

        }
        val DBContext = AppDatabase(this)
        groupDB = DBContext.groupdao()
        groupdetailDB = DBContext.groupdetaildao()

        groupDB.getAllGroup().observe(this, Observer {
            val list = it as ArrayList<entityGroup>
            listGroup.clear()
            list.forEach() {
                val groupBMP = BitmapFactory.decodeByteArray(it.image,0,it.image.size)
                listGroup.add(ItemsLV_Group(it.name, groupBMP, it.groupid))
            }
            listGroup.add(ItemsLV_Group("新增群組", addGroupICON, ""))


            if(list.count()!=0)
            {
                if(selectGroupID == "") {
                    selectGroupID = list[0].groupid
                    selectGroupName = list[0].name
                }

                val tmpGroupInfotext = "$selectGroupName :好友列表"
                textViewMemberGroupName.text = tmpGroupInfotext

                val getFriendList =  groupdetailDB.getMemberByGroupID(selectGroupID)
                listCandidate.clear()

                getFriendList.forEach()
                {
                    listCandidate.add(ItemsLV_Canditate(it, "image_default_member","","", "",true))
                }
            }
            else
            {
                selectGroupID = ""
                selectGroupName = ""
                textViewMemberGroupName.text = "好友列表"
                listCandidate.clear()
            }
            recycleViewRefresh()
        })

    }

    private fun recycleViewRefresh() {
        recyclerViewGroupList!!.adapter?.notifyDataSetChanged()
        recyclerViewGroupMemberList!!.adapter?.notifyDataSetChanged()
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        when(type)
        {
            // Normal Click
            0 -> {
                if(sender == "Group") {
                    val click = listGroup[pos] as ItemsLV_Group
                    if (click.Name != "新增群組") {
                        selectGroupName = click.Name
                        selectGroupID = click.GroupID
                        textViewMemberGroupName.text = selectGroupName + ":好友列表"
                        listCandidate.clear()

                        val groupMemberList = groupdetailDB.getMemberByGroupID(selectGroupID)
                        groupMemberList.forEach() {
                            listCandidate.add(ItemsLV_Canditate(it, "image_default_member","","","",true))
                        }
                        recycleViewRefresh()
                    }
                    else
                    {
                        val memoryContext = MemoryDatabase(this)
                        val friendDB      = memoryContext.frienddao()
                        //----- 新建立的群組使用全部的好友資訊進去 ----
                        val friendList = friendDB.getFriendslist()
                        val friendArray =  ArrayList(friendList)
                        val bundle = Bundle()
                        bundle.putStringArrayList("FriendList", friendArray)
                        val intent = Intent(this, ActivityAddGroup::class.java)
                        intent.putExtras(bundle)
                        startActivityForResult(intent, ACTION_ADD_GROUP_REQUEST_CODE)
                    }
                }
            }
        }

    }

    override fun onChanged(SelectPosition: Int, checked: Boolean) {

        listCandidate[SelectPosition].checked = checked
        recyclerViewGroupMemberList.adapter?.notifyDataSetChanged()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")
        data?.extras?.let {
            when (requestCode) {
                ACTION_ADD_GROUP_REQUEST_CODE -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val addGroupName = data.extras?.get("GroupName") as String ?: null
                        val addGroupDesc = data.extras?.get("GroupDesc") as String ?: null
                        val addGroupImageByteArray = data.extras?.get("GroupImage") as ByteArray ?: null
                        val addMembersList = data.extras?.get("AddFriendMembers") as ArrayList<String> ?: arrayListOf<String>()

                        if (addGroupName != null && addGroupDesc != null && addGroupImageByteArray != null) {
                            addGroupWithMember(addGroupName, addGroupDesc, addGroupImageByteArray, addMembersList)
                        } else {
                            Toast.makeText(this, "新增群組失敗", Toast.LENGTH_LONG).show()
                        }

                    }
                }
            }
        }
    }

    private fun addGroupWithMember(GroupName: String, GroupDesc: String, GroupImage: ByteArray, MemberList : ArrayList<String>)
    {
        try {
            //-----Group Key Use Datetime
            val timestamp: String =  DATATIMEFORMAT_NORMAL.format(Date())
            val group: entityGroup = entityGroup(null, timestamp, GroupName, GroupDesc, GroupImage)

            groupDB.insertRow(group)
            addMembers( group.groupid, MemberList)

            //---重新整理資訊 ------
            groupDB.getAllGroup().observe(this, Observer {
                val list = it as java.util.ArrayList<entityGroup>
                listGroup.clear()
                list.forEach() {it->
                    val groupBMP = BitmapFactory.decodeByteArray(it.image, 0, it.image.size)
                    listGroup.add(ItemsLV_Group(it.name, groupBMP, it.groupid))
                }
                listGroup.add(ItemsLV_Group("新增群組", addGroupICON, ""))
                recycleViewRefresh()
                recyclerViewGroupList!!.scrollToPosition(listGroup.count()-1)
            })

            selectGroupName = group.name
            selectGroupID   = group.groupid
            textViewMemberGroupName!!.text = "$selectGroupName : 好友列表"

        } catch (e: Exception) {
            val errorMsg = e.localizedMessage
            Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show()
        }
    }


    private fun addMembers(GroupID: String, MemberList: ArrayList<*>)
    {
        val array = arrayListOf<entityGroup_detail>()
        MemberList.forEach()
        {
            array.add(entityGroup_detail(null,GroupID, it.toString()))
        }

        try {
            groupdetailDB.inserAll(array)
            listCandidate.clear()
            array.forEach()
            {
                listCandidate.add(ItemsLV_Canditate(it.friend, "image_default_member","","","",true))
            }
            recycleViewRefresh()
        }
        catch (e: Exception) {
            val errorMsg = e.localizedMessage
            Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show()
        }
    }

}