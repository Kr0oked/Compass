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

package com.bobek.compass.licenses

import android.content.res.Resources
import com.bobek.compass.R
import de.philipp_bobek.oss_licenses_parser.OssLicensesParser
import de.philipp_bobek.oss_licenses_parser.ThirdPartyLicenseMetadata

private val LICENSE_MANUAL_RESOURCES =
    mapOf(
        "Material Symbols" to R.raw.license_apache_2_0,
    )

private val LICENSE_URL_RESOURCES =
    mapOf(
        "http://www.apache.org/licenses/LICENSE-2.0.txt" to R.raw.license_apache_2_0,
        "https://www.apache.org/licenses/LICENSE-2.0.txt" to R.raw.license_apache_2_0,
        "https://opensource.org/licenses/BSD-3-Clause" to R.raw.license_bsd_3_clause,
        "https://www.gnu.org/licenses/lgpl+gpl-3.0.txt" to R.raw.license_lgpl_gpl_3_0,
    )

object LicenseRepository {

    fun getAppLicenseContent(resources: Resources): String = readRawResource(resources, R.raw.license_gpl_3_0)

    fun getThirdPartyLibraryNames(resources: Resources): List<String> {
        val ossLicenseNames =
            resources.openRawResource(R.raw.third_party_license_metadata).use(OssLicensesParser::parseMetadata).map {
                it.libraryName
            }

        return (ossLicenseNames + LICENSE_MANUAL_RESOURCES.keys).sorted()
    }

    fun getThirdPartyLicenseContent(resources: Resources, libraryName: String): String {
        val manualResourceId = LICENSE_MANUAL_RESOURCES[libraryName]

        return if (manualResourceId != null) {
            readRawResource(resources, manualResourceId)
        } else {
            resources
                .openRawResource(R.raw.third_party_license_metadata)
                .use(OssLicensesParser::parseMetadata)
                .find { it.libraryName == libraryName }
                ?.let { getThirdPartyLicenseContent(resources, it) } ?: ""
        }
    }

    private fun getThirdPartyLicenseContent(resources: Resources, metadata: ThirdPartyLicenseMetadata): String {
        val licenseUrl =
            resources
                .openRawResource(R.raw.third_party_licenses)
                .use { OssLicensesParser.parseLicense(metadata, it) }
                .licenseContent

        return LICENSE_URL_RESOURCES[licenseUrl]?.let { readRawResource(resources, it) } ?: licenseUrl
    }

    private fun readRawResource(resources: Resources, resourceId: Int): String =
        resources.openRawResource(resourceId).bufferedReader().readText()
}
