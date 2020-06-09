package com.iew.fun2order.ui.my_setup.decodeQR

import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.iew.fun2order.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanActivity : AppCompatActivity() {

    var mDBV: DecoratedBarcodeView? = null
    private var captureManager : CaptureManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)


        mDBV = findViewById(R.id.dbv)

        captureManager = CaptureManager(this, mDBV)
        captureManager!!.initializeFromIntent(intent, savedInstanceState)
        captureManager!!.decode()
    }


    override fun onPause() {
        super.onPause()
        captureManager!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        captureManager!!.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager!!.onDestroy()
    }

    override fun onSaveInstanceState(
        outState: Bundle?,
        outPersistentState: PersistableBundle?
    ) {
        super.onSaveInstanceState(outState, outPersistentState)
        captureManager!!.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mDBV!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}


