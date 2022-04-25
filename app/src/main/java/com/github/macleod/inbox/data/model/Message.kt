package com.github.macleod.inbox.data.model

abstract class Message(val id: Long, val dateSent: Long, val dateReceived: Long, var address: String, val contact: Contact?, val type: MessageType)
{
    /**
     * EquaWls
     *
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is Message) return false

        if (id != other.id) return false
        if (dateSent != other.dateSent) return false
        if (dateReceived != other.dateReceived) return false
        if (address != other.address) return false
        if (type != other.type) return false

        return true
    }

    /**
     * Hash code
     *
     * @return
     */
    override fun hashCode(): Int
    {
        var result = id.hashCode()
        result = 31 * result + dateSent.hashCode()
        result = 31 * result + dateReceived.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}