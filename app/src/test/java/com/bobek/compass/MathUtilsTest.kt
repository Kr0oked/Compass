package com.bobek.compass

import com.bobek.compass.CardinalDirection.*
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
    fun normalizeAnglePrecision() {
        val delta = 0.0001f

        assertEquals(0.0123456789f, MathUtils.normalizeAngle(360.0123456789f), delta)
        assertEquals(90.0123456789f, MathUtils.normalizeAngle(450.0123456789f), delta)
        assertEquals(180.0123456789f, MathUtils.normalizeAngle(540.0123456789f), delta)
        assertEquals(270.0123456789f, MathUtils.normalizeAngle(630.0123456789f), delta)
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
    fun determineCardinalDirection() {
        assertEquals(NORTH, MathUtils.determineCardinalDirection(0.0f))
        assertEquals(NORTH_EAST, MathUtils.determineCardinalDirection(45.0f))
        assertEquals(EAST, MathUtils.determineCardinalDirection(90.0f))
        assertEquals(SOUTH_EAST, MathUtils.determineCardinalDirection(135.0f))
        assertEquals(SOUTH, MathUtils.determineCardinalDirection(180.0f))
        assertEquals(SOUTH_WEST, MathUtils.determineCardinalDirection(225.0f))
        assertEquals(WEST, MathUtils.determineCardinalDirection(270.0f))
        assertEquals(NORTH_WEST, MathUtils.determineCardinalDirection(315.0f))
    }

    @Test
    fun determineCardinalDirectionJunctions() {
        assertEquals(NORTH, MathUtils.determineCardinalDirection(337.5f))
        assertEquals(NORTH_EAST, MathUtils.determineCardinalDirection(22.5f))
        assertEquals(EAST, MathUtils.determineCardinalDirection(67.5f))
        assertEquals(SOUTH_EAST, MathUtils.determineCardinalDirection(112.5f))
        assertEquals(SOUTH, MathUtils.determineCardinalDirection(157.5f))
        assertEquals(SOUTH_WEST, MathUtils.determineCardinalDirection(202.5f))
        assertEquals(WEST, MathUtils.determineCardinalDirection(247.5f))
        assertEquals(NORTH_WEST, MathUtils.determineCardinalDirection(292.5f))
    }

    @Test
    fun determineCardinalDirectionPrecision() {
        assertEquals(NORTH, MathUtils.determineCardinalDirection(22.4999f))
        assertEquals(NORTH_EAST, MathUtils.determineCardinalDirection(67.4999f))
        assertEquals(EAST, MathUtils.determineCardinalDirection(112.4999f))
        assertEquals(SOUTH_EAST, MathUtils.determineCardinalDirection(157.4999f))
        assertEquals(SOUTH, MathUtils.determineCardinalDirection(202.4999f))
        assertEquals(SOUTH_WEST, MathUtils.determineCardinalDirection(247.4999f))
        assertEquals(WEST, MathUtils.determineCardinalDirection(292.4999f))
        assertEquals(NORTH_WEST, MathUtils.determineCardinalDirection(337.4999f))
    }

    @Test
    fun determineCardinalDirectionSpecialCases() {
        assertEquals(NORTH, MathUtils.determineCardinalDirection(Float.MIN_VALUE))
        assertEquals(NORTH, MathUtils.determineCardinalDirection(Float.MAX_VALUE))
        assertEquals(NORTH, MathUtils.determineCardinalDirection(Float.NEGATIVE_INFINITY))
        assertEquals(NORTH, MathUtils.determineCardinalDirection(Float.POSITIVE_INFINITY))
        assertEquals(NORTH, MathUtils.determineCardinalDirection(Float.NaN))
    }
}
