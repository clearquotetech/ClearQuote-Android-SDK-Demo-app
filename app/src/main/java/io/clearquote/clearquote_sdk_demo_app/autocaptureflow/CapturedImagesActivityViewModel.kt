package io.clearquote.clearquote_sdk_demo_app.autocaptureflow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.clearquote.clearquote_sdk_demo_app.models.VerticalAdapterDataItem

class CapturedImagesActivityViewModel(application: Application) : AndroidViewModel(application) {
    // Images data
    val data = arrayListOf<VerticalAdapterDataItem>()
}