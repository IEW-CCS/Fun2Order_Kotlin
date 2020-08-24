package com.iew.fun2order.contact

import android.content.Context
import android.provider.ContactsContract
import java.util.*

/**
 * Created by Administrator on 2019/6/21.
 */
object contactUtils_ContactBase {
    fun getAllContacts(context: Context): ArrayList<ContactsBase> {
        val contacts = ArrayList<ContactsBase>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )
        while (cursor.moveToNext()) {
            //新建一个联系人实例
            val temp = ContactsBase()
            val contactId = cursor.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts._ID)
            )
            //获取联系人姓名
            val name = cursor.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            )
            temp.name = name

            //获取联系人电话号码
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                null,
                null
            )
            while (phoneCursor.moveToNext()) {
                var phone =
                    phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phone = phone.replace("-", "")
                phone = phone.replace(" ", "")
                temp.phone.add(phone)
            }

            //获取联系人备注信息
            val noteCursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data._ID,
                    ContactsContract.CommonDataKinds.Nickname.NAME
                ),
                ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                arrayOf(contactId),
                null
            )
            if (noteCursor.moveToFirst()) {
                do {
                    val note = noteCursor.getString(
                        noteCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)
                    )
                    temp.note = note
                } while (noteCursor.moveToNext())
            }
            contacts.add(temp)
            //记得要把cursor给close掉
            phoneCursor.close()
            noteCursor.close()
        }
        cursor.close()
        return contacts
    }
}


object contactUtils_PhoneBase {
    fun getAllContacts(context: Context): ArrayList<PhoneBase> {
        val contacts = ArrayList<PhoneBase>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )
        while (cursor.moveToNext()) {
            val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            //取得電話號碼
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                null,
                null
            )
            while (phoneCursor.moveToNext()) {
                var phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phone = phone.replace("-", "")
                phone = phone.replace(" ", "")
                if(phone.startsWith("09"))
                {
                    phone = phone.replace("09", "+8869")
                    val temp = PhoneBase()
                    temp.name = name
                    temp.phone = phone
                    contacts.add(temp)
                }
            }

            //记得要把cursor给close掉
            phoneCursor.close()
        }
        cursor.close()
        return contacts
    }
}