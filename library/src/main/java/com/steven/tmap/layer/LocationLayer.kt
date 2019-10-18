package com.steven.tmap.layer

import android.graphics.*

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/17
 */
class LocationLayer : BaseLayer() {

    init {
        level = LEVEL_LOCATION
    }

    var locationColor = Color.parseColor("#FF5495e6")
        set(value) {
            mPaint.color = value
            field = value
        }
    var locationRadius = 12F

    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            setShadowLayer(8F, 0F, 4F, Color.LTGRAY)
        }
    }
    private var currentLocation: PointF? = null

    override fun draw(canvas: Canvas, matrix: Matrix, zoom: Float, rotate: Float) {
        if (!visable) return
        currentLocation?.let {
            val location = floatArrayOf(it.x, it.y)
            matrix.mapPoints(location)
            canvas.save()
            mPaint.color = Color.WHITE
            canvas.drawCircle(location[0], location[1], locationRadius * 1.2F, mPaint)
            mPaint.color = locationColor
            canvas.drawCircle(location[0], location[1], locationRadius, mPaint)
            canvas.restore()
        }
    }

    override fun onTouch(point: FloatArray, matrix: Matrix) {}

    fun setCurrentLocation(location: PointF) {
        this.currentLocation = location
        onActionListener?.onPostRefresh()
    }

    fun getCurrentLocation(): PointF? = currentLocation
}