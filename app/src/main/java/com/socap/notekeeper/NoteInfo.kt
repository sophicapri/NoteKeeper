package com.socap.notekeeper

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class NoteInfo : Parcelable {
    lateinit var course: CourseInfo
    var title: String? = null
    var text: String? = null
    val id: Int
        get() = _id
    private var _id = -1
    private val compareKey: String
        get() = "${course.courseId}|$title|$text"

    constructor(id: Int = -1, course: CourseInfo = CourseInfo(), title: String? = null, text: String? = null) {
        this._id = id
        this.course = course
        this.title = title
        this.text = text
    }

    private constructor(parcel: Parcel) {
        parcel.readParcelable<CourseInfo>(CourseInfo::class.java.classLoader)?.let { course = it }
        parcel.readString()?.let { title = it }
        parcel.readString()?.let { text = it }
    }

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
        @JvmField
        val CREATOR: Parcelable.Creator<NoteInfo?> = object : Parcelable.Creator<NoteInfo?> {
            override fun createFromParcel(source: Parcel) = NoteInfo(source)

            override fun newArray(size: Int) = arrayOfNulls<NoteInfo>(size)
        }
    }
}