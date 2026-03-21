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

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bobek.compass.data.AppNightMode
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "DataStoreSettingsRepository"

class DataStoreSettingsRepository @Inject constructor(
    private val preferencesDataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        val TRUE_NORTH_KEY = booleanPreferencesKey(PreferenceConstants.TRUE_NORTH)
        val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey(PreferenceConstants.HAPTIC_FEEDBACK)
        val SCREEN_ORIENTATION_LOCKED_KEY = booleanPreferencesKey(PreferenceConstants.SCREEN_ORIENTATION_LOCKED)
        val NIGHT_MODE_KEY = stringPreferencesKey(PreferenceConstants.NIGHT_MODE)
        val ACCESS_LOCATION_PERMISSION_REQUESTED_KEY =
            booleanPreferencesKey(PreferenceConstants.ACCESS_LOCATION_PERMISSION_REQUESTED)
    }

    override fun getTrueNorth(): Flow<Boolean> = preferencesDataStore.data
        .map { it[TRUE_NORTH_KEY] ?: true }

    override suspend fun setTrueNorth(trueNorth: Boolean) {
        preferencesDataStore.edit { it[TRUE_NORTH_KEY] = trueNorth }
        Log.d(TAG, "Persisted trueNorth: $trueNorth")
    }

    override fun getHapticFeedback(): Flow<Boolean> = preferencesDataStore.data
        .map { it[HAPTIC_FEEDBACK_KEY] ?: true }

    override suspend fun setHapticFeedback(hapticFeedback: Boolean) {
        preferencesDataStore.edit { it[HAPTIC_FEEDBACK_KEY] = hapticFeedback }
        Log.d(TAG, "Persisted hapticFeedback: $hapticFeedback")
    }

    override fun getScreenOrientationLocked(): Flow<Boolean> = preferencesDataStore.data
        .map { it[SCREEN_ORIENTATION_LOCKED_KEY] ?: true }

    override suspend fun setScreenOrientationLocked(screenOrientationLocked: Boolean) {
        preferencesDataStore.edit { it[SCREEN_ORIENTATION_LOCKED_KEY] = screenOrientationLocked }
        Log.d(TAG, "Persisted screenOrientationLocked: $screenOrientationLocked")
    }

    override fun getNightMode(): Flow<AppNightMode> = preferencesDataStore.data
        .map { it[NIGHT_MODE_KEY] ?: AppNightMode.FOLLOW_SYSTEM.preferenceValue }
        .map { AppNightMode.forPreferenceValue(it) }

    override suspend fun setNightMode(nightMode: AppNightMode) {
        preferencesDataStore.edit { it[NIGHT_MODE_KEY] = nightMode.preferenceValue }
        Log.d(TAG, "Persisted nightMode: ${nightMode.preferenceValue}")
    }

    override fun getAccessLocationPermissionRequested(): Flow<Boolean> = preferencesDataStore.data
        .map { it[ACCESS_LOCATION_PERMISSION_REQUESTED_KEY] ?: false }

    override suspend fun setAccessLocationPermissionRequested(accessLocationPermissionRequested: Boolean) {
        preferencesDataStore.edit {
            it[ACCESS_LOCATION_PERMISSION_REQUESTED_KEY] = accessLocationPermissionRequested
        }
        Log.d(TAG, "Persisted accessLocationPermissionRequested: $accessLocationPermissionRequested")
    }
}
