package com.github.macleod.inbox.data.model

class MMSMessage(id: Long, dateSent: Long, dateReceived: Long, address: String, contact: Contact? = null, type: MessageType, val attachments: List<Attachment>): Message(id,
                                                                                                                                                                         dateSent,
                                                                                                                                                                         dateReceived,
                                                                                                                                                                         address,
                                                                                                                                                                         contact,
                                                                                                                                                                         type)
{
    /**
     *
     */
    constructor(messageData: MMSMessageData, contact: Contact? = null): this(messageData.id,
                                                                             messageData.dateSent,
                                                                             messageData.dateReceived,
                                                                             messageData.address,
                                                                             contact,
                                                                             messageData.type,
                                                                             messageData.attachments)

    /**
     * Equals
     *
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is MMSMessage) return false

        if (attachments != other.attachments) return false
        if (contact != other.contact) return false

        return true
    }

    /**
     * Hash code
     *
     * @return
     */
    override fun hashCode(): Int
    {
        var result = attachments.hashCode()
        result = 31 * result + (contact?.hashCode() ?: 0)
        return result
    }
}