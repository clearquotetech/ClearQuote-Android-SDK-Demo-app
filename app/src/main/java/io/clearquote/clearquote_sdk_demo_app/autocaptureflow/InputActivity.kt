package io.clearquote.clearquote_sdk_demo_app.autocaptureflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import io.clearquote.clearquote_sdk_demo_app.databinding.ActivityInputBinding
import io.clearquote.clearquote_sdk_demo_app.models.InspectionData
import io.clearquote.clearquote_sdk_demo_app.support.IntentExtrasKeys

class InputActivity : AppCompatActivity() {
    // Binding
    private lateinit var binding: ActivityInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener on the start auto image capture button
        binding.btnNext.setOnClickListener {
            navigateToCapturedImagesScreen()
        }
    }

    private fun navigateToCapturedImagesScreen() {
        // Create an instance of the intent
        val intent = Intent(this, CapturedImagesActivity::class.java)

        // Create an instance of inspection data class
        val inspectionData = InspectionData(
            modelCode = binding.etModelCode.text.toString(),
            overlayId = null
        )

        // Create a json string of inspection data
        val inspectionDataJsonString = Gson().toJson(inspectionData)

        // Add intent extras
        intent.putExtra(IntentExtrasKeys.inspectionDataExtrasKey, inspectionDataJsonString)

        // Navigate to captured images activity
        startActivity(intent)

        // Finish
        finish()
    }
}