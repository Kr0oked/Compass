package com.bobek.compass

import org.junit.Assert.assertEquals
import org.junit.Test

class MathUtilsTest {

    @Test
    fun normalizeAngleKeepsValue() {
        assertEquals(0.0f, MathUtils.normalizeAngle(0.0f))
        assertEquals(90.0f, MathUtils.normalizeAngle(90.0f))
        assertEquals(180.0f, MathUtils.normalizeAngle(180.0f))
        assertEquals(270.0f, MathUtils.normalizeAngle(270.0f))
    }

    @Test
    fun normalizeAngleHandlesValuesAboveTheRange() {
        assertEquals(0.0f, MathUtils.normalizeAngle(360.0f))
        assertEquals(90.0f, MathUtils.normalizeAngle(450.0f))
        assertEquals(180.0f, MathUtils.normalizeAngle(540.0f))
        assertEquals(270.0f, MathUtils.normalizeAngle(630.0f))
    }

    @Test
    fun normalizeAngleHandlesValuesUnderTheRange() {
        assertEquals(270.0f, MathUtils.normalizeAngle(-90.0f))
        assertEquals(180.0f, MathUtils.normalizeAngle(-180.0f))
        assertEquals(90.0f, MathUtils.normalizeAngle(-270.0f))
        assertEquals(0.0f, MathUtils.normalizeAngle(-360.0f))
    }

    @Test
    fun normalizeAngleHandlesSpecialCases() {
        assertEquals(0.0f, MathUtils.normalizeAngle(Float.MIN_VALUE))
        assertEquals(0.0f, MathUtils.normalizeAngle(Float.MAX_VALUE))
        assertEquals(Float.NaN, MathUtils.normalizeAngle(Float.NEGATIVE_INFINITY))
        assertEquals(Float.NaN, MathUtils.normalizeAngle(Float.POSITIVE_INFINITY))
        assertEquals(Float.NaN, MathUtils.normalizeAngle(Float.NaN))
    }

    @Test
    fun normalizeAnglePrecision() {
        val delta = 0.0001f

        assertEquals(0.0123456789f, MathUtils.normalizeAngle(360.0123456789f), delta)
        assertEquals(90.0123456789f, MathUtils.normalizeAngle(450.0123456789f), delta)
        assertEquals(180.0123456789f, MathUtils.normalizeAngle(540.0123456789f), delta)
        assertEquals(270.0123456789f, MathUtils.normalizeAngle(630.0123456789f), delta)
    }
}
