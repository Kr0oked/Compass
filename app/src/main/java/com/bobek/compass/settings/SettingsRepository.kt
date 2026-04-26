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

package com.bobek.compass.settings

import com.bobek.compass.data.AppNightMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTrueNorth(): Flow<Boolean>
    suspend fun setTrueNorth(trueNorth: Boolean)
    fun getHapticFeedback(): Flow<Boolean>
    suspend fun setHapticFeedback(hapticFeedback: Boolean)
    fun getScreenOrientationLocked(): Flow<Boolean>
    suspend fun setScreenOrientationLocked(screenOrientationLocked: Boolean)
    fun getNightMode(): Flow<AppNightMode>
    suspend fun setNightMode(nightMode: AppNightMode)
    fun getAccessLocationPermissionRequested(): Flow<Boolean>
    suspend fun setAccessLocationPermissionRequested(accessLocationPermissionRequested: Boolean)
}
