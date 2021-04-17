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

import com.bobek.compass.model.ModelUtils.normalizeAngle
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelUtilsTest {

    @Test
    fun normalizeAngleKeepsValue() {
        assertEquals(0.0f, normalizeAngle(0.0f))
        assertEquals(90.0f, normalizeAngle(90.0f))
        assertEquals(180.0f, normalizeAngle(180.0f))
        assertEquals(270.0f, normalizeAngle(270.0f))
    }

    @Test
    fun normalizeAngleHandlesValuesAboveTheRange() {
        assertEquals(0.0f, normalizeAngle(360.0f))
        assertEquals(90.0f, normalizeAngle(450.0f))
        assertEquals(180.0f, normalizeAngle(540.0f))
        assertEquals(270.0f, normalizeAngle(630.0f))
    }

    @Test
    fun normalizeAngleHandlesValuesUnderTheRange() {
        assertEquals(270.0f, normalizeAngle(-90.0f))
        assertEquals(180.0f, normalizeAngle(-180.0f))
        assertEquals(90.0f, normalizeAngle(-270.0f))
        assertEquals(0.0f, normalizeAngle(-360.0f))
    }

    @Test
    fun normalizeAnglePrecision() {
        val delta = 0.0001f

        assertEquals(0.0123456789f, normalizeAngle(360.0123456789f), delta)
        assertEquals(90.0123456789f, normalizeAngle(450.0123456789f), delta)
        assertEquals(180.0123456789f, normalizeAngle(540.0123456789f), delta)
        assertEquals(270.0123456789f, normalizeAngle(630.0123456789f), delta)
    }

    @Test
    fun normalizeAngleHandlesSpecialCases() {
        assertEquals(0.0f, normalizeAngle(Float.MIN_VALUE))
        assertEquals(0.0f, normalizeAngle(Float.MAX_VALUE))
        assertEquals(Float.NaN, normalizeAngle(Float.NEGATIVE_INFINITY))
        assertEquals(Float.NaN, normalizeAngle(Float.POSITIVE_INFINITY))
        assertEquals(Float.NaN, normalizeAngle(Float.NaN))
    }
}
