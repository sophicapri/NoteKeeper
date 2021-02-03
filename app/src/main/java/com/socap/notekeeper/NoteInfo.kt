package com.socap.notekeeper

class NoteInfo(var course: CourseInfo?, var title: String?, var text: String?) {
    private val compareKey: String
        get() = course?.courseId + "|" + title + "|" + text

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
}