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

import android.hardware.SensorManager.getOrientation
import android.hardware.SensorManager.getRotationMatrix

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9
private const val ZERO = 0.0f

class SensorHandler {

    private val accelerometerReading = FloatArray(AXIS_SIZE)
    private val magnetometerReading = FloatArray(AXIS_SIZE)

    fun handleAccelerometerValues(values: SensorValues): Float? {
        floatArrayOf(values.x, values.y, values.z).copyInto(accelerometerReading)
        return updateAzimuth()
    }

    fun handleMagneticFieldValues(values: SensorValues): Float? {
        floatArrayOf(values.x, values.y, values.z).copyInto(magnetometerReading)
        return updateAzimuth()
    }

    private fun updateAzimuth(): Float? {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)

        val success = getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

        return if (success) {
            calculateAzimuth(rotationMatrix)
        } else {
            null
        }
    }

    private fun calculateAzimuth(rotationMatrix: FloatArray): Float {
        val orientationAnglesInRadians = getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationAnglesInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        return SensorUtils.normalizeAngle(azimuthInDegrees)
    }

    fun reset() {
        accelerometerReading.fill(ZERO)
        magnetometerReading.fill(ZERO)
    }
}
