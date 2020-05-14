package com.steven.tmap

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.*

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/16
 */

val MAIN by lazy { Handler(Looper.getMainLooper()) }

val IO: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

fun getDistance(p1: PointF, p2: PointF) = sqrt(
    (p1.x - p2.x).toDouble().pow(2.0) + (p1.y - p2.y).toDouble().pow(2.0)
).toFloat()

fun getDistance(p1: FloatArray, p2: FloatArray) = sqrt(
    (p1[0] - p2[0]).toDouble().pow(2.0) + (p1[1] - p2[1]).toDouble().pow(2.0)
).toFloat()

fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt(
    (x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0)
).toFloat()

fun getIncludedAngleToXAxis(start: PointF, end: PointF): Float {
    var angle = 90.0f
    if (start.x != end.x) {
        angle = Math.toDegrees(
            atan(((end.y - start.y) / (end.x - start.x)).toDouble())
        ).toFloat()
        if (end.x < start.x && end.y >= start.y) {
            angle += 180.0f
        } else if (end.x < start.x && end.y > start.y) {
            angle -= 180f
        }
    } else {
        if (start.y < end.y) {
            angle = 90.0f
        } else if (start.y > end.y) {
            angle = -90.0f
        }
    }
    return angle
}

fun getIncludedAngleToYAxis(start: PointF, end: PointF): Float {
    var angle = 90.0f
    if (start.y != end.y) {
        angle = (-Math.toDegrees(
            atan(((end.x - start.x) / (end.y - start.y)).toDouble())
        )).toFloat()
        if (end.y > start.y && end.x >= start.x) {
            angle += 180.0f
        } else if (end.y > start.y && end.x > start.x) {
            angle -= 180f
        }
    } else {
        if (start.x < end.x) {
            angle = 90.0f
        } else if (start.x > end.x) {
            angle = -90.0f
        }
    }
    return angle
}

/**
 *  get degree between two points
 */
fun getDegreeBetweenTwoPoints(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float
): Float {
    val radians = atan2(y1 - y2.toDouble(), x1 - x2.toDouble())
    return Math.toDegrees(radians).toFloat()
}

fun getMiddlePoint(p1: PointF, p2: PointF) = PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)

fun getDistanceFromPointToLine(p: PointF, linePoint1: PointF, linePoint2: PointF): Float {
    return if (linePoint1.x != linePoint2.x) { // with slope
        val k = (linePoint2.y - linePoint1.y) / (linePoint2.x - linePoint1.x)
        val b = linePoint2.y - k * linePoint2.x
        abs(k * p.x - p.y + b) / sqrt(k * k + 1.toDouble()).toFloat()
    } else { // no slope
        abs(p.x - linePoint1.x)
    }
}

fun getIntersectionFromPointToLine(p: PointF, linePoint1: PointF, linePoint2: PointF): PointF {
    val x: Float
    val y: Float
    if (linePoint1.x != linePoint2.x) { // with slope
        val k = (linePoint2.y - linePoint1.y) / (linePoint2.x - linePoint1.x)
        val b = linePoint2.y - k * linePoint2.x
        // The equation of point
        if (k != 0f) {
            val kV = -1 / k
            val bV: Float = p.y - kV * p.x
            x = (b - bV) / (kV - k)
            y = kV * x + bV
        } else {
            x = p.x
            y = linePoint1.y
        }
    } else { // no slope
        x = linePoint1.x
        y = p.y
    }
    return PointF(x, y)
}

fun isObtuseAnglePointAndLine(point: PointF, linePoint1: PointF, linePoint2: PointF): Boolean {
    val a = getDistance(point, linePoint1)
    val b = getDistance(point, linePoint2)
    val c = getDistance(linePoint1, linePoint2)
    return a.pow(2) + c.pow(2) < b.pow(2) || b.pow(2) + c.pow(2) < a.pow(2)
}