package com.steven.tmap.layer

import android.graphics.*

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/9
 */
class OutlineLayer : BaseLayer() {

    private var mPaths: MutableList<Path>? = null
    private val mPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    var onClick: ((screenPoint: FloatArray) -> Unit)? = null

    override fun draw(canvas: Canvas, matrix: Matrix, zoom: Float, rotate: Float) {
        if (visable) {
            canvas.save()
            canvas.matrix = matrix
            mPaths?.forEach {
                canvas.drawPath(it, mPaint)
            }
            canvas.restore()
        }
    }

    override fun onTouch(point: FloatArray, matrix: Matrix) {
        onClick?.invoke(point)
    }

    fun setOutline(paths: MutableList<Path>, color: Int, width: Float) {
        this.mPaths = paths
        this.mPaint.apply {
            setColor(color)
            strokeWidth = width
            style = Paint.Style.STROKE
        }
    }

    /**
     * Get initial scope.
     */
    fun getInitialScope(): RectF {
        return RectF(45f, 21f, 668f, 819f)
    }
}