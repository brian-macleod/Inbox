package com.github.macleod.inbox.data.model

enum class ContentType(val prefix: String?)
{
    AUDIO("audio"),
    IMAGE("image"),
    TEXT("text/plain"),
    UNKNOWN(null),
    VIDEO("video");

    companion object
    {
        /**
         * Get content type from MIME type
         *
         * @param mimeType
         * @return
         */
        fun getContentTypeFromMIMEType(mimeType: String): ContentType
        {
            return with (mimeType)
            {
                when
                {
                    startsWith(AUDIO.prefix!!, ignoreCase = true) -> AUDIO
                    startsWith(IMAGE.prefix!!, ignoreCase = true) -> IMAGE
                    startsWith(TEXT.prefix!!, ignoreCase = true) -> TEXT
                    startsWith(VIDEO.prefix!!, ignoreCase = true) -> VIDEO
                    else -> UNKNOWN
                }
            }
        }
    }
}