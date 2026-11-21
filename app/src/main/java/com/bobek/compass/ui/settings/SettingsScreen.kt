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

package com.bobek.compass.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bobek.compass.BuildConfig
import com.bobek.compass.ui.compass.ComposeCompassViewModel
import com.bobek.compass.ui.compass.ICompassViewModel
import com.bobek.compass.R
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.ui.ComposeAppViewModel
import com.bobek.compass.ui.IAppViewModel
import com.bobek.compass.ui.NavigateBackButton

private const val TAG = "SettingsScreen"

@Composable
@PreviewScreenSizes
fun SettingsScreen(
    appViewModel: IAppViewModel = ComposeAppViewModel(),
    compassViewModel: ICompassViewModel = ComposeCompassViewModel(),
    onBackClick: () -> Unit = {},
    onLicenseClick: () -> Unit = {},
    onThirdPartyLicensesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val trueNorth by compassViewModel.getTrueNorthFlow().collectAsState()
    val hapticFeedback by compassViewModel.getHapticFeedbackFlow().collectAsState()
    val sightingMode by compassViewModel.getSightingModeFlow().collectAsState()
    val autoShowSensorStatusDialogEnabled by compassViewModel.getAutoShowSensorStatusDialogEnabledFlow()
        .collectAsState()
    val showMagneticFieldStrength by compassViewModel.getShowMagneticFieldStrengthFlow().collectAsState()
    val nightMode by appViewModel.getNightModeFlow().collectAsState()

    var showNightModeDialog by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { NavigateBackButton(onClick = onBackClick) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(
                title = stringResource(R.string.compass),
                iconResourceId = R.drawable.ic_explore
            ) {
                SwitchSettingItem(
                    title = stringResource(R.string.true_north),
                    summary = stringResource(R.string.true_north_summary),
                    checked = trueNorth,
                    onCheckedChange = compassViewModel::setTrueNorth
                )
                SwitchSettingItem(
                    title = stringResource(R.string.haptic_feedback),
                    summary = stringResource(R.string.haptic_feedback_summary),
                    checked = hapticFeedback,
                    onCheckedChange = compassViewModel::setHapticFeedback
                )
                SwitchSettingItem(
                    title = stringResource(R.string.sighting_mode),
                    summary = stringResource(R.string.sighting_mode_summary),
                    checked = sightingMode,
                    onCheckedChange = compassViewModel::setSightingMode
                )
                SwitchSettingItem(
                    title = stringResource(R.string.sensor_status_warnings),
                    summary = stringResource(R.string.sensor_status_warnings_summary),
                    checked = autoShowSensorStatusDialogEnabled,
                    onCheckedChange = compassViewModel::setAutoShowSensorStatusDialogEnabled
                )
                SwitchSettingItem(
                    title = stringResource(R.string.magnetic_field_strength),
                    summary = stringResource(R.string.magnetic_field_strength_summary),
                    checked = showMagneticFieldStrength,
                    onCheckedChange = compassViewModel::setShowMagneticFieldStrength
                )
            }

            HorizontalDivider()
            SettingsSection(
                title = stringResource(R.string.display),
                iconResourceId = R.drawable.ic_display_settings
            ) {
                ClickableSettingItem(
                    title = stringResource(R.string.night_mode),
                    summary = stringResource(nightMode.labelResourceId),
                    onClick = { showNightModeDialog = true }
                )
            }

            HorizontalDivider()
            SettingsSection(
                title = stringResource(R.string.about),
                iconResourceId = R.drawable.ic_help
            ) {
                ClickableSettingItem(
                    title = stringResource(R.string.author),
                    summary = stringResource(R.string.author_name),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "mailto:philipp.bobek@mailbox.org".toUri())
                        context.startActivitySafely(intent)
                    }
                )
                ClickableSettingItem(
                    title = stringResource(R.string.license),
                    summary = stringResource(R.string.license_name),
                    onClick = onLicenseClick
                )
                ClickableSettingItem(
                    title = stringResource(R.string.third_party_licenses),
                    summary = stringResource(R.string.third_party_licenses_summary),
                    onClick = onThirdPartyLicensesClick
                )
                ClickableSettingItem(
                    title = stringResource(R.string.source_code),
                    summary = stringResource(R.string.source_code_name),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Kr0oked/Compass".toUri())
                        context.startActivitySafely(intent)
                    }
                )
                ListItem(
                    supportingContent = { Text(BuildConfig.VERSION_NAME) }
                ) {
                    Text(stringResource(R.string.version))
                }
            }
        }
    }

    if (showNightModeDialog) {
        NightModeDialog(
            viewModel = appViewModel,
            onDismiss = {
                showNightModeDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    iconResourceId: Int,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(iconResourceId), contentDescription = null)
        ListItem {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
    Column(modifier = Modifier.padding(start = 40.dp)) {
        content()
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        onClick = { onCheckedChange(!checked) },
        supportingContent = { Text(summary) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    ) {
        Text(title)
    }
}

@Composable
private fun ClickableSettingItem(
    title: String,
    summary: String,
    onClick: () -> Unit
) {
    ListItem(
        onClick = onClick,
        supportingContent = { Text(summary) }
    ) {
        Text(title)
    }
}

@Composable
@Preview
private fun NightModeDialog(
    viewModel: IAppViewModel = ComposeAppViewModel(),
    onDismiss: () -> Unit = {},
) {
    val nightMode by viewModel.getNightModeFlow().collectAsState()

    SingleChoiceDialog(
        SingleChoiceDialogState(
            title = stringResource(R.string.night_mode),
            entries = AppNightMode.entries,
            currentValue = nightMode.preferenceValue
        ),
        onValueSelected = { newValue ->
            viewModel.setNightMode(newValue)
            onDismiss()
        },
        onDismiss = {
            onDismiss()
        }
    )
}

private fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Log.d(TAG, "No activity found to handle intent: $intent")
    }
}
