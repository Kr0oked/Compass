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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bobek.compass.R

enum class SensorAccuracy(
    @StringRes val labelResourceId: Int,
    @DrawableRes val iconResourceId: Int,
    val isWarning: Boolean
) {
    NO_CONTACT(
        R.string.sensor_accuracy_no_contact,
        R.drawable.ic_signal_cellular_nodata,
        isWarning = true
    ),
    UNRELIABLE(
        R.string.sensor_accuracy_unreliable,
        R.drawable.ic_signal_cellular_off,
        isWarning = true
    ),
    LOW(
        R.string.sensor_accuracy_low,
        R.drawable.ic_signal_cellular_0_bar,
        isWarning = true
    ),
    MEDIUM(
        R.string.sensor_accuracy_medium,
        R.drawable.ic_signal_cellular_2_bar,
        isWarning = true
    ),
    HIGH(
        R.string.sensor_accuracy_high,
        R.drawable.ic_signal_cellular_4_bar,
        isWarning = false
    )
}
