package com.github.macleod.inbox.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.provider.Telephony.Mms
import android.util.Size
import com.github.macleod.inbox.InboxApp
import com.github.macleod.inbox.data.model.*
import kotlin.math.roundToInt

internal class MMSDataSource: MessageDataSource<MMSMessageData>
{
    /**
     * Get content resolver
     *
     * @return
     */
    private fun getContentResolver(): ContentResolver
    {
        val context = InboxApp.context
        return context.contentResolver
    }

    /**
     * Get message IDs
     *
     * @param threadID
     * @return
     */
    override suspend fun getMessageIDs(threadID: Long): List<Pair<Long, Long>>
    {
        val projection = arrayOf(Mms._ID, Mms.DATE)
        val selection = Mms.THREAD_ID + " = ?"
        val selectionArgs = arrayOf(threadID.toString())
        val sortOrder = Mms.DATE + " DESC"

        val idList = ArrayList<Pair<Long, Long>>()

        val contentResolver = getContentResolver()
        contentResolver.query(Mms.CONTENT_URI, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            if (cursor != null)
            {
                val idIndex = cursor.getColumnIndex(Mms._ID)
                val dateIndex = cursor.getColumnIndex(Mms.DATE)

                while (cursor.moveToNext())
                {
                    val id = cursor.getLong(idIndex)
                    val date = cursor.getLong(dateIndex) * 1000

                    val message = Pair(id, date)
                    idList.add(message)
                }
            }
        }
        return idList
    }

    /**
     * Get message descriptors
     *
     * @param threadID
     * @param messageIDs
     * @return
     */
    override suspend fun getMessageData(threadID: Long, vararg messageIDs: Long): List<MMSMessageData>
    {
        val messageDataList = mutableListOf<MMSMessageData>()
        if (messageIDs.isEmpty()) return messageDataList

        val projection = arrayOf(Mms._ID,
                                 Mms.DATE_SENT,
                                 Mms.DATE,
                                 Mms.MESSAGE_TYPE)

        val selectionArgList = mutableListOf<String>()
        selectionArgList.add(threadID.toString())

        val inListBuilder = StringBuilder()
        for (id in messageIDs)
        {
            if (inListBuilder.isNotEmpty())
            {
                inListBuilder.append(", ")
            }
            inListBuilder.append("?")
            selectionArgList.add(id.toString())
        }

        val selection = Mms.THREAD_ID + " = ? AND " + Mms._ID + " IN (" + inListBuilder.toString() + ")"
        val selectionArgs = selectionArgList.toTypedArray()
        val sortOrder = Mms.DATE + " DESC"

        val contentResolver = getContentResolver()
        contentResolver.query(Mms.CONTENT_URI, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndex(Mms._ID)
            val dateSentIndex = cursor.getColumnIndex(Mms.DATE_SENT)
            val dateReceivedIndex = cursor.getColumnIndex(Mms.DATE)
            val typeIndex = cursor.getColumnIndex(Mms.MESSAGE_TYPE)

            while (cursor.moveToNext())
            {
                val id = cursor.getLong(idIndex)
                val dateSent = cursor.getLong(dateSentIndex) * 1000
                val dateReceived = cursor.getLong(dateReceivedIndex) * 1000
                val address = getMMSAddress(id)
                val typeInt = cursor.getInt(typeIndex)
                val type = MessageType.fromInt(typeInt) ?: MessageType.RECEIVED // TODO

                if (address == null)
                {
                    continue // TODO: Handle this case
                }

                val partList = getAttachments(id)

                val messageData = MMSMessageData(id, dateSent, dateReceived, address, type, partList)
                messageDataList.add(messageData)
            }
        }
        return messageDataList
    }

    /**
     * Get data for last message
     *
     * @param threadID
     * @return
     */
    override suspend fun getDataForLastMessage(threadID: Long): MMSMessageData?
    {
        val projection = arrayOf(Mms._ID,
                                 Mms.DATE_SENT,
                                 Mms.DATE,
                                 Mms.MESSAGE_TYPE)
        val selection = Mms.THREAD_ID + " = ?"
        val selectionArgs = arrayOf(threadID.toString())
        val sortOrder = Mms.DATE + " DESC"

        val contentResolver = getContentResolver()
        contentResolver.query(Mms.CONTENT_URI, null, selection, selectionArgs, sortOrder)?.use { cursor -> // TODO: Add a projection
            if (cursor != null)
            {
                val idIndex = cursor.getColumnIndex(Mms._ID)
                val dateSentIndex = cursor.getColumnIndex(Mms.DATE_SENT)
                val dateReceivedIndex = cursor.getColumnIndex(Mms.DATE)
                val typeIndex = cursor.getColumnIndex(Mms.MESSAGE_TYPE)

                if (cursor.moveToFirst())
                {
                    val id = cursor.getLong(idIndex)
                    val dateSent = cursor.getLong(dateSentIndex) * 1000
                    val dateReceived = cursor.getLong(dateReceivedIndex) * 1000
                    val address = getMMSAddress(id)
                    val typeInt = cursor.getInt(typeIndex)
                    val type = MessageType.fromInt(typeInt) ?: MessageType.RECEIVED // TODO

                    if (address == null)
                    {
                        return null
                    }

                    val partList = getAttachments(id)

                    return MMSMessageData(id, dateSent, dateReceived, address, type, partList)
                }
            }
        }
        return null
    }

    /**
     * Get MMS address
     *
     * @param messageID
     * @return
     */
    private suspend fun getMMSAddress(messageID: Long): String?
    {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            Mms.Addr.getAddrUriForMessage(messageID.toString())
        }
        else
        {
            Mms.CONTENT_URI.buildUpon().appendPath(messageID.toString()).appendPath("addr").build() // TODO: Clean this up
        }

        val selection = Mms.Addr.TYPE + " = ?"
        val selectionArgs = arrayOf("137") // TODO: This should be a constant... https://developer.android.com/reference/kotlin/android/provider/Telephony.Mms.Addr#TYPE:kotlin.String

        val contentResolver = getContentResolver()
        contentResolver.query(uri, null, selection, selectionArgs, null)?.use { cursor ->
            if (cursor != null)
            {
                val addressIndex = cursor.getColumnIndex(Mms.Addr.ADDRESS)
                if (cursor.moveToFirst())
                {
                    return cursor.getString(addressIndex)
                }
            }
        }
        return null
    }

    /**
     * Get attachments
     *
     * @param messageID
     * @return
     */
    private suspend fun getAttachments(messageID: Long): List<Attachment>
    {
        val selection = Mms.Part.MSG_ID + " = ?"
        val selectionArgs = arrayOf(messageID.toString())

        val attachmentList = mutableListOf<Attachment>()
        val contentResolver = getContentResolver()
        contentResolver.query(Mms.Part.CONTENT_URI, null, selection, selectionArgs, null)?.use { cursor ->
            if (cursor != null)
            {
                val partIDIndex = cursor.getColumnIndex(Mms.Part._ID)
                val contentTypeIndex = cursor.getColumnIndex(Mms.Part.CONTENT_TYPE)
                val dataIndex = cursor.getColumnIndex(Mms.Part._DATA)
                val textIndex = cursor.getColumnIndex(Mms.Part.TEXT)

                while (cursor.moveToNext())
                {
                    val partID = cursor.getLong(partIDIndex)
                    val mimeType = cursor.getString(contentTypeIndex)
                    val data = cursor.getString(dataIndex)
                    var text = cursor.getString(textIndex)
                    var content: String? = null

                    val contentType = ContentType.getContentTypeFromMIMEType(mimeType)
                    val attachment = when (contentType)
                    {
                        ContentType.AUDIO ->
                        {
                            TextAttachment("[Audio Attachment]") // TODO:
                        }
                        ContentType.IMAGE ->
                        {
                            val image = getMMSImageThumbnail(partID)
                            if (image != null)
                            {
                                ImageAttachment(image)
                            }
                            else
                            {
                                TextAttachment("[ERROR]") // TODO:
                            }
                        }
                        ContentType.TEXT ->
                        {
                            // If the data is not null, the text needs to be retrieved from the disk
                            if (data != null)
                            {
                                text = getMMSText(partID)
                            }
                            TextAttachment(text)
                        }
                        ContentType.VIDEO ->
                        {
                            TextAttachment("[Video Attachment]") // TODO:
                        }
                        else -> continue
                    }

                    attachmentList.add(attachment)
                }
            }
        }
        return attachmentList
    }

    /**
     * Get MMS text
     *
     * @param partID
     * @return
     */
    private suspend fun getMMSText(partID: Long): String?
    {
        val partURI = ContentUris.withAppendedId(Mms.Part.CONTENT_URI, partID)
        val contentResolver = getContentResolver()
        contentResolver.openInputStream(partURI).use { inputStream ->
            inputStream?.reader(charset = Charsets.UTF_8)?.use { reader ->
                return reader.readText()
            }
        }
        return null
    }

    /**
     * Get MMS image
     *
     * @param partID
     * @return
     */
    private suspend fun getMMSImageThumbnail(partID: Long): Bitmap?
    {
        val partURI = ContentUris.withAppendedId(Mms.Part.CONTENT_URI, partID)
        val contentResolver = getContentResolver()

        val context = InboxApp.context
        val resources = context.resources
        val displayMetrics = resources.displayMetrics

        val targetWidth = (displayMetrics.widthPixels * .8).roundToInt() // TODO: Get the target size fraction from the configuration

        val size = Size(targetWidth, targetWidth)
        return contentResolver.loadThumbnail(partURI, size, null)
    }

//    /**
//     * Get MMS image
//     *
//     * @param partID
//     * @return
//     */
//    private fun getMMSImage(partID: Long): Bitmap?
//    {
//        val partURI = ContentUris.withAppendedId(Telephony.Mms.Part.CONTENT_URI, partID)
//        contentResolver.openInputStream(partURI).use { inputStream ->
//            val image = BitmapFactory.decodeStream(inputStream)
//            val orientation = getImageOrientation(partURI)
//            return if (orientation > 0)
//            {
//                val matrix = Matrix()
//                matrix.postRotate(orientation)
//                Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
//            }
//            else
//            {
//                image
//            }
//        }
//        return null
//    }
//
//    /**
//     * Image orientation
//     *
//     * @param imageURI
//     * @return
//     */
//    private fun getImageOrientation(imageURI: Uri): Float
//    {
//        contentResolver.openInputStream(imageURI).use { inputStream ->
//            if (inputStream != null)
//            {
//                val exifInterface = ExifInterface(inputStream)
//                return when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED))
//                {
//                    ExifInterface.ORIENTATION_NORMAL -> 0F
//                    ExifInterface.ORIENTATION_ROTATE_90 -> 90F
//                    ExifInterface.ORIENTATION_ROTATE_180 -> 180F
//                    ExifInterface.ORIENTATION_ROTATE_270 -> 270F
//                    else -> -1F
//                }
//            }
//        }
//        return -1F
//    }
}