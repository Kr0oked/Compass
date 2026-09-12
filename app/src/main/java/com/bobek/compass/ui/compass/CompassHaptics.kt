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

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import com.bobek.compass.data.Azimuth
import com.bobek.compass.util.MathUtils

private const val HAPTIC_FEEDBACK_INTERVAL = 2.0f

/**
 * Fires a short haptic tick every [HAPTIC_FEEDBACK_INTERVAL] degrees as [bearing] sweeps past the
 * marks, while [enabled] is true.
 *
 * The reference point is cleared whenever [enabled] goes false, so anything that re-enables it
 * later (a regime change, a recovered reading, the setting toggled back on) re-seeds silently
 * instead of firing a burst.
 */
@Composable
fun CompassHapticFeedback(enabled: Boolean, bearing: Azimuth) {
    val view = LocalView.current
    var lastFeedbackPoint by remember { mutableStateOf<Azimuth?>(null) }

    LaunchedEffect(bearing, enabled) {
        if (!enabled) {
            lastFeedbackPoint = null
            return@LaunchedEffect
        }

        val lastPoint = lastFeedbackPoint
        if (lastPoint == null) {
            lastFeedbackPoint = closestMark(bearing)
            return@LaunchedEffect
        }

        val insideCurrentMark = MathUtils.isAzimuthBetweenTwoPoints(
            bearing,
            lastPoint - HAPTIC_FEEDBACK_INTERVAL,
            lastPoint + HAPTIC_FEEDBACK_INTERVAL
        )
        if (!insideCurrentMark) {
            lastFeedbackPoint = closestMark(bearing)
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}

private fun closestMark(bearing: Azimuth): Azimuth =
    Azimuth(MathUtils.getClosestNumberFromInterval(bearing.degrees, HAPTIC_FEEDBACK_INTERVAL))
