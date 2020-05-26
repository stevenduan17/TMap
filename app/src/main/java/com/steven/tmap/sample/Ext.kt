package com.steven.tmap.sample

import android.content.Context
import android.widget.Toast

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/22
 */

fun Context.toast(msg: String) {
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}