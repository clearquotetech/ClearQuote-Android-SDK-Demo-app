package io.clearquote.clearquote_sdk_demo_app.support

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.color.MaterialColors
import io.clearquote.clearquote_sdk_demo_app.R

class SampleView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        val color = MaterialColors.getColor(this, R.attr.colorTransparent)
        setBackgroundColor(color)
    }
}