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

package com.bobek.compass

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.philipp_bobek.oss_licenses_parser.OssLicensesParser
import de.philipp_bobek.oss_licenses_parser.ThirdPartyLicense
import de.philipp_bobek.oss_licenses_parser.ThirdPartyLicenseMetadata

class ThirdPartyLicensesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        context.resources
            .openRawResource(R.raw.third_party_license_metadata)
            .use(OssLicensesParser::parseMetadata)
            .sortedBy { metadata -> metadata.libraryName }
            .map(::getPreference)
            .forEach(screen::addPreference)

        preferenceScreen = screen
    }

    private fun getPreference(metadata: ThirdPartyLicenseMetadata): Preference {
        val preference = Preference(requireContext())
        preference.title = metadata.libraryName
        preference.setOnPreferenceClickListener {
            navigateToThirdPartyLicenseFragment(metadata)
            true
        }
        return preference
    }

    private fun navigateToThirdPartyLicenseFragment(metadata: ThirdPartyLicenseMetadata) {
        val thirdPartyLicense = requireContext().resources
            .openRawResource(R.raw.third_party_licenses)
            .use { thirdPartyLicensesFile -> OssLicensesParser.parseLicense(metadata, thirdPartyLicensesFile) }

        navigateToThirdPartyLicenseFragment(thirdPartyLicense)
    }

    private fun navigateToThirdPartyLicenseFragment(thirdPartyLicense: ThirdPartyLicense) {
        val bundle = bundleOf(
            "libraryName" to thirdPartyLicense.libraryName,
            "licenseContent" to thirdPartyLicense.licenseContent
        )
        findNavController().navigate(R.id.action_ThirdPartyLicensesFragment_to_ThirdPartyLicenseFragment, bundle)
    }
}
