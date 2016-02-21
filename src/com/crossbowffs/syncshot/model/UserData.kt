package com.crossbowffs.syncshot.model

import android.os.Parcel
import android.os.Parcelable

data class UserData(
        val id: String?,
        val name: String) : Parcelable {

    companion object {
        @JvmField
        final val CREATOR = object : Parcelable.Creator<UserData> {
            override fun createFromParcel(p0: Parcel): UserData {
                return UserData(p0)
            }

            override fun newArray(p0: Int): Array<out UserData?> {
                return arrayOfNulls(p0)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(id)
        p0.writeString(name)
    }

    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString()) {

    }
}

