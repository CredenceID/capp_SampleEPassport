package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.graphics.Bitmap

sealed class ImageAnalyserOutput{
    data class Success(val result: IcaoMrzResult) : ImageAnalyserOutput()
    data class ImagePreview(val preview: Bitmap): ImageAnalyserOutput()
    data class Error(val exception: Exception): ImageAnalyserOutput()
}
