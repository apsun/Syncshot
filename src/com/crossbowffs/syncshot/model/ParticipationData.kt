package com.crossbowffs.syncshot.model

import android.os.Parcel
import android.os.Parcelable

data class ParticipationData(
        val id: String?,
        val userId: String,
        val eventId: String,
        val imageCount: Int) : Parcelable {

    companion object {
        @JvmField
        final val CREATOR = object : Parcelable.Creator<ParticipationData> {
            override fun createFromParcel(p0: Parcel): ParticipationData {
                return ParticipationData(p0)
            }

            override fun newArray(p0: Int): Array<out ParticipationData?> {
                return arrayOfNulls(p0)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(id)
        p0.writeString(userId)
        p0.writeString(eventId)
        p0.writeInt(imageCount)
    }

    constructor(parcel: Parcel) : this(parcel.readString(),
        parcel.readString(), parcel.readString(), parcel.readInt()) {

    }
}
