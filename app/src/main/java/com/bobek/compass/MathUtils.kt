package com.bobek.compass

object MathUtils {

    @JvmStatic
    fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 720) % 360
    }
}
