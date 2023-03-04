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

package com.bobek.compass.preference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

private const val TAG = "PreferenceMigrations"

private const val MIGRATION_V11 = "migration_v11"

class PreferenceMigrations(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * Migrate night_mode preference from integer to string values
     */
    fun migrateV5() {
        val migrated = sharedPreferences.getBoolean(MIGRATION_V11, false)

        if (!migrated) {
            val nightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            val edit = sharedPreferences.edit()
            edit.remove("night_mode")

            when (nightMode) {
                AppCompatDelegate.MODE_NIGHT_NO -> edit.putString("night_mode", "no")
                AppCompatDelegate.MODE_NIGHT_YES -> edit.putString("night_mode", "yes")
                else -> edit.putString("night_mode", "follow_system")
            }

            edit.putBoolean(MIGRATION_V11, true)
            edit.apply()
            Log.i(TAG, "V11 - night_mode preference migrated")
        }
    }
}
