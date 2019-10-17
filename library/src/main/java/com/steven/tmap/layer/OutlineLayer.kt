package com.steven.tmap.layer

import android.graphics.*

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/9
 */
class OutlineLayer : BaseLayer() {

    private var mPath: Path? = null
    private val mPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    override fun draw(canvas: Canvas, matrix: Matrix, zoom: Float, rotate: Float) {
        mPath?.let {
            if (visable) {
                canvas.save()
                canvas.matrix = matrix
                canvas.drawPath(it, mPaint)
                canvas.restore()
            }
        }
    }

    override fun onTouch(point: FloatArray, matrix: Matrix) {
    }

    fun setOutline(path: Path, color: Int, width: Float) {
        this.mPath = path
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
        return RectF(0f, 0f, 300f, 600f)
    }
}