package io.clearquote.clearquote_sdk_demo_app

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.clearquote.assessment.cq_sdk.CQSDKInitializer
import io.clearquote.assessment.cq_sdk.R
import io.clearquote.assessment.cq_sdk.datasources.remote.network.datamodels.createQuoteApi.payload.ClientAttrs
import io.clearquote.assessment.cq_sdk.singletons.PublicConstants
import io.clearquote.clearquote_sdk_demo_app.databinding.ActivityMainBinding
import io.clearquote.clearquote_sdk_demo_app.support.ErrorDialog
import io.clearquote.clearquote_sdk_demo_app.support.LoadingDialog
import io.clearquote.clearquote_sdk_demo_app.support.QuoteCreationStatusDialog
import io.clearquote.clearquote_sdk_demo_app.support.app_shared_preferences_file_name
import io.clearquote.clearquote_sdk_demo_app.support.cq_sdk_key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // Binding
    private lateinit var binding: ActivityMainBinding

    // CQ SDK initializer
    private lateinit var cqSDKInitializer: CQSDKInitializer

    // Loading dialogs
    private var clearingSDKDataLoadingDialog: LoadingDialog? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize other vars
        cqSDKInitializer = CQSDKInitializer(context = this)
        clearingSDKDataLoadingDialog = LoadingDialog(this, "Clearing data")

        // Check offline inspections sync status
        cqSDKInitializer.checkOfflineQuoteSyncStates()
    }

    override fun onStart() {
        super.onStart()

        // Set up UI
        setUpUI()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            // Get status
            val message = intent.getStringExtra(PublicConstants.quoteCreationFlowStatusMsgKeyInIntent) ?: "Could not identify status message"
            val tempCode = intent.getIntExtra(PublicConstants.quoteCreationFlowStatusCodeKeyInIntent, -1)
            val code = if (tempCode == -1) {
                "Could not identify status code"
            } else {
                tempCode
            }

            // Update message in the dialog
            QuoteCreationStatusDialog(
                mContext = this,
                message = "Code = $code \n Message = $message"
            ).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpUI() {
        // Shared preferences
        val sharedPreferences = getSharedPreferences(
            app_shared_preferences_file_name,
            MODE_PRIVATE
        )

        // Get SDK key
        val sdkKey = sharedPreferences.getString(cq_sdk_key, "")

        // Check if it sdk key was available
        if (!sdkKey.isNullOrEmpty() && sdkKey.isNotBlank()) { // SDK key available
            // Hide configure key button
            binding.btnConfigureKey.visibility = View.GONE
            binding.btnConfigureKey.setOnClickListener(null)

            // Show log out button
            binding.btnLogOut.visibility = View.VISIBLE
            binding.btnLogOut.setOnClickListener {
                // Show a loading dialog
                clearingSDKDataLoadingDialog?.show()

                // Clear data from SDK
                CoroutineScope(Dispatchers.IO).launch {
                    // Clear SDK data
                    cqSDKInitializer.logOut()

                    // Close loading dialog
                    CoroutineScope(Dispatchers.Main).launch {
                        sharedPreferences.edit().clear().apply()
                        clearingSDKDataLoadingDialog?.dismiss()
                        setUpUI()
                    }
                }
            }

            // Show Sdk key heading
            binding.tvSdkKeyHeading.visibility = View.VISIBLE
            binding.tvSdkKeyHeading.text = "SDK Key: $sdkKey"

            // Show Start inspection
            binding.btnStartInspection.visibility = View.VISIBLE
            binding.btnStartInspection.setOnClickListener {
                // Send to sdk initialization activity
                if (cqSDKInitializer.isCQSDKInitialized()) {
                    cqSDKInitializer.startInspection(
                        activityContext = this,
                        clientAttrs = ClientAttrs(
                            userName = binding.etUserName.text.toString().trim(),
                            dealer = binding.etDealer.text.toString().trim()
                        ),
                        result = { isStarted, msg ->
                            // Show error if required
                            if (!isStarted) {
                                showErrorDialog(message = msg)
                            }
                        }
                    )
                }
            }

            // Show user name input field
            binding.tlUserName.visibility = View.VISIBLE

            // Show location input field
            binding.tlDealer.visibility = View.VISIBLE
        } else { // SDK key not available
            // Show Configure key button
            binding.btnConfigureKey.visibility = View.VISIBLE
            binding.btnConfigureKey.setOnClickListener {
                startActivity(Intent(this, SdkInitializationActivity::class.java))
            }

            // Hide the log out button
            binding.btnLogOut.visibility = View.GONE
            binding.btnLogOut.setOnClickListener(null)

            // Hide Sdk key heading
            binding.tvSdkKeyHeading.visibility = View.GONE
            binding.tvSdkKeyHeading.text = ""

            // Hide Start inspection
            binding.btnStartInspection.visibility = View.GONE
            binding.btnStartInspection.setOnClickListener(null)

            // Hide user name input field
            binding.tlUserName.visibility = View.GONE

            // Hide location input field
            binding.tlDealer.visibility = View.GONE
        }

        // Set up CQ SDK version name
        binding.tvCQSDKVersionName.text = CQSDKInitializer.sdkVersionName

        // Set up test app version name
        binding.tvTestAppVersionName.text = BuildConfig.VERSION_NAME

        // Set up app name
        binding.tvAppName.text = getString(R.string.app_name)
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