package com.iew.fun2order.ui.shop

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.menuImageDAO
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import kotlinx.android.synthetic.main.row_shop_branditem.view.*


class AdapterRC_Brand(var context: Context, var lstItemsGroup: List<ItemsLV_Brand>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_Brand.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_shop_branditem,  null)
        return ViewHolder(view).listen()
        { pos, type ->

            IAdapterOnClick.onClick("Brand",pos,type)
        }
    }

    override fun getItemCount(): Int {
        return lstItemsGroup.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstItemsGroup[position] )
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dbContext: MemoryDatabase = MemoryDatabase(context)
        private val menuImageDB: menuImageDAO = dbContext.menuImagedao()
        fun bindModel(Items: ItemsLV_Brand) {
            itemView.BrandName.text = Items.Name
            itemView.BrandView.setImageBitmap(null)

            val islandRef = Firebase.storage.reference.child(Items.ImageURL)
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))
            islandRef.downloadUrl.addOnSuccessListener {
                Glide.with(context)
                    .load(it)
                    .apply(requestOptions)
                    .into(itemView.BrandView)

            }.addOnFailureListener {
                itemView.BrandView.setImageBitmap(null)
            }

            /*
            if (Items.ImageURL != null) {
                val menuImageInfo =   menuImageDB.getMenuImageByName(Items.ImageURL.toString())
                if (menuImageInfo != null) {
                    var bmp = BitmapFactory.decodeByteArray(menuImageInfo.image, 0, menuImageInfo.image.size)
                    val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, bmp)
                    roundedBitmapDrawable.cornerRadius =  30F
                    itemView.BrandView.setImageDrawable(roundedBitmapDrawable)
                } else {
                    val islandRef = Firebase.storage.reference.child(Items.ImageURL)
                    val ONE_MEGABYTE = 1024 * 1024.toLong()
                    islandRef.getBytes(ONE_MEGABYTE)
                        .addOnSuccessListener { bytesPrm: ByteArray ->
                            try {
                                val menuImage: entityMeunImage = entityMeunImage(null, Items.ImageURL.toString(), "", bytesPrm)
                                menuImageDB.insertRow(menuImage)

                            } catch (ex: Exception) {
                                itemView.BrandView.setImageBitmap(null)
                            }
                            notifyItemChanged(adapterPosition)
                        }
                        .addOnFailureListener {
                            itemView.BrandView.setImageBitmap(null)
                        }
                }
            }*/
        }
    }
}








