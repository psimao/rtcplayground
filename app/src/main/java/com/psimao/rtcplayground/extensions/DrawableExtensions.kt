package com.psimao.rtcplayground.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

fun Drawable.createBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).let {
        setBounds(0, 0, it.width, it.height)
        draw(it)
    }
    return bitmap
}