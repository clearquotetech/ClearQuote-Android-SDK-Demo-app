package io.clearquote.clearquote_sdk_demo_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.clearquote.assessment.cq_sdk.CQSDKInitializer
import io.clearquote.assessment.cq_sdk.singletons.PublicConstants
import io.clearquote.clearquote_sdk_demo_app.databinding.ActivitySdkInitializationBinding
import io.clearquote.clearquote_sdk_demo_app.support.ErrorDialog
import io.clearquote.clearquote_sdk_demo_app.support.LoadingDialog
import io.clearquote.clearquote_sdk_demo_app.support.app_shared_preferences_file_name
import io.clearquote.clearquote_sdk_demo_app.support.cq_sdk_key

class SdkInitializationActivity : AppCompatActivity() {
    // Binding
    private lateinit var binding: ActivitySdkInitializationBinding

    // CQ SDK initializer
    private lateinit var cqSDKInitializer: CQSDKInitializer

    // Dialogs
    private var sdkInitializationDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivitySdkInitializationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize vars
        cqSDKInitializer = CQSDKInitializer(this)
        sdkInitializationDialog = LoadingDialog(this, "Initializing ClearQuote SDK")

        // Set click listener on the save button
        binding.btnSave.setOnClickListener {
            // Get trimmed input
            val enteredSdkKey = binding.etSDKKey.text.toString().trim()

            // Show a loading dialog
            sdkInitializationDialog?.show()

            // Init SDK
            cqSDKInitializer.initSDK(
                sdkKey = enteredSdkKey,
                result = { isInitialized, code, message ->
                    // Dismiss loading dialog
                    sdkInitializationDialog?.dismiss()

                    // Check response
                    if (isInitialized && code == PublicConstants.sdkInitializationSuccessCode) {
                        // Save key in the shared preferences
                        getSharedPreferences(app_shared_preferences_file_name, MODE_PRIVATE).edit().putString(cq_sdk_key, enteredSdkKey).apply()

                        // Finish activity
                        finish()
                    } else {
                        showErrorDialog(message = message)
                    }
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        sdkInitializationDialog?.dismiss()
    }


    private fun showErrorDialog(message: String) {
        val errorDialog = ErrorDialog(
            mContext = this,
            message = message
        )

        // Show the dialog
        errorDialog.show()
    }
}