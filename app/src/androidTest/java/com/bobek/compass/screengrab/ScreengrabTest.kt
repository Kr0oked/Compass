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

package com.bobek.compass.screengrab

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.ui.ComposeAppViewModel
import com.bobek.compass.ui.MainContent
import com.bobek.compass.ui.compass.ComposeCompassViewModel
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.IconVisibility
import tools.fastlane.screengrab.locale.LocaleTestRule

@LargeTest
@RunWith(AndroidJUnit4::class)
class ScreengrabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun grabScreenshot() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        val appViewModel = ComposeAppViewModel(AppNightMode.NO)

        composeTestRule.setContent {
            MainContent(
                appViewModel = appViewModel,
                compassViewModel = ComposeCompassViewModel(
                    compassReading = CompassReading.INITIAL.copy(azimuth = Azimuth(320.0f), reliable = true),
                    sensorAccuracy = SensorAccuracy.HIGH,
                    screenOrientationLocked = false
                )
            )
        }
        composeTestRule.waitForIdle()
        enableCleanStatusBar()
        Screengrab.screenshot("1")

        appViewModel.setNightMode(AppNightMode.YES)
        composeTestRule.waitForIdle()
        enableCleanStatusBar()
        Screengrab.screenshot("2")
    }

    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            enableCleanStatusBar()
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            CleanStatusBar.disable()
        }

        /**
         * The mobile network icon doesn't reliably respect the app's light/dark status bar
         * appearance on all system images, sometimes rendering white regardless of theme.
         * Hiding it avoids that inconsistency instead of chasing it.
         */
        private fun enableCleanStatusBar() {
            CleanStatusBar()
                .setMobileNetworkVisibility(IconVisibility.HIDE)
                .enable()
        }
    }
}
