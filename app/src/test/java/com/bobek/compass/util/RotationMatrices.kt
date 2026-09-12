/*
 * This file is part of Compass.
 * Copyright (C) 2026 Philipp Bobek <philipp.bobek@mailbox.org>
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

import kotlin.math.cos
import kotlin.math.sin

/**
 * Builds a 9-element device-to-world rotation matrix (world = East-North-Up) that
 * `SensorManager.getOrientation`, and therefore [CompassReadingCalculator], reads back as the
 * given angles.
 *
 * Reference poses (all angles 0 unless noted):
 * - identity: phone flat, screen up, top edge pointing north
 * - `yaw = 90`: top edge rotated to point east
 * - `pitch = 90`: phone upright, screen facing north, back facing south (tilt 90)
 * - `pitch = 180`: phone flat, face down (tilt 180)
 *
 * @param yawDegrees rotation about the vertical axis, clockwise from north (matches azimuth)
 * @param pitchDegrees rotation tipping the top edge down toward the ground
 * @param rollDegrees rotation about the screen normal
 */
fun rotationMatrix(yawDegrees: Float = 0f, pitchDegrees: Float = 0f, rollDegrees: Float = 0f): FloatArray {
    val rz = rotationAboutZ(Math.toRadians(yawDegrees.toDouble()))
    val rx = rotationAboutX(Math.toRadians(pitchDegrees.toDouble()))
    val ry = rotationAboutY(Math.toRadians(rollDegrees.toDouble()))
    return multiply(multiply(rz, rx), ry)
}

private fun rotationAboutZ(a: Double) = floatArrayOf(
    cos(a).toFloat(), sin(a).toFloat(), 0f,
    (-sin(a)).toFloat(), cos(a).toFloat(), 0f,
    0f, 0f, 1f
)

private fun rotationAboutX(p: Double) = floatArrayOf(
    1f, 0f, 0f,
    0f, cos(p).toFloat(), sin(p).toFloat(),
    0f, (-sin(p)).toFloat(), cos(p).toFloat()
)

private fun rotationAboutY(r: Double) = floatArrayOf(
    cos(r).toFloat(), 0f, sin(r).toFloat(),
    0f, 1f, 0f,
    (-sin(r)).toFloat(), 0f, cos(r).toFloat()
)

private fun multiply(a: FloatArray, b: FloatArray): FloatArray {
    val result = FloatArray(9)
    for (row in 0 until 3) {
        for (col in 0 until 3) {
            var sum = 0f
            for (k in 0 until 3) {
                sum += a[row * 3 + k] * b[k * 3 + col]
            }
            result[row * 3 + col] = sum
        }
    }
    return result
}
