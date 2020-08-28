package com.iew.fun2order.brand.product


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.brand.news.ActivityNewsDetail
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFORMATION
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.shop.ActivityOfficalMenu

import info.hoang8f.android.segmented.SegmentedGroup

import kotlinx.android.synthetic.main.product_fragment.*
import kotlinx.android.synthetic.main.product_fragment.segmentedItemCategory
import kotlinx.android.synthetic.main.row_detail_productitems.view.*

class ProductFragment : Fragment(), IAdapterOnClick {


    private lateinit var viewModel: ProductViewModel

    private  lateinit var detailMenuInfo : DETAIL_MENU_INFORMATION
    private  var productItemInfo : MutableList<ItemsLV_Products_withExtra> = mutableListOf()
    private  var productPriceSequence : MutableList<String> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.product_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view?.setBackgroundColor(ActivityOfficalMenu.getBackGroundColor())

        if(ActivityOfficalMenu.getBrandProfile()!= null) {
            eventMenuBannerImageView.visibility = View.VISIBLE
            val menuBannerURL = ActivityOfficalMenu.getBrandProfile()!!.brandMenuBannerURL
            val brandMenuNumber = ActivityOfficalMenu.getBrandProfile()!!.menuNumber
            Glide.with(requireContext())
                .load(menuBannerURL)
                .error(null)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        eventMenuBannerImageView.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(eventMenuBannerImageView)

            downloadBrandDetailMenuInfoFromFireBase(brandMenuNumber)
        }
        else
        {
            val alert = AlertDialog.Builder(requireContext())
            with(alert) {
                setTitle("系統錯誤")
                setMessage("品牌資料不存在")
                setPositiveButton("確定") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            val dialog = alert.create()
            dialog.show()
        }


        segmentedItemCategory.removeAllViews()
        productItemInfo.clear()
        productPriceSequence.clear()

        segmentedItemCategory.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            val selectItemCategory = radioButton.text.toString()

            if (detailMenuInfo != null && selectItemCategory != "") {
                //----- Clear -------
                productItemInfo.clear()
                productPriceSequence.clear()

                //----- Setup Title Bar ---
                menuBrandItemsTitle.removeAllViews()
                val title =
                    LayoutInflater.from(context).inflate(R.layout.row_detail_productitems, null)
                title.itemName.setTextColor(Color.BLUE)
                title.itemName.text = "品名"
                title.itemName.textSize = 16F

                val lp2: TableRow.LayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT
                );
                lp2.setMargins(20, 0, 0, 0);

                val tbrow = TableRow(context)
                val selectProductCategory =
                    detailMenuInfo.productCategory?.firstOrNull { it -> it.categoryName == selectItemCategory }
                if (selectProductCategory != null) {
                    selectProductCategory.priceTemplate?.recipeList?.sortedBy { it.itemSequence }
                        ?.forEach {
                            val t1v = TextView(context)
                            t1v.text = it.itemName
                            t1v.setTextColor(Color.BLACK)
                            t1v.textSize = 16F
                            t1v.width = 20
                            t1v.gravity = Gravity.CENTER
                            t1v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                            tbrow.addView(t1v, lp2)
                            productPriceSequence.add(it.itemName)
                        }

                    title.itemAttribute.addView(tbrow)
                    menuBrandItemsTitle.addView(title)

                    //---------- Setup Body ------
                    selectProductCategory.productItems?.forEach {
                        val productDesc = it.productDescription ?: ""
                        productItemInfo.add(
                            ItemsLV_Products_withExtra(
                                it.productName,
                                it.priceList,
                                selectProductCategory?.priceTemplate.standAloneProduct,
                                productDesc,
                                it.productWebURL
                            )
                        )
                    }
                    rcv_menuBrandItems.adapter?.notifyDataSetChanged()
                }
            }
        })

        rcv_menuBrandItems.layoutManager = LinearLayoutManager(requireContext())
        rcv_menuBrandItems.adapter = AdapterRC_Items_with_ExtraInfo(requireContext(), productItemInfo, productPriceSequence, this)
        rcv_menuBrandItems.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

    }

    private fun downloadBrandDetailMenuInfoFromFireBase(DetailMenuNumber: String?) {

        if (DetailMenuNumber != null) {
            val detailMenuURL = "/DETAIL_MENU_INFORMATION/$DetailMenuNumber"
            val database = Firebase.database
            val myRef = database.getReference(detailMenuURL)

            ProgressDialogUtil.showProgressDialog(requireContext(),"處理中");
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    ProgressDialogUtil.dismiss()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    ProgressDialogUtil.dismiss()
                    val objectClass = dataSnapshot.getValue(Any::class.java)
                    if (objectClass == null) {
                        AlertDialog.Builder(context!!)
                            .setTitle("通知訊息")
                            .setMessage("$DetailMenuNumber 資訊不存在!!")
                            .setPositiveButton("確定", null)
                            .create()
                            .show()
                    } else {

                        val json = Gson().toJson(objectClass)
                        try {
                            detailMenuInfo = Gson().fromJson(json, DETAIL_MENU_INFORMATION::class.java)
                            setupUIInfo()
                        } catch (ex: Exception) {
                        }
                    }
                }
            })


        }
    }

    private fun setupUIInfo()
    {
        if(detailMenuInfo != null)
        {
            val category = detailMenuInfo.productCategory?.map { it.categoryName }?.toList()
            category?.forEach {
                addButtonToSegment(layoutInflater, segmentedItemCategory,it)
            }

            segmentedItemCategory.setTintColor(Color.DKGRAY);
            if (segmentedItemCategory.childCount > 0) {
                val default = segmentedItemCategory.getChildAt(0) as RadioButton
                default.isChecked = true
            }
        }
    }

    private fun addButtonToSegment(inflater: LayoutInflater, group: SegmentedGroup, btnName: String)
    {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        if(sender == "ItemExtraInfo" && type == 0)
        {

            if(productItemInfo[pos].ItemDescURL != null) {

                val bundle = Bundle()
                bundle.putString("PRODUCT_ITEM_URL", productItemInfo[pos].ItemDescURL)
                val intent = Intent(context, ActivityProductDetailInfo::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

        }

    }
}


