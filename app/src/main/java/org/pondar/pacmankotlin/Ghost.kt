package org.pondar.pacmankotlin;

import android.content.Context

// Class to emulate the ghost enemy entity that chases pac-man.
class Ghost(context: Context, resourceId: Int) : Entity(context, resourceId) {
    var isVulnerable = false

    // Moves the ghost a specific amount of pixels to the right.
    fun moveGhostRight(pixels: Int, boundaryWidth: Int) {
        var speed = 0
        if (!isVulnerable && !isConsumed)
            speed = pixels
        else if (isVulnerable && !isConsumed)
            speed = pixels * 2
        if (coordX + speed + bitmap.width < boundaryWidth) {
            coordX += speed
        }
    }

    // Moves the ghost a specific amount of pixels to the left.
    fun moveGhostLeft(pixels: Int) {
        var speed = 0
        if (!isVulnerable && !isConsumed)
            speed = pixels
        else if (isVulnerable && !isConsumed)
            speed = pixels * 2
        if (coordX - speed > 0) {
            coordX -= speed
        }
    }

    // Moves the ghost a specific amount of pixels downwards.
    fun moveGhostDown(pixels: Int, boundaryHeight: Int) {
        var speed = 0
        if (!isVulnerable && !isConsumed)
            speed = pixels
        else if (isVulnerable && !isConsumed)
            speed = pixels * 2
        if (coordY + speed + bitmap.height < boundaryHeight) {
            coordY += speed
        }
    }

    // Moves the pac man a specific amount of pixels upwards.
    fun moveGhostUp(pixels: Int) {
        var speed = 0
        if (!isVulnerable && !isConsumed)
            speed = pixels
        else if (isVulnerable && !isConsumed)
            speed = pixels * 2
        if (coordY - speed > 0) {
            coordY -= speed
        }
    }
}
