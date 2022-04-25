package com.github.macleod.inbox.data.model

class SMSMessage(id: Long, val text: String, dateSent: Long, dateReceived: Long, address: String, contact: Contact? = null, type: MessageType): Message(id, dateSent, dateReceived, address, contact, type)
{
    /**
     *
     */
    constructor(messageData: SMSMessageData, contact: Contact? = null): this(messageData.id,
                                                                             messageData.text,
                                                                             messageData.dateSent,
                                                                             messageData.dateReceived,
                                                                             messageData.address,
                                                                        contact,
                                                                             messageData.type)

    /**
     * Equals
     *
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is SMSMessage) return false

        if (text != other.text) return false
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
        var result = text.hashCode()
        result = 31 * result + (contact?.hashCode() ?: 0)
        return result
    }
}