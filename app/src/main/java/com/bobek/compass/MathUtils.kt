package com.bobek.compass

object MathUtils {

    @JvmStatic
    fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360.0f) % 360.0f
    }
}
