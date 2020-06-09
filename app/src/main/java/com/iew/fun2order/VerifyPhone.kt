package com.iew.fun2order

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_logon_sms.*

class VerifyPhone: AppCompatActivity(),CountryCodePicker.OnCountryChangeListener {

    private var ccp:CountryCodePicker?=null
    private var countryCode:String?=null
    private var countryName:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logon_sms)
        supportActionBar?.hide()

        ccp = findViewById(R.id.country_code_picker)
        ccp!!.setOnCountryChangeListener(this)
        ccp!!.setAutoDetectedCountry(true)
        ccp!!.registerCarrierNumberEditText(editTextPhoneNumber)

        // 按下按鈕 觸發事件
        btnNextStep.setOnClickListener{
            val phoneValid = ccp!!.isValidFullNumber
            if (phoneValid)
            {
                val intent = Intent()
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setClass(this@VerifyPhone, VerifyCode::class.java!!)
                val bundle = Bundle()
                bundle.putString("VerifyPhoneNo", ccp!!.fullNumberWithPlus)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }
            else
            {
                editTextPhoneNumber!!.error = "不合法的電話號碼."
            }
        }
    }

    override fun onCountrySelected() {
        countryCode=ccp!!.selectedCountryCode
        countryName=ccp!!.selectedCountryName
    }

}
