package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

class BlockWrapper(val gmsTextBLock : Text.TextBlock? = null) {
    val boundingBox : Rect = gmsTextBLock!!.boundingBox!!
    val text : String = gmsTextBLock!!.text
}
