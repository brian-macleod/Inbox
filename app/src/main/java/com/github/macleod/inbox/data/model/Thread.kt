package com.github.macleod.inbox.data.model

data class Thread(val id: Long, val date: Long, val recipients: List<Long>, val messageCount: Int)