package com.github.macleod.inbox.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import com.github.macleod.inbox.R

class ImageViewHolder(view: View): ViewHolder<Bitmap?>(view)
{
    private val imageView: ImageView = view.findViewById(R.id.image_item_image)

    /**
     * Bind
     *
     * @param item
     */
    override fun bind(item: Bitmap?)
    {
        if (item != null)
        {
            imageView.setImageBitmap(item)
        }
        else
        {
            // TODO: Handle this case
        }
    }
}