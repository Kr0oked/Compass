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

import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.CompassRegime
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Turns a remapped device-to-world rotation matrix into a [CompassReading].
 *
 * Pure Kotlin, no Android dependencies, so the whole thing is unit-testable. The
 * Activity is responsible for producing the remapped matrix via `SensorManager`.
 *
 * The world frame is East-North-Up, matching `SensorManager.getRotationMatrixFromVector`.
 * A 9-element row-major matrix `r` maps a device vector to world coordinates, so its
 * columns are the device axes expressed in the world:
 *
 * - column 1 (`r[1]`, `r[4]`, `r[7]`) is the device +Y axis (top edge of the phone)
 * - column 2 (`r[2]`, `r[5]`, `r[8]`) is the device +Z axis (screen normal)
 */
object CompassReadingCalculator {

    /** Screen-normal tilt above which the rose gives way to the sighting strip. */
    const val ROSE_TO_SIGHTING_ENTER_DEGREES = 70f

    /** Screen-normal tilt below which the sighting strip gives way back to the rose. */
    const val SIGHTING_TO_ROSE_EXIT_DEGREES = 52f

    /** Screen-normal tilt above which the sighting strip gives way to the "hold level" hint. */
    const val SIGHTING_TO_HINT_ENTER_DEGREES = 130f

    /** Screen-normal tilt below which the hint gives way back to the sighting strip. */
    const val HINT_TO_SIGHTING_EXIT_DEGREES = 110f

    /**
     * Minimum horizontal component of the active regime's reference axis for its bearing to be
     * considered trustworthy. 0.30 corresponds to the axis being within ~17.5 of vertical.
     */
    private const val RELIABLE_MIN_HORIZONTAL_COMPONENT = 0.30f

    /**
     * @param previous the reading from the last sensor event, for regime hysteresis
     * @param r remapped 9-element device-to-world (East-North-Up) rotation matrix
     * @param declinationDegrees magnetic declination to add to both bearings, or 0 for magnetic north
     */
    fun next(previous: CompassReading, r: FloatArray, declinationDegrees: Float): CompassReading {
        require(r.size == 9) {
            "rotation matrix must have 9 elements but had ${r.size}"
        }

        val azimuthDegrees = toDegrees(atan2(r[1], r[4]))
        val sightingBearingDegrees = toDegrees(atan2(-r[2], -r[5]))
        val rollDegrees = toDegrees(atan2(-r[6], r[8]))
        val tiltDegrees = toDegrees(acos(r[8].coerceIn(-1f, 1f)))

        val regime = nextRegime(previous.regime, tiltDegrees)

        val horizontalComponent = when (regime) {
            CompassRegime.ROSE -> hypot(r[1], r[4])
            CompassRegime.SIGHTING -> hypot(r[2], r[5])
            CompassRegime.HINT -> 0f
        }
        val reliable = regime != CompassRegime.HINT &&
                horizontalComponent >= RELIABLE_MIN_HORIZONTAL_COMPONENT

        return CompassReading(
            azimuth = Azimuth(azimuthDegrees + declinationDegrees),
            sightingBearing = Azimuth(sightingBearingDegrees + declinationDegrees),
            tilt = tiltDegrees,
            roll = rollDegrees,
            regime = regime,
            reliable = reliable
        )
    }

    /**
     * Regime is chosen from the current tilt only, with per-boundary hysteresis. Non-adjacent
     * jumps (ROSE to HINT and back) are allowed so a hard, fast flip is not stuck in SIGHTING.
     */
    private fun nextRegime(previous: CompassRegime, tilt: Float): CompassRegime = when (previous) {
        CompassRegime.ROSE -> when {
            tilt > SIGHTING_TO_HINT_ENTER_DEGREES -> CompassRegime.HINT
            tilt > ROSE_TO_SIGHTING_ENTER_DEGREES -> CompassRegime.SIGHTING
            else -> CompassRegime.ROSE
        }

        CompassRegime.SIGHTING -> when {
            tilt > SIGHTING_TO_HINT_ENTER_DEGREES -> CompassRegime.HINT
            tilt < SIGHTING_TO_ROSE_EXIT_DEGREES -> CompassRegime.ROSE
            else -> CompassRegime.SIGHTING
        }

        CompassRegime.HINT -> when {
            tilt < SIGHTING_TO_ROSE_EXIT_DEGREES -> CompassRegime.ROSE
            tilt < HINT_TO_SIGHTING_EXIT_DEGREES -> CompassRegime.SIGHTING
            else -> CompassRegime.HINT
        }
    }

    private fun toDegrees(radians: Float): Float = Math.toDegrees(radians.toDouble()).toFloat()
}
