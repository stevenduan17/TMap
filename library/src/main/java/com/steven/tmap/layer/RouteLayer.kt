package com.steven.tmap.layer

import android.graphics.*

/**
 * 路线导航图层
 *
 * @author Steven
 * @version 1.0
 * @since 2020/5/13
 */
class RouteLayer(
    private var mPath: Path,
    private val startIcon: Bitmap,
    private val startPosition: PointF,
    private val endIcon: Bitmap,
    private val endPosition: PointF
) : BaseLayer() {

    init {
        level = LEVEL_ROUTE
    }


    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2DB7A2")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
    }
    private val mIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }

    override fun draw(canvas: Canvas, matrix: Matrix, zoom: Float, rotate: Float) {
        if (!visable) return
        canvas.save()
        canvas.matrix = matrix
        canvas.drawBitmap(
            startIcon, startPosition.x - startIcon.width / 2F,
            startPosition.y - startIcon.height / 2F,
            mIconPaint
        )
        canvas.drawPath(mPath, mPaint)
        canvas.drawBitmap(
            endIcon, endPosition.x - endIcon.width / 2F,
            endPosition.y - endIcon.height / 2F,
            mIconPaint
        )
        canvas.restore()
    }

    override fun onTouch(point: FloatArray, matrix: Matrix) {
    }
}