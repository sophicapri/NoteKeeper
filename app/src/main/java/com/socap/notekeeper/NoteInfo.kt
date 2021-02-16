package com.socap.notekeeper

import android.os.Parcel
import android.os.Parcelable

class NoteInfo(var course: CourseInfo?, var title: String?, var text: String?) : Parcelable {
    private val compareKey: String
        get() = "${course?.courseId}|$title|$text"

    private constructor(parcel: Parcel) : this(
        parcel.readParcelable(CourseInfo::class.java.classLoader),
        parcel.readString(),
        parcel.readString()
    )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as NoteInfo
        return compareKey == that.compareKey
    }

    override fun hashCode(): Int {
        return compareKey.hashCode()
    }

    override fun toString(): String {
        return compareKey
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(course, flags)
        parcel.writeString(title)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<NoteInfo?> = object : Parcelable.Creator<NoteInfo?> {
            override fun createFromParcel(source: Parcel) = NoteInfo(source)

            override fun newArray(size: Int) = arrayOfNulls<NoteInfo>(size)
        }
    }
}