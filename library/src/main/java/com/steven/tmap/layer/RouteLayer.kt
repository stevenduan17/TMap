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
    private var mPaths: HashMap<Path, Int>?,
    private var startIcon: Pair<Bitmap, PointF>? = null,
    private var endIcon: Pair<Bitmap, PointF>? = null
) : BaseLayer() {

    init {
        level = LEVEL_ROUTE
    }

    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
        startIcon?.let {
            canvas.drawBitmap(
                it.first, it.second.x - it.first.width / 2F,
                it.second.y - it.first.height / 2F,
                mIconPaint
            )
        }
        mPaths?.keys?.forEach {
            mPaths?.get(it)?.let { color -> mPaint.color = color }
            canvas.drawPath(it, mPaint)
        }
        endIcon?.let {
            canvas.drawBitmap(
                it.first, it.second.x - it.first.width / 2F,
                it.second.y - it.first.height / 2F,
                mIconPaint
            )
        }
        canvas.restore()
    }

    override fun onTouch(point: FloatArray, matrix: Matrix) {
    }

    fun updatePaths(
        paths: HashMap<Path, Int>,
        ignoreEnds: Boolean = false,
        startIcon: Pair<Bitmap, PointF>? = null,
        endIcon: Pair<Bitmap, PointF>? = null
    ) {
        this.mPaths = paths
        if (!ignoreEnds) {
            this.startIcon = startIcon
            this.endIcon = endIcon
        }
        onActionListener?.onPostRefresh()
    }

    fun clearPaths() {
        this.mPaths = null
        onActionListener?.onPostRefresh()
    }
}