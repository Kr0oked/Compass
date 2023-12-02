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

package com.bobek.compass.model

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bobek.compass.R

enum class SensorAccuracy(
    @StringRes val textResourceId: Int,
    @DrawableRes val iconResourceId: Int,
    @AttrRes val iconTintAttributeResourceId: Int
) {
    NO_CONTACT(
        R.string.sensor_accuracy_no_contact,
        R.drawable.ic_sensor_no_contact,
        androidx.appcompat.R.attr.colorError
    ),
    UNRELIABLE(
        R.string.sensor_accuracy_unreliable,
        R.drawable.ic_sensor_unreliable,
        androidx.appcompat.R.attr.colorError
    ),
    LOW(
        R.string.sensor_accuracy_low,
        R.drawable.ic_sensor_low,
        androidx.appcompat.R.attr.colorError
    ),
    MEDIUM(
        R.string.sensor_accuracy_medium,
        R.drawable.ic_sensor_medium,
        androidx.appcompat.R.attr.colorControlNormal
    ),
    HIGH(
        R.string.sensor_accuracy_high,
        R.drawable.ic_sensor_high,
        androidx.appcompat.R.attr.colorControlNormal
    )
}
