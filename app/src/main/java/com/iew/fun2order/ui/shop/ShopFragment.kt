package com.iew.fun2order.ui.shop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.BRAND_CATEGORY_ITEM
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.fragment_shop.view.*


class ShopFragment : Fragment(), IAdapterOnClick {

    private val  numberOfColumns = 5
    private var  mapBrandInfo: MutableMap<String,BRAND_CATEGORY_ITEM?> = mutableMapOf()

    private lateinit var root : View
    private lateinit var mInflater: LayoutInflater
    private lateinit var mapSortedBrandInfo: Map<String,List<BRAND_CATEGORY_ITEM?>>
    private var  lstBrand: MutableList<ItemsLV_Brand> = mutableListOf()
    private var  lstTmpBrand: MutableList<ItemsLV_Brand> = mutableListOf()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mInflater = inflater
        root = inflater.inflate(R.layout.fragment_shop, container, false)
        //root.mSearchView.onActionViewExpanded()
        root.mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                    lstBrand.clear()
                    val lstMatch =
                        mapBrandInfo.values.filter { it -> it!!.brandName!!.contains(query!!) }
                    lstMatch?.forEach {
                        lstBrand.add(ItemsLV_Brand(it!!.brandName, null, it!!.brandIconImage))
                    }
                    root.rcvShopMenuItems!!.adapter!!.notifyDataSetChanged()


                return false
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText == "")
                {
                    lstBrand.clear()
                    lstTmpBrand.forEach{ lstBrand.add(it) }
                    root.rcvShopMenuItems!!.adapter!!.notifyDataSetChanged()

                }
                else
                {
                    lstBrand.clear()
                    val lstMatch = mapBrandInfo.values.filter { it -> it!!.brandName!!.contains(newText!!) }
                    lstMatch?.forEach {
                        lstBrand.add(ItemsLV_Brand(it!!.brandName, null, it!!.brandIconImage))
                    }
                    root.rcvShopMenuItems!!.adapter!!.notifyDataSetChanged()

                }
                return false
            }
        })


        root.rcvShopMenuItems!!.layoutManager =  GridLayoutManager(requireContext(), numberOfColumns)
        root.rcvShopMenuItems!!.adapter = AdapterRC_Brand( requireContext(), lstBrand , this)


        lstBrand.clear()
        mapBrandInfo.clear()
        lstTmpBrand.clear()

        root.segmentedShopMenuType.removeAllViews()
        root.segmentedShopMenuType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            val selectBrandCategory = radioButton.text.toString()
            val selectBrandCategoryList = mapSortedBrandInfo[selectBrandCategory]

            lstBrand.clear()

            selectBrandCategoryList?.forEach {
               lstBrand.add(ItemsLV_Brand(it!!.brandName,null, it!!.brandIconImage))
            }

            lstTmpBrand = lstBrand.toMutableList()
            root.rcvShopMenuItems!!.adapter!!.notifyDataSetChanged()
        })



        val menuPath = "BRAND_CATEGORY"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach()
                {
                    val key = it.key as String
                    val value = it.getValue(BRAND_CATEGORY_ITEM::class.java)
                    mapBrandInfo.put(key,value)
                }

                mapSortedBrandInfo = mapBrandInfo.values.groupBy { it->it!!.brandCategory }
                setupBrandCategory()
            }
        })

        root.suggestion.setOnClickListener {
            var intent = Intent(context, ActivitySuggest::class.java)
            startActivity(intent)
        }


        return root
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        if(type == 0 && sender == "Brand")
        {
            val selectBrandItemName = lstBrand[pos].Name
            val selectBrandItemImageURL = lstBrand[pos].ImageURL

            val bundle = Bundle()
            bundle.putString("BRAND_NAME", selectBrandItemName)
            bundle.putString("BRAND_IMAGE_URL", selectBrandItemImageURL)
            var intent = Intent(context, ActivityDetailMenu::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun setupBrandCategory()
    {

        //addButtonToSegment(mInflater, root.segmentedShopMenuType,"收尋結果")
        mapSortedBrandInfo.forEach {
            addButtonToSegment(mInflater, root.segmentedShopMenuType,it.key)
        }

        if (root.segmentedShopMenuType.childCount > 0) {
            val default = root.segmentedShopMenuType.getChildAt(0) as RadioButton
            default.isChecked = true
        }
    }

    private fun addButtonToSegment(inflater: LayoutInflater, group: SegmentedGroup, btnName: String)
    {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }
}