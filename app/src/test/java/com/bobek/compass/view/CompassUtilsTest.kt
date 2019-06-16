package com.bobek.compass.view

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassUtilsTest {

    @Test
    fun determineCardinalDirection() {
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(0.0f))
        assertEquals(CardinalDirection.NORTH_EAST, CompassUtils.determineCardinalDirection(45.0f))
        assertEquals(CardinalDirection.EAST, CompassUtils.determineCardinalDirection(90.0f))
        assertEquals(CardinalDirection.SOUTH_EAST, CompassUtils.determineCardinalDirection(135.0f))
        assertEquals(CardinalDirection.SOUTH, CompassUtils.determineCardinalDirection(180.0f))
        assertEquals(CardinalDirection.SOUTH_WEST, CompassUtils.determineCardinalDirection(225.0f))
        assertEquals(CardinalDirection.WEST, CompassUtils.determineCardinalDirection(270.0f))
        assertEquals(CardinalDirection.NORTH_WEST, CompassUtils.determineCardinalDirection(315.0f))
    }

    @Test
    fun determineCardinalDirectionJunctions() {
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(337.5f))
        assertEquals(CardinalDirection.NORTH_EAST, CompassUtils.determineCardinalDirection(22.5f))
        assertEquals(CardinalDirection.EAST, CompassUtils.determineCardinalDirection(67.5f))
        assertEquals(CardinalDirection.SOUTH_EAST, CompassUtils.determineCardinalDirection(112.5f))
        assertEquals(CardinalDirection.SOUTH, CompassUtils.determineCardinalDirection(157.5f))
        assertEquals(CardinalDirection.SOUTH_WEST, CompassUtils.determineCardinalDirection(202.5f))
        assertEquals(CardinalDirection.WEST, CompassUtils.determineCardinalDirection(247.5f))
        assertEquals(CardinalDirection.NORTH_WEST, CompassUtils.determineCardinalDirection(292.5f))
    }

    @Test
    fun determineCardinalDirectionPrecision() {
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(22.4999f))
        assertEquals(CardinalDirection.NORTH_EAST, CompassUtils.determineCardinalDirection(67.4999f))
        assertEquals(CardinalDirection.EAST, CompassUtils.determineCardinalDirection(112.4999f))
        assertEquals(CardinalDirection.SOUTH_EAST, CompassUtils.determineCardinalDirection(157.4999f))
        assertEquals(CardinalDirection.SOUTH, CompassUtils.determineCardinalDirection(202.4999f))
        assertEquals(CardinalDirection.SOUTH_WEST, CompassUtils.determineCardinalDirection(247.4999f))
        assertEquals(CardinalDirection.WEST, CompassUtils.determineCardinalDirection(292.4999f))
        assertEquals(CardinalDirection.NORTH_WEST, CompassUtils.determineCardinalDirection(337.4999f))
    }

    @Test
    fun determineCardinalDirectionSpecialCases() {
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(Float.MIN_VALUE))
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(Float.MAX_VALUE))
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(Float.NEGATIVE_INFINITY))
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(Float.POSITIVE_INFINITY))
        assertEquals(CardinalDirection.NORTH, CompassUtils.determineCardinalDirection(Float.NaN))
    }
}
