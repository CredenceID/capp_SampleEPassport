package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.* // ktlint-disable no-wildcard-imports
import android.util.AttributeSet
import android.util.Size
import android.view.View
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.credenceid.sample.epassport.App.Companion.isPortrait
import com.credenceid.sample.epassport.R
import kotlin.math.min

class ScannerOverlayImpl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ScannerOverlay {

    private val transparentPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private val strokePaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#9A8BD5")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
    }

    private var graphicBlocks: List<GraphicBlock>? = null

    init {
        setWillNotDraw(false)
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.ScannerOverlayImpl,
            0,
            0
        )
        typedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#88000000"))

        val radius = 4f
        val rectF = scanRect
        canvas.drawRoundRect(rectF, radius, radius, transparentPaint)
        strokePaint.color = Color.parseColor("#9A8BD5")
        canvas.drawRoundRect(rectF, radius, radius, strokePaint)

        graphicBlocks?.forEach { block ->
            val scaleX = scanRect.width() / block.bitmapSize.width
            val scaleY = scanRect.height() / block.bitmapSize.height

            canvas.withTranslation(scanRect.left, scanRect.top) {
                withScale(scaleX, scaleY) {
                    drawRoundRect(RectF(block.textBlock.boundingBox), radius, radius, strokePaint)
                }
            }
        }
        graphicBlocks = null
    }

    override val size: Size
        get() = Size(width, height)

    override val scanRect: RectF
        get() =
            if (context.isPortrait()) {
                val rectW = min(width * 0.95f, MAX_WIDTH_PORTRAIT)
                val l = (width - rectW) / 2
                val r = width - l
                val rectH = rectW / 4f
                val t = height * 0.3f
                val b = t + rectH
                RectF(l, t, r, b)
            } else {
                val rectW = min(width * 0.4f, MAX_WIDTH_LANDSCAPE)
                val l = width * 0.05f
                val r = l + rectW
                val rectH = rectW / 1.5f
                val t = height * 0.05f
                val b = t + rectH
                RectF(l, t, r, b)
            }

    data class GraphicBlock(val textBlock: BlockWrapper, val bitmapSize: Size)

    companion object {
        const val MAX_WIDTH_PORTRAIT = 1200f
        const val MAX_WIDTH_LANDSCAPE = 1600f
    }
}
