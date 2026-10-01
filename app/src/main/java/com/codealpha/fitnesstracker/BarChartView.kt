package com.codealpha.fitnesstracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/** Simple bar chart that scales to any screen size (all sizes in dp/sp). */
class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: List<Float> = emptyList()
    private var labels: List<String> = emptyList()
    private val dm = resources.displayMetrics
    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, dm)
    private fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, dm)

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2E7D32.toInt() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF555555.toInt(); textSize = sp(11f); textAlign = Paint.Align.CENTER
    }
    private val rect = RectF()

    fun setData(v: List<Float>, l: List<String>) {
        values = v; labels = l; invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val n = values.size
        if (n == 0) return
        val maxV = (values.maxOrNull() ?: 0f).let { if (it <= 0f) 1f else it }
        val top = dp(22f)
        val bottom = height - dp(24f)
        val slot = width.toFloat() / n
        val barW = slot * 0.55f
        val avail = (bottom - top).coerceAtLeast(1f)
        for (i in 0 until n) {
            val cx = slot * i + slot / 2f
            val h = (avail * values[i] / maxV).coerceAtLeast(dp(2f))
            rect.set(cx - barW / 2f, bottom - h, cx + barW / 2f, bottom)
            canvas.drawRoundRect(rect, dp(6f), dp(6f), barPaint)
            if (values[i] > 0f) canvas.drawText(values[i].toInt().toString(), cx, bottom - h - dp(4f), textPaint)
            if (i < labels.size) canvas.drawText(labels[i], cx, height - dp(6f), textPaint)
        }
    }
}
