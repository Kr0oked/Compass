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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.data.PreferenceChoice

@Composable
@Preview
fun <T : PreferenceChoice> SingleChoiceDialog(
    @PreviewParameter(SingleChoiceDialogStateProvider::class) state: SingleChoiceDialogState<T>,
    onValueSelected: (T) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
            ) {
                state.entries.forEach { entry ->
                    val selected = entry.preferenceValue == state.currentValue
                    ListItem(
                        selected = selected,
                        onClick = { onValueSelected(entry) },
                        leadingContent = { RadioButton(selected = selected, onClick = null) },
                        // Transparent so the rows blend into the dialog surface
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = stringResource(entry.labelResourceId),
                            style = if (selected) {
                                MaterialTheme.typography.bodyLargeEmphasized
                            } else {
                                MaterialTheme.typography.bodyLarge
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

data class SingleChoiceDialogState<T : PreferenceChoice>(
    val title: String,
    val entries: List<T>,
    val currentValue: String,
)

private class SingleChoiceDialogStateProvider : PreviewParameterProvider<SingleChoiceDialogState<AppNightMode>> {
    override val values: Sequence<SingleChoiceDialogState<AppNightMode>> = sequenceOf(
        SingleChoiceDialogState(
            title = "The title",
            entries = AppNightMode.entries,
            currentValue = AppNightMode.FOLLOW_SYSTEM.preferenceValue
        )
    )
}
