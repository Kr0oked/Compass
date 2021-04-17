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

import com.bobek.compass.model.ModelUtils
import com.bobek.compass.model.SensorValues

class LowPassFilter(private val timeConstantInSeconds: Float) : SensorFilter {

    init {
        check(timeConstantInSeconds > 0.0f) { "Time constant must be greater than 0" }
    }

    private var count = 1L
    private var baseValues: SensorValues? = null

    override fun filter(values: SensorValues): SensorValues {
        return if (baseValues == null) {
            initializeWithFirstValues(values)
        } else {
            update(values, baseValues!!)
        }
    }

    private fun initializeWithFirstValues(values: SensorValues): SensorValues {
        baseValues = values
        return values
    }

    private fun update(latest: SensorValues, base: SensorValues): SensorValues {
        val durationInSeconds = ModelUtils.nanosToSeconds(latest.timestamp - base.timestamp)
        val deliveryRate = 1L / (count / durationInSeconds)
        val alpha = deliveryRate / (deliveryRate + timeConstantInSeconds)

        val x = base.x + alpha * (latest.x - base.x)
        val y = base.y + alpha * (latest.y - base.y)
        val z = base.z + alpha * (latest.z - base.z)

        baseValues = SensorValues(x, y, z, base.timestamp)
        count++

        return SensorValues(x, y, z, latest.timestamp)
    }

    override fun reset() {
        count = 1L
        baseValues = null
    }
}
