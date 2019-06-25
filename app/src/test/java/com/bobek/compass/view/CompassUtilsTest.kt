/*
 * This file is part of Compass.
 * Copyright (C) 2019 Philipp Bobek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Compass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

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
