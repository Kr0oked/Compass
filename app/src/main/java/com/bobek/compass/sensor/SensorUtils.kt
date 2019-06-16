package com.bobek.compass.sensor

object SensorUtils {

    @JvmStatic
    fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360.0f) % 360.0f
    }
}
