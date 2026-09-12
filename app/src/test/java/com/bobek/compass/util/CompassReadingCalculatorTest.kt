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

import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.CompassRegime
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val TOLERANCE_DEGREES = 0.5f

class CompassReadingCalculatorTest {

    private fun next(
        matrix: FloatArray,
        previous: CompassReading = CompassReading.INITIAL,
        declination: Float = 0f
    ): CompassReading = CompassReadingCalculator.next(previous, matrix, declination)

    // Rotation matrix helper sanity check

    @Test
    fun rotationMatrixHelperIsIdentityForZeroAngles() {
        assertArrayEquals(
            floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
            rotationMatrix(),
            1e-5f
        )
    }

    // Bearings and tilt

    @Test
    fun flatFacingNorthIsRoseAtZeroDegrees() {
        val reading = next(rotationMatrix())

        assertEquals(CompassRegime.ROSE, reading.regime)
        assertEquals(0f, reading.azimuth.degrees, TOLERANCE_DEGREES)
        assertEquals(0f, reading.tilt, TOLERANCE_DEGREES)
        assertTrue(reading.reliable)
    }

    @Test
    fun yawMapsToAzimuth() {
        assertEquals(90f, next(rotationMatrix(yawDegrees = 90f)).azimuth.degrees, TOLERANCE_DEGREES)
        assertEquals(180f, next(rotationMatrix(yawDegrees = 180f)).azimuth.degrees, TOLERANCE_DEGREES)
        assertEquals(270f, next(rotationMatrix(yawDegrees = -90f)).azimuth.degrees, TOLERANCE_DEGREES)
    }

    @Test
    fun uprightPhoneReportsBackBearingAwayFromScreen() {
        // pitch 90: screen faces north, so the phone's back faces south
        val reading = next(rotationMatrix(pitchDegrees = 90f))

        assertEquals(90f, reading.tilt, TOLERANCE_DEGREES)
        assertEquals(180f, reading.sightingBearing.degrees, TOLERANCE_DEGREES)
    }

    @Test
    fun declinationIsAddedToTheAzimuth() {
        val reading = next(rotationMatrix(yawDegrees = 10f), declination = 5f)
        assertEquals(15f, reading.azimuth.degrees, TOLERANCE_DEGREES)
    }

    @Test
    fun declinationIsAddedToTheSightingBearing() {
        val reading = next(rotationMatrix(pitchDegrees = 90f), declination = 5f)
        assertEquals(185f, reading.sightingBearing.degrees, TOLERANCE_DEGREES)
    }

    @Test
    fun faceDownIsHintAndUnreliable() {
        val reading = next(rotationMatrix(pitchDegrees = 180f))

        assertEquals(180f, reading.tilt, TOLERANCE_DEGREES)
        assertEquals(CompassRegime.HINT, reading.regime)
        assertFalse(reading.reliable)
    }

    // Regime hysteresis

    @Test
    fun risingTiltStaysRoseUntilSeventyDegrees() {
        var reading = next(rotationMatrix(pitchDegrees = 65f))
        assertEquals(CompassRegime.ROSE, reading.regime)

        reading = next(rotationMatrix(pitchDegrees = 75f), previous = reading)
        assertEquals(CompassRegime.SIGHTING, reading.regime)
    }

    @Test
    fun fallingTiltStaysSightingUntilFiftyTwoDegrees() {
        var reading = CompassReading.INITIAL.copy(regime = CompassRegime.SIGHTING)

        reading = next(rotationMatrix(pitchDegrees = 60f), previous = reading)
        assertEquals(CompassRegime.SIGHTING, reading.regime)

        reading = next(rotationMatrix(pitchDegrees = 45f), previous = reading)
        assertEquals(CompassRegime.ROSE, reading.regime)
    }

    @Test
    fun fastFlipGoesFromRoseToHintDirectly() {
        val reading = next(rotationMatrix(pitchDegrees = 170f))
        assertEquals(CompassRegime.HINT, reading.regime)
    }

    @Test
    fun fastFlipBackGoesFromHintToRoseDirectly() {
        val reading = next(
            rotationMatrix(pitchDegrees = 30f),
            previous = CompassReading.INITIAL.copy(regime = CompassRegime.HINT)
        )
        assertEquals(CompassRegime.ROSE, reading.regime)
    }

    @Test
    fun sightingToHintBoundaryHasHysteresis() {
        var reading = CompassReading.INITIAL.copy(regime = CompassRegime.SIGHTING)

        reading = next(rotationMatrix(pitchDegrees = 120f), previous = reading)
        assertEquals(CompassRegime.SIGHTING, reading.regime)

        reading = next(rotationMatrix(pitchDegrees = 135f), previous = reading)
        assertEquals(CompassRegime.HINT, reading.regime)

        reading = next(rotationMatrix(pitchDegrees = 115f), previous = reading)
        assertEquals(CompassRegime.HINT, reading.regime)

        reading = next(rotationMatrix(pitchDegrees = 105f), previous = reading)
        assertEquals(CompassRegime.SIGHTING, reading.regime)
    }
}
