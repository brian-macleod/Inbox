package com.github.macleod.inbox.adapter

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.github.macleod.inbox.R
import com.github.macleod.inbox.data.model.*

class ConversationViewHolder private constructor(private val layout: ViewGroup, private val numParticipants: Int): ViewHolder<Message?>(layout)
{
    var onRowClick: ((Row) -> Unit)? = null
    private val rowList = ArrayList<Row>()

    /**
     * Companion
     *
     * @constructor Create empty Companion
     */
    companion object
    {
        private const val RECEIVED_MESSAGE_BG_COLOR = "#E9EAEB"
        private const val SENT_MESSAGE_BG_COLOR     = "#0F84FF"
        private const val RECEIVED_MESSAGE_FG_COLOR = "#000000"
        private const val SENT_MESSAGE_FG_COLOR     = "#FFFFFF"

        /**
         * Create
         *
         * @param context
         * @return
         */
        fun create(context: Context, numParticipants: Int): ConversationViewHolder
        {
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            val layout = LinearLayout(context)
            layout.layoutParams = layoutParams
            layout.orientation = LinearLayout.VERTICAL

            return ConversationViewHolder(layout, numParticipants)
        }
    }

    /**
     * Row
     *
     * @constructor
     *
     * @param parent
     */
    inner class Row(val index: Int, parent: View) // TODO: Clean this up
    {
        internal val layout: LinearLayout        = parent.findViewById(R.id.conversation_item_layout)
        internal val avatarHolder: CardView      = parent.findViewById(R.id.conversation_item_avatar_holder)
        internal val contentLayout: LinearLayout = parent.findViewById(R.id.conversation_item_content_layout)
        internal val messageBubble: CardView = parent.findViewById(R.id.conversation_item_message_bubble)
        internal val addressLabel: TextView      = parent.findViewById(R.id.conversation_item_address_label)
        internal val messageText: TextView       = parent.findViewById(R.id.conversation_item_message_text)
        internal val messageImage: ImageView = parent.findViewById(R.id.conversation_item_message_image)
        internal val spacer: Space = parent.findViewById(R.id.conversation_item_spacer)
    }

    /**
     * Create row
     *
     * @return
     */
    private fun createRow(index: Int): Row
    {
        val context = itemView.context
        val inflater = LayoutInflater.from(context)
        val messageView = inflater.inflate(R.layout.row_conversation_item, layout, false)

        val row = Row(index, messageView)
        row.layout.setOnClickListener {
            onRowClick?.invoke(row)
        }

        return row
    }

    /**
     * Bind
     *
     * @param item
     */
    override fun bind(item: Message?)
    {
        if (item is SMSMessage)
        {
            setSMSMessage(item)
        }
        else
        {
            setMMSMessage(item as MMSMessage)
        }
    }

    /**
     * Set SMS message
     *
     * @param message
     */
    private fun setSMSMessage(message: SMSMessage)
    {
        layout.removeAllViewsInLayout()
        val row = if (rowList.isEmpty()) createRow(rowList.size) else rowList[0]
        if (rowList.isEmpty())
        {
            rowList.add(row)
        }

        val messageType = message.type
        orientRow(row, messageType)

        row.messageBubble.removeAllViewsInLayout()
        row.messageText.text = message.text
        row.messageBubble.addView(row.messageText)

        layout.addView(row.layout)
    }

    /**
     * Set MMS message
     *
     * @param message
     */
    private fun setMMSMessage(message: MMSMessage)
    {
        layout.removeAllViewsInLayout()
        for ((rowNum, attachment) in message.attachments.withIndex())
        {
            val row = if (rowList.size > rowNum) rowList[rowNum] else createRow(rowList.size)
            if (rowNum >= rowList.size)
            {
                rowList.add(row)
            }

            val messageType = message.type
            orientRow(row, messageType)

            val contact = message.contact
            row.addressLabel.text = if (contact != null) contact.displayName else message.address

            row.messageBubble.removeAllViewsInLayout()
            if (messageType == MessageType.SENT)
            {
                row.messageBubble.addView(row.spacer) // TODO:
            }

            if (attachment is TextAttachment)
            {
                row.messageText.text = attachment.text
                row.messageBubble.addView(row.messageText)
            }
            else if (attachment is ImageAttachment)
            {
                row.messageImage.setImageBitmap(attachment.image)
                row.messageBubble.addView(row.messageImage)
            }

            layout.addView(row.layout)
        }
    }

    /**
     * Orient row
     *
     * @param row
     * @param messageType
     */
    private fun orientRow(row: Row, messageType: MessageType)
    {
        row.layout.removeAllViewsInLayout()
        row.layout.addView(row.contentLayout)
        row.contentLayout.removeAllViewsInLayout()

        var avatarHolderLeftMargin = 20
        var avatarHolderRightMargin = 0
        var layoutGravity = Gravity.END
        var messageBackgroundColor = SENT_MESSAGE_BG_COLOR
        var messageFontColor = SENT_MESSAGE_FG_COLOR

        val contentWeight = if (numParticipants > 1) 75f else 85f

        if (messageType == MessageType.RECEIVED)
        {
            val tmp = avatarHolderLeftMargin
            avatarHolderLeftMargin = avatarHolderRightMargin
            avatarHolderRightMargin = tmp

            layoutGravity = Gravity.START

            messageBackgroundColor = RECEIVED_MESSAGE_BG_COLOR
            messageFontColor = RECEIVED_MESSAGE_FG_COLOR

            if (numParticipants > 1)
            {
                row.contentLayout.addView(row.addressLabel)
                row.layout.addView(row.avatarHolder, 0)
            }
        }

        row.messageBubble.setBackgroundColor(Color.parseColor(messageBackgroundColor))
        row.messageText.setTextColor(Color.parseColor(messageFontColor))

        row.contentLayout.addView(row.messageBubble)

        val contentLayoutParams = row.contentLayout.layoutParams as LinearLayout.LayoutParams
        contentLayoutParams.weight = contentWeight

        val avatarHolderLayoutParams = row.avatarHolder.layoutParams as ViewGroup.MarginLayoutParams
        avatarHolderLayoutParams.leftMargin = avatarHolderLeftMargin
        avatarHolderLayoutParams.rightMargin = avatarHolderRightMargin

        row.layout.gravity = layoutGravity
        row.contentLayout.gravity = layoutGravity
    }
}