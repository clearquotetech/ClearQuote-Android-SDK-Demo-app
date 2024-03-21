package io.clearquote.clearquote_sdk_demo_app.models

import io.clearquote.assessment.cq_sdk.msilAssets.models.OverlayImageData

data class VerticalAdapterDataItem(
    val overlayId: String,
    val mandatory: Boolean,
    val images: ArrayList<OverlayImageData>
)