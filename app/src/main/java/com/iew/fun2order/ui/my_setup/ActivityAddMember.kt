package com.iew.fun2order.ui.my_setup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R

class ActivityAddMember : AppCompatActivity() , IAdapterCheckBOXChanged{

    private val  lstCandidate: MutableList<ItemsLV_Canditate> = mutableListOf()

    private lateinit var rcvCandidate: RecyclerView
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)

        rcvCandidate = findViewById(R.id.rcvCandidateMembers)
        btnAdd = findViewById(R.id.addmemberOK)
        btnCancel = findViewById(R.id.addmemberCancel)

        intent?.extras?.let {
            val values = it.getStringArrayList("Candidate")
            values?.forEach()
            {   it ->
                lstCandidate.add(ItemsLV_Canditate(it, "image1","", "","",false))
            }
        }

        rcvCandidate.layoutManager = LinearLayoutManager(this)
        rcvCandidate.adapter = AdapterRC_Candidate( this, lstCandidate, this)

        btnAdd.setOnClickListener{
            val array = arrayListOf<String>()
            lstCandidate.forEach {
                    it ->
                if(it.checked) { array.add(it.Name) }
            }
            val bundle = Bundle()
            bundle.putStringArrayList("AddMembers", array)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        btnCancel.setOnClickListener{
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onChanged(SelectPosition:Int, checked:Boolean) {
        lstCandidate[SelectPosition].checked = checked
        rcvCandidate.adapter!!.notifyDataSetChanged()
    }
}



