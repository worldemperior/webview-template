package com.template.webview

import android.app.Activity
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {

    fun requestConsent(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // This single unified method loads and shows the form ONLY if required (EEA/UK first launch)
                // If consent is already OBTAINED or NOT_REQUIRED, it immediately passes to the callback!
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    // Proceed regardless of form errors so the app doesn't lock up for the user
                    onComplete()
                }
            },
            { requestConsentError ->
                // Fallback for network issues / offline mode
                onComplete()
            }
        )
    }
}