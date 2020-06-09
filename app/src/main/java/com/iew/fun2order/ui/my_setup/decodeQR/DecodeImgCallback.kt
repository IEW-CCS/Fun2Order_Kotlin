package com.iew.fun2order.ui.my_setup.decodeQR

import com.google.zxing.Result

// Define Call Back Function
interface DecodeImgCallback {
    fun onImageDecodeSuccess(result: Result?)
    fun onImageDecodeFailed()
}