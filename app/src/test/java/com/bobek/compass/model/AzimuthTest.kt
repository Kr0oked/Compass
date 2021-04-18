/*
 * This file is part of Compass.
 * Copyright (C) 2021 Philipp Bobek <philipp.bobek@mailbox.org>
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

package com.bobek.compass.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AzimuthTest {

    @Test
    fun valueBoundaries() {
        assertThrows(IllegalArgumentException::class.java) { Azimuth(-1f) }
        assertThrows(IllegalArgumentException::class.java) { Azimuth(360f) }
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.MAX_VALUE) }
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.MIN_VALUE) }
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.NEGATIVE_INFINITY) }
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.POSITIVE_INFINITY) }
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.NaN) }
    }

    @Test
    fun getCardinalDirection() {
        assertEquals(CardinalDirection.NORTH, Azimuth(0.0f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(45.0f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(90.0f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(135.0f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(180.0f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(225.0f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(270.0f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(315.0f).cardinalDirection)
    }

    @Test
    fun getCardinalDirectionJunctions() {
        assertEquals(CardinalDirection.NORTH, Azimuth(337.5f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(22.5f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(67.5f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(112.5f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(157.5f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(202.5f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(247.5f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(292.5f).cardinalDirection)
    }

    @Test
    fun getCardinalDirectionPrecision() {
        assertEquals(CardinalDirection.NORTH, Azimuth(22.4999f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(67.4999f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(112.4999f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(157.4999f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(202.4999f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(247.4999f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(292.4999f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(337.4999f).cardinalDirection)
    }
}
