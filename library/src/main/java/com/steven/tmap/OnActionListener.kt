package com.steven.tmap

import android.graphics.PointF

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/16
 */
interface OnActionListener {

    fun onPostRefresh()

    fun onRotateRequired(p1: PointF, p2: PointF, centerToPoint: PointF? = null)
}