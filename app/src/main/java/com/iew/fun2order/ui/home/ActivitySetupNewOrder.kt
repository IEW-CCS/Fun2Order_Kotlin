package com.iew.fun2order.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.groupDAO
import com.iew.fun2order.db.dao.group_detailDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.ui.my_setup.*
import kotlinx.android.synthetic.main.activity_setup_detail_order.*
import java.util.ArrayList

class ActivitySetupNewOrder : AppCompatActivity(), IAdapterOnClick, IAdapterCheckBOXChanged {

    private var  listGroup: MutableList<ItemsLV_Group> = mutableListOf()
    private val  listCandidate: MutableList<ItemsLV_Canditate> = mutableListOf()
    private  var selectGroupID: String = ""
    private  var selectGroupName: String = ""

    private lateinit var groupDB : groupDAO
    private lateinit var groupdetailDB : group_detailDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_detail_order)

        supportActionBar?.title = "邀請好友"

        val mUserMenu = intent?.extras?.get("USER_MENU") as USER_MENU

        brandName.text = mUserMenu.brandName

        recyclerViewGroupList!!.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL ,false)
        recyclerViewGroupList!!.adapter = AdapterRC_Group( this, listGroup , this)

        recyclerViewGroupMemberList!!.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.VERTICAL ,false)
        recyclerViewGroupMemberList!!.adapter = AdapterRC_Candidate(this, listCandidate, this)


        btnNext.setOnClickListener {

            val checkedList = listCandidate.filter { it -> it.checked }
          //  val tokenIDList = checkedList.map { it -> it.tokenid }

            val bundle = Bundle()
            bundle.putParcelable("USER_MENU", mUserMenu)
            bundle.putParcelableArrayList("INVITE_TOKEN_ID", ArrayList(checkedList))

            val intent = Intent(this, ActivitySetupNewOrderNext::class.java)
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
                }
            }
        }

    }

    override fun onChanged(SelectPosition: Int, checked: Boolean) {

        listCandidate[SelectPosition].checked = checked
        recyclerViewGroupMemberList.adapter?.notifyDataSetChanged()
    }
}