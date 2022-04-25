package com.github.macleod.inbox.data.model

enum class MessageType(val SMSValue: Int, val MMSValue: Int)
{
    RECEIVED(1, 132),
    SENT(2, 128);

    companion object
    {
        private val values = values()
        fun fromInt(value: Int) = values.firstOrNull { it.SMSValue == value || it.MMSValue == value}
    }
}