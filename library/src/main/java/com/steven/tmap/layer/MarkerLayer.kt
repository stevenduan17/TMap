package com.steven.tmap.layer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import com.steven.tmap.IO
import com.steven.tmap.marker.Marker
import com.steven.tmap.marker.OnMarkerCheckedListener
import com.steven.tmap.getDistance

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/16
 */
class MarkerLayer(private val activeIcon: Bitmap) : BaseLayer() {

    init {
        level = LEVEL_MARKER
    }

    var onMarkerCheckedListener: OnMarkerCheckedListener? = null

    private var mMarkers: MutableList<Marker>? = null
    private var mActiveRadius = 20F
    private var mActiveMarker: Marker? = null
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }
    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    override fun draw(canvas: Canvas, matrix: Matrix, zoom: Float, rotate: Float) {
        if (!visable) return
        mMarkers?.let {
            canvas.save()
            for (marker in it) {
                val position = floatArrayOf(marker.position.x, marker.position.y)
                matrix.mapPoints(position)
                val icon = marker.icon
                canvas.drawBitmap(
                    icon,
                    position[0] - icon.width / 2F,
                    position[1] - icon.height / 2F,
                    mPaint
                )
                if (marker.id == mActiveMarker?.id) {
                    canvas.drawBitmap(
                        activeIcon,
                        position[0] - icon.width / 2F,
                        position[1] - icon.height / 2F,
                        mPaint
                    )
                }
            }
            canvas.restore()
        }
    }

    override fun onTouch(point: FloatArray, matrix: Matrix) {
        mMarkers?.let {
            IO.execute {
                for (marker in it) {
                    val position = floatArrayOf(marker.position.x, marker.position.y)
                    matrix.mapPoints(position)
                    val distance = getDistance(point, position)
                    if (distance <= mActiveRadius) {
                        this.mActiveMarker = marker
                        mHandler.post {
                            onActionListener?.onPostRefresh()
                            onMarkerCheckedListener?.onMarkerChecked(marker)
                        }
                        break
                    }
                }
            }
        }
    }

    fun setActiveRadius(radius: Float) {
        this.mActiveRadius = radius
    }

    fun addMarker(marker: Marker) {
        if (this.mMarkers == null) this.mMarkers = mutableListOf()
        this.mMarkers?.add(marker)
        onActionListener?.onPostRefresh()
    }

    fun addMarkers(markers: List<Marker>) {
        if (this.mMarkers == null) this.mMarkers = mutableListOf()
        this.mMarkers?.addAll(markers)
        onActionListener?.onPostRefresh()
    }
}