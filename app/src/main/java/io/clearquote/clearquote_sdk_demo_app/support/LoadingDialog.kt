package io.clearquote.clearquote_sdk_demo_app.support

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import io.clearquote.assessment.cq_sdk.R

class LoadingDialog(context: Context, message: String) : Dialog(context) {
    private lateinit var tvMessage: TextView
    private val tempMessage = message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove the titles and set cancelable as false
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_dialog)
        setCancelable(false)

        // Find the view and set the proper message
        tvMessage = findViewById(R.id.tvMessage)
        tvMessage.text = tempMessage

        // Set transparent color for the dialog
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}