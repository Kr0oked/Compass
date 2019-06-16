package com.bobek.compass.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class SensorUtilsTest {

    @Test
    fun normalizeAngleKeepsValue() {
        assertEquals(0.0f, SensorUtils.normalizeAngle(0.0f))
        assertEquals(90.0f, SensorUtils.normalizeAngle(90.0f))
        assertEquals(180.0f, SensorUtils.normalizeAngle(180.0f))
        assertEquals(270.0f, SensorUtils.normalizeAngle(270.0f))
    }

    @Test
    fun normalizeAngleHandlesValuesAboveTheRange() {
        assertEquals(0.0f, SensorUtils.normalizeAngle(360.0f))
        assertEquals(90.0f, SensorUtils.normalizeAngle(450.0f))
        assertEquals(180.0f, SensorUtils.normalizeAngle(540.0f))
        assertEquals(270.0f, SensorUtils.normalizeAngle(630.0f))
    }

    @Test
    fun normalizeAngleHandlesValuesUnderTheRange() {
        assertEquals(270.0f, SensorUtils.normalizeAngle(-90.0f))
        assertEquals(180.0f, SensorUtils.normalizeAngle(-180.0f))
        assertEquals(90.0f, SensorUtils.normalizeAngle(-270.0f))
        assertEquals(0.0f, SensorUtils.normalizeAngle(-360.0f))
    }

    @Test
    fun normalizeAnglePrecision() {
        val delta = 0.0001f

        assertEquals(0.0123456789f, SensorUtils.normalizeAngle(360.0123456789f), delta)
        assertEquals(90.0123456789f, SensorUtils.normalizeAngle(450.0123456789f), delta)
        assertEquals(180.0123456789f, SensorUtils.normalizeAngle(540.0123456789f), delta)
        assertEquals(270.0123456789f, SensorUtils.normalizeAngle(630.0123456789f), delta)
    }

    @Test
    fun normalizeAngleHandlesSpecialCases() {
        assertEquals(0.0f, SensorUtils.normalizeAngle(Float.MIN_VALUE))
        assertEquals(0.0f, SensorUtils.normalizeAngle(Float.MAX_VALUE))
        assertEquals(Float.NaN, SensorUtils.normalizeAngle(Float.NEGATIVE_INFINITY))
        assertEquals(Float.NaN, SensorUtils.normalizeAngle(Float.POSITIVE_INFINITY))
        assertEquals(Float.NaN, SensorUtils.normalizeAngle(Float.NaN))
    }
}
