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

package com.bobek.compass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private val SETTINGS_DEBOUNCE = 1.seconds

interface IAppViewModel {
    fun getNightModeFlow(): StateFlow<AppNightMode>

    fun setNightMode(nightMode: AppNightMode)
}

@HiltViewModel
@OptIn(FlowPreview::class)
class AppViewModel
@Inject
constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel(), IAppViewModel {

    private val nightModeFlow = MutableStateFlow(AppNightMode.FOLLOW_SYSTEM)

    init {
        viewModelScope.launch { initFromSettings() }
        viewModelScope.launch {
            nightModeFlow.drop(1).debounce(SETTINGS_DEBOUNCE).collect { settingsRepository.setNightMode(it) }
        }
    }

    private suspend fun initFromSettings() {
        settingsRepository.getNightMode().firstOrNull()?.let { nightModeFlow.value = it }
    }

    override fun getNightModeFlow(): StateFlow<AppNightMode> = nightModeFlow

    override fun setNightMode(nightMode: AppNightMode) {
        nightModeFlow.value = nightMode
    }
}

class ComposeAppViewModel(
    nightMode: AppNightMode = AppNightMode.FOLLOW_SYSTEM,
) : IAppViewModel {

    private val nightModeFlow = MutableStateFlow(nightMode)

    override fun getNightModeFlow(): StateFlow<AppNightMode> = nightModeFlow

    override fun setNightMode(nightMode: AppNightMode) {
        nightModeFlow.value = nightMode
    }
}
