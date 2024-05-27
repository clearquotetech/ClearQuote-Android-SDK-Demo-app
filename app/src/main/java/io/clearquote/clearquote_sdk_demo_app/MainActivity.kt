package io.clearquote.clearquote_sdk_demo_app

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.clearquote.assessment.cq_sdk.CQSDKInitializer
import io.clearquote.assessment.cq_sdk.datasources.remote.network.datamodels.createQuoteApi.payload.ClientAttrs
import io.clearquote.assessment.cq_sdk.models.CustomerDetails
import io.clearquote.assessment.cq_sdk.models.InputDetails
import io.clearquote.assessment.cq_sdk.models.UserFlowParams
import io.clearquote.assessment.cq_sdk.models.VehicleDetails
import io.clearquote.assessment.cq_sdk.singletons.PublicConstants
import io.clearquote.clearquote_sdk_demo_app.autocaptureflow.InputActivity
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
    private var loadingDialog: LoadingDialog? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize other vars
        cqSDKInitializer = CQSDKInitializer(context = this)
        clearingSDKDataLoadingDialog = LoadingDialog(this, "Clearing data")
        loadingDialog = LoadingDialog(this, "Loading...")

        // Check offline inspections sync status
        cqSDKInitializer.checkOfflineQuoteSyncCompleteStatus{}

        // Set click listener on the open cq native app
        binding.btnOpenCqNativeApp.setOnClickListener {
            openCqNativeApp()
        }

        // Trigger sync of offline inspections
        cqSDKInitializer.triggerOfflineSync()
    }

    override fun onResume() {
        super.onResume()

        // Set up UI
        setUpUI()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            // Get status
            val identifier = intent.getStringExtra(PublicConstants.quoteCreationFlowStatusIdentifierKeyInIntent) ?: "Could not identify Identifier"
            val message = intent.getStringExtra(PublicConstants.quoteCreationFlowStatusMsgKeyInIntent) ?: "Could not identify status message"
            val tempCode = intent.getIntExtra(PublicConstants.quoteCreationFlowStatusCodeKeyInIntent, -1)

            // Check if identifier is valid
            if (identifier == PublicConstants.quoteCreationFlowStatusIdentifier) {
                // Get code
                val code = if (tempCode == -1) {
                    "Could not identify status code"
                } else {
                    tempCode
                }

                // Update message in the dialog
                Handler(mainLooper).postDelayed({
                    QuoteCreationStatusDialog(
                        mContext = this,
                        message = "Code = $code \n Message = $message"
                    ).show()
                }, 1000L)

            }
        }
    }

    override fun onStop() {
        // Dismiss loading dialogs
        clearingSDKDataLoadingDialog?.dismiss()
        loadingDialog?.dismiss()

        // Call super
        super.onStop()
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

        // Get SDK user details
        val sdkUserDetails = cqSDKInitializer.getUserDetails()

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

            // Show dealer code
            binding.tvDealerCode.visibility = View.VISIBLE
            binding.tvDealerCode.text = "Dealer Code: ${sdkUserDetails.dealerCode}"

            // Show user name
            binding.tvUserName.visibility = View.VISIBLE
            binding.tvUserName.text = "Username: ${sdkUserDetails.userName}"

            // Show Start inspection
            binding.btnStartInspection.visibility = View.VISIBLE
            binding.btnStartInspection.setOnClickListener {
                // Send to sdk initialization activity
                if (cqSDKInitializer.isCQSDKInitialized()) {
                    // Show a loading dialog
                    loadingDialog?.show()

                    // Create an instance of client attrs
                    val clientAttrs = ClientAttrs(
                        userName = binding.etUserName.text.toString().trim(),
                        dealer = binding.etDealer.text.toString().trim(),
                        dealerIdentifier = binding.etDealerIdentifier.text.toString().trim(),
                        client_unique_id = binding.etClientUniqueId.text.toString().trim()
                    )

                    // Customer details
                    val customerDetails = CustomerDetails(
                        name = binding.etCustomerName.text.toString(),
                        email = binding.etCustomerEmail.text.toString(),
                        dialCode = binding.etCustomerDialCode.text.toString(),
                        phoneNumber = binding.etCustomerPhoneNumber.text.toString(),
                    )

                    // Vehicle details
                    val vehicleDetails = VehicleDetails(
                        regNumber = binding.etRegNumber.text.toString() ,
                        make = binding.etMake.text.toString(),
                        model = binding.etModel.text.toString(),
                        bodyStyle = binding.etBodyStyle.text.toString()
                    )

                    // Create an instance of input details
                    val inputDetails = InputDetails(
                        vehicleDetails = vehicleDetails,
                        customerDetails = customerDetails
                    )

                    // Create an instance of user flow params
                    val userFlowParams = UserFlowParams(
                        isOffline = binding.swOfflineMode.isChecked,
                        skipInputPage = false
                    )

                    // Make request to start an inspection
                    cqSDKInitializer.startInspection(
                        activity = this,
                        clientAttrs = clientAttrs,
                        inputDetails = inputDetails,
                        userFlowParams = userFlowParams,
                        result = { isStarted, msg, code ->
                            // Show error if required
                            if (!isStarted) {
                                // Dismiss the loading dialog
                                loadingDialog?.dismiss()

                                // Show error
                                showErrorDialog(message = "message= $msg, code= $code")
                            }
                        }
                    )
                }
            }

            // Show start inspection : skip input
            binding.btnStartInspectionWithSkipInput.visibility = View.VISIBLE
            binding.btnStartInspectionWithSkipInput.setOnClickListener {
                // Send to sdk initialization activity
                if (cqSDKInitializer.isCQSDKInitialized()) {
                    // Show a loading dialog
                    loadingDialog?.show()

                    // Create an instance of client attrs
                    val clientAttrs = ClientAttrs(
                        userName = binding.etUserName.text.toString().trim(),
                        dealer = binding.etDealer.text.toString().trim(),
                        dealerIdentifier = binding.etDealerIdentifier.text.toString().trim(),
                        client_unique_id = binding.etClientUniqueId.text.toString().trim()
                    )

                    // Customer details
                    val customerDetails = CustomerDetails(
                        name = binding.etCustomerName.text.toString(),
                        email = binding.etCustomerEmail.text.toString(),
                        dialCode = binding.etCustomerDialCode.text.toString(),
                        phoneNumber = binding.etCustomerPhoneNumber.text.toString(),
                    )

                    // Vehicle details
                    val vehicleDetails = VehicleDetails(
                        regNumber = binding.etRegNumber.text.toString() ,
                        make = binding.etMake.text.toString(),
                        model = binding.etModel.text.toString(),
                        bodyStyle = binding.etBodyStyle.text.toString()
                    )

                    // Create an instance of input details
                    val inputDetails = InputDetails(
                        vehicleDetails = vehicleDetails,
                        customerDetails = customerDetails
                    )

                    // Create an instance of user flow params
                    val userFlowParams = UserFlowParams(
                        isOffline = binding.swOfflineMode.isChecked,
                        skipInputPage = true
                    )

                    // Make request to start an inspection
                    cqSDKInitializer.startInspection(
                        activity = this,
                        clientAttrs = clientAttrs,
                        inputDetails = inputDetails,
                        userFlowParams = userFlowParams,
                        result = { isStarted, msg, code ->
                            // Show error if required
                            if (!isStarted) {
                                // Dismiss the loading dialog
                                loadingDialog?.dismiss()

                                // Show error
                                showErrorDialog(message = "message= $msg, code= $code")
                            }
                        }
                    )
                }
            }

            // Show offline mode switch
            binding.llOfflineModeSwitchContainer.visibility = View.VISIBLE

            // Show client attrs heading
            binding.tvClientAttrsHeading.visibility = View.VISIBLE

            // Show user name input field
            binding.tlUserName.visibility = View.VISIBLE

            // Show dealer input field
            binding.tlDealer.visibility = View.VISIBLE

            // Show dealer identifier input field
            binding.tlDealerIdentifier.visibility = View.VISIBLE

            // Show client unique id input field
            binding.tlClientUniqueId.visibility = View.VISIBLE

            // Show offline quote sync complete status
            binding.btnOfflineQuoteSyncCompleteStatus.visibility = View.VISIBLE

            // Set click listener from the check quote sync complete status button
            binding.btnOfflineQuoteSyncCompleteStatus.setOnClickListener {
                cqSDKInitializer.checkOfflineQuoteSyncCompleteStatus {
                    Toast.makeText(this@MainActivity, "Result: $it", Toast.LENGTH_LONG).show()
                }
            }

            // Show auto capture button
            binding.btnStartAutoCaptureFlow.visibility = View.VISIBLE

            // Add click listener on the start auto capture button
            binding.btnStartAutoCaptureFlow.setOnClickListener{
                startAutoCaptureFlow()
            }

            // Show input details heading
            binding.tvInputDetailsHeading.visibility = View.VISIBLE

            // Show reg number ip
            binding.tlRegNumber.visibility = View.VISIBLE

            // Show make ip
            binding.tlMake.visibility = View.VISIBLE

            // Show model ip
            binding.tlModel.visibility = View.VISIBLE

            // Show bodystyle ip
            binding.tlBodyStyle.visibility = View.VISIBLE

            // Show customer name ip
            binding.tlCustomerName.visibility = View.VISIBLE

            // Show customer email ip
            binding.tlCustomerEmail.visibility = View.VISIBLE

            // Show phone number ll
            binding.llDialCodeAndPhoneNumber.visibility = View.VISIBLE

            // Show dividers
            binding.md1.visibility = View.VISIBLE
            binding.md2.visibility = View.VISIBLE
            binding.md3.visibility = View.VISIBLE
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

            // Hide Dealer code heading
            binding.tvDealerCode.visibility = View.GONE
            binding.tvDealerCode.text = ""

            // Hide User name heading
            binding.tvUserName.visibility = View.GONE
            binding.tvUserName.text = ""

            // Hide Start inspection
            binding.btnStartInspection.visibility = View.GONE
            binding.btnStartInspection.setOnClickListener(null)

            // Hide stat inspection : skip input
            binding.btnStartInspectionWithSkipInput.visibility = View.GONE
            binding.btnStartInspectionWithSkipInput.setOnClickListener(null)

            // Hide offline mode switch
            binding.llOfflineModeSwitchContainer.visibility = View.GONE

            // Hide client attrs heading
            binding.tvClientAttrsHeading.visibility = View.GONE

            // Hide user name input field
            binding.tlUserName.visibility = View.GONE

            // Hide location input field
            binding.tlDealer.visibility = View.GONE

            // Hide dealer identifier input field
            binding.tlDealerIdentifier.visibility = View.GONE

            // Hide client unique id input field
            binding.tlClientUniqueId.visibility = View.GONE

            // Hide offline quote sync complete status button
            binding.btnOfflineQuoteSyncCompleteStatus.visibility = View.GONE
            binding.btnOfflineQuoteSyncCompleteStatus.setOnClickListener(null)

            // Hide auto capture button
            binding.btnStartAutoCaptureFlow.visibility = View.GONE
            binding.btnStartAutoCaptureFlow.setOnClickListener(null)

            // Hide input details heading
            binding.tvInputDetailsHeading.visibility = View.GONE

            // Hide reg number ip
            binding.tlRegNumber.visibility = View.GONE

            // Hide make ip
            binding.tlMake.visibility = View.GONE

            // Hide model ip
            binding.tlModel.visibility = View.GONE

            // Hide bodystyle ip
            binding.tlBodyStyle.visibility = View.GONE

            // Hide customer name ip
            binding.tlCustomerName.visibility = View.GONE

            // Hide customer email ip
            binding.tlCustomerEmail.visibility = View.GONE

            // Hide phone number ll
            binding.llDialCodeAndPhoneNumber.visibility = View.GONE

            // Hide dividers
            binding.md1.visibility = View.GONE
            binding.md2.visibility = View.GONE
            binding.md3.visibility = View.GONE
        }

        // Set up CQ SDK version name
        binding.tvCQSDKVersionName.text = CQSDKInitializer.sdkVersionName

        // Set up test app version name
        binding.tvTestAppVersionName.text = "CQ Android SDK Demo app version: ${BuildConfig.VERSION_NAME}"

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

    private fun openCqNativeApp() {
        val packageName = "io.clearquote.assessment"
        val cName = ComponentName(packageName, "${packageName}.main.MainActivity")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.component = cName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not find the target app", Toast.LENGTH_LONG).show()
        }
    }

    private fun startAutoCaptureFlow() {
        // CQ SDK is initialized
        if (cqSDKInitializer.isCQSDKInitialized()) {
            startActivity(Intent(this, InputActivity::class.java))
        }

        // SDK is not initialized
        else {
            Toast.makeText(this, "SDK is not initialized", Toast.LENGTH_LONG).show()
        }
    }
}