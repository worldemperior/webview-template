package com.template.webview

import android.app.Activity
import com.google.android.ump.*

object ConsentManager {

    fun requestConsent(
        activity: Activity,
        onComplete: () -> Unit
    ) {

        val params =

            ConsentRequestParameters
                .Builder()
                .build()

        val consentInformation =

            UserMessagingPlatform
                .getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(

            activity,

            params,

            {

                if (
                    consentInformation
                        .isConsentFormAvailable
                ) {

                    loadForm(
                        activity,
                        consentInformation,
                        onComplete
                    )

                } else {

                    onComplete()
                }
            },

            {

                onComplete()
            }
        )
    }

    private fun loadForm(

        activity: Activity,

        consentInformation:
        ConsentInformation,

        onComplete: () -> Unit

    ) {

        UserMessagingPlatform
            .loadConsentForm(

                activity,

                { form ->

                    if (
                        consentInformation
                            .consentStatus ==
                        ConsentInformation
                            .ConsentStatus
                            .REQUIRED
                    ) {

                        form.show(activity) {

                            onComplete()
                        }

                    } else {

                        onComplete()
                    }
                },

                {

                    onComplete()
                }
            )
    }
}