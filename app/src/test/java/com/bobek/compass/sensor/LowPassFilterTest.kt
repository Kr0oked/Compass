/*
 * This file is part of Compass.
 * Copyright (C) 2019 Philipp Bobek <philipp.bobek@mailbox.org>
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

package com.bobek.compass.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class LowPassFilterTest {

    private val oneSecondInNanos = 1_000_000_000L

    @Test
    fun filter() {
        val lowPassFilter = LowPassFilter(1f)

        var timestamp = 0L
        assertEquals(
            SensorValues(0.0f, 1.0f, 2.0f, timestamp),
            lowPassFilter.filter(SensorValues(0.0f, 1.0f, 2.0f, timestamp))
        )

        timestamp += oneSecondInNanos
        assertEquals(
            SensorValues(0.5f, 1.0f, 1.5f, timestamp),
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp))
        )

        timestamp += oneSecondInNanos
        assertEquals(
            SensorValues(0.75f, 1.0f, 1.25f, timestamp),
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp))
        )

        timestamp += oneSecondInNanos
        assertEquals(
            SensorValues(0.875f, 1.0f, 1.125f, timestamp),
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp))
        )

        timestamp += oneSecondInNanos
        assertEquals(
            SensorValues(0.9375f, 1.0f, 1.0625f, timestamp),
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp))
        )

        timestamp += oneSecondInNanos
        assertEquals(
            SensorValues(0.96875f, 1.0f, 1.03125f, timestamp),
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp))
        )
    }

    @Test(expected = IllegalStateException::class)
    fun filterThrowsExceptionWhenAlphaIsTooSmall() {
        LowPassFilter(0.0f)
    }

    @Test
    fun reset() {
        val lowPassFilter = LowPassFilter(1f)

        var timestamp = 0L
        assertEquals(
            lowPassFilter.filter(SensorValues(0.0f, 0.0f, 0.0f, timestamp)),
            SensorValues(0.0f, 0.0f, 0.0f, timestamp)
        )

        timestamp += oneSecondInNanos
        assertEquals(
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp)),
            SensorValues(0.5f, 0.5f, 0.5f, timestamp)
        )

        lowPassFilter.reset()

        timestamp += oneSecondInNanos
        assertEquals(
            lowPassFilter.filter(SensorValues(0.0f, 0.0f, 0.0f, timestamp)),
            SensorValues(0.0f, 0.0f, 0.0f, timestamp)
        )

        timestamp += oneSecondInNanos
        assertEquals(
            lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f, timestamp)),
            SensorValues(0.5f, 0.5f, 0.5f, timestamp)
        )
    }
}
