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

import com.bobek.compass.sensor.SensorUtils.nanosToSeconds

class LowPassFilter(private val timeConstantInSeconds: Float) :
    SensorFilter(timeConstantInSeconds) {

    private var previousValues: SensorValues? = null

    override fun filter(values: SensorValues): SensorValues {
        return filter(values, (previousValues ?: values))
            .also { filteredValues -> previousValues = filteredValues }
    }

    private fun filter(newValues: SensorValues, lastValues: SensorValues): SensorValues {
        val durationInNanos = newValues.timestamp - lastValues.timestamp
        val durationInSeconds = nanosToSeconds(durationInNanos)
        val alpha = timeConstantInSeconds / (timeConstantInSeconds + durationInSeconds)

        val x = filter(newValues.x, lastValues.x, alpha)
        val y = filter(newValues.y, lastValues.y, alpha)
        val z = filter(newValues.z, lastValues.z, alpha)

        return SensorValues(x, y, z, newValues.timestamp)
    }

    private fun filter(newValue: Float, lastValue: Float, alpha: Float): Float {
        return alpha * lastValue + (1 - alpha) * newValue
    }

    override fun reset() {
        previousValues = null
    }
}
