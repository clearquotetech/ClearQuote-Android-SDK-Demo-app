package io.clearquote.clearquote_sdk_demo_app.autocaptureflow

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.clearquote.assessment.cq_sdk.CQSDKInitializer
import io.clearquote.assessment.cq_sdk.datasources.remote.network.datamodels.overlaysApi.response.LiveDetectionDTO
import io.clearquote.assessment.cq_sdk.msilAssets.CQMsilBodystyleToModelCodeMapping.getLiveDetectionDTOBasisModelCode
import io.clearquote.assessment.cq_sdk.msilAssets.models.OverlayImageData
import io.clearquote.assessment.cq_sdk.singletons.CQSDKBroadCastActions
import io.clearquote.assessment.cq_sdk.singletons.CQSDKBroadcastExtrasKey
import io.clearquote.clearquote_sdk_demo_app.adapters.VerticalRvAdapter
import io.clearquote.clearquote_sdk_demo_app.databinding.ActivityCapturedImagesBinding
import io.clearquote.clearquote_sdk_demo_app.models.InspectionData
import io.clearquote.clearquote_sdk_demo_app.models.VerticalAdapterDataItem
import io.clearquote.clearquote_sdk_demo_app.support.IntentExtrasKeys
import io.clearquote.clearquote_sdk_demo_app.support.LoadingDialog
import java.io.File

class CapturedImagesActivity : AppCompatActivity(), VerticalRvAdapter.VerticalAdapterListener {
    // Binding
    private lateinit var binding: ActivityCapturedImagesBinding

    // View model
    private lateinit var viewModel: CapturedImagesActivityViewModel

    // CQ SDK initializer
    private lateinit var cqSdkInitializer: CQSDKInitializer

    // Vertical adapter instance
    private var verticalRvAdapter: VerticalRvAdapter? = null

    // To be initialized from intent
    private var inspectionData: InspectionData? = null

    // Tobe initialized later
    private var liveDetectionDTO: LiveDetectionDTO = LiveDetectionDTO(
        bodystyle = "",
        overlays = listOf(),
        panelsToZoneMapping = null,
        tfLiteConfig = listOf()
    )

    // Loading dialogs
    private var loadingDialog: LoadingDialog? = null

    // Broadcast receiver
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    CQSDKBroadCastActions.overlayImageCaptured -> {
                        // Get extra data from intent
                        val imageCapturedForOverlayId = intent.getStringExtra(CQSDKBroadcastExtrasKey.imageCapturedForOverlayId)
                        val overlayImageFileCanonicalPath = intent.getStringExtra(CQSDKBroadcastExtrasKey.overlayImageFileCanonicalPath)

                        // Temp var
                        val overlayImageDataObjTemp = OverlayImageData(
                            imageFilePath = overlayImageFileCanonicalPath ?: ""
                        )

                        // Iterate through array and add images in the data
                        synchronized(this@CapturedImagesActivity) {
                            for (dataObj in viewModel.data) {
                                if (dataObj.overlayId == imageCapturedForOverlayId) {
                                    val tempArr = dataObj.images
                                    tempArr.add(overlayImageDataObjTemp)
                                }
                            }
                        }
                    }

                    CQSDKBroadCastActions.overlayImagesDiscarded -> {
                        // Get extra data from intent
                        val imagesDiscardedForOverlayId = intent.getStringExtra(CQSDKBroadcastExtrasKey.imagesDiscardedForOverlayId)
                        val tempDiscardedImagesList = intent.getStringExtra(CQSDKBroadcastExtrasKey.discardedImagesList)
                        val discardedImagesList = try {
                            Gson().fromJson(tempDiscardedImagesList, Array<OverlayImageData>::class.java).toList()
                        } catch (e: Exception) {
                            listOf()
                        }

                        // Iterate through array and add images in the data
                        synchronized(this@CapturedImagesActivity) {
                            for (dataObj in viewModel.data) {
                                if (dataObj.overlayId == imagesDiscardedForOverlayId) {
                                    val indexesToBeDeleted = arrayListOf<Int>()
                                    for ((imageIndex, image) in dataObj.images.withIndex()) {
                                        for (imageToBeDeleted in discardedImagesList) {
                                            if (image.imageFilePath == imageToBeDeleted.imageFilePath) {
                                                indexesToBeDeleted.add(imageIndex)
                                            }
                                        }
                                    }
                                    for (index in indexesToBeDeleted) {
                                        dataObj.images.removeAt(index)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Other overrides
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityCapturedImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize other members
        viewModel = ViewModelProvider(this)[CapturedImagesActivityViewModel::class.java]
        cqSdkInitializer = CQSDKInitializer(context = this)
        val tempInspectionData = intent.getStringExtra(IntentExtrasKeys.inspectionDataExtrasKey) ?: "{}"
        inspectionData = try {
            Gson().fromJson(tempInspectionData, InspectionData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        liveDetectionDTO = getLiveDetectionDTOBasisModelCode(
            modelCode = inspectionData?.modelCode ?: ""
        )
        loadingDialog = LoadingDialog(
            context = this,
            message = "Loading..."
        )

        // Set model code
        binding.tvModelCode.text = "Model Code: ${inspectionData?.modelCode}"

        // Set body style
        binding.tvBodyStyle.text = "Bodystyle: ${liveDetectionDTO.bodystyle}"

        // Set vertical RV
        setVerticalRv()

        // Set click listener on the exit button
        binding.btnExit.setOnClickListener {
            finish()
        }

        // Create an instance of intent filter
        val intentFilter = IntentFilter()
        intentFilter.addAction(CQSDKBroadCastActions.overlayImageCaptured)
        intentFilter.addAction(CQSDKBroadCastActions.overlayImagesDiscarded)

        // Register a broadcast
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        // Update images data
        verticalRvAdapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        // Unregister broadcast receiver
        unregisterReceiver(broadcastReceiver)

        // Delete image files from storage
        for (dataObj in viewModel.data) {
            for (overlayImage in dataObj.images) {
                File(overlayImage.imageFilePath).delete()
            }
        }

        // Clean the SDK data
        cqSdkInitializer.clearAutoCaptureData()

        // Call super
        super.onDestroy()
    }

    private fun setVerticalRv() {
        // Add data items in the data list
        for (obj in liveDetectionDTO.overlays) {
            viewModel.data.add(
                VerticalAdapterDataItem(
                    overlayId = obj.id,
                    mandatory = obj.mandatory,
                    images = arrayListOf()
                )
            )
        }

        // Create an instance of vertical items adapter
        verticalRvAdapter = VerticalRvAdapter(
            context = this,
            data = viewModel.data,
            eventsListener = this
        )

        // Set an adapter to the vertical rv
        binding.rvCapturedImages.adapter = verticalRvAdapter

        // Set layout manager
        binding.rvCapturedImages.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun captureMore(position: Int) {
        // Get overlay id
        var overlayId = ""
        for ((overlayIndex, overlay) in liveDetectionDTO.overlays.withIndex()) {
            if (overlayIndex == position) {
                overlayId = overlay.id
            }
        }

        // Show a loading dialog
        loadingDialog?.show()

        // Navigate user to auto image capture page
        cqSdkInitializer.startAutoCapture(
            activityContext = this,
            modelCode = inspectionData?.modelCode ?: "",
            overlayId = overlayId,
            result = { cqCameraInvokeResult ->
                // Dismiss a loading dialog
                loadingDialog?.dismiss()

                // Success
                if (cqCameraInvokeResult.isStarted) {
                    // Handle success case
                }

                // Failure
                else {
                    Toast.makeText(this, cqCameraInvokeResult.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    override fun deleteImage(overlayId: String, overlayImageDataObj: OverlayImageData) {
        // Delete image from the SDK data structure
        for (obj in CQSDKInitializer.hMapOfOverlayToImages) {
            if (obj.key == overlayId) {
                // Search for the index
                var index1ToBeDeleted: Int? = null
                for ((index1, image1) in obj.value.withIndex()){
                    if (image1.imageFilePath == overlayImageDataObj.imageFilePath) {
                        index1ToBeDeleted =  index1
                    }
                }

                // Delete the image using index
                if (index1ToBeDeleted != null) {
                    obj.value.removeAt(index1ToBeDeleted)
                }
            }
        }

        // Delete image from the app data structure
        var updatedIndex = 0
        for ((itemIndex, item) in viewModel.data.withIndex()) {
            if (item.overlayId == overlayId) {
                // Check which main index updated
                updatedIndex = itemIndex

                // Search for the index
                var index2ToBeDeleted: Int? = null
                for ((index2, image2) in item.images.withIndex()) {
                    if (image2.imageFilePath == overlayImageDataObj.imageFilePath) {
                        index2ToBeDeleted = index2
                    }
                }

                // Delete the image using index
                if (index2ToBeDeleted != null) {
                    item.images.removeAt(index2ToBeDeleted)
                }
            }
        }

        // Delete image file
        File(overlayImageDataObj.imageFilePath).delete()

        // Update RV
        verticalRvAdapter?.notifyItemChanged(updatedIndex)
    }
}