package com.github.macleod.inbox.data.model

class SMSMessageData(id: Long, val text: String, dateSent: Long, dateReceived: Long, address: String, type: MessageType): MessageData(id, dateSent, dateReceived, address, type)