/*
 * This file is part of Compass.
 * Copyright (C) 2020 Philipp Bobek <philipp.bobek@mailbox.org>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bobek.compass.view

import com.bobek.compass.view.CardinalDirection.*
import com.bobek.compass.view.CompassUtils.determineCardinalDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class CompassUtilsTest {

    @Test
    fun determineCardinalDirection() {
        assertEquals(NORTH, determineCardinalDirection(0.0f))
        assertEquals(NORTH_EAST, determineCardinalDirection(45.0f))
        assertEquals(EAST, determineCardinalDirection(90.0f))
        assertEquals(SOUTH_EAST, determineCardinalDirection(135.0f))
        assertEquals(SOUTH, determineCardinalDirection(180.0f))
        assertEquals(SOUTH_WEST, determineCardinalDirection(225.0f))
        assertEquals(WEST, determineCardinalDirection(270.0f))
        assertEquals(NORTH_WEST, determineCardinalDirection(315.0f))
    }

    @Test
    fun determineCardinalDirectionJunctions() {
        assertEquals(NORTH, determineCardinalDirection(337.5f))
        assertEquals(NORTH_EAST, determineCardinalDirection(22.5f))
        assertEquals(EAST, determineCardinalDirection(67.5f))
        assertEquals(SOUTH_EAST, determineCardinalDirection(112.5f))
        assertEquals(SOUTH, determineCardinalDirection(157.5f))
        assertEquals(SOUTH_WEST, determineCardinalDirection(202.5f))
        assertEquals(WEST, determineCardinalDirection(247.5f))
        assertEquals(NORTH_WEST, determineCardinalDirection(292.5f))
    }

    @Test
    fun determineCardinalDirectionPrecision() {
        assertEquals(NORTH, determineCardinalDirection(22.4999f))
        assertEquals(NORTH_EAST, determineCardinalDirection(67.4999f))
        assertEquals(EAST, determineCardinalDirection(112.4999f))
        assertEquals(SOUTH_EAST, determineCardinalDirection(157.4999f))
        assertEquals(SOUTH, determineCardinalDirection(202.4999f))
        assertEquals(SOUTH_WEST, determineCardinalDirection(247.4999f))
        assertEquals(WEST, determineCardinalDirection(292.4999f))
        assertEquals(NORTH_WEST, determineCardinalDirection(337.4999f))
    }

    @Test
    fun determineCardinalDirectionSpecialCases() {
        assertEquals(NORTH, determineCardinalDirection(Float.MIN_VALUE))
        assertEquals(NORTH, determineCardinalDirection(Float.MAX_VALUE))
        assertEquals(NORTH, determineCardinalDirection(Float.NEGATIVE_INFINITY))
        assertEquals(NORTH, determineCardinalDirection(Float.POSITIVE_INFINITY))
        assertEquals(NORTH, determineCardinalDirection(Float.NaN))
    }
}
