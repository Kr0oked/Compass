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

package com.bobek.compass.ui.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.bobek.compass.R
import de.philipp_bobek.oss_licenses_parser.ThirdPartyLicenseMetadata

@Composable
@PreviewScreenSizes
@OptIn(ExperimentalMaterial3Api::class)
fun ThirdPartyLicensesScreen(
    @PreviewParameter(ThirdPartyLicensesScreenStateProvider::class) licenses: List<ThirdPartyLicenseMetadata>,
    onBackClick: () -> Unit = {},
    onLicenseClick: (ThirdPartyLicenseMetadata) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.third_party_licenses)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(licenses) { license ->
                ListItem(
                    headlineContent = { Text(license.libraryName) },
                    modifier = Modifier.clickable { onLicenseClick(license) }
                )
            }
        }
    }
}

private class ThirdPartyLicensesScreenStateProvider : PreviewParameterProvider<List<ThirdPartyLicenseMetadata>> {
    override val values: Sequence<List<ThirdPartyLicenseMetadata>> = sequenceOf(
        listOf(
            ThirdPartyLicenseMetadata("Library A", 0, 0),
            ThirdPartyLicenseMetadata("Library B", 0, 0),
            ThirdPartyLicenseMetadata("Library C", 0, 0)
        )
    )
}
