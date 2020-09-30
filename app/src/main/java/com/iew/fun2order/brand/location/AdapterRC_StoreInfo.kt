package com.iew.fun2order.brand.location

import android.R.attr.x
import android.R.string
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.GeoPoint
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_STORE
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import kotlinx.android.synthetic.main.row_brandstore_item.view.*


class AdapterRC_StoreInfo(var context: Context, var lstStoreInfo : List<DETAIL_BRAND_STORE>, var oriLocation : Location?, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_StoreInfo.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_brandstore_item,  parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lstStoreInfo.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstStoreInfo[position],oriLocation)
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(ItemStore: DETAIL_BRAND_STORE, location: Location?) {

            itemView.textBrandStoreName.text = ItemStore.storeName ?: ""
            itemView.textBrandStorePhoneNumber.text = ItemStore.storePhoneNumber ?: ""
            itemView.textBrandStoreAddress.text = ItemStore.storeAddress ?: ""

            if(ItemStore.businessTime!= null)
            {
                if(!ItemStore.businessTime!!.dayOffFlag)
                {
                    itemView.textBrandBusinssesTime.text = "今日公休"
                    itemView.textBrandBusinssesTime.setTextColor(Color.RED)
                }
                else
                {
                    val openTime = ItemStore.businessTime!!.openTime ?: ""
                    val closeTime = ItemStore.businessTime!!.closeTime ?: ""
                    itemView.textBrandBusinssesTime.text = "今日營業時間 : ${openTime} 至 ${closeTime}"
                }
            }


            itemView.imageBrandStore.setOnClickListener {
                IAdapterOnClick.onClick("MAP",adapterPosition,0)
            }

            itemView.btnOrderAction.setOnClickListener {
                IAdapterOnClick.onClick("ACTION",adapterPosition,0)
            }

            if ( ItemStore.storeImageURL != null) {
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))
                Glide.with(context)
                    .load( ItemStore.storeImageURL)
                    .apply(requestOptions)
                    .error(null)
                    .into(itemView.imageBrandStore)
            }

            if (location != null && ItemStore.storeAddress != null) {
                val storeLatitude = ItemStore.storeLatitude?.toDoubleOrNull()
                val storeLongitude = ItemStore.storeLongitude?.toDoubleOrNull()

                if (storeLatitude != null && storeLongitude != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude,
                        location.longitude,
                        storeLatitude,
                        storeLongitude,
                        results
                    )
                    val distance = results[0] / 1000.0
                    val disString: String = String.format("%.1f", distance)
                    itemView.textBrandStoreDistance.text = "${disString} 公里"
                }

            }

        }

        fun getLocationFromAddress( strAddress : String) : GeoPoint?{
            val coder : Geocoder =  Geocoder(context)
            var address :  MutableList<Address> =  mutableListOf<Address>()
            var p1 : GeoPoint? = null
            try {
                address = coder.getFromLocationName(strAddress,5);
                if (address != null) {
                    val location = address[0]
                    p1 = GeoPoint ((location.latitude ),
                        (location.longitude ))
                }
            }
            catch (ex: Exception)
            {
                val exception = ex.message
            }
            return p1
        }
    }
}
