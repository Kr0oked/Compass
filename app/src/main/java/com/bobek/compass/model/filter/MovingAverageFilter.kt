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

package com.bobek.compass.model.filter

import com.bobek.compass.model.ModelUtils.nanosToSeconds
import com.bobek.compass.model.SensorValues
import java.util.*
import kotlin.math.roundToInt

class MovingAverageFilter(private val timeConstantInSeconds: Float) : SensorFilter {

    init {
        check(timeConstantInSeconds > 0.0f) { "Time constant must be greater than 0" }
    }

    private val previousValues = ArrayDeque<SensorValues>()

    override fun filter(values: SensorValues): SensorValues {
        previousValues.push(values)

        return if (previousValues.size > 1) {
            removeOldSensorValues()
            calculateAverageSensorValues()
        } else {
            values
        }
    }

    private fun removeOldSensorValues() {
        val earliestTimestamp = previousValues.last.timestamp
        val latestTimestamp = previousValues.first.timestamp
        val durationInSeconds = nanosToSeconds(latestTimestamp - earliestTimestamp)
        val sampleRateInHz = previousValues.size / durationInSeconds

        val filterWindow = (sampleRateInHz * timeConstantInSeconds)
            .roundToInt()
            .let { number -> minimumOne(number) }

        while (previousValues.size > filterWindow) {
            previousValues.removeLast()
        }
    }

    private fun minimumOne(number: Int) = if (number < 1) 1 else number

    private fun calculateAverageSensorValues(): SensorValues {
        val x = previousValues.map { it.x }.average().toFloat()
        val y = previousValues.map { it.y }.average().toFloat()
        val z = previousValues.map { it.z }.average().toFloat()
        val timestamp = previousValues.first.timestamp

        return SensorValues(x, y, z, timestamp)
    }

    override fun reset() {
        previousValues.clear()
    }
}
