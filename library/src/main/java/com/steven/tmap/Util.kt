package com.steven.tmap

import android.graphics.Path
import android.graphics.Point
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

fun getDistance(points: List<PointF>): Float {
    var distance = 0f
    points.forEachIndexed { index, point ->
        if (index != points.size - 1) {
            distance += getDistance(point, points[index + 1])
        }
    }
    return distance
}

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

fun getShortestDistanceFromPointToLine(p: PointF, a: PointF, b: PointF): DistanceResult {
    val ap = PointF(p.x - a.x, p.y - a.y)
    val ab = PointF(b.x - a.x, b.y - a.y)
    val bp = PointF(p.x - b.x, p.y - b.y)

    val r = (ap.x * ab.x + ap.y * ab.y) / (ab.x.pow(2) + ab.y.pow(2))

    if (r <= 0) return DistanceResult(sqrt(ap.x.pow(2) + ap.y.pow(2)), isA = true)
    if (r >= 1) return DistanceResult(sqrt(bp.x.pow(2) + bp.y.pow(2)), isB = true)

    val px = a.x + ab.x * r
    val py = a.y + ab.y * r

    return DistanceResult(sqrt((p.x - px).pow(2) + (p.y - py).pow(2)))
}

data class DistanceResult(
    val distance: Float,
    val isA: Boolean = false,
    val isB: Boolean = false
)

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

fun getShortestIntersectionFromPointToLine(p: PointF, f: PointF, s: PointF): PointF {
    val d1 = getDistance(p, f)
    val d2 = getDistance(p, s)
    val d3 = getDistance(f, s)

    if (d1.pow(2) + d3.pow(2) < d2.pow(2)) return f
    if (d2.pow(2) + d3.pow(2) < d1.pow(2)) return s
    return getIntersectionFromPointToLine(p, f, s)
}

fun isObtuseAnglePointAndLine(point: PointF, linePoint1: PointF, linePoint2: PointF): Boolean {
    val a = getDistance(point, linePoint1)
    val b = getDistance(point, linePoint2)
    val c = getDistance(linePoint1, linePoint2)
    return a.pow(2) + c.pow(2) < b.pow(2) || b.pow(2) + c.pow(2) < a.pow(2)
}

/**
 * 判断点是否在多边形内
 */
fun isPointInPolygon(point: PointF, polygon: List<PointF>): Boolean {
    if (polygon.size < 3) return false
    var sum = 0
    var x1: Float
    var y1: Float
    var x2: Float
    var y2: Float
    var dx: Float
    for ((i, v) in polygon.withIndex()) {
        x1 = v.x
        y1 = v.y
        if (i == polygon.size - 1) {
            x2 = polygon[0].x
            y2 = polygon[0].y
        } else {
            x2 = polygon[i + 1].x
            y2 = polygon[i + 1].y
        }
        if ((point.y >= y1 && point.y < y2) || (point.y >= y2 && point.y < y1)) {
            if (abs(y1 - y2) > 0) {
                dx = x1 - ((x1 - x2) * (y1 - point.y) / (y1 - y2))
                if (dx < point.x) sum++
            }
        }
    }
    return sum % 2 != 0
}

fun getPath(points: List<PointF>): Path = Path().apply {
    if (points.isNotEmpty()) {
        moveTo(points.first().x, points.first().y)
        points.forEach {
            lineTo(it.x, it.y)
        }
    }
}


