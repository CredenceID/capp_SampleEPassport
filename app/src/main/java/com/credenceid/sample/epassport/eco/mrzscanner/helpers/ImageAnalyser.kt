package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.credenceid.sample.epassport.App
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.ImageAnalyserOutput.*
import timber.log.Timber
import java.io.ByteArrayOutputStream

abstract class ImageAnalyser<ImageAnalyserOutput>(private val scannerOverlay: ScannerOverlay) :
    ImageAnalysis.Analyzer, LifecycleObserver {


    private val mutableLiveData = MutableLiveData<ImageAnalyserOutput>()
    fun liveData(): MutableLiveData<ImageAnalyserOutput> = mutableLiveData

    var emitDebugInfo: Boolean = true

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        try {
            val imageProxyReadyEpoch = System.currentTimeMillis()
            val rotation = imageProxy.imageInfo.rotationDegrees
            Timber.tag(App.TAG).d("New image from proxy width : ${imageProxy.width} height : ${imageProxy.height} format : ${imageProxy.format} rotation: $rotation")
            val scannerRect = getScannerRectToPreviewViewRelation(
                Size(imageProxy.width, imageProxy.height),
                rotation
            )

            val image = imageProxy.image!!
            val cropRect = image.getCropRectAccordingToRotation(scannerRect, rotation)
            image.cropRect = cropRect

            val byteArray = yuv420toNV21(image)
            val bitmap = getBitmap(
                byteArray,
                FrameMetadata(cropRect.width(), cropRect.height(), rotation)
            )
            Timber.tag(App.TAG).d("Bitmap prepared width: ${cropRect.width()} height: ${cropRect.height()}")
            val imagePreparedReadyEpoch = System.currentTimeMillis()

            mutableLiveData.postValue(
                ImagePreview(bitmap) as ImageAnalyserOutput
            )

            onBitmapPrepared(bitmap)

            val imageProcessedEpoch = System.currentTimeMillis()

            Timber.tag(App.TAG).d(
                    """
                   Image proxy (${imageProxy.width},${imageProxy.height}) format : ${imageProxy.format} rotation: $rotation 
                   Cropped Image (${bitmap.width},${bitmap.height}) Preparing took: ${imagePreparedReadyEpoch - imageProxyReadyEpoch}ms
                   OCR Processing took : ${imageProcessedEpoch - imagePreparedReadyEpoch}ms
                """.trimIndent()
                )

            imageProxy.close()
        } catch (e: Exception) {
            mutableLiveData.postValue(
                Error(e) as ImageAnalyserOutput
            )
        }
    }

    protected fun postResult(value: IcaoMrzResult?) {
        mutableLiveData.postValue(
            Success(value!!) as ImageAnalyserOutput
        )
    }

    private fun getScannerRectToPreviewViewRelation(
        proxySize: Size,
        rotation: Int
    ): ScannerRectToPreviewViewRelation {
        return when (rotation) {
            0, 180 -> {
                val size = scannerOverlay.size
                val width = size.width
                val height = size.height
                val previewHeight = width / (proxySize.width.toFloat() / proxySize.height)
                val heightDeltaTop = (previewHeight - height) / 2

                val scannerRect = scannerOverlay.scanRect
                val rectStartX = scannerRect.left
                val rectStartY = heightDeltaTop + scannerRect.top

                ScannerRectToPreviewViewRelation(
                    rectStartX / width,
                    rectStartY / previewHeight,
                    scannerRect.width() / width,
                    scannerRect.height() / previewHeight
                )
            }
            90, 270 -> {
                val size = scannerOverlay.size
                val width = size.width
                val height = size.height
                val previewWidth = height / (proxySize.width.toFloat() / proxySize.height)
                val widthDeltaLeft = (previewWidth - width) / 2

                val scannerRect = scannerOverlay.scanRect
                val rectStartX = widthDeltaLeft + scannerRect.left
                val rectStartY = scannerRect.top

                ScannerRectToPreviewViewRelation(
                    rectStartX / previewWidth,
                    rectStartY / height,
                    scannerRect.width() / previewWidth,
                    scannerRect.height() / height
                )
            }
            else -> throw IllegalArgumentException("Rotation degree ($rotation) not supported!")
        }
    }

    abstract fun onBitmapPrepared(bitmap: Bitmap)

    data class ScannerRectToPreviewViewRelation(
        val relativePosX: Float,
        val relativePosY: Float,
        val relativeWidth: Float,
        val relativeHeight: Float
    )

    private fun Image.getCropRectAccordingToRotation(
        scannerRect: ScannerRectToPreviewViewRelation,
        rotation: Int
    ): Rect {
        return when (rotation) {
            0 -> {
                val startX = (scannerRect.relativePosX * this.width).toInt()
                val numberPixelW = (scannerRect.relativeWidth * this.width).toInt()
                val startY = (scannerRect.relativePosY * this.height).toInt()
                val numberPixelH = (scannerRect.relativeHeight * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            90 -> {
                val startX = (scannerRect.relativePosY * this.width).toInt()
                val numberPixelW = (scannerRect.relativeHeight * this.width).toInt()
                val numberPixelH = (scannerRect.relativeWidth * this.height).toInt()
                val startY =
                    height - (scannerRect.relativePosX * this.height).toInt() - numberPixelH
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            180 -> {
                val numberPixelW = (scannerRect.relativeWidth * this.width).toInt()
                val startX =
                    (this.width - scannerRect.relativePosX * this.width - numberPixelW).toInt()
                val numberPixelH = (scannerRect.relativeHeight * this.height).toInt()
                val startY =
                    (height - scannerRect.relativePosY * this.height - numberPixelH).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            270 -> {
                val numberPixelW = (scannerRect.relativeHeight * this.width).toInt()
                val numberPixelH = (scannerRect.relativeWidth * this.height).toInt()
                val startX =
                    (this.width - scannerRect.relativePosY * this.width - numberPixelW).toInt()
                val startY = (scannerRect.relativePosX * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            else -> throw IllegalArgumentException("Rotation degree ($rotation) not supported!")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    abstract fun close()

    fun getBitmap(data: ByteArray, metadata: FrameMetadata): Bitmap {

        val image = YuvImage(
            data, ImageFormat.NV21, metadata.width, metadata.height, null
        )
        val stream = ByteArrayOutputStream()
        image.compressToJpeg(
            Rect(0, 0, metadata.width, metadata.height),
            80,
            stream
        )
        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
        stream.close()
        return rotateBitmap(bmp, metadata.rotation, false, false)
    }

    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    fun yuv420toNV21(image: Image): ByteArray {
        val crop = image.cropRect
        val format = image.format
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val data =
            ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
        val rowData = ByteArray(planes[0].rowStride)
        var channelOffset = 0
        var outputStride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> {
                    channelOffset = width * height + 1
                    outputStride = 2
                }
                2 -> {
                    channelOffset = width * height
                    outputStride = 2
                }
            }
            val buffer = planes[i].buffer
            val rowStride = planes[i].rowStride
            val pixelStride = planes[i].pixelStride
            val shift = if (i == 0) 0 else 1
            val shiftedWidth = width shr shift
            val shiftedHeight = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until shiftedHeight) {
                var length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = shiftedWidth
                    buffer[data, channelOffset, length]
                    channelOffset += length
                } else {
                    length = (shiftedWidth - 1) * pixelStride + 1
                    buffer[rowData, 0, length]
                    for (col in 0 until shiftedWidth) {
                        data[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < shiftedHeight - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }
        return data
    }
}
