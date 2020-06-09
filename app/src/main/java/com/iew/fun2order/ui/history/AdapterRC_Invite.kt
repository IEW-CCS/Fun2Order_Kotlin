package com.iew.fun2order.ui.history

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_REJECT
import kotlinx.android.synthetic.main.activity_reference_order.view.*
import kotlinx.android.synthetic.main.row_history_invite.view.*
import java.text.SimpleDateFormat


class RCAdapter_Invite(var context: Context, var ItemsLV_Invite: List<ItemsLV_Invite>, val IAdapterOnClick: IAdapterOnClick, val IAdapterBtnOnClick: IAdapterBtnOnClick) : RecyclerView.Adapter<RCAdapter_Invite.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_history_invite,  null)
        return ViewHolder(view).listen()
        { pos, type ->
            IAdapterOnClick.onClick("Invite",pos,type)
        }
    }

    override fun getItemCount(): Int {
        return ItemsLV_Invite.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( ItemsLV_Invite[position] )
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var btnJoin : Button = itemView.findViewById<View>(R.id.invite_Join) as Button
        var btnReject : Button = itemView.findViewById<View>(R.id.invite_Reject) as Button
        init {
            btnJoin.setOnClickListener(View.OnClickListener {
                IAdapterBtnOnClick.onBtnClick(MENU_ORDER_REPLY_STATUS_ACCEPT,adapterPosition)
            })

            btnReject.setOnClickListener(View.OnClickListener {
                IAdapterBtnOnClick.onBtnClick(MENU_ORDER_REPLY_STATUS_REJECT,adapterPosition)
            })
        }

        @SuppressLint("ResourceAsColor")
        fun bindModel(invite: ItemsLV_Invite){


            val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
            val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

            // set description
            itemView.invitetitle.text = invite.title
            itemView.inviteUser.text = invite.inviteUser


            val startDateTime = sdfDecode.parse(invite.startTime)
            val formatStartDatetime = sdfEncode.format(startDateTime).toString()
            itemView.inviteStartTime.text = formatStartDatetime


            if (invite.inviteReplyTime == "") {
                if(invite.inviteReplyStatus == MENU_ORDER_REPLY_STATUS_EXPIRE )
                {
                    itemView.invitetitle.text = invite.title + " -- 團購單已逾期"
                    itemView.invitetitle.setTextColor(Color.RED)

                    btnJoin.isEnabled = false
                    btnReject.isEnabled = false
                    btnJoin.isClickable = false
                    btnReject.isClickable = false

                }
                else {
                    itemView.inviteNote.text = "尚未回覆"

                    btnJoin.isEnabled = true
                    btnReject.isEnabled = true
                    btnJoin.isClickable = true
                    btnReject.isClickable = true
                    itemView.inviteNote.setTextColor(Color.rgb(128,128,128))
                }

               // itemView.inviteNote.text = ""
            }
            else {

                val replyDateTime = sdfDecode.parse(invite.inviteReplyTime)
                val formatReplyDatetime = sdfEncode.format(replyDateTime).toString()
                var replyStatus = ""
                if (invite.inviteReplyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                    itemView.inviteNote.text = "已於 $formatReplyDatetime \n回覆 參加"
                    itemView.inviteNote.setTextColor(Color.rgb(0,0,255))
                } else if(invite.inviteReplyStatus == MENU_ORDER_REPLY_STATUS_REJECT){
                    itemView.inviteNote.text = "已於 $formatReplyDatetime \n回覆 不參加"
                    itemView.inviteNote.setTextColor(Color.rgb(255,0,0))
                }

            }

        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                     context.packageName)
            return context.resources.getDrawable(id)
        }

    }

}








