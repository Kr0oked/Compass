/*
 * This file is part of Compass.
 * Copyright (C) 2024 Philipp Bobek <philipp.bobek@mailbox.org>
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

package com.bobek.compass.screengrab

import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bobek.compass.AbstractAndroidTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.locale.LocaleTestRule

@LargeTest
class ScreengrabTest : AbstractAndroidTest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun grabScreenshot() {
        val screenshotName = InstrumentationRegistry.getArguments()
            .getString("screenshotName", "default")

        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        Screengrab.screenshot(screenshotName)
    }

    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            CleanStatusBar.enableWithDefaults()
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            CleanStatusBar.disable()
        }
    }
}
