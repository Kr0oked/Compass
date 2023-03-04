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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.bobek.compass.model.AppNightMode

private const val TAG = "PreferenceStore"

class PreferenceStore(context: Context, lifecycle: Lifecycle) {

    val screenOrientationLocked = MutableLiveData<Boolean>()
    val nightMode = MutableLiveData<AppNightMode>()

    private val preferenceStoreLifecycleObserver = PreferenceStoreLifecycleObserver()
    private val sharedPreferenceChangeListener = SharedPreferenceChangeListener()
    private val screenOrientationLockedObserver = getScreenOrientationLockedObserver()
    private val nightModeObserver = getNightModeObserver()

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        updateScreenOrientationLocked()
        updateNightMode()

        lifecycle.addObserver(preferenceStoreLifecycleObserver)
    }

    private inner class PreferenceStoreLifecycleObserver : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            screenOrientationLocked.observeForever(screenOrientationLockedObserver)
            nightMode.observeForever(nightModeObserver)

            sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            screenOrientationLocked.removeObserver(screenOrientationLockedObserver)
            nightMode.removeObserver(nightModeObserver)
        }
    }

    private inner class SharedPreferenceChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            when (key) {
                PreferenceConstants.SCREEN_ORIENTATION_LOCKED -> updateScreenOrientationLocked()
                PreferenceConstants.NIGHT_MODE -> updateNightMode()
            }
        }
    }

    private fun updateScreenOrientationLocked() {
        val storedValue = sharedPreferences.getBoolean(PreferenceConstants.SCREEN_ORIENTATION_LOCKED, false)
        if (screenOrientationLocked.value != storedValue) {
            screenOrientationLocked.value = storedValue
        }
    }

    private fun updateNightMode() {
        val storedNightMode = sharedPreferences
            .getString(PreferenceConstants.NIGHT_MODE, AppNightMode.FOLLOW_SYSTEM.preferenceValue)
            ?.let { AppNightMode.forPreferenceValue(it) }
            ?: run { AppNightMode.FOLLOW_SYSTEM }

        if (nightMode.value != storedNightMode) {
            nightMode.value = storedNightMode
        }
    }

    private fun getScreenOrientationLockedObserver(): (t: Boolean) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putBoolean(PreferenceConstants.SCREEN_ORIENTATION_LOCKED, it)
        edit.apply()
        Log.d(TAG, "Persisted screenOrientationLocked: $it")
    }

    private fun getNightModeObserver(): (t: AppNightMode) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putString(PreferenceConstants.NIGHT_MODE, it.preferenceValue)
        edit.apply()
        Log.d(TAG, "Persisted nightMode: $it")
    }
}
