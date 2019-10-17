package com.steven.tmap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.steven.tmap.layer.BaseLayer
import com.steven.tmap.layer.OutlineLayer
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/9
 */
class TMap @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SurfaceView(context, attrs, defStyle), SurfaceHolder.Callback, OnActionListener {

    companion object {
        private const val TOUCH_STATE_REST = 0
        private const val TOUCH_STATE_SCROLL = 1
        private const val TOUCH_STATE_SCALE = 2
        private const val TOUCH_STATE_ROTATE = 3
        private const val TOUCH_STATE_POINTED = 4

        private const val MIN_ZOOM = 0.5F
        private const val MAX_ZOOM = 2F
    }

    private val TAG = "TMAP"
    private var isOutlineLoad = false
    private var mLayers: MutableList<BaseLayer>
    private val currentMatrix =  Matrix()
    private var currentZoom = 1F
    private var currentRotate = 0F
    private var outlineLayer: OutlineLayer? = null
    private var surfaceHeight = 0
    private var surfaceWidth = 0

    private val mTouchMatrix = Matrix()
    private var mTouchZoom = 0F
    private var mTouchRotate = 0F
    private val mStartPoint = PointF()
    private var mMiddlePoint = PointF()
    private var mStartDistance = 0F
    private var mStartRotate = 0F
    private var mLastDistance = 0F
    private var mLastRotate = 0F

    private var currentTouchState = TOUCH_STATE_REST

    init {
        holder.addCallback(this)
        mLayers = object : ArrayList<BaseLayer>() {
            override fun add(element: BaseLayer): Boolean {
                if (this.isNotEmpty()) {
                    if (element.level >= this.last().level) {
                        super.add(element)
                    } else {
                        for (i in 0 until this.size) {
                            if (element.level < this[i].level) {
                                super.add(i, element)
                                break
                            }
                        }
                    }
                }
                return super.add(element)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        draw()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        this.surfaceHeight = height
        this.surfaceWidth = width
        drawToCenter()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isOutlineLoad) return false

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouchMatrix.set(currentMatrix)
                mStartPoint.set(event.x, event.y)
                currentTouchState = TOUCH_STATE_SCROLL
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    mTouchMatrix.set(currentMatrix)
                    mTouchZoom = currentZoom
                    mTouchRotate = currentRotate
                    mStartPoint.set(event.getX(0), event.getY(0))
                    currentTouchState = TOUCH_STATE_POINTED

                    setMiddlePoint(event)
                    mStartDistance = getDistance(event, mMiddlePoint)
                    mStartRotate = getRotate(event, mMiddlePoint)
                }
            }
            MotionEvent.ACTION_UP -> {
                for (layer in mLayers) {
                    layer.onTouch(floatArrayOf(event.x, event.y), currentMatrix)
                }
                currentTouchState = TOUCH_STATE_REST
            }
            MotionEvent.ACTION_POINTER_UP -> {
                currentTouchState = TOUCH_STATE_REST
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentTouchState == TOUCH_STATE_SCROLL) {
                    currentMatrix.set(mTouchMatrix)
                    currentMatrix.postTranslate(event.x - mStartPoint.x, event.y - mStartPoint.y)
                    draw()
                } else if (currentTouchState == TOUCH_STATE_POINTED) {
                    currentMatrix.set(mTouchMatrix)
                    mLastDistance = getDistance(event, mMiddlePoint)
                    mLastRotate = getRotate(event, mMiddlePoint)

                    val rotate = mLastRotate - mStartRotate
                    var scale = mLastDistance / mStartDistance
                    if (scale * mTouchZoom < MIN_ZOOM) {
                        scale = MIN_ZOOM / mTouchZoom
                    } else if (scale * mTouchZoom > MAX_ZOOM) {
                        scale = MAX_ZOOM / mTouchZoom
                    }
                    currentZoom = scale * mTouchZoom
                    currentRotate = (mLastRotate - mStartRotate + currentRotate) % 360F
                    currentMatrix.postScale(scale, scale, mMiddlePoint.x, mMiddlePoint.y)
                    currentMatrix.postRotate(rotate, mMiddlePoint.x, mMiddlePoint.y)
                    draw()
                }
            }
        }
        return true
    }

    override fun onPostRefresh() = draw()

    private fun getRotate(event: MotionEvent, p: PointF) = Math.toDegrees(
        atan2((event.y - p.y).toDouble(), (event.x - p.x).toDouble())
    ).toFloat()

    private fun setMiddlePoint(event: MotionEvent) {
        mMiddlePoint.set(
            (event.getX(0) + event.getX(1)) / 2,
            (event.getY(0) + event.getY(1)) / 2
        )
    }

    private fun getDistance(event: MotionEvent, p: PointF) = getDistance(
        PointF(event.x, event.y), p
    )

    /***
     * draw all layers.
     */
    private fun draw() {
        val canvas = holder.lockCanvas()
        canvas?.let {
            it.drawColor(-1)
            if (isOutlineLoad) {
                for (layer in mLayers) {
                    if (layer.visable) {
                        layer.draw(it, currentMatrix, currentZoom, currentRotate)
                    }
                }
            }
            holder.unlockCanvasAndPost(it)
        }
    }

    /**
     * Move layers to center.
     */
    private fun drawToCenter(horizontal: Boolean = true, vertical: Boolean = true) {
        outlineLayer?.let {
            val matrix = Matrix()
            matrix.set(currentMatrix)
            val scope = it.getInitialScope()
            matrix.mapRect(scope)
            val oHeight = scope.height()
            val oWidth = scope.width()
            var dx = 0F
            var dy = 0F

            if (horizontal) {
                dx = (this.surfaceWidth - oWidth) / 2 - scope.left
            }
            if (vertical) {
                dy = (this.surfaceHeight - oHeight) / 2 - scope.top
            }

            //TODO  暂时不缩放 不旋转

            currentMatrix.postTranslate(dx, dy)
        }
        draw()
    }

    fun addLayer(layer: BaseLayer) {
        if (this.mLayers.isEmpty() && layer !is OutlineLayer) {
            throw  IllegalStateException("OutlineLayer should be added first.")
        }
        layer.onActionListener = this
        this.mLayers.add(layer)
        if (layer is OutlineLayer) {
            this.outlineLayer = layer
            drawToCenter()
            isOutlineLoad = true
        } else {
            draw()
        }
    }

    fun isOutlineLoad() = this.isOutlineLoad

    fun getLayers() = this.mLayers

    fun setCurrentZoom(
        zoom: Float,
        centerX: Float = surfaceWidth / 2F,
        centerY: Float = surfaceHeight / 2F
    ) {
        val z = zoom / currentZoom
        currentMatrix.postScale(z, z, centerX, centerY)
        currentZoom = zoom
    }

    fun getCurrentZoom() = this.currentZoom

    fun setCurrentRotate(
        rotateDegree: Float,
        centerX: Float = surfaceWidth / 2F,
        centerY: Float = surfaceHeight / 2F
    ) {
        currentMatrix.postRotate(rotateDegree - currentRotate, centerX, centerY)
        currentRotate = rotateDegree % 360
        if (currentRotate < 0) currentRotate += 360F
    }

    fun getCurrentRotate() = this.currentRotate

    fun translate(x: Float, y: Float) {
        this.currentMatrix.postTranslate(x, y)
    }

    /**
     * Move map center to real point [dest].
     */
    fun centerToPoint(dest: FloatArray) {
        currentMatrix.mapPoints(dest)
        currentMatrix.postTranslate(surfaceWidth / 2F - dest[0], surfaceHeight / 2F - dest[1])
    }

    fun getRealCoordinateWithScreenXY(x: Float, y: Float): FloatArray {
        val invertMatrix = Matrix()
        val coordinate = floatArrayOf(x, y)
        currentMatrix.invert(invertMatrix)
        invertMatrix.mapPoints(coordinate)
        return coordinate
    }
}