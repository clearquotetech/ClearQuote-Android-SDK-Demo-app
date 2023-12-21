package io.clearquote.clearquote_sdk_demo_app.support

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.clearquote.clearquote_sdk_demo_app.databinding.DialogMessageBinding

class MessageDialog(
    mContext: Context,
    private val message: String
) : Dialog(mContext) {
    // Binding
    private lateinit var binding: DialogMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set binding
        binding = DialogMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        // Set transparent color for the dialog
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout((context.resources.displayMetrics.widthPixels * 0.70).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        // Set error message
        binding.tvErrorMessage.text = message

        // Set click listener on the okay button
        binding.btnOkay.setOnClickListener {
            dismiss()
        }
    }
}