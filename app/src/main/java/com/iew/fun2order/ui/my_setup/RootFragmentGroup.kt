//------  20200511 Chris 整理修正
package com.iew.fun2order.ui.my_setup

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.dao.groupDAO
import com.iew.fun2order.db.dao.group_detailDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.entity.entityGroup_detail
import com.iew.fun2order.utility.ACTION_ADD_GROUP_REQUEST_CODE
import com.iew.fun2order.utility.ACTION_ADD_MEMBER_REQUEST_CODE
import com.iew.fun2order.utility.ACTION_MODIFY_GROUP_REQUEST_CODE
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class GroupInfo(var Name: String, var Desc: String, var Image: Bitmap, var listMember: MutableList<Any> )

class RootFragmentGroup() : Fragment(),IAdapterOnClick {

    //------  20200511 chris 移動到全域半數去

    private val datetimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

    private var  rcvGroupDetail : RecyclerView? = null
    private var  rcvGroup       : RecyclerView? = null
    private var  txtGroupLabel   : TextView? = null

    private var  lstGroup: MutableList<ItemsLV_Group> = mutableListOf()
    private val  lstGroupDetail: MutableList<Any> = mutableListOf()


    private lateinit var dbContext     : AppDatabase
    private lateinit var memoryContext : MemoryDatabase
    private lateinit var groupDetailDB : group_detailDAO
    private lateinit var groupDB       : groupDAO
    private lateinit var friendDB      : friendDAO

    private lateinit var addGroupICON : Bitmap

    private  var selectedGroupID: String = ""
    private  var selectedGroupName: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addGroupICON = BitmapFactory.decodeResource(this.resources,R.drawable.icon_add_group)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater!!.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbContext = AppDatabase(context!!)
        memoryContext = MemoryDatabase(context!!)

        groupDB       = dbContext.groupdao()
        friendDB      = memoryContext.frienddao()
        groupDetailDB = dbContext.groupdetaildao()

        activity?.let {
            txtGroupLabel   = it.findViewById<TextView>(R.id.groupLabelInfo)   // 20200511 Chris 修改Layout
            rcvGroup       = it.findViewById<RecyclerView>(R.id.RecycleViewGroupList)
            rcvGroupDetail = it.findViewById<RecyclerView>(R.id.RecycleViewMemberList)
        }

        rcvGroup!!.layoutManager =  LinearLayoutManager(context!!,LinearLayoutManager.HORIZONTAL ,false)
        rcvGroup!!.adapter = AdapterRC_Group( context!!, lstGroup , this)

        rcvGroupDetail!!.layoutManager =  LinearLayoutManager(context!!,LinearLayoutManager.VERTICAL ,false)
        rcvGroupDetail!!.adapter = AdapterRC_GroupDetail(context!!, lstGroupDetail, this)

        groupDB.getAllGroup().observe(this, Observer {
            val list = it as java.util.ArrayList<entityGroup>
            lstGroup.clear()
            list.forEach() { it ->
                val groupBMP = BitmapFactory.decodeByteArray(it.image,0,it.image.size)
                lstGroup.add(ItemsLV_Group(it.name, groupBMP, it.groupid))
            }
            lstGroup.add(ItemsLV_Group("新增群組", addGroupICON, ""))

            //---------------------------------------
            if(list.count()!=0)
            {
                if(selectedGroupID == "") {
                    selectedGroupID   = list[0].groupid
                    selectedGroupName = list[0].name
                }

                txtGroupLabel!!.text = "$selectedGroupName : 好友列表"

                val getFriendList =  groupDetailDB.getMemberByGroupID(selectedGroupID)
                lstGroupDetail.clear()
                lstGroupDetail.add(ItemsLV_GroupAddMembersItem("新增好友", "icon_add_group"))
                getFriendList.forEach(){ it->
                    lstGroupDetail.add(ItemsLV_Favourite(it, "icon_cup",""))
                }
            }
            else
            {
                selectedGroupID = ""
                selectedGroupName = ""
                txtGroupLabel!!.text = "好友列表"
                lstGroupDetail.clear()
            }
            recycleViewRefresh()
        })
    }

    private fun recycleViewRefresh() {
        rcvGroup!!.adapter!!.notifyDataSetChanged()
        rcvGroupDetail!!.adapter!!.notifyDataSetChanged()
    }

    override fun onClick(sender: String,pos: Int, type:Int) {

        when(type)
        {
            // Normal Click 正常點選
            0 -> {
                if(sender == "Group") {
                    val click = lstGroup[pos] as ItemsLV_Group
                    if (click.Name != "新增群組") {

                        //------ 切換時馬上先清掉再重新更新資料 -----

                        selectedGroupName = click.Name
                        selectedGroupID = click.GroupID
                        txtGroupLabel!!.text = "$selectedGroupName : 好友列表"

                        lstGroupDetail.clear()
                        lstGroupDetail.add(ItemsLV_GroupAddMembersItem("新增好友", "icon_add_group"))

                        val groupMemberList = groupDetailDB.getMemberByGroupID(selectedGroupID)
                        groupMemberList.forEach() {
                            lstGroupDetail.add(ItemsLV_Favourite(it, "icon_cup",""))
                        }
                        recycleViewRefresh()

                    } else {
                        val intent = Intent(context, ActivityAddGroup::class.java)
                        startActivityForResult(intent, ACTION_ADD_GROUP_REQUEST_CODE)
                    }
                }

                else if (sender == "Group_Detail")   // GroupDetail 會員清單
                {
                    //----  User 點選到新增好友Items -----
                    if (lstGroupDetail[pos] is ItemsLV_GroupAddMembersItem) {
                        if(selectedGroupID != "") {
                            val groupMemberList = groupDetailDB.getMemberByGroupID(selectedGroupID)
                            val friendList = friendDB.getFriendslist()
                            val candidateList = (friendList - groupMemberList)

                            if (candidateList.count() > 0) {
                                val array =  ArrayList(candidateList)
                                val bundle = Bundle()
                                bundle.putStringArrayList("Candidate", array)
                                val intent = Intent(context, ActivityAddMember::class.java)
                                intent.putExtras(bundle)
                                startActivityForResult(intent, ACTION_ADD_MEMBER_REQUEST_CODE)
                            } else {
                                Toast.makeText(activity, "沒有可加入好友的清單", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else
                        {
                            Toast.makeText(activity, "請先選擇Group", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            // Long Click 長按
            1 -> {

                if(sender == "Group") {
                    val click = lstGroup[pos] as ItemsLV_Group
                    if (click.Name != "新增群組") {
                        val buttonActions = arrayOf("編輯群組", "刪除群組","取消")

                        AlertDialog.Builder(context!!)
                            .setItems(buttonActions, DialogInterface.OnClickListener { _, which ->
                                when (which) {
                                    0 -> {   // 編輯群組資訊
                                        val mGroup = groupDB.getGroupByID(click.GroupID)
                                        if (mGroup != null && mGroup.groupid != "") {

                                            val bundle = Bundle()
                                            bundle.putString("GroupID", mGroup.groupid)
                                            bundle.putString("GroupName", mGroup.name)
                                            bundle.putString("GroupDesc", mGroup.desc)
                                            bundle.putByteArray("GroupImage", mGroup.image)

                                            val intent = Intent(context, ActivityAddGroup::class.java)
                                            intent.putExtras(bundle)
                                            startActivityForResult(intent, ACTION_MODIFY_GROUP_REQUEST_CODE
                                            )
                                        }
                                    }
                                    1 -> {  // 刪除群組資訊
                                        deleteGroup(click.GroupID, click.Name)
                                    }
                                    else -> {
                                        Toast.makeText(activity, "選取到取消", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                            .create()
                            .show()
                    }
                }
                else if (sender == "Group_Detail")
                {
                    // 長按點選到會員就執行刪除作業
                    if (lstGroupDetail[pos] is ItemsLV_Favourite)
                    {
                        val memberEntity = lstGroupDetail[pos] as ItemsLV_Favourite
                        val memberName = memberEntity.Name ?: ""
                        deleteGroupFriend(selectedGroupID,memberName)
                    }
                }
            }
        }
    }


    private fun deleteGroupFriend(GroupID: String, FriendID: String)
    {
        if(FriendID!= "") {
            val alert = AlertDialog.Builder(context!!)
            with(alert) {
                setTitle("刪除會員資訊")
                setMessage("確認要刪除此會員資訊嗎？")
                setPositiveButton("確定") { dialog, _ ->
                    try {
                        groupDetailDB.deleteGruopMember(GroupID, FriendID)
                        val getfriend = groupDetailDB.getMemberByGroupID(GroupID)
                        lstGroupDetail.clear()
                        lstGroupDetail.add(ItemsLV_GroupAddMembersItem("新增好友", "icon_add_group"))
                        getfriend.forEach()
                        {
                            lstGroupDetail.add(ItemsLV_Favourite(it, "icon_cup",""))
                        }
                        recycleViewRefresh()
                    } catch (e: Exception) {
                        val errormsg = e.localizedMessage
                        Toast.makeText(activity, errormsg.toString(), Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }
                setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
            }

            val dialog = alert.create()
            dialog.show()
        }
    }

    private fun deleteGroup(GroupID: String, GroupName: String)
    {

        val alert = AlertDialog.Builder(context!!)
        with(alert) {
            setTitle("確認刪除群組")
            setMessage(GroupName)
            setPositiveButton("確定") { dialog, _ ->

                try {
                    groupDetailDB.deleteGruop(GroupID)
                } catch (e: Exception) {
                    val errormsg = e.localizedMessage
                    Toast.makeText(activity, errormsg.toString(), Toast.LENGTH_LONG).show()
                }

                val tmpGroupEntity = groupDB.getGroupByID(GroupID)
                if (tmpGroupEntity != null) {
                    try {
                        groupDB.delete(tmpGroupEntity)
                    } catch (e: Exception) {
                        val errorMsg = e.localizedMessage
                        Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                selectedGroupID = ""
                selectedGroupName = ""
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = alert.create()
        dialog.show()
    }

    //-----------------------------------
    private fun addGroup(GroupName: String, GroupDesc: String, GroupImage: ByteArray)
    {
        try {
            //-----Group Key Use Datetime
            val timestamp: String = LocalDateTime.now().format(datetimeFormatter).toString()
            val group: entityGroup = entityGroup(null, timestamp, GroupName, GroupDesc, GroupImage)
            groupDB.insertRow(group)
            groupDB.getAllGroup().observe(this, Observer {
                val list = it as java.util.ArrayList<entityGroup>
                lstGroup.clear()
                list.forEach() {it->
                    val groupBMP = BitmapFactory.decodeByteArray(it.image, 0, it.image.size)
                    lstGroup.add(ItemsLV_Group(it.name, groupBMP, it.groupid))
                }
                lstGroup.add(ItemsLV_Group("新增群組", addGroupICON, ""))
            })

            //---------------------------------------
            selectedGroupName = group.name
            selectedGroupID   = group.groupid
            txtGroupLabel!!.text = "$selectedGroupName : 好友列表"
            lstGroupDetail.clear()
            lstGroupDetail.add(ItemsLV_GroupAddMembersItem("新增好友", "icon_add_group"))
            recycleViewRefresh()
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage
            Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun modifyGroup(GroupID:String, GroupName: String, GroupDesc: String, GroupImage: ByteArray)
    {
        try {
            val modifyedGroup = groupDB.getGroupByID(GroupID)
            modifyedGroup.name = GroupName
            modifyedGroup.desc = GroupDesc
            modifyedGroup.image = GroupImage
            groupDB.updateTodo(modifyedGroup)
            groupDB.getAllGroup().observe(this, Observer {
                val list = it as java.util.ArrayList<entityGroup>
                lstGroup.clear()
                list.forEach() {it->
                    val groupBMP = BitmapFactory.decodeByteArray(it.image, 0, it.image.size)
                    lstGroup.add(ItemsLV_Group(it.name, groupBMP, it.groupid))
                }
                lstGroup.add(ItemsLV_Group("新增群組", addGroupICON, ""))
            })
            //---------------------------------------
            selectedGroupName = modifyedGroup.name
            selectedGroupID = modifyedGroup.groupid
            txtGroupLabel!!.text = "$selectedGroupName : 好友列表"
            lstGroupDetail.clear()
            lstGroupDetail.add(ItemsLV_GroupAddMembersItem("新增好友", "icon_add_group"))
            recycleViewRefresh()
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage
            Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
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
            groupDetailDB.inserAll(array)
            var getfriend =  groupDetailDB.getMemberByGroupID(selectedGroupID)
            lstGroupDetail.clear()
            lstGroupDetail.add(ItemsLV_GroupAddMembersItem("新增好友", "icon_add_group"))
            getfriend.forEach()
            {
                lstGroupDetail.add(ItemsLV_Favourite(it, "icon_cup",""))
            }
            recycleViewRefresh()
        }
        catch (e: Exception) {
            val errorMsg = e.localizedMessage
            Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
        }
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
                        val addGroupImageByteArray =
                            data.extras?.get("GroupImage") as ByteArray ?: null

                        if (addGroupName != null && addGroupDesc != null && addGroupImageByteArray != null) {
                            addGroup(addGroupName, addGroupDesc, addGroupImageByteArray)
                        } else {
                            Toast.makeText(activity, "新增群組失敗", Toast.LENGTH_LONG).show()
                        }

                    }
                }

                ACTION_MODIFY_GROUP_REQUEST_CODE -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val modifyGroupID = data.extras?.get("GroupID") as String ?: null
                        val modifyGroupName = data.extras?.get("GroupName") as String ?: null
                        val modifyGroupDesc = data.extras?.get("GroupDesc") as String ?: null
                        val modifyGroupImageByteArray =
                            data.extras?.get("GroupImage") as ByteArray ?: null

                        if (modifyGroupID != null && modifyGroupName != null && modifyGroupDesc != null && modifyGroupImageByteArray != null) {
                            modifyGroup(
                                modifyGroupID,
                                modifyGroupName,
                                modifyGroupDesc,
                                modifyGroupImageByteArray
                            )
                        } else {
                            Toast.makeText(activity, "修改群組失敗", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                ACTION_ADD_MEMBER_REQUEST_CODE -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val addMembersList =
                            data.extras?.get("AddMembers") as ArrayList<*> ?: null
                        if (addMembersList != null) {
                            if (addMembersList.count() > 0) {
                                addMembers(selectedGroupID, addMembersList)
                            }
                        } else {
                            Toast.makeText(activity, "新增成員失敗", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else -> {
                    println("no handler onActivityReenter")
                }
            }
        }
    }
}
