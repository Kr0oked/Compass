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

package com.bobek.compass.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class MovingAverageFilterTest {

    @Test(expected = IllegalStateException::class)
    fun constructorThrowsExceptionWhenAlphaIsTooSmall() {
        MovingAverageFilter(0.0f)
    }

    @Test
    fun filter() {
        val movingAverageFilter = MovingAverageFilter(3f)

        assertEquals(
            SensorValues(0.0f, 1.0f, 2.0f, 1_000_000_000L),
            movingAverageFilter.filter(SensorValues(0.0f, 1.0f, 2.0f, 1_000_000_000L))
        )

        assertEquals(
            SensorValues(0.5f, 1.0f, 1.5f, 2_000_000_000L),
            movingAverageFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, 2_000_000_000L))
        )

        assertEquals(
            SensorValues(0.6666667f, 1.0f, 1.3333334f, 3_000_000_000L),
            movingAverageFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, 3_000_000_000L))
        )

        assertEquals(
            SensorValues(0.75f, 1.0f, 1.25f, 4_000_000_000L),
            movingAverageFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, 4_000_000_000L))
        )

        assertEquals(
            SensorValues(1.0f, 1.0f, 1.0f, 5_000_000_000L),
            movingAverageFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, 5_000_000_000L))
        )
    }

    @Test
    fun reset() {
        val movingAverageFilter = MovingAverageFilter(1f)

        assertEquals(
            SensorValues(0.0f, 0.0f, 0.0f, 1_000_000_000L),
            movingAverageFilter.filter(SensorValues(0.0f, 0.0f, 0.0f, 1_000_000_000L))
        )

        assertEquals(
            SensorValues(0.5f, 0.5f, 0.5f, 2_000_000_000L),
            movingAverageFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, 2_000_000_000L))
        )

        movingAverageFilter.reset()

        assertEquals(
            SensorValues(0.0f, 0.0f, 0.0f, 3_000_000_000L),
            movingAverageFilter.filter(SensorValues(0.0f, 0.0f, 0.0f, 3_000_000_000L))
        )

        assertEquals(
            SensorValues(0.5f, 0.5f, 0.5f, 4_000_000_000L),
            movingAverageFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, 4_000_000_000L))
        )
    }
}
