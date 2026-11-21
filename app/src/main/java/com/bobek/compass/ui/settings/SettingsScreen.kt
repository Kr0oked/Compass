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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private const val TAG = "SettingsScreen"

@Composable
@PreviewScreenSizes
@OptIn(ExperimentalMaterial3Api::class)
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
    val autoShowSensorStatusDialogEnabled by compassViewModel.getAutoShowSensorStatusDialogEnabledFlow().collectAsState()
    val showMagneticFieldStrength by compassViewModel.getShowMagneticFieldStrengthFlow().collectAsState()
    val nightMode by appViewModel.getNightModeFlow().collectAsState()

    var showNightModeDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
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
                icon = {
                    Icon(painter = painterResource(R.drawable.ic_explore), contentDescription = null)
                }
            ) {
                ListItem(
                    onClick = { compassViewModel.setTrueNorth(!trueNorth) },
                    supportingContent = { Text(stringResource(R.string.true_north_summary)) },
                    trailingContent = {
                        Switch(
                            checked = trueNorth,
                            onCheckedChange = null
                        )
                    }
                ) {
                    Text(stringResource(R.string.true_north))
                }

                ListItem(
                    onClick = { compassViewModel.setHapticFeedback(!hapticFeedback) },
                    supportingContent = { Text(stringResource(R.string.haptic_feedback_summary)) },
                    trailingContent = {
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = null
                        )
                    }
                ) {
                    Text(stringResource(R.string.haptic_feedback))
                }

                ListItem(
                    onClick = { compassViewModel.setSightingMode(!sightingMode) },
                    supportingContent = { Text(stringResource(R.string.sighting_mode_summary)) },
                    trailingContent = {
                        Switch(
                            checked = sightingMode,
                            onCheckedChange = null
                        )
                    }
                ) {
                    Text(stringResource(R.string.sighting_mode))
                }

                ListItem(
                    onClick = {
                        compassViewModel.setAutoShowSensorStatusDialogEnabled(!autoShowSensorStatusDialogEnabled)
                    },
                    supportingContent = { Text(stringResource(R.string.sensor_status_warnings_summary)) },
                    trailingContent = {
                        Switch(
                            checked = autoShowSensorStatusDialogEnabled,
                            onCheckedChange = null
                        )
                    }
                ) {
                    Text(stringResource(R.string.sensor_status_warnings))
                }

                ListItem(
                    onClick = { compassViewModel.setShowMagneticFieldStrength(!showMagneticFieldStrength) },
                    supportingContent = { Text(stringResource(R.string.magnetic_field_strength_summary)) },
                    trailingContent = {
                        Switch(
                            checked = showMagneticFieldStrength,
                            onCheckedChange = null
                        )
                    }
                ) {
                    Text(stringResource(R.string.magnetic_field_strength))
                }
            }

            HorizontalDivider()
            SettingsSection(
                title = stringResource(R.string.display),
                icon = {
                    Icon(painter = painterResource(R.drawable.ic_display_settings), contentDescription = null)
                }
            ) {
                ListItem(
                    onClick = { showNightModeDialog = true },
                    supportingContent = { Text(stringResource(nightMode.labelResourceId)) }
                ) {
                    Text(stringResource(R.string.night_mode))
                }
            }

            HorizontalDivider()
            SettingsSection(
                title = stringResource(R.string.about),
                icon = {
                    Icon(painter = painterResource(R.drawable.ic_help), contentDescription = null)
                }
            ) {
                ListItem(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "mailto:philipp.bobek@mailbox.org".toUri())
                        context.startActivitySafely(intent)
                    },
                    supportingContent = { Text(stringResource(R.string.author_name)) }
                ) {
                    Text(stringResource(R.string.author))
                }

                ListItem(
                    onClick = onLicenseClick,
                    supportingContent = { Text(stringResource(R.string.license_name)) }
                ) {
                    Text(stringResource(R.string.license))
                }

                ListItem(
                    onClick = onThirdPartyLicensesClick,
                    supportingContent = { Text(stringResource(R.string.third_party_licenses_summary)) }
                ) {
                    Text(stringResource(R.string.third_party_licenses))
                }

                ListItem(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Kr0oked/Compass".toUri())
                        context.startActivitySafely(intent)
                    },
                    supportingContent = { Text(stringResource(R.string.source_code_name)) }
                ) {
                    Text(stringResource(R.string.source_code))
                }

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
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
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
