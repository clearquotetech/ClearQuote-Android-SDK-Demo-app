package io.clearquote.clearquote_sdk_demo_app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import io.clearquote.clearquote_sdk_demo_app.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.clearquote.assessment.cq_sdk.msilAssets.models.OverlayImageData

class HorizontalRvAdapter(
    private val context: Context,
    private val data: List<OverlayImageData>
) : RecyclerView.Adapter<HorizontalRvAdapter.HorizontalRvAdapterViewHolder>() {

    /**
     * View holder class
     */
    class HorizontalRvAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView = itemView.findViewById(R.id.ivImage)
    }

    /**
     * All overridden methods
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalRvAdapterViewHolder {
        return HorizontalRvAdapterViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.horizontal_adapter_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HorizontalRvAdapterViewHolder, position: Int) {
        // Get the view object
        val viewObject = data[holder.absoluteAdapterPosition]

        // Load the image
        Glide.with(holder.itemView.context)
            .load(viewObject.photoFile)
            .into(holder.ivImage)
    }

    override fun getItemCount(): Int {
        // Return
        return data.size
    }
}