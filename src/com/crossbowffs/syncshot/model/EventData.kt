package com.crossbowffs.syncshot.model

import android.os.Parcel
import android.os.Parcelable

data class EventData(
        val id: String?,
        val creatorId: String,
        val name: String,
        val details: String,
        val location: String,
        val startTime: Long,
        val endTime: Long) : Parcelable {

    companion object {
        @JvmField
        final val CREATOR = object : Parcelable.Creator<EventData> {
            override fun createFromParcel(p0: Parcel): EventData {
                return EventData(p0)
            }

            override fun newArray(p0: Int): Array<out EventData?> {
                return arrayOfNulls(p0)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(id)
        p0.writeString(creatorId)
        p0.writeString(name)
        p0.writeString(details)
        p0.writeString(location)
        p0.writeLong(startTime)
        p0.writeLong(endTime)
    }

    constructor(parcel: Parcel) : this(parcel.readString(),
            parcel.readString(), parcel.readString(), parcel.readString(),
            parcel.readString(), parcel.readLong(),  parcel.readLong()) {

    }
}
