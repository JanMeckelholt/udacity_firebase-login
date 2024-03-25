/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.janmeckelholt.firebase_login_sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {

    // Get a reference to the ViewModel scoped to this Fragment
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer {authentictionState ->
            when (authentictionState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> Timber.i("Authenticteed")
                LoginViewModel.AuthenticationState.UNAUTHENTICATED -> findNavController().navigate(R.id.loginFragment)
                else -> Timber.e("New $authentictionState state that does not require any UI change")
            }
        })
    }
}
