package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.graphics.RectF
import android.util.Size

interface ScannerOverlay {
    val size : Size
    val scanRect : RectF
}
