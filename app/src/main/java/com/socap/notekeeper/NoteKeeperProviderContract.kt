package com.socap.notekeeper

import android.net.Uri

class NoteKeeperProviderContract private constructor() {

    protected interface CoursesIdColumns {
        val COLUMN_COURSE_ID: String
            get() = "course_id"
    }

    protected interface CoursesColumns {
        val COLUMN_COURSE_TITLE: String
            get() = "course_title"
    }

    protected interface NotesColumns {
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

    object Notes : BaseColumns, NotesColumns, CoursesIdColumns, CoursesColumns {
        const val PATH = "notes"
        val CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH)
        const val PATH_EXPANDED = "notes_expanded"
        val CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED)
    }

    companion object {
        const val AUTHORITY = "com.socap.notekeeper.provider"
        val AUTHORITY_URI: Uri = Uri.parse("content://$AUTHORITY")
    }
}