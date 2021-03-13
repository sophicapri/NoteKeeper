package com.socap.notekeeper

import android.net.Uri

class NoteKeeperProviderContract private constructor() {

    private interface CoursesIdColumns {
        val COLUMN_COURSE_ID: String
            get() = "course_id"
    }

    private interface CoursesColumns {
        val COLUMN_COURSE_TITLE: String
            get() = "course_title"
    }

    private interface NotesColumns {
        val COLUMN_NOTE_TITLE: String
            get() = "note_title"
        val COLUMN_NOTE_TEXT: String
            get() = "note_text"
    }

    object Courses : BaseColumns, CoursesColumns, CoursesIdColumns {
        const val PATH = "courses"

        // content://com.socap.notekeeper.provider/courses
        val CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH)
    }

    object Notes : BaseColumns, NotesColumns, CoursesIdColumns {
        const val PATH = "notes"
        val CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH)
    }

    companion object {
        const val AUTHORITY = "com.socap.notekeeper.provider"
        val AUTHORITY_URI = Uri.parse("content://$AUTHORITY")
    }
}