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

import kotlin.math.sqrt

/**
 * Smooths the magnitude of the calibrated magnetometer vector into a steady field-strength reading.
 *
 * Only the magnitude is shown, so the low-pass runs on the magnitude rather than per axis.
 */
object MagneticFieldStrengthFilter {

    /**
     * Weight of each new sample in the exponential low-pass. At the ~5 Hz of `SENSOR_DELAY_NORMAL`
     * this settles within about a second, steadying the readout while still reacting visibly to a
     * nearby magnet.
     */
    const val SMOOTHING_FACTOR = 0.25f

    /**
     * Returns the smoothed field strength in µT after the sample ([x], [y], [z], in µT). The first
     * sample (no [previous]) passes through unfiltered so the readout doesn't ramp up from zero.
     */
    fun next(previous: Float?, x: Float, y: Float, z: Float): Float {
        val magnitude = sqrt(x * x + y * y + z * z)
        return if (previous == null) magnitude else previous + SMOOTHING_FACTOR * (magnitude - previous)
    }
}
