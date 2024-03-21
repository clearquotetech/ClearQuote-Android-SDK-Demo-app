package io.clearquote.clearquote_sdk_demo_app.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.clearquote.assessment.cq_sdk.R
import io.clearquote.assessment.cq_sdk.msilAssets.models.OverlayImageData
import io.clearquote.clearquote_sdk_demo_app.models.VerticalAdapterDataItem

class VerticalRvAdapter(
    private val context: Context,
    private val data: ArrayList<VerticalAdapterDataItem>,
    private val capMoreListener: CaptureMoreListener
) : RecyclerView.Adapter<VerticalRvAdapter.VerticalRvViewHolder>() {

    /**
     * View holder class
     */
    class VerticalRvViewHolder(itemView: View, capMoreListener: CaptureMoreListener) :
        RecyclerView.ViewHolder(itemView) {
        val captureMoreListenerHolderInstance = capMoreListener
        var tvViewTitle: TextView = itemView.findViewById(R.id.tvViewTitle)
        var tvViewTitleCount: TextView = itemView.findViewById(R.id.tvViewTitleCount)
        var tvMandatoryMark: TextView = itemView.findViewById(R.id.tvMandatoryMark)
        var rvItemRecyclerView: RecyclerView = itemView.findViewById(R.id.rvItemRecyclerView)
        var ivCaptureMore: ImageView = itemView.findViewById(R.id.ivCaptureMore)
    }

    /**
     * All overridden methods
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalRvViewHolder {
        return VerticalRvViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.upload_images_main_rv_row_item, parent, false), capMoreListener
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VerticalRvViewHolder, position: Int) {
        // Get the view object
        val viewObject = data[holder.absoluteAdapterPosition]

        // Setting the star mark for the mandatory panel
        holder.tvMandatoryMark.visibility = if (viewObject.mandatory) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Set the label
        holder.tvViewTitle.text = viewObject.overlayId

        // Set number of images
        holder.tvViewTitleCount.text = "(${viewObject.images.size})"

        // Set the inner horizontal recycler view
        setViewItemRv(
            recyclerView = holder.rvItemRecyclerView,
            data = viewObject.images
        )

        // Call listener a capture more listener interface
        holder.ivCaptureMore.setOnClickListener {
            holder.captureMoreListenerHolderInstance.captureMore(holder.absoluteAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        // Return
        return data.size
    }

    /**
     * Set the adapter for the inner recycler
     * view and keep track of all the instances which are created.
     */
    private fun setViewItemRv(
        recyclerView: RecyclerView,
        data: List<OverlayImageData>
    ) {
        val itemRecyclerAdapter = HorizontalRvAdapter(
                context = context,
                data = data
            )

        // Set adapter
        recyclerView.adapter = itemRecyclerAdapter

        // Set layout manager
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    /**
     * A listener to listen the click on capture more button
     * this click will get navigate towards the upload images activity.
     */
    interface CaptureMoreListener {
        fun captureMore(position: Int)
    }
}