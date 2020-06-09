package com.iew.fun2order.ui.my_setup.decodeQR

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.*


class DecodeImgThread(
    private val imgPath: String,
    private val callback: DecodeImgCallback?
) : Thread() {

    private val scanBitmap: Bitmap? = null
    override fun run() {
        super.run()
        if (TextUtils.isEmpty(imgPath) || callback == null) {
            return
        }


        val scanBitmap = getBitmap(imgPath, 400, 400)
        val multiFormatReader = MultiFormatReader()
        // 解码的参数
        val hints = Hashtable<DecodeHintType, Any?>(2)
        // 可以解析的编码类型
        val decodeFormats = Vector<BarcodeFormat>()
        // 扫描的类型  一维码和二维码
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats

        multiFormatReader.setHints(hints)

        var rawResult: Result? = null
        try {
            rawResult = multiFormatReader.decodeWithState(
                BinaryBitmap(
                    HybridBinarizer(
                        BitmapLuminanceSource(
                            scanBitmap
                        )
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()

        }
        if (rawResult != null) {
            callback.onImageDecodeSuccess(rawResult)
        } else {
            callback.onImageDecodeFailed()
        }
    }

    companion object {
        /**
         * @param filePath  文件路径
         * @param maxWidth  最大寬度
         * @param maxHeight 最大高度
         * @return bitmap
         */
        private fun getBitmap(filePath: String, maxWidth: Int, maxHeight: Int): Bitmap {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            options.inSampleSize =
                calculateInSampleSize(
                    options,
                    maxWidth,
                    maxHeight
                )
            options.inJustDecodeBounds = false
            var genbitmap = BitmapFactory.decodeFile(filePath, options)
            return genbitmap
        }

        /**
         * Return the sample size.
         * @param options    options.
         * @param maxWidth   maximum width.
         * @param maxHeight  maximum height.
         * @return the sample size
         */
        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            maxWidth: Int,
            maxHeight: Int
        ): Int {
            var height = options.outHeight
            var width = options.outWidth
            var inSampleSize = 1
            while (1.let { width = width shr it; width } >= maxWidth && 1.let {
                    height = height shr it; height
                } >= maxHeight) {
                inSampleSize = inSampleSize shl 1
            }
            return inSampleSize
        }
    }
}