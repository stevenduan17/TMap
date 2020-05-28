package com.steven.tmap.layer

import android.graphics.*
import com.steven.tmap.IO
import com.steven.tmap.MAIN
import com.steven.tmap.getDistance

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

    //navigate line for whole map direct.
    var navigateLine: List<PointF>? = null

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

    fun getCurrentLocation(): PointF? = currentLocation

    fun setCurrentLocation(
        location: PointF,
        centerToLocation: Boolean = false,
        rotateCallback: ((degree: Double) -> Unit)? = null
    ) {
        this.currentLocation = location
        getNavigationPoints(location, centerToLocation, rotateCallback)
        onActionListener?.onPostRefresh()
    }

    private fun getNavigationPoints(
        location: PointF,
        centerToLocation: Boolean = false,
        rotateCallback: ((degree: Double) -> Unit)?
    ) {
        if (navigateLine.isNullOrEmpty() || navigateLine?.size ?: 0 < 2) return
        IO.execute {
            var minDistance = getDistance(location, navigateLine!![0])
            var minIndex = 0
            for (i in 1 until navigateLine!!.size) {
                val d = getDistance(location, navigateLine!![i])
                if (d < minDistance) {
                    minIndex = i
                    minDistance = d
                }
            }
            if (minIndex + 1 > navigateLine!!.size - 1) return@execute
            if (minDistance < 100F) {
                MAIN.post {
                    onActionListener?.onRotateRequired(
                        navigateLine!![minIndex],
                        navigateLine!![minIndex + 1],
                        if (centerToLocation) location else null,
                        rotateCallback
                    )
                }
            }
        }
    }
}