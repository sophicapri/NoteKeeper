package com.socap.notekeeper

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class CourseInfo : Parcelable {
    val courseId: String?
    val title: String
    private val modules: List<ModuleInfo>

    constructor(courseId: String, title: String, modules: List<ModuleInfo>) {
        this.courseId = courseId
        this.title = title
        this.modules = modules
    }

    private constructor(source: Parcel) {
        courseId = source.readString()
        title = source.readString()!!
        modules = ArrayList()
        source.readTypedList(modules, ModuleInfo.CREATOR)
    }

    var modulesCompletionStatus: BooleanArray
        get() {
            val status = BooleanArray(modules.size)
            for (i in modules.indices) status[i] = modules[i].isComplete
            return status
        }
        set(status) {
            for (i in modules.indices) modules[i].isComplete = status[i]
        }

    fun getModule(moduleId: String): ModuleInfo? {
        for (moduleInfo in modules) {
            if (moduleId == moduleInfo.moduleId) return moduleInfo
        }
        return null
    }

    override fun toString(): String {
        return title
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as CourseInfo
        return courseId == that.courseId
    }

    override fun hashCode(): Int {
        return courseId.hashCode()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(courseId)
        dest.writeString(title)
        dest.writeTypedList(modules)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CourseInfo?> = object : Parcelable.Creator<CourseInfo?> {
            override fun createFromParcel(source: Parcel) = CourseInfo(source)

            override fun newArray(size: Int) = arrayOfNulls<CourseInfo>(size)
        }
    }
}