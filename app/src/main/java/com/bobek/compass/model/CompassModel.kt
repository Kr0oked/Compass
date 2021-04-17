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

import android.hardware.SensorManager
import com.bobek.compass.model.filter.SensorFilter

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9
private const val ZERO = 0.0f

class CompassModel(
    private val accelerometerFilter: SensorFilter,
    private val magnetometerFilter: SensorFilter
) {

    var azimuth = ZERO
        private set

    private val accelerometerReading = FloatArray(AXIS_SIZE)
    private val magnetometerReading = FloatArray(AXIS_SIZE)

    fun updateAccelerometer(values: SensorValues) {
        floatArrayOf(values.x, values.y, values.z).copyInto(accelerometerReading)
        updateAzimuth()
    }

    fun updateMagneticField(values: SensorValues) {
        floatArrayOf(values.x, values.y, values.z).copyInto(magnetometerReading)
        updateAzimuth()
    }

    private fun updateAzimuth() {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)

        val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

        if (success) {
            azimuth = calculateAzimuth(rotationMatrix)
        }
    }

    private fun calculateAzimuth(rotationMatrix: FloatArray): Float {
        val orientationAnglesInRadians = SensorManager.getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationAnglesInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        return ModelUtils.normalizeAngle(azimuthInDegrees)
    }

    fun reset() {
        azimuth = ZERO
        accelerometerReading.fill(ZERO)
        magnetometerReading.fill(ZERO)
        accelerometerFilter.reset()
        magnetometerFilter.reset()
    }
}
