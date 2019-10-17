package com.steven.tmap.layer

import android.graphics.Canvas
import android.graphics.Matrix
import com.steven.tmap.OnActionListener

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/9
 */
abstract class BaseLayer {

    companion object {
        const val LEVEL_OUTLINE = 0
        const val LEVEL_MARKER = 1
        const val LEVAL_LOCATION = 2
    }

    var level: Int = LEVEL_OUTLINE

    var visable: Boolean = true

    var onActionListener: OnActionListener? = null

    abstract fun draw(canvas: Canvas, matrix: Matrix, zoom: Float, rotate: Float)

    abstract fun onTouch(point: FloatArray, matrix: Matrix)
}