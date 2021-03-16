package com.socap.notekeeper

import android.content.Intent

object CourseEventBroadcastHelper {
    private const val ACTION_COURSE_EVENT = "com.socap.notekeeper.action.COURSE_EVENT"
    private const val EXTRA_COURSE_ID = "com.socap.notekeeper.extra.COURSE_ID"
    private const val EXTRA_COURSE_MESSAGE = "com.socap.notekeeper.extra.COURSE_MESSAGE"

    fun getEventBroadcastIntent(courseId: String?, message: String?): Intent {
        val intent = Intent(ACTION_COURSE_EVENT)
        intent.putExtra(EXTRA_COURSE_ID, courseId)
        intent.putExtra(EXTRA_COURSE_MESSAGE, message)
        return intent
    }
}