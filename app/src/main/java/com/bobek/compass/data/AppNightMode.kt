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

import androidx.annotation.StringRes
import com.bobek.compass.R
import com.bobek.compass.settings.PreferenceConstants

enum class AppNightMode(
    @StringRes override val labelResourceId: Int,
    override val preferenceValue: String
) : PreferenceChoice {

    FOLLOW_SYSTEM(R.string.night_mode_follow_system, PreferenceConstants.NIGHT_MODE_VALUE_FOLLOW_SYSTEM),
    NO(R.string.night_mode_no, PreferenceConstants.NIGHT_MODE_VALUE_NO),
    YES(R.string.night_mode_yes, PreferenceConstants.NIGHT_MODE_VALUE_YES);

    companion object {
        fun forPreferenceValue(preferenceValue: String): AppNightMode =
            when (preferenceValue) {
                PreferenceConstants.NIGHT_MODE_VALUE_NO -> NO
                PreferenceConstants.NIGHT_MODE_VALUE_YES -> YES
                else -> FOLLOW_SYSTEM
            }
    }
}
