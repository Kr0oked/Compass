/*
 * This file is part of Compass.
 * Copyright (C) 2021 Philipp Bobek <philipp.bobek@mailbox.org>
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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bobek.compass.R

enum class SensorAccuracy(@StringRes val textResourceId: Int, @DrawableRes val iconResourceId: Int) {
    NO_CONTACT(R.string.sensor_accuracy_no_contact, R.drawable.ic_sensor_no_contact),
    UNRELIABLE(R.string.sensor_accuracy_unreliable, R.drawable.ic_sensor_unreliable),
    LOW(R.string.sensor_accuracy_low, R.drawable.ic_sensor_low),
    MEDIUM(R.string.sensor_accuracy_medium, R.drawable.ic_sensor_medium),
    HIGH(R.string.sensor_accuracy_high, R.drawable.ic_sensor_high)
}
