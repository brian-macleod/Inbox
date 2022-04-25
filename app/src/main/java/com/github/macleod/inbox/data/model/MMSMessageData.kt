package com.github.macleod.inbox.data.model

class MMSMessageData(id: Long, dateSent: Long, dateReceived: Long, address: String, type: MessageType, val attachments: List<Attachment>): MessageData(id, dateSent, dateReceived, address, type)