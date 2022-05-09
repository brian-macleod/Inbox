package com.github.macleod.inbox.adapter

import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.macleod.inbox.R
import com.github.macleod.inbox.data.model.*

/**
 * Inbox view holder
 *
 * @constructor
 *
 * @param view
 */
class InboxViewHolder(view: View): ViewHolder<Conversation?>(view)
{
    private val participantsLabel: TextView = view.findViewById(R.id.inbox_item_participants_label)
    private val dateLabel: TextView = view.findViewById(R.id.inbox_item_date_label)
    private val snippetContainer: LinearLayout = view.findViewById(R.id.inbox_item_snippet_container)
    private val snippetLabel: TextView = view.findViewById(R.id.inbox_item_snippet_label)
    private val snippetThumbnail: ImageView = view.findViewById(R.id.inbox_item_snippet_thumbnail)

    /**
     * Bind
     *
     * @param item
     */
    override fun bind(item: Conversation?)
    {
        if (item == null)
        {
            // TODO: Show a loading spinner
            snippetLabel.text = "[TODO: Loading Spinner]"
        }
        else
        {
            val dateFormat = SimpleDateFormat("MM/dd/yy")
            participantsLabel.text = item.getParticipantLabel()
            dateLabel.text = dateFormat.format(item.date)

            setSnippet(item.lastMessage)
        }
    }

    /**
     * Set snippet
     *
     * @param message
     */
    private fun setSnippet(message: Message?)
    {
        var foundAttachment = false
        if (message is MMSMessage)
        {
            val attachments = message.attachments
            val it = attachments.listIterator(attachments.size)
            while (!foundAttachment && it.hasPrevious())
            {
                val attachment = it.previous()
                if (attachment is TextAttachment)
                {
                    setSnippet(attachment.text)
                    foundAttachment = true
                }
                else if (attachment is ImageAttachment)
                {
                    setSnippet(attachment.image)
                    foundAttachment = true
                }
            }
        }
        else if (message is SMSMessage)
        {
            setSnippet(message.text)
            foundAttachment = true
        }

        if (!foundAttachment)
        {
            setSnippet("[ERROR]")
        }
    }

    /**
     * Set snippet
     *
     * @param text
     */
    private fun setSnippet(text: String)
    {
        snippetContainer.removeAllViewsInLayout()
        snippetLabel.text = text
        snippetContainer.addView(snippetLabel)
    }

    /**
     * Set snippet
     *
     * @param image
     */
    private fun setSnippet(image: Bitmap)
    {
        snippetContainer.removeAllViewsInLayout()
        snippetContainer.addView(snippetThumbnail)
        snippetThumbnail.setImageBitmap(image)
    }
}