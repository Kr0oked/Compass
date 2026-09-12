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

package com.bobek.compass.data

/**
 * The complete compass state derived from a single device orientation.
 *
 * Both bearings are always populated (the crossfade between regimes needs both);
 * declination is already applied to them when true north is active.
 *
 * @param azimuth heading of the phone's top edge projected onto the horizontal plane, for the rose
 * @param sightingBearing heading of the phone's back projected onto the horizontal plane, for the sighting strip
 * @param tilt angle of the screen normal away from straight up, in degrees. 0 = flat face-up,
 * 90 = upright, 180 = face-down
 * @param roll rotation about the screen normal, in degrees
 * @param regime which visualization is appropriate for this orientation
 * @param reliable whether the active regime's bearing is currently trustworthy (gates haptic feedback)
 */
data class CompassReading(
    val azimuth: Azimuth,
    val sightingBearing: Azimuth,
    val tilt: Float,
    val roll: Float,
    val regime: CompassRegime,
    val reliable: Boolean
) {
    companion object {
        val INITIAL = CompassReading(
            azimuth = Azimuth(0.0f),
            sightingBearing = Azimuth(0.0f),
            tilt = 0.0f,
            roll = 0.0f,
            regime = CompassRegime.ROSE,
            reliable = false
        )
    }
}
