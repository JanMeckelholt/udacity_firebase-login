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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import de.janmeckelholt.firebase_login_sample.databinding.FragmentMainBinding
import timber.log.Timber


class MainFragment : Fragment() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
    }

    // Get a reference to the ViewModel scoped to this Fragment
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        registerLauncher()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()
        binding.settingsBtn.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragmentToSettingsFragment)
        }

    }

    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display their name.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState() {
        val factToDisplay = viewModel.getFactToDisplay(requireContext())
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.authButton.text = getString(R.string.logout_button_text)
                    binding.authButton.setOnClickListener{
                        AuthUI.getInstance().signOut(requireContext())
                    }
                    binding.welcomeText.text = getFactWithPersonalization(factToDisplay)
                }
                LoginViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    binding.authButton.text = getString(R.string.login_btn)
                    binding.authButton.setOnClickListener {
                        launchSignInFlow()
                    }
                    binding.welcomeText.text = factToDisplay
                }
                else -> {
                    Snackbar.make(binding.root, "Error: Undefined Login State!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Exit") {
                            activity?.finishAndRemoveTask()
                        }.show()
                }
            }
        })

    }


    private fun getFactWithPersonalization(fact: String): String {
        return String.format(
            resources.getString(
                R.string.welcome_message_authed,
                FirebaseAuth.getInstance().currentUser?.displayName,
                Character.toLowerCase(fact[0]) + fact.substring(1)
            )
        )
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Timber.i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                Timber.i("Sign in unsuccessful: ${result.data}")
            }

        }
    }

    private fun launchSignInFlow() {
        activityResultLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build()
        )
    }
}