package com.github.macleod.inbox.data.model

enum class ContentType
{
    AUDIO,
    IMAGE,
    TEXT,
    UNKNOWN,
    VIDEO;

    companion object
    {
        private const val AUDIO_PREFIX = "audio"
        private const val IMAGE_PREFIX = "image"
        private const val TEXT_PREFIX  = "text/plain"
        private const val VIDEO_PREFIX = "video"

        fun getContentTypeFromMIMEType(mimeType: String): ContentType
        {
            return with (mimeType)
            {
                when
                {
                    startsWith(AUDIO_PREFIX, ignoreCase = true) -> AUDIO
                    startsWith(IMAGE_PREFIX, ignoreCase = true) -> IMAGE
                    startsWith(TEXT_PREFIX, ignoreCase = true) -> TEXT
                    startsWith(VIDEO_PREFIX, ignoreCase = true) -> VIDEO
                    else -> UNKNOWN
                }
            }
        }
    }
}