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

package com.bobek.compass.ui.compass

import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.CompassRegime

/**
 * Hand-picked [CompassReading]s used to display each regime in a deliberately non-trivial,
 * good-looking orientation (not aligned to an axis) wherever we need a canned reading instead of
 * live sensor data: Compose previews and the Fastlane screenshot test. Shared so both stay in sync.
 */
internal object CompassReadingShowcase {

    val ROSE = CompassReading.INITIAL.copy(azimuth = Azimuth(320.0f), reliable = true)

    val SIGHTING = CompassReading.INITIAL.copy(
        sightingBearing = Azimuth(147.0f),
        tilt = 90.0f,
        regime = CompassRegime.SIGHTING,
        reliable = true
    )

    val HINT = CompassReading.INITIAL.copy(azimuth = ROSE.azimuth, tilt = 175.0f, regime = CompassRegime.HINT)
}
