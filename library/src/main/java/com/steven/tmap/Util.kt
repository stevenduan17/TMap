package com.steven.tmap

import android.graphics.PointF
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/16
 */

val IO: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

fun getDistance(p1: PointF, p2: PointF) = sqrt(
    (p1.x - p2.x).toDouble().pow(2.0) + (p1.y - p2.y).toDouble().pow(2.0)
).toFloat()

fun getDistance(p1: FloatArray, p2: FloatArray) = sqrt(
    (p1[0] - p2[0]).toDouble().pow(2.0) + (p1[1] - p2[1]).toDouble().pow(2.0)
).toFloat()