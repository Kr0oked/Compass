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
import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class LicenseRepositoryTest {

    @Test
    fun getAppLicenseContentReadsGplRawResource() {
        val resources = FakeResources(R.raw.license_gpl_3_0 to "GPL v3 license text".toByteArray())
        assertEquals("GPL v3 license text", LicenseRepository.getAppLicenseContent(resources))
    }

    @Test
    fun getThirdPartyLibraryNamesCombinesParsedAndManualLibrariesSortedAlphabetically() {
        val (metadata, licenses) = thirdPartyLicenseFixture("Zeta" to "zeta license", "Alpha" to "alpha license")
        val resources =
            FakeResources(
                R.raw.third_party_license_metadata to metadata,
                R.raw.third_party_licenses to licenses,
            )

        assertEquals(
            listOf("Alpha", "Material Symbols", "Zeta"),
            LicenseRepository.getThirdPartyLibraryNames(resources),
        )
    }

    @Test
    fun getThirdPartyLicenseContentReturnsManualResourceForMaterialSymbols() {
        val resources = FakeResources(R.raw.license_apache_2_0 to "Apache 2.0 text".toByteArray())
        assertEquals("Apache 2.0 text", LicenseRepository.getThirdPartyLicenseContent(resources, "Material Symbols"))
    }

    @Test
    fun getThirdPartyLicenseContentMapsKnownLicenseUrlToBundledLicenseText() {
        val (metadata, licenses) =
            thirdPartyLicenseFixture(
                "SomeLib" to "https://opensource.org/licenses/BSD-3-Clause",
            )
        val resources =
            FakeResources(
                R.raw.third_party_license_metadata to metadata,
                R.raw.third_party_licenses to licenses,
                R.raw.license_bsd_3_clause to "BSD 3-Clause text".toByteArray(),
            )

        assertEquals("BSD 3-Clause text", LicenseRepository.getThirdPartyLicenseContent(resources, "SomeLib"))
    }

    @Test
    fun getThirdPartyLicenseContentReturnsRawLicenseTextForUnknownUrl() {
        val licenseContent = "Some custom license text that is not a known URL"
        val (metadata, licenses) = thirdPartyLicenseFixture("CustomLib" to licenseContent)
        val resources =
            FakeResources(
                R.raw.third_party_license_metadata to metadata,
                R.raw.third_party_licenses to licenses,
            )

        assertEquals(licenseContent, LicenseRepository.getThirdPartyLicenseContent(resources, "CustomLib"))
    }

    @Test
    fun getThirdPartyLicenseContentReturnsEmptyStringForUnknownLibrary() {
        val (metadata, licenses) = thirdPartyLicenseFixture()
        val resources =
            FakeResources(
                R.raw.third_party_license_metadata to metadata,
                R.raw.third_party_licenses to licenses,
            )

        assertEquals("", LicenseRepository.getThirdPartyLicenseContent(resources, "DoesNotExist"))
    }

    private fun FakeResources(vararg rawResources: Pair<Int, ByteArray>) = FakeResources(rawResources.toMap())

    private fun thirdPartyLicenseFixture(vararg libraries: Pair<String, String>): Pair<ByteArray, ByteArray> {
        val licenses = StringBuilder()
        val metadataLines = libraries.map { (name, content) ->
            val offset = licenses.toString().toByteArray().size
            licenses.append(content)
            "$offset:${content.toByteArray().size} $name"
        }
        return metadataLines.joinToString("\n").toByteArray() to licenses.toString().toByteArray()
    }
}

@Suppress("DEPRECATION")
private class FakeResources(private val rawResources: Map<Int, ByteArray>) : Resources(null, null, null) {
    override fun openRawResource(id: Int): InputStream =
        rawResources[id]?.let { ByteArrayInputStream(it) } ?: throw NotFoundException("Unknown resource id: $id")
}
