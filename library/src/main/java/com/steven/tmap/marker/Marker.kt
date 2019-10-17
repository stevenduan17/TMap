package com.steven.tmap.marker

import android.graphics.Bitmap
import android.graphics.PointF
import java.util.*

/**
 * @author Steven
 * @version 1.0
 * @since 2019/10/16
 */
data class Marker(
    val position: PointF,
    val icon: Bitmap,
    var desc: String? = null,
    var extra: String? = null,
    val id: String = UUID.randomUUID().toString()
) {

    override fun toString(): String {
        return "Marker(position=$position, desc=$desc, extra=$extra, id='$id')"
    }
}