package com.github.macleod.inbox.data.model

import android.os.Parcel
import android.os.Parcelable

data class Contact(val id: Long?, val address: String?, val displayName: String?): Parcelable
{
    /**
     * Creator
     *
     * @constructor Create empty creator
     */
    companion object CREATOR : Parcelable.Creator<Contact>
    {
        override fun createFromParcel(parcel: Parcel): Contact
        {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?>
        {
            return arrayOfNulls(size)
        }
    }

    /**
     *
     */
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString()
    )
    {
    }

    /**
     * Write to parcel
     *
     * @param parcel
     * @param flags
     */
    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeValue(id)
        parcel.writeString(address)
        parcel.writeString(displayName)
    }

    /**
     * Describe contents
     *
     * @return
     */
    override fun describeContents(): Int
    {
        return 0
    }

    /**
     * Gets the initials for this contact.
     *
     * @return the initials of the contact, or null if this object does not represent a valid
     *         contact
     */
    fun getInitials(): String?
    {
        if (displayName == null)
        {
            return null
        }

        val nameList = displayName.split(" ")
        val numNames = nameList.size
        if (numNames == 0)
        {
            return null
        }

        val firstInitial = nameList[0][0]
        return if (nameList.size == 1)
        {
            firstInitial.toString()
        }
        else
        {
            val lastInitial = nameList[numNames - 1][0]
            firstInitial + lastInitial.toString()
        }
    }
}