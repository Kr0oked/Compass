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

package com.bobek.compass.util

import org.junit.Assert.assertEquals
import org.junit.Test

private const val DELTA = 0.001f

class MagneticFieldStrengthFilterTest {

    @Test
    fun firstSamplePassesThroughAsMagnitude() {
        assertEquals(50f, MagneticFieldStrengthFilter.next(null, 30f, 40f, 0f), DELTA)
        assertEquals(50f, MagneticFieldStrengthFilter.next(null, 0f, -30f, -40f), DELTA)
    }

    @Test
    fun steadyInputStaysSteady() {
        assertEquals(50f, MagneticFieldStrengthFilter.next(50f, 30f, 40f, 0f), DELTA)
    }

    @Test
    fun stepMovesBySmoothingFactorPerSample() {
        val next = MagneticFieldStrengthFilter.next(50f, 0f, 0f, 90f)
        assertEquals(50f + MagneticFieldStrengthFilter.SMOOTHING_FACTOR * 40f, next, DELTA)
    }

    @Test
    fun stepSettlesWithinAboutASecondAtFiveHertz() {
        var strength: Float? = 50f
        repeat(10) { strength = MagneticFieldStrengthFilter.next(strength, 0f, 0f, 90f) }
        assertEquals(90f, strength!!, 40f * 0.1f)
    }
}
