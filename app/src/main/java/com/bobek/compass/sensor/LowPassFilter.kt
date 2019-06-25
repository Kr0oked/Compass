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

package com.bobek.compass.sensor

class LowPassFilter(private val alpha: Float) {

    private var previousValues: SensorValues? = null

    init {
        check(alpha > 0.0f && alpha < 1.0f) { "Alpha must be between 0 and 1" }
    }

    fun filter(values: SensorValues): SensorValues {
        val filteredValues = previousValues?.let { previousValues -> filter(values, previousValues) } ?: values
        previousValues = filteredValues
        return filteredValues
    }

    private fun filter(newValues: SensorValues, lastValues: SensorValues): SensorValues {
        val x = filter(newValues.x, lastValues.x)
        val y = filter(newValues.y, lastValues.y)
        val z = filter(newValues.z, lastValues.z)

        return SensorValues(x, y, z)
    }

    private fun filter(newValue: Float, lastValue: Float): Float {
        return lastValue + alpha * (newValue - lastValue)
    }

    fun reset() {
        previousValues = null
    }
}
