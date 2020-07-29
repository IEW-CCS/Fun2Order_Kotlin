package com.iew.fun2order.ui.shop

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_PROFILE
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFORMATION
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_detail_menu.*
import kotlinx.android.synthetic.main.row_detail_productitems.view.*
import kotlinx.android.synthetic.main.row_shop_branditem.view.*


/*
     object to json
        var gson = Gson()
        var jsonString = gson.toJson(TestModel(1,"Test"))
     json to Object
        var jsonString = """{"id":1,"description":"Test"}""";
        var testModel = gson.fromJson(jsonString, TestModel::class.java)

      firebase json to Object
          val objectclass = dataSnapshot.getValue(Any::class.java)
          val json = Gson().toJson(objectclass)
         val brandProfile = Gson().fromJson(json, DETAIL_BRAND_PROFILE::class.java)
 */

class ActivityDetailMenu : AppCompatActivity() {


    private  var  selectBrandName : String? = null
    private  var  selectBrandImageURL: String? = null
    private  var  selectBrandMenuNumber: String? = null
    private  var  menuExist:Boolean = false
    private  lateinit var detailMenuInfo : DETAIL_MENU_INFORMATION
    private  var productItemInfo : MutableList<ItemsLV_Products> = mutableListOf()
    private  var productPriceSequence : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)
        supportActionBar?.title = "品牌詳細資料"


        menuExist = false
        selectBrandName = intent.extras?.getString("BRAND_NAME")
        selectBrandImageURL = intent.extras?.getString("BRAND_IMAGE_URL")

        val request: AdRequest = AdRequest.Builder().build()
        adView.loadAd(request)
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(errorCode: Int) {
                adView.visibility = View.GONE
            }
        }


        segmentedItemCategory.removeAllViews()
        productItemInfo.clear()
        productPriceSequence.clear()

        segmentedItemCategory.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            val selectItemCategory = radioButton.text.toString()


            if(detailMenuInfo != null && selectItemCategory != "")
            {
               //----- Clear -------
                productItemInfo.clear()
                productPriceSequence.clear()

                //----- Setup Title Bar ---
                brandItemsTitle.removeAllViews()
                val title = LayoutInflater.from(this).inflate(R.layout.row_detail_productitems, null)
                title.itemName.setTextColor(Color.BLUE)
                title.itemName.text = "品名"
                title.itemName.textSize = 16F

                val lp2:TableRow.LayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                lp2.setMargins(20, 0, 0, 0);

                val tbrow = TableRow(this)
                val selectProductCategory = detailMenuInfo.productCategory?.firstOrNull { it -> it.categoryName == selectItemCategory}
                if(selectProductCategory!= null)
                {
                    selectProductCategory.priceTemplate?.recipeList?.sortedBy { it.itemSequence }?.forEach {

                        val t1v = TextView(this)
                        t1v.text =  it.itemName
                        t1v.setTextColor(Color.BLACK)
                        t1v.textSize = 16F
                        t1v.width = 20
                        t1v.gravity = Gravity.CENTER
                        t1v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                        tbrow.addView(t1v,lp2)

                        productPriceSequence.add( it.itemName)
                    }

                    title.itemAttribute.addView(tbrow)
                    brandItemsTitle.addView(title)


                    //---------- Setup Body ------
                    selectProductCategory.productItems?.forEach {

                        val productDesc = it.productDescription ?: ""
                        productItemInfo.add(ItemsLV_Products(it.productName,it.priceList, selectProductCategory?.priceTemplate.standAloneProduct, productDesc))
                    }
                    rcv_brandItems.adapter?.notifyDataSetChanged()
                }
            }
        })

        brandName.text = selectBrandName
        downloadBrandImageFromFireBaseGlide(selectBrandImageURL)
        downloadBrandProfileInfoFromFireBase(selectBrandName)

        rcv_brandItems.layoutManager =  LinearLayoutManager(this)
        rcv_brandItems.adapter = AdapterRC_Items( this, productItemInfo,productPriceSequence)
        rcv_brandItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        btnGroupBuyInvite.setOnClickListener {


            if(menuExist == true) {
                val bundle = Bundle()
                bundle.putString("BRAND_NAME", selectBrandName)
                bundle.putString("BRAND_MENU_NUMBER", selectBrandMenuNumber)
                var intent = Intent(this, ActivitySetupDetailOrder::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            else
            {
                AlertDialog.Builder(this@ActivityDetailMenu)
                    .setTitle("通知訊息")
                    .setMessage("$selectBrandMenuNumber 資訊不存在!!\n無法揪團!!")
                    .setPositiveButton("確定",null)
                    .create()
                    .show()

            }
        }

    }

    private fun downloadBrandImageFromFireBase(ImageURL:String?)
    {
        if (ImageURL != null) {
            val islandRef = Firebase.storage.reference.child(ImageURL)
            val ONE_MEGABYTE = 1024 * 1024.toLong()
            islandRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener { bytesPrm: ByteArray ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    brandImage.setImageBitmap(bmp)
                }
                .addOnFailureListener {
                }
        }
    }


    private fun downloadBrandImageFromFireBaseGlide(ImageURL:String?)
    {
        if (ImageURL != null) {
            brandImage.setImageBitmap(null)
            val islandRef = Firebase.storage.reference.child(ImageURL)
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))
            islandRef.downloadUrl.addOnSuccessListener {
                Glide.with(this)
                    .load(it)
                    .apply(requestOptions)
                    .into(brandImage)

            }.addOnFailureListener {
                brandImage.setImageBitmap(null)
            }
        }
    }

    private fun downloadBrandProfileInfoFromFireBase(BrandName:String?)
    {
        if (BrandName != null) {
            val detailProfileURL = "/DETAIL_BRAND_PROFILE/$BrandName"
            val database = Firebase.database
            val myRef = database.getReference(detailProfileURL)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val brandProfile = dataSnapshot.getValue(DETAIL_BRAND_PROFILE::class.java)
                    if(brandProfile!= null) {
                        selectBrandMenuNumber = brandProfile!!.menuNumber
                        downloadBrandDetailMenuInfoFromFireBase(brandProfile!!.menuNumber)
                    }
                }
            })
        }
    }


    private fun downloadBrandDetailMenuInfoFromFireBase(DetailMenuNumber:String?)
    {
        if (DetailMenuNumber != null) {
            val detailMenuURL = "/DETAIL_MENU_INFORMATION/$DetailMenuNumber"
            val database = Firebase.database
            val myRef = database.getReference(detailMenuURL)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val objectClass = dataSnapshot.getValue(Any::class.java)
                    if(objectClass == null)
                    {
                        AlertDialog.Builder(this@ActivityDetailMenu)
                            .setTitle("通知訊息")
                            .setMessage("$DetailMenuNumber 資訊不存在!!")
                            .setPositiveButton("確定",null)
                            .create()
                            .show()
                    }
                    else {

                        val json = Gson().toJson(objectClass)
                        try {
                            detailMenuInfo = Gson().fromJson(json, DETAIL_MENU_INFORMATION::class.java)
                            setupUIInfo()
                            menuExist = true
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


}
