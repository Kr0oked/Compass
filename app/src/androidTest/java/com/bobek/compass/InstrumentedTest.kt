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

package com.bobek.compass

import android.Manifest
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.ui.TestConstants
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule as createAndroidComposeTestRule

// Remapped device-to-world rotation matrices (row-major, world = East-North-Up).
private val FLAT_FACING_NORTH = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
private val FLAT_FACING_SOUTH = floatArrayOf(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
private val UPRIGHT_SCREEN_FACING_NORTH = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, -1f, 0f)
private val FACE_DOWN = floatArrayOf(1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f)

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class InstrumentedTest {

    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> =
        createAndroidComposeTestRule(
            activityRule = ActivityScenarioRule<MainActivity>(
                Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                    .putExtra(OPTION_INSTRUMENTED_TEST, true)
            ),
            activityProvider = { rule ->
                var activity: MainActivity? = null
                rule.scenario.onActivity { activity = it }
                activity ?: throw IllegalStateException("Activity not found")
            }
        )

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Before
    fun setup() {
        waitUntilCompassIsDisplayed()
    }

    @Test
    fun initialState() {
        onTopBarTitle().assertIsDisplayed()
        onSettingsButton().assertIsDisplayed()
    }

    @Test
    fun compass() {
        // Phone flat, screen up: identity matrix faces north, 180 about the vertical axis faces south.
        setDeviceRotation(FLAT_FACING_NORTH)
        onCompassRose().assertStateDescription(expectedStateDescription(R.string.cardinal_direction_north, 0))

        setDeviceRotation(FLAT_FACING_SOUTH)
        onCompassRose().assertStateDescription(expectedStateDescription(R.string.cardinal_direction_south, 180))
    }

    @Test
    fun compassIntercardinalDirections() {
        val sectorCenters = listOf(
            22.5f to R.string.cardinal_direction_north_northeast,
            67.5f to R.string.cardinal_direction_east_northeast,
            112.5f to R.string.cardinal_direction_east_southeast,
            157.5f to R.string.cardinal_direction_south_southeast,
            202.5f to R.string.cardinal_direction_south_southwest,
            247.5f to R.string.cardinal_direction_west_southwest,
            292.5f to R.string.cardinal_direction_west_northwest,
            337.5f to R.string.cardinal_direction_north_northwest
        )

        for ((azimuthDegrees, expectedStringRes) in sectorCenters) {
            setDeviceRotation(flatRotationAt(azimuthDegrees))
            onCompassRose().assertStateDescription(expectedStateDescription(expectedStringRes, azimuthDegrees.roundToInt()))
        }
    }

    @Test
    fun sightingStripShownWhenUpright() {
        setDeviceRotation(UPRIGHT_SCREEN_FACING_NORTH)
        composeTestRule.onNodeWithTag(TestConstants.COMPASS_STRIP).assertIsDisplayed()
    }

    @Test
    fun holdLevelHintShownWhenFaceDown() {
        setDeviceRotation(FACE_DOWN)
        composeTestRule.onNodeWithText(getString(R.string.compass_hold_level)).assertIsDisplayed()
    }

    @Test
    fun sensorStatusDialog() {
        onSensorStatusButton().performClick()

        setAccuracy(SensorAccuracy.NO_CONTACT)
        assertSensorAccuracyText(R.string.sensor_accuracy_no_contact)

        setAccuracy(SensorAccuracy.UNRELIABLE)
        assertSensorAccuracyText(R.string.sensor_accuracy_unreliable)

        setAccuracy(SensorAccuracy.LOW)
        assertSensorAccuracyText(R.string.sensor_accuracy_low)

        setAccuracy(SensorAccuracy.MEDIUM)
        assertSensorAccuracyText(R.string.sensor_accuracy_medium)

        setAccuracy(SensorAccuracy.HIGH)
        assertSensorAccuracyText(R.string.sensor_accuracy_high)

        onOkButton().performClick()
    }


    @Test
    fun navigatingToSettingsAndBackShowsCompassScreenAgain() {
        onSettingsButton().performClick()
        composeTestRule.waitForIdle()
        onTopBarTitle(R.string.settings).assertIsDisplayed()

        pressBack()
        composeTestRule.waitForIdle()
        onTopBarTitle().assertIsDisplayed()
    }

    @Test
    fun changingNightModeUpdatesSelectedTheme() {
        openSettings()

        onNightModeOption(R.string.night_mode_follow_system).assertIsDisplayed()

        selectNightMode(R.string.night_mode_yes)
        onNightModeOption(R.string.night_mode_yes).assertIsDisplayed()

        selectNightMode(R.string.night_mode_follow_system)
        onNightModeOption(R.string.night_mode_follow_system).assertIsDisplayed()
    }

    @Test
    fun navigatingToLicenseShowsGplLicenseTextAndBackReturnsToSettings() {
        openSettings()

        onLicenseListItem().performClick()
        composeTestRule.waitForIdle()

        onTopBarTitle(R.string.license_name).assertIsDisplayed()
        waitUntilTextExists("GNU GENERAL PUBLIC LICENSE")

        pressBack()
        composeTestRule.waitForIdle()
        onTopBarTitle(R.string.settings).assertIsDisplayed()
    }

    @Test
    fun navigatingToThirdPartyLicenseShowsApacheLicenseForMaterialSymbols() {
        openSettings()

        onThirdPartyLicensesListItem().performScrollTo().performClick()
        composeTestRule.waitForIdle()
        onTopBarTitle(R.string.third_party_licenses).assertIsDisplayed()

        scrollToListItem("Material Symbols")
        onListItem("Material Symbols").performClick()
        composeTestRule.waitForIdle()

        onTopBarTitle("Material Symbols").assertIsDisplayed()
        waitUntilTextExists("Apache License")

        pressBack()
        composeTestRule.waitForIdle()
        onTopBarTitle(R.string.third_party_licenses).assertIsDisplayed()
    }

    private fun expectedStateDescription(@StringRes cardinalDirectionResId: Int, degrees: Int): String {
        val cardinalDirectionText = getString(cardinalDirectionResId)
        val degreesText = composeTestRule.activity.getString(R.string.degrees, degrees)
        return composeTestRule.activity.getString(
            R.string.compass_rose_state_description,
            cardinalDirectionText,
            degreesText
        )
    }

    private fun assertSensorAccuracyText(@StringRes resourceId: Int) {
        val expectedText = composeTestRule.activity.getString(resourceId)
        onSensorAccuracyText().assertTextEquals(expectedText)
    }

    private fun waitUntilCompassIsDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 15_000L) { onCompassRose().isDisplayed() }
        composeTestRule.waitForIdle()
    }

    private fun openSettings() {
        composeTestRule.waitForIdle()
        onSettingsButton().performClick()
        composeTestRule.waitForIdle()
    }

    private fun selectNightMode(@StringRes labelResId: Int) {
        composeTestRule.waitForIdle()
        onNightModeListItem().performClick()
        composeTestRule.waitForIdle()
        onNightModeOption(labelResId).performClick()
        composeTestRule.waitForIdle()
    }

    private fun setDeviceRotation(rotationMatrix: FloatArray) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.compassViewModel.setDeviceRotation(rotationMatrix)
        }
        composeTestRule.waitForIdle()
    }

    // Phone flat, screen up, top edge pointing at the given compass bearing (0° = north, clockwise).
    // Generalizes FLAT_FACING_NORTH (0°) and FLAT_FACING_SOUTH (180°) to arbitrary azimuths.
    private fun flatRotationAt(azimuthDegrees: Float): FloatArray {
        val radians = Math.toRadians(azimuthDegrees.toDouble())
        val sin = sin(radians).toFloat()
        val cos = cos(radians).toFloat()
        return floatArrayOf(cos, sin, 0f, -sin, cos, 0f, 0f, 0f, 1f)
    }

    private fun setAccuracy(accuracy: SensorAccuracy) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.compassViewModel.setSensorAccuracy(accuracy)
        }
        composeTestRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.assertStateDescription(value: String) {
        assert(hasStateDescription(value))
    }

    private fun onCompassRose(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(TestConstants.COMPASS_ROSE)

    private fun onSensorStatusButton(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(TestConstants.SENSOR_STATUS_BUTTON)

    private fun onSensorAccuracyText(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(TestConstants.SENSOR_ACCURACY_TEXT)

    private fun onLicenseListItem(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.license))

    private fun onThirdPartyLicensesListItem(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.third_party_licenses))

    private fun onNightModeListItem(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.night_mode))

    private fun onNightModeOption(@StringRes labelResId: Int): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(labelResId))

    private fun onListItem(text: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(text)

    private fun scrollToListItem(text: String) {
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    }

    private fun waitUntilTextExists(text: String, timeoutMillis: Long = 5_000) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(text, substring = true), timeoutMillis = timeoutMillis)
    }

    private fun onTopBarTitle(@StringRes titleResId: Int = R.string.compass): SemanticsNodeInteraction =
        onTopBarTitle(getString(titleResId))

    private fun onTopBarTitle(title: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(title)

    private fun onSettingsButton(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithContentDescription(getString(R.string.settings))

    private fun onOkButton(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.ok))

    private fun getString(@StringRes resId: Int): String = composeTestRule.activity.getString(resId)
}
