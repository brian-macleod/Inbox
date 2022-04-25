package com.github.macleod.inbox.data.model

sealed class MessageData(val id: Long, val dateSent: Long, val dateReceived: Long, var address: String, val type: MessageType)