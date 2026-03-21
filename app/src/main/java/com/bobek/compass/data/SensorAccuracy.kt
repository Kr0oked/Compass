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

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularNodata
import androidx.compose.material.icons.filled.SignalCellularNull
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.bobek.compass.R

enum class SensorAccuracy(
    @StringRes val labelResourceId: Int,
    val imageVector: ImageVector,
    @AttrRes val iconTintAttributeResourceId: Int
) {
    NO_CONTACT(
        R.string.sensor_accuracy_no_contact,
        Icons.Default.SignalCellularNodata,
        androidx.appcompat.R.attr.colorError
    ),
    UNRELIABLE(
        R.string.sensor_accuracy_unreliable,
        Icons.Default.SignalCellularOff,
        androidx.appcompat.R.attr.colorError
    ),
    LOW(
        R.string.sensor_accuracy_low,
        Icons.Default.SignalCellularNull,
        androidx.appcompat.R.attr.colorError
    ),
    MEDIUM(
        R.string.sensor_accuracy_medium,
        Icons.Default.SignalCellularNull,
        androidx.appcompat.R.attr.colorError
    ),
    HIGH(
        R.string.sensor_accuracy_high,
        Icons.Default.SignalCellular4Bar,
        androidx.appcompat.R.attr.colorControlNormal
    )
}
