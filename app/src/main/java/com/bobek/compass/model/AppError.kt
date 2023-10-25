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

import androidx.annotation.StringRes
import com.bobek.compass.R

enum class AppError(@StringRes val messageId: Int) {
    SENSOR_MANAGER_NOT_PRESENT(R.string.sensor_error_message),
    ROTATION_VECTOR_SENSOR_NOT_AVAILABLE(R.string.sensor_error_message),
    ROTATION_VECTOR_SENSOR_FAILED(R.string.sensor_error_message),
    LOCATION_MANAGER_NOT_PRESENT(R.string.location_error_message),
    LOCATION_DISABLED(R.string.location_error_message),
    NO_LOCATION_PROVIDER_AVAILABLE(R.string.location_error_message)
}
