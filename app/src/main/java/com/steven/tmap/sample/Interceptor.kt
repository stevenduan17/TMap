package com.steven.tmap.sample

import android.graphics.PointF
import com.steven.tmap.getMiddlePoint

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/20
 */

fun getIntercptedRoute(list: List<PointF>): List<PointF> {
    val result = mutableListOf<PointF>()
    for ((k, v) in list.withIndex()) {
        if (k != list.size - 1) {
            insert(v, list[k + 1], result)
        } else {
            result.add(v)
        }
    }
    return result
}

fun insert(p1: PointF, p2: PointF, result: MutableList<PointF>) {
    val center = getMiddlePoint(p1, p2)
    val c1 = getMiddlePoint(p1, center)
    val c2 = getMiddlePoint(center, p2)
    result.add(p1)
    result.add(c1)
    result.add(center)
    result.add(c2)
}