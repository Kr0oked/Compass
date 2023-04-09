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

class MathUtilsTest {

    @Test
    fun getClosestNumberFromInterval() {
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.0f, 1.0f))
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.1f, 1.0f))
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.4f, 1.0f))
        assertEquals(1.0f, MathUtils.getClosestNumberFromInterval(0.5f, 1.0f))
        assertEquals(1.0f, MathUtils.getClosestNumberFromInterval(0.9f, 1.0f))
        assertEquals(1.0f, MathUtils.getClosestNumberFromInterval(1.0f, 1.0f))
        assertEquals(1.0f, MathUtils.getClosestNumberFromInterval(1.4f, 1.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(1.5f, 1.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(1.9f, 1.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(2.0f, 1.0f))

        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.0f, 2.0f))
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.1f, 2.0f))
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.9f, 2.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(1.0f, 2.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(2.0f, 2.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(2.4f, 2.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(2.5f, 2.0f))
        assertEquals(2.0f, MathUtils.getClosestNumberFromInterval(2.9f, 2.0f))
        assertEquals(4.0f, MathUtils.getClosestNumberFromInterval(3.0f, 2.0f))
        assertEquals(4.0f, MathUtils.getClosestNumberFromInterval(3.9f, 2.0f))
        assertEquals(4.0f, MathUtils.getClosestNumberFromInterval(4.0f, 2.0f))

        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.0f, 0.2f))
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.01f, 0.2f))
        assertEquals(0.0f, MathUtils.getClosestNumberFromInterval(0.09f, 0.2f))
        assertEquals(0.2f, MathUtils.getClosestNumberFromInterval(0.1f, 0.2f))
        assertEquals(0.2f, MathUtils.getClosestNumberFromInterval(0.2f, 0.2f))
        assertEquals(0.2f, MathUtils.getClosestNumberFromInterval(0.24f, 0.2f))
        assertEquals(0.2f, MathUtils.getClosestNumberFromInterval(0.25f, 0.2f))
        assertEquals(0.2f, MathUtils.getClosestNumberFromInterval(0.29f, 0.2f))
        assertEquals(0.4f, MathUtils.getClosestNumberFromInterval(0.30f, 0.2f))
        assertEquals(0.4f, MathUtils.getClosestNumberFromInterval(0.39f, 0.2f))
        assertEquals(0.4f, MathUtils.getClosestNumberFromInterval(0.40f, 0.2f))
    }
}
