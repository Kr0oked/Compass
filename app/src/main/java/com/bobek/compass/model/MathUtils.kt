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

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

object MathUtils {

    @JvmStatic
    fun calculateAzimuth(rotationVector: RotationVector): Azimuth {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        val orientationAnglesInRadians = SensorManager.getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationAnglesInRadians[AZIMUTH]
        val azimuth = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        val normalizedAzimuth = normalizeAngle(azimuth)
        return Azimuth(normalizedAzimuth)
    }

    @JvmStatic
    fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }
}
