package com.iew.fun2order.ui.home.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.home.data.RecipeItemListData


class RecipeItemAdapter(listdata: MutableList<RecipeItemListData>, orderMode : Boolean) :
    RecyclerView.Adapter<RecipeItemAdapter.ViewHolder>() {
    private val listdata: MutableList<RecipeItemListData>
    private val context: Context? = null
    private var orderMode: Boolean? = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View =
            layoutInflater.inflate(R.layout.row_recipe_item, parent, false)

        return ViewHolder(listItem, orderMode!!)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val myListData: RecipeItemListData = listdata[position]
        holder.allowMulti =  listdata[position].getAllowMulti()!!
        holder.textViewRecipeType.setText(listdata[position].getItemName())
        holder.itemDataList = listdata[position].getItemDataList()
        holder.itemDataSelectList = listdata[position].getItemDataSelectList()
        if(holder.orderMode){
            holder.btnAddRecipe.visibility=View.GONE
        }
        if(holder.BtnList.size>0){
            var iRow:Int = 0;
            iRow = (1)/3+1;

            holder.BtnList.forEach(){
                holder.gridLayoutRecipeBtnList.removeView(it)
            }
        }
        holder.BtnList.clear()

        holder.btnAddRecipe.setOnClickListener { view ->
            /*
            Toast.makeText(
                view.context,
                "Add Recipe item: " + myListData.getItemName(),
                Toast.LENGTH_LONG
            ).show()
             */


            val item = LayoutInflater.from(view.context).inflate(R.layout.alert_input_recipe, null)



            var alertDialog = AlertDialog.Builder(view.context)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var editTextRecipe = item.findViewById(R.id.editTextRecipe) as EditText

                    if (TextUtils.isEmpty(editTextRecipe.text.trim()))
                    {
                        editTextRecipe.requestFocus()
                        editTextRecipe.error = "配方項目不能為空白!"
                    }else {

                        //Toast.makeText(item.context,
                        //    "加入地點:"+editTextRecipe.getText().toString(), Toast.LENGTH_SHORT).show()

                        holder.AddNewRecipeItem(view, holder.gridLayoutRecipeBtnList,holder.BtnList,holder.btnAddRecipe, editTextRecipe.getText().toString(), true, holder.allowMulti, holder.orderMode)

                        alertDialog.dismiss()
                    }
                }
        }

        //holder.gridLayoutRecipeBtnList.removeAllViews()

        holder.itemDataList.forEach(){
            println(it.toString())
            holder.AddNewRecipeItem(holder.ItemView, holder.gridLayoutRecipeBtnList,holder.BtnList,holder.btnAddRecipe,it.toString(), false, holder.allowMulti, holder.orderMode)
        }


    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    class ViewHolder(itemView: View, ordermode: Boolean) : RecyclerView.ViewHolder(itemView) {
        //public ImageView imageView;
        //public TextView textView;
        var textViewRecipeType: TextView
        var gridLayoutRecipeBtnList: GridLayout
        var btnAddRecipe : Button
        var BtnList :MutableList<Button> = mutableListOf()
        var itemDataList :MutableList<String> = mutableListOf()
        var itemDataSelectList :MutableList<Boolean> = mutableListOf()
        var ItemView: View
        var orderMode: Boolean
        var allowMulti:Boolean
        init {
            allowMulti = false
            orderMode = ordermode
            ItemView = itemView
            //this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            //this.textView = (TextView) itemView.findViewById(R.id.textView);
            textViewRecipeType =
                itemView.findViewById<View>(R.id.textViewRecipeType) as TextView
            gridLayoutRecipeBtnList =
                itemView.findViewById<View>(R.id.gridLayoutRecipeBtnList) as GridLayout

            btnAddRecipe =
                itemView.findViewById<View>(R.id.btnAddRecipe) as Button
            // 點擊項目中的Button時
            BtnList.clear()
            // 點擊項目中的Button時
            btnAddRecipe.setOnClickListener(View.OnClickListener {
                // 按下Button要做的事
                //AddNewRecipeItem(it, gridLayoutRecipeBtnList,BtnList,btnAddRecipe)


            })
        }

        fun AddNewRecipeItem(itemView: View,gridLayoutRecipeBtnList: GridLayout,btnList: MutableList<Button>, addButton: Button, recipe: String, addFlag : Boolean, multiFlag : Boolean, orderMode: Boolean) {

            // 將 TextView 加入到 LinearLayout 中
            val displayMetrics = itemView.context!!.resources.displayMetrics
            val pxWidth = displayMetrics.widthPixels

            var width = (pxWidth/3)-50 ;        // 螢幕的寬度/4放近with

            // 將 Button 1 加入到 LinearLayout 中
            val b1 = Button(itemView.context)
            if(addFlag){
                b1.tag = itemDataList.size
                itemDataList.add(recipe.toString())
                itemDataSelectList.add(true)
                b1.setText(recipe.toString())
                b1.setBackgroundResource(R.drawable.shape_rectangle_select)
            }else{
                b1.tag = btnList.size
                b1.setText(recipe.toString())
                //itemDataSelectList[b1.tag.toString().toInt()] = !itemDataSelectList[b1.tag.toString().toInt()]
                var bSelect  = itemDataSelectList[b1.tag.toString().toInt()]
                if(bSelect){
                    b1.setBackgroundResource(R.drawable.shape_rectangle_select)
                }else{
                    b1.setBackgroundResource(R.drawable.shape_rectangle_unselect)
                }
                //b1.setBackgroundResource(R.drawable.shape_rectangle_select)
            }

            var  lp2:LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
//设置padding值

            lp2.setMargins(20, 20, 0, 0);

            b1.setMinWidth(width)
            b1.setPadding(0,0,0,0)
            b1.setOnClickListener {
                if(allowMulti){
                    //Toast.makeText(it.context, "AllowMulti", Toast.LENGTH_SHORT).show();
                }else{
                    if(orderMode) {
                        //Toast.makeText(it.context, "Not AllowMulti", Toast.LENGTH_SHORT).show();
                        gridLayoutRecipeBtnList.removeAllViews()
                        btnList.forEach() {
                            it.setBackgroundResource(R.drawable.shape_rectangle_unselect)
                            it.setPadding(0, 0, 0, 0)
                            gridLayoutRecipeBtnList.addView(it)
                        }
                        if (!orderMode) {
                            gridLayoutRecipeBtnList.addView(addButton)
                        }
                        var iIdx = 0;
                        itemDataSelectList.forEach() {
                            if (iIdx != b1.tag.toString().toInt()) {
                                itemDataSelectList[iIdx] = false
                            }
                            iIdx++
                        }
                    }
                }



                itemDataSelectList[b1.tag.toString().toInt()] = !itemDataSelectList[b1.tag.toString().toInt()]
                var bSelect  = itemDataSelectList[b1.tag.toString().toInt()]
                if(bSelect){
                    var left = b1.getPaddingLeft()
                    var top = b1.getPaddingTop()
                    var right = b1.getPaddingRight()
                    var bottom = b1.getPaddingBottom()
                    b1.setBackgroundResource(R.drawable.shape_rectangle_select)

                    b1.setPadding(left, top, right, bottom)
                    //b1.setPadding(0,0,0,0)
                }else{
                    var left = b1.getPaddingLeft()
                    var top = b1.getPaddingTop()
                    var right = b1.getPaddingRight()
                    var bottom = b1.getPaddingBottom()
                    b1.setBackgroundResource(R.drawable.shape_rectangle_unselect)
                    b1.setPadding(left, top, right, bottom)
                    //b1.setPadding(0,0,0,0)
                }
                //Toast.makeText(it.context, b1.tag.toString()+" text:"+b1.getText().toString(), Toast.LENGTH_SHORT).show();

            }
            //b1.setBackgroundResource(com.iew.fun2order.R.drawable.button_bg)
            btnList.add(b1)
            //button = new Button[9] ;
            //GridLayout gridLayout = (GridLayout)findViewById(R.id.root) ;


            gridLayoutRecipeBtnList.removeAllViews()

            val total = btnList.size+1
            val column = 3
            //val row = total / column
            val row = 9
            println("Row:"+(row+1).toString()+", Col:"+column.toString() + ", Total:"+total.toString())

            gridLayoutRecipeBtnList.setColumnCount(column)
            gridLayoutRecipeBtnList.setRowCount(row + 1)
            var c = 0;
            var r = 0;

            btnList.forEach(){
                gridLayoutRecipeBtnList.addView(it, lp2)

            }
                //val button1:Button = it

            gridLayoutRecipeBtnList.addView(addButton)


            /*
            var iRow:Int = 0;
            iRow = ((btnList.size+1)/3);
            if(iRow == 0){
                iRow = 1
            }else{
                if(((btnList.size+1)%3)>0){
                   iRow++
                }
            }
            println("1-iRow:"+iRow.toString()+ " btnList.size:"+btnList.size.toString() + " RowCOunt:"+gridLayoutRecipeBtnList.getRowCount().toString())
            if(btnList.size == 1){
                gridLayoutRecipeBtnList.removeAllViews()
                gridLayoutRecipeBtnList.setColumnCount(3);           // 設定GridLayout有幾行

            }
            println("2-iRow:"+iRow.toString()+ " btnList.size:"+btnList.size.toString() + " RowCOunt:"+gridLayoutRecipeBtnList.getRowCount().toString())

            //gridLayoutRecipeBtnList.setRowCount(iRow);              // 設定GridLayout有幾列
            gridLayoutRecipeBtnList.removeView(addButton)
            gridLayoutRecipeBtnList.addView(b1,lp2);
            //gridLayoutRecipeBtnList.addView(b1,width,100);
            //gridLayoutRecipeBtnList.addView(b1)
            gridLayoutRecipeBtnList.addView(addButton)

             */
        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
        this.orderMode = orderMode
    }


}