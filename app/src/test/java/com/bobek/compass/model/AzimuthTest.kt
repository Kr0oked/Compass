/*
 * This file is part of Compass.
 * Copyright (C) 2023 Philipp Bobek <philipp.bobek@mailbox.org>
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
    fun azimuthThrowsIllegalArgumentExceptionWhenDegreesAreNegativeInfinity() {
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.NEGATIVE_INFINITY) }
    }

    @Test
    fun azimuthThrowsIllegalArgumentExceptionWhenDegreesArePositiveInfinity() {
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.NEGATIVE_INFINITY) }
    }

    @Test
    fun azimuthThrowsIllegalArgumentExceptionWhenDegreesAreNotANumber() {
        assertThrows(IllegalArgumentException::class.java) { Azimuth(Float.NaN) }
    }

    @Test
    fun normalizationKeepsValue() {
        assertEquals(0f, Azimuth(0f).degrees)
        assertEquals(90f, Azimuth(90f).degrees)
        assertEquals(180f, Azimuth(180f).degrees)
        assertEquals(270f, Azimuth(270f).degrees)
    }

    @Test
    fun normalizationHandlesValuesAboveTheRange() {
        assertEquals(Azimuth(0f), Azimuth(360f))
        assertEquals(Azimuth(90f), Azimuth(450f))
        assertEquals(Azimuth(180f), Azimuth(540f))
        assertEquals(Azimuth(270f), Azimuth(630f))
    }

    @Test
    fun normalizationHandlesValuesUnderTheRange() {
        assertEquals(Azimuth(270f), Azimuth(-90f))
        assertEquals(Azimuth(180f), Azimuth(-180f))
        assertEquals(Azimuth(90f), Azimuth(-270f))
        assertEquals(Azimuth(0f), Azimuth(-360f))
    }

    @Test
    fun normalizationPrecision() {
        val delta = 0.0001f

        assertEquals(0.012345678f, Azimuth(360.01236f).degrees, delta)
        assertEquals(90.012344f, Azimuth(450.01236f).degrees, delta)
        assertEquals(180.01234f, Azimuth(540.0123f).degrees, delta)
        assertEquals(270.01236f, Azimuth(630.0123f).degrees, delta)
    }

    @Test
    fun normalizeAngleHandlesSpecialCases() {
        assertEquals(0f, Azimuth(Float.MIN_VALUE).degrees)
        assertEquals(0f, Azimuth(Float.MAX_VALUE).degrees)
    }

    @Test
    fun roundedDegrees() {
        assertEquals(0, Azimuth(0.0f).roundedDegrees)
        assertEquals(0, Azimuth(0.1f).roundedDegrees)
        assertEquals(0, Azimuth(0.2f).roundedDegrees)
        assertEquals(0, Azimuth(0.3f).roundedDegrees)
        assertEquals(0, Azimuth(0.4f).roundedDegrees)
        assertEquals(1, Azimuth(0.5f).roundedDegrees)
        assertEquals(1, Azimuth(0.6f).roundedDegrees)
        assertEquals(1, Azimuth(0.7f).roundedDegrees)
        assertEquals(1, Azimuth(0.8f).roundedDegrees)
        assertEquals(1, Azimuth(0.9f).roundedDegrees)
        assertEquals(1, Azimuth(1.0f).roundedDegrees)

        assertEquals(359, Azimuth(359.0f).roundedDegrees)
        assertEquals(359, Azimuth(359.1f).roundedDegrees)
        assertEquals(359, Azimuth(359.2f).roundedDegrees)
        assertEquals(359, Azimuth(359.3f).roundedDegrees)
        assertEquals(359, Azimuth(359.4f).roundedDegrees)
        assertEquals(0, Azimuth(359.5f).roundedDegrees)
        assertEquals(0, Azimuth(359.6f).roundedDegrees)
        assertEquals(0, Azimuth(359.7f).roundedDegrees)
        assertEquals(0, Azimuth(359.8f).roundedDegrees)
        assertEquals(0, Azimuth(359.9f).roundedDegrees)
        assertEquals(0, Azimuth(360.0f).roundedDegrees)
    }

    @Test
    fun cardinalDirection() {
        assertEquals(CardinalDirection.NORTH, Azimuth(0f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(45f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(90f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(135f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(180f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(225f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(270f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(315f).cardinalDirection)
    }

    @Test
    fun cardinalDirectionJunctions() {
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
    fun cardinalDirectionPrecision() {
        assertEquals(CardinalDirection.NORTH, Azimuth(22.4999f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(67.4999f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(112.4999f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(157.4999f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(202.4999f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(247.4999f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(292.4999f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(337.4999f).cardinalDirection)
    }

    @Test
    fun plus() {
        assertEquals(Azimuth(90f), Azimuth(0f) + 90f)
        assertEquals(Azimuth(180f), Azimuth(0f) + 180f)
        assertEquals(Azimuth(0f), Azimuth(0f) + 360f)
        assertEquals(Azimuth(20f), Azimuth(300f) + 80f)
        assertEquals(Azimuth(301.5f), Azimuth(300f) + 1.5f)
    }

    @Test
    fun minus() {
        assertEquals(Azimuth(270f), Azimuth(0f) - 90f)
        assertEquals(Azimuth(180f), Azimuth(0f) - 180f)
        assertEquals(Azimuth(0f), Azimuth(0f) - 360f)
        assertEquals(Azimuth(220f), Azimuth(300f) - 80f)
        assertEquals(Azimuth(298.5f), Azimuth(300f) - 1.5f)
    }

    @Test
    fun compareTo() {
        assertEquals(true, Azimuth(180f) < Azimuth(181f))
        assertEquals(false, Azimuth(180f) < Azimuth(179f))
        assertEquals(false, Azimuth(180f) < Azimuth(180f))

        assertEquals(true, Azimuth(180f) > Azimuth(179f))
        assertEquals(false, Azimuth(180f) > Azimuth(181f))
        assertEquals(false, Azimuth(180f) > Azimuth(180f))

        assertEquals(true, Azimuth(180f) <= Azimuth(181f))
        assertEquals(false, Azimuth(180f) <= Azimuth(179f))
        assertEquals(true, Azimuth(180f) <= Azimuth(180f))

        assertEquals(true, Azimuth(180f) >= Azimuth(179f))
        assertEquals(false, Azimuth(180f) >= Azimuth(181f))
        assertEquals(true, Azimuth(180f) >= Azimuth(180f))

        assertEquals(false, Azimuth(180f) == Azimuth(179f))
        assertEquals(false, Azimuth(180f) == Azimuth(181f))
        assertEquals(true, Azimuth(180f) == Azimuth(180f))
    }
}
