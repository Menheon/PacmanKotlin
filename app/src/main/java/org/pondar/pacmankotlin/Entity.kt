package org.pondar.pacmankotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

// Class to emulate entities in the game.
open class Entity(context: Context, resourceId: Int) {
    var coordX = 0
    var coordY = 0
    var isConsumed = false
    var bitmap: Bitmap

    init {
        bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        bitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                bitmap.width / 2,
                bitmap.height / 2,
                false
            )
    }

    fun setCoordinates(x: Int, y: Int) {
        this.coordX = x
        this.coordY = y
    }
}
