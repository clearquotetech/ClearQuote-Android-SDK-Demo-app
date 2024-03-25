package io.clearquote.clearquote_sdk_demo_app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import io.clearquote.clearquote_sdk_demo_app.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.clearquote.assessment.cq_sdk.msilAssets.models.OverlayImageData
import java.io.File

class HorizontalRvAdapter(
    private val context: Context,
    private val data: List<OverlayImageData>,
    private val eventsListener: VerticalRvAdapter.VerticalAdapterListener,
    private val overlayId: String
) : RecyclerView.Adapter<HorizontalRvAdapter.HorizontalRvAdapterViewHolder>() {

    /**
     * View holder class
     */
    class HorizontalRvAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        var btnDeleteImage: ImageButton = itemView.findViewById(R.id.btnDeleteImage)
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
            .load(File(viewObject.imageFilePath))
            .into(holder.ivImage)

        // Add click listener on the delete icon
        holder.btnDeleteImage.setOnClickListener {
            eventsListener.deleteImage(
                overlayId = overlayId,
                overlayImageDataObj = viewObject
            )
        }
    }

    override fun getItemCount(): Int {
        // Return
        return data.size
    }
}