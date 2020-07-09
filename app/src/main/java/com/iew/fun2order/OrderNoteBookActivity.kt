package com.iew.fun2order

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.ClipboardManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.rangeTo
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import info.hoang8f.android.segmented.SegmentedGroup


const val CELLHEIGHT = 100
const val TEXTSIZE = 16F

const val SUMMARY_TITLEWIDTH = 300
const val SUMMARY_COLOMNWIDTH1 = 500
const val SUMMARY_COLOMNWIDTH2 = 200
const val SUMMARY_COLOMNWIDTH3 = 400


const val ADDRESS_WIDTH = 500
const val RESPECT_TITLEWIDTH = 300
const val RESPECT_COLOMNWIDTH0 = 400
const val RESPECT_COLOMNWIDTH1 = 500
const val RESPECT_COLOMNWIDTH2 = 200
const val RESPECT_COLOMNWIDTH3 = 400

class OrderNoteBookActivity : AppCompatActivity() {


    private lateinit var tablemain: TableLayout
    private lateinit var tableheader: TableLayout
    private lateinit var tableaddress: TableLayout
    private lateinit var mSegmentedGroupLocation: SegmentedGroup
    private lateinit var txtcopyOrderContent: TextView
    private lateinit var txtshareOrderContent: ImageView
    private lateinit var txtStoreName : TextView
    private lateinit var txtStorePhone : TextView

    private lateinit var meunOrder : USER_MENU_ORDER


    private var shareContext: String = ""


    private lateinit var lstAccept: List<ORDER_MEMBER>

    private val summyReport: MutableMap<String, MutableList<ORDER_STATSTUCS>> =
        mutableMapOf<String, MutableList<ORDER_STATSTUCS>>()
    private val summyLocation: MutableMap<String, MutableList<String>> =
        mutableMapOf<String, MutableList<String>>()
    private var mLocationList : MutableList<String> = mutableListOf()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_note_book)
        supportActionBar?.hide()


        tablemain = findViewById<TableLayout>(R.id.table_main)
        tableheader = findViewById<TableLayout>(R.id.table_header)
        tableaddress= findViewById<TableLayout>(R.id.table_address)

        mSegmentedGroupLocation = findViewById(R.id.SegmentedGroupLocation)
        txtcopyOrderContent = findViewById(R.id.copyOrderContent)
        txtshareOrderContent = findViewById(R.id.shareOrderContent)

        txtStoreName = findViewById(R.id.storeName)
        txtStorePhone = findViewById(R.id.storePhoneNumber)





        val mInflater: LayoutInflater? = LayoutInflater.from(this);

        intent?.extras?.let {

            meunOrder = it.getParcelable("USER_MENU_ORDER") as USER_MENU_ORDER

            addRadioButton(mInflater!!, mSegmentedGroupLocation, "合併顯示項目")

            if(meunOrder?.locations?.count()==0)
            {
                addRadioButton(mInflater!!, mSegmentedGroupLocation, "全部項目")
            }
            else {
                meunOrder?.locations?.forEach { location ->
                    addRadioButton(mInflater!!, mSegmentedGroupLocation, location.toString())
                    mLocationList.add(location.toString())
                }
            }

            lstAccept = meunOrder.contentItems!!.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT }
            statisticsSummyReport()

            if (mSegmentedGroupLocation.childCount > 0) {
                val default = mSegmentedGroupLocation.getChildAt(0) as RadioButton
                summaryReport()
                default.isChecked = true
            }


            txtStoreName.text = meunOrder.storeInfo?.storeName
            txtStorePhone.text = meunOrder.storeInfo?.storePhoneNumber

        }

        txtcopyOrderContent!!.setOnClickListener()
        {
            setClipboard(this, shareContext)
            Toast.makeText(this, "訂單資料已經複製到剪貼簿中!!", Toast.LENGTH_SHORT).show()
        }


        txtshareOrderContent!!.setOnClickListener()
        {
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody = shareContext
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Fun2Order")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            val chooserIntent = Intent.createChooser(intent, "Select app to share")
            if (chooserIntent != null) {
                try {
                    startActivity(chooserIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(this, "Can't find share component to share", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }


        //---- User Select Location  -----
        mSegmentedGroupLocation!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            when (radioButton.text) {
                "合併顯示項目" -> {
                    summaryReport()
                }
                "全部項目" -> {
                    respectivelyReport("")
                }
                else -> {
                    respectivelyReport(radioButton.text.toString())
                }
            }

        })

    }


    private fun setClipboard(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = text
        } else {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.primaryClip = clip
        }
    }


    private fun addRadioButton(inflater: LayoutInflater, group: SegmentedGroup, btnName: String) {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }

    private fun statisticsSummyReport() {
        var selectkey: String = ""
        var selectRecipe: String = ""
        var location: String = ""

        lstAccept.forEach()
        {
            it.orderContent.menuProductItems?.forEach()
            { menuProducts ->
                location = it.orderContent.location?.toString() ?: ""
                selectkey = ""
                selectkey = location
                selectkey += "_"
                selectkey += menuProducts.itemName.toString()
                selectRecipe = ""
                menuProducts.menuRecipes?.forEach()
                { menuRecipes ->
                    menuRecipes.recipeItems?.forEach()
                    { recipe ->
                        if (recipe.checkedFlag == true) {
                            selectRecipe = selectRecipe + recipe.recipeName + " "
                        }
                    }
                }

                val items: ORDER_STATSTUCS = ORDER_STATSTUCS()
                items.itemOwner = it.orderContent.itemOwnerName
                items.itemOwnerID = it.orderContent.itemOwnerID
                items.itemComments = menuProducts.itemComments
                items.itemName = menuProducts.itemName
                items.itemPrice = menuProducts.itemPrice
                items.itemQuantity = menuProducts.itemQuantity
                items.sequenceNumber = menuProducts.sequenceNumber
                items.itemRecipe = selectRecipe
                items.uniquetKey = selectRecipe + menuProducts.itemComments

                if (summyReport.containsKey(selectkey)) {
                    summyReport[selectkey]!!.add(items)
                } else {
                    val data: MutableList<ORDER_STATSTUCS> = mutableListOf()
                    data.add(items)
                    summyReport[selectkey] = data
                }

                if (summyLocation.containsKey(location)) {
                    val result = summyLocation[location]!!.filter { it -> it == selectkey }.count()
                    if (result == 0) {
                        summyLocation[location]!!.add(selectkey)
                    }
                } else {
                    val locationdata: MutableList<String> = mutableListOf()
                    locationdata.add(selectkey)
                    summyLocation[location] = locationdata
                }
            }
        }
    }


    private fun summaryReport() {

        tablemain.removeAllViews()
        tableheader.removeAllViews()
        tableaddress.removeAllViews()
        shareContext = ""
        var reportIndex: Int = 0


        if(mLocationList.count() == 0)
        {
            mLocationList.add("")
        }

        mLocationList.forEach()
        { it ->

            if (summyLocation.containsKey(it)) {
                var data = summyLocation[it]
                // 清空 Report Index by Location  When Summary Report
                reportIndex = 0
                //-----  Location ------
                val tableLocationHeader = TableRow(this)
                val textLocationHeader = TextView(this)
                textLocationHeader.text = it
                textLocationHeader.gravity = Gravity.CENTER
                textLocationHeader.setTextColor(Color.BLACK)
                textLocationHeader.width = SUMMARY_TITLEWIDTH
                textLocationHeader.setBackgroundResource(R.drawable.shape_rectangle_notebook_location)
                textLocationHeader.height = CELLHEIGHT
                textLocationHeader.textSize = TEXTSIZE
                tableLocationHeader.addView(textLocationHeader)
                tableheader.addView(tableLocationHeader)

                val tableLocationMain = TableRow(this)
                val textCol1 = TextView(this)
                textCol1.text = ""
                textCol1.width = SUMMARY_COLOMNWIDTH1
                textCol1.height = CELLHEIGHT
                textCol1.textSize = TEXTSIZE
                textCol1.setBackgroundResource(R.drawable.shape_rectangle_notebook_location)
                tableLocationMain.addView(textCol1)

                val textCol2 = TextView(this)
                textCol2.text = ""
                textCol2.width = SUMMARY_COLOMNWIDTH2
                textCol2.height = CELLHEIGHT
                textCol2.textSize = TEXTSIZE
                textCol2.setBackgroundResource(R.drawable.shape_rectangle_notebook_location)
                tableLocationMain.addView(textCol2)

                val textCol3 = TextView(this)
                textCol3.text = ""
                textCol3.width = SUMMARY_COLOMNWIDTH3
                textCol3.height = CELLHEIGHT
                textCol3.textSize = TEXTSIZE
                textCol3.setBackgroundResource(R.drawable.shape_rectangle_notebook_location)
                tableLocationMain.addView(textCol3)

                tablemain.addView(tableLocationMain)

                //----- TO Share -----
                if (it != "") {
                    shareContext = shareContext + "---" + it.toString() + "\n"
                }

                //--------------------------------------
                //--------- Title ---------------------
                val header = TableRow(this)
                val hd0 = TextView(this)
                hd0.text = "產品"
                hd0.gravity = Gravity.CENTER
                hd0.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
                hd0.setTextColor(Color.BLACK)
                hd0.width = SUMMARY_TITLEWIDTH
                hd0.height = CELLHEIGHT
                hd0.textSize = TEXTSIZE
                hd0.setTypeface(null, Typeface.BOLD)
                header.addView(hd0)
                tableheader.addView(header)

                val tbrow0 = TableRow(this)
                val tv0 = TextView(this)
                tv0.text = " 配方內容 "
                tv0.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
                tv0.width = SUMMARY_COLOMNWIDTH1
                tv0.height = CELLHEIGHT
                tv0.textSize = TEXTSIZE
                tv0.setTextColor(Color.BLACK)
                tv0.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
                tbrow0.addView(tv0)
                val tv1 = TextView(this)
                tv1.text = " 數量 "
                tv1.gravity = Gravity.CENTER
                tv1.setTextColor(Color.BLACK)
                tv1.width = SUMMARY_COLOMNWIDTH2
                tv1.height = CELLHEIGHT
                tv1.textSize = TEXTSIZE
                tv1.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
                tbrow0.addView(tv1)
                val tv2 = TextView(this)
                tv2.text = " 備註 "
                tv2.setTextColor(Color.BLACK)
                tv2.gravity = Gravity.CENTER
                tv2.width = SUMMARY_COLOMNWIDTH3
                tv2.height = CELLHEIGHT
                tv2.textSize = TEXTSIZE
                tv2.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
                tbrow0.addView(tv2)
                tablemain.addView(tbrow0)

                data!!.forEach()
                {
                    val data = summyReport[it] ?: null
                    if (data != null) {
                        val groupbyRecipe = data.groupBy { it -> it.uniquetKey }
                        val header = TableRow(this)
                        val hd0 = TextView(this)
                        hd0.text = data?.get(0)!!.itemName
                        hd0.gravity = Gravity.CENTER
                        hd0.height = CELLHEIGHT * groupbyRecipe.count()
                        hd0.width = SUMMARY_TITLEWIDTH
                        hd0.textSize = TEXTSIZE
                        hd0.setTextColor(Color.BLACK)
                        hd0.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                        header.addView(hd0)
                        tableheader.addView(header)

                        groupbyRecipe.forEach()
                        { group ->
                            val quantity = group.value.sumBy { it.itemQuantity!! }.toInt()
                            val tbrow = TableRow(this)
                            val t1v = TextView(this)
                            t1v.text = group.value[0].itemRecipe
                            t1v.setTextColor(Color.BLACK)
                            t1v.setPadding(10, 0, 0, 0)
                            t1v.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
                            t1v.width = SUMMARY_COLOMNWIDTH1
                            t1v.height = CELLHEIGHT
                            t1v.textSize = TEXTSIZE
                            t1v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                            tbrow.addView(t1v)
                            val t2v = TextView(this)
                            t2v.text = quantity.toString()
                            t2v.setTextColor(Color.BLACK)
                            t2v.gravity = Gravity.CENTER
                            t2v.width = SUMMARY_COLOMNWIDTH2
                            t2v.height = CELLHEIGHT
                            t2v.textSize = TEXTSIZE
                            t2v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                            tbrow.addView(t2v)
                            val t3v = TextView(this)
                            t3v.text = group.value[0].itemComments
                            t3v.setTextColor(Color.BLACK)
                            t3v.setPadding(10, 0, 0, 0)
                            t3v.width = SUMMARY_COLOMNWIDTH3
                            t3v.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
                            t3v.height = CELLHEIGHT
                            t3v.textSize = TEXTSIZE
                            t3v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                            tbrow.addView(t3v)

                            tablemain.addView(tbrow)


                            //------- To Share ------
                            reportIndex++
                            shareContext += "${reportIndex}. "
                            shareContext += "${data[0].itemName}"
                            if (group.value[0].itemRecipe != "") {
                                shareContext += " ( ${group.value[0].itemRecipe} )"
                            }
                            shareContext += " * ${quantity.toString()}"

                            if (group.value[0].itemComments != "") {
                                shareContext += " [${group.value[0].itemComments}]"
                            }
                            shareContext += "\n"
                            //------- To Share ------

                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun respectivelyReport(location: String) {

        tablemain.removeAllViews()
        tableheader.removeAllViews()
        tableaddress.removeAllViews()
        shareContext = ""
        var reportIndex = 0

        if(location !="") {
            shareContext = "$shareContext---$location\n"
        }

        val reportItemLocation = summyLocation.get(location)
        if (reportItemLocation != null) {
            val header = TableRow(this)
            val hd0 = TextView(this)
            hd0.text = "參與者"
            hd0.gravity = Gravity.CENTER
            hd0.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
            hd0.setTextColor(Color.BLACK)
            hd0.width = RESPECT_TITLEWIDTH
            hd0.height = CELLHEIGHT
            hd0.textSize = TEXTSIZE
            header.addView(hd0)
            tableheader.addView(header)

            val tbrow0 = TableRow(this)

            val tv0 = TextView(this)
            tv0.text = " 產品 "
            tv0.width = RESPECT_COLOMNWIDTH0
            tv0.height = CELLHEIGHT
            tv0.textSize = TEXTSIZE
            tv0.gravity = Gravity.CENTER
            tv0.setTextColor(Color.BLACK)
            tv0.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
            tbrow0.addView(tv0)

            val tv1 = TextView(this)
            tv1.text = " 配方內容 "
            tv1.width = RESPECT_COLOMNWIDTH1
            tv1.height = CELLHEIGHT
            tv1.textSize = TEXTSIZE
            tv1.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
            tv1.setTextColor(Color.BLACK)
            tv1.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
            tbrow0.addView(tv1)

            val tv2 = TextView(this)
            tv2.text = " 數量 "
            tv2.gravity = Gravity.CENTER
            tv2.setTextColor(Color.BLACK)
            tv2.width = RESPECT_COLOMNWIDTH2
            tv2.height = CELLHEIGHT
            tv2.textSize = TEXTSIZE
            tv2.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
            tbrow0.addView(tv2)

            val tv3 = TextView(this)
            tv3.text = " 備註 "
            tv3.setTextColor(Color.BLACK)
            tv3.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
            tv3.width = RESPECT_COLOMNWIDTH3
            tv3.height = CELLHEIGHT
            tv3.textSize = TEXTSIZE
            tv3.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
            tbrow0.addView(tv3)

            tablemain.addView(tbrow0)


            val address = TableRow(this)
            val addr0 = TextView(this)
            addr0.text = "聯絡方式"
            addr0.gravity = Gravity.CENTER
            addr0.setBackgroundResource(R.drawable.shape_rectangle_notebook_title)
            addr0.setTextColor(Color.BLACK)
            addr0.width = ADDRESS_WIDTH
            addr0.height = CELLHEIGHT
            addr0.textSize = TEXTSIZE
            address.addView(addr0)
            tableaddress.addView(address)


            val totalData: MutableList<ORDER_STATSTUCS> = mutableListOf<ORDER_STATSTUCS>()
            reportItemLocation.forEach()
            {
                val data = summyReport[it] ?: null
                if (data != null) {
                    data.forEach()
                    { it ->
                        totalData.add(it)
                    }
                }
            }


            val groupbyRecipe = totalData.groupBy { it -> it.itemOwner }
            groupbyRecipe.forEach()
            { data ->

                val header = TableRow(this)
                val hd0 = TextView(this)
                hd0.text = data?.value[0].itemOwner
                hd0.gravity = Gravity.CENTER
                hd0.height = CELLHEIGHT * data?.value.count()
                hd0.width = RESPECT_TITLEWIDTH
                hd0.textSize = TEXTSIZE
                hd0.setTextColor(Color.BLACK)
                hd0.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                header.addView(hd0)
                tableheader.addView(header)

                reportIndex++
                shareContext += "${reportIndex}. 『${data?.value[0].itemOwner}』的訂單內容：\n"
                shareContext += "[產品內容]：\n"

                val items = data.value.sortedBy { it -> it.itemName }
                items.forEach()
                { item ->

                    val tbrow = TableRow(this)
                    val t1v = TextView(this)
                    t1v.text = item.itemName
                    t1v.setTextColor(Color.BLACK)
                    t1v.setPadding(10,0,0,0)
                    t1v.width = RESPECT_COLOMNWIDTH0
                    t1v.height = CELLHEIGHT
                    t1v.textSize = TEXTSIZE
                    t1v.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
                    t1v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                    tbrow.addView(t1v)

                    val t2v = TextView(this)
                    t2v.text = item.itemRecipe
                    t2v.setTextColor(Color.BLACK)
                    t2v.setPadding(10,0,0,0)
                    t2v.width = RESPECT_COLOMNWIDTH1
                    t2v.height = CELLHEIGHT
                    t2v.textSize = TEXTSIZE
                    t2v.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
                    t2v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                    tbrow.addView(t2v)

                    val t3v = TextView(this)
                    t3v.text = item.itemQuantity.toString()
                    t3v.setTextColor(Color.BLACK)
                    t3v.gravity = Gravity.CENTER
                    t3v.width = RESPECT_COLOMNWIDTH2
                    t3v.height = CELLHEIGHT
                    t3v.textSize = TEXTSIZE
                    t3v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                    tbrow.addView(t3v)

                    val t4v = TextView(this)
                    t4v.text = item.itemComments
                    t4v.setTextColor(Color.BLACK)
                    t4v.setPadding(10,0,0,0)
                    t4v.width = RESPECT_COLOMNWIDTH3
                    t4v.height = CELLHEIGHT
                    t4v.textSize = TEXTSIZE
                    t4v.gravity = Gravity.CENTER_VERTICAL + Gravity.LEFT
                    t4v.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)
                    tbrow.addView(t4v)

                    tablemain.addView(tbrow)


                    //------- To Share ------
                    shareContext += "\t ${item.itemName} ${item.itemRecipe} * ${item.itemQuantity}"
                    if (item.itemComments != "") {
                        shareContext += " [${item.itemComments}]"
                    }
                    shareContext += "\n"
                    //------- To Share ------

                }


                val address = TableRow(this)
                val addr0 = TextView(this)
                addr0.gravity = Gravity.CENTER
                addr0.height = CELLHEIGHT * data?.value.count()
                addr0.width = RESPECT_TITLEWIDTH
                addr0.textSize = TEXTSIZE
                addr0.setTextColor(Color.BLACK)
                addr0.setBackgroundResource(R.drawable.shape_rectangle_notebook_cell)

                var txtmailinfo =""
                val myCotent = meunOrder.contentItems?.firstOrNull { it.memberID  ==data?.value[0].itemOwnerID }
                if(myCotent != null)
                {
                    if(myCotent.orderContent.userContactInfo!= null) {
                        txtmailinfo += "姓名: ${myCotent.orderContent.userContactInfo?.userName} \n"
                        txtmailinfo += "地址: ${myCotent.orderContent.userContactInfo?.userAddress} \n"
                        txtmailinfo += "電話: ${myCotent.orderContent.userContactInfo?.userPhoneNumber} \n"

                        if(data?.value.count() < 3) {
                            addr0.text = "請長按點擊以查看訊息"
                            addr0.tooltipText = txtmailinfo
                        }
                        else
                        {
                            addr0.text = txtmailinfo
                            addr0.tooltipText = txtmailinfo
                        }
                        shareContext += "[聯絡資訊]：\n"
                        shareContext += "$txtmailinfo\n"
                    }
                }
                address.addView(addr0)
                tableaddress.addView(address)


            }
        }
    }
}
