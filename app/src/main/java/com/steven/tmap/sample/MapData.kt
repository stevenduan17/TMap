package com.steven.tmap.sample

import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/12
 */

val outline = mutableListOf(
    Path().apply {
        moveTo(45f, 816f)
        lineTo(44f, 334f)
        lineTo(160f, 21f)
        lineTo(331f, 54f)
        lineTo(225f, 342f)
        lineTo(223f, 478f)
        lineTo(433f, 477f)
        lineTo(566f, 256f)
        lineTo(668f, 352f)
        lineTo(529f, 586f)
        lineTo(226f, 586f)
        lineTo(227f, 819f)
    },
    Path().apply {
        moveTo(130f, 818f)
        lineTo(130f, 336f)
        lineTo(168f, 235f)
    },
    Path().apply {
        moveTo(176f, 210f)
        lineTo(230f, 67f)
    },
    Path().apply {
        moveTo(226f, 525f)
        lineTo(472f, 522f)
        lineTo(599f, 324f)
    }
)

val flags = mutableListOf(
    PointF(89f, 850f),
    PointF(89f, 334f),
    PointF(131f, 215f),
    PointF(196f, 39f),
    PointF(285f, 56f),
    PointF(216f, 230f),
    PointF(179f, 339f),
    PointF(179f, 502f),
    PointF(450f, 498f),
    PointF(576f, 295f),
    PointF(634f, 348f),
    PointF(495f, 556f),
    PointF(179f, 556f),
    PointF(179f, 850f)
)

val topology = mutableListOf(
    Point(0,1),
    Point(0,13),
    Point(1,2),
    Point(2,3),
    Point(2,5),
    Point(3,4),
    Point(4,5),
    Point(5,6),
    Point(6,7),
    Point(7,8),
    Point(7,12),
    Point(8,9),
    Point(9,10),
    Point(10,11),
    Point(11,12),
    Point(12,13)
)