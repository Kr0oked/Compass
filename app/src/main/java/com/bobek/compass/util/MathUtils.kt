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

import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import com.bobek.compass.model.Azimuth
import com.bobek.compass.model.DisplayRotation
import com.bobek.compass.model.DisplayRotation.ROTATION_0
import com.bobek.compass.model.DisplayRotation.ROTATION_180
import com.bobek.compass.model.DisplayRotation.ROTATION_270
import com.bobek.compass.model.DisplayRotation.ROTATION_90
import com.bobek.compass.model.RotationVector
import kotlin.math.roundToInt

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

object MathUtils {

    @JvmStatic
    fun calculateAzimuth(rotationVector: RotationVector, displayRotation: DisplayRotation): Azimuth {
        val rotationMatrix = getRotationMatrix(rotationVector)
        val remappedRotationMatrix = remapRotationMatrix(rotationMatrix, displayRotation)
        val orientationInRadians = SensorManager.getOrientation(remappedRotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        return Azimuth(azimuthInDegrees)
    }

    private fun getRotationMatrix(rotationVector: RotationVector): FloatArray {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        return rotationMatrix
    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, displayRotation: DisplayRotation): FloatArray {
        return when (displayRotation) {
            ROTATION_0 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
            ROTATION_90 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            ROTATION_180 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            ROTATION_270 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
        }
    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val remappedRotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remappedRotationMatrix)
        return remappedRotationMatrix
    }

    @JvmStatic
    fun getMagneticDeclination(location: Location): Float {
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val altitude = location.altitude.toFloat()
        val time = location.time
        val geomagneticField = GeomagneticField(latitude, longitude, altitude, time)
        return geomagneticField.declination
    }

    fun getClosestNumberFromInterval(number: Float, interval: Float): Float =
        (number / interval).roundToInt() * interval

    /**
     * @see <a href="https://math.stackexchange.com/questions/2275439/check-if-point-on-circle-is-between-two-other-points-on-circle">Stackexchange</a>
     */
    fun isAzimuthBetweenTwoPoints(azimuth: Azimuth, pointA: Azimuth, pointB: Azimuth): Boolean {
        val aToB = (pointB.degrees - pointA.degrees + 360f) % 360f
        val aToAzimuth = (azimuth.degrees - pointA.degrees + 360f) % 360f
        return aToB <= 180f != aToAzimuth > aToB
    }
}
