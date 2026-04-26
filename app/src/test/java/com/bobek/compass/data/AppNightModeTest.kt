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

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNightModeTest {

    @Test
    fun followSystemMapsToCorrectPreferenceValue() {
        assertEquals("follow_system", AppNightMode.FOLLOW_SYSTEM.preferenceValue)
    }

    @Test
    fun noMapsToCorrectPreferenceValue() {
        assertEquals("no", AppNightMode.NO.preferenceValue)
    }

    @Test
    fun yesMapsToCorrectPreferenceValue() {
        assertEquals("yes", AppNightMode.YES.preferenceValue)
    }

    @Test
    fun forPreferenceValueReturnsFollowSystem() {
        assertEquals(AppNightMode.FOLLOW_SYSTEM, AppNightMode.forPreferenceValue("follow_system"))
    }

    @Test
    fun forPreferenceValueReturnsNo() {
        assertEquals(AppNightMode.NO, AppNightMode.forPreferenceValue("no"))
    }

    @Test
    fun forPreferenceValueReturnsYes() {
        assertEquals(AppNightMode.YES, AppNightMode.forPreferenceValue("yes"))
    }

    @Test
    fun forPreferenceValueFallsBackToFollowSystemForUnknownValue() {
        assertEquals(AppNightMode.FOLLOW_SYSTEM, AppNightMode.forPreferenceValue("unknown"))
    }

    @Test
    fun forPreferenceValueFallsBackToFollowSystemForEmptyValue() {
        assertEquals(AppNightMode.FOLLOW_SYSTEM, AppNightMode.forPreferenceValue(""))
    }
}
