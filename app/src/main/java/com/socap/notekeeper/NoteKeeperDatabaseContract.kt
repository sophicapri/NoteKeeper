package com.socap.notekeeper

class NoteKeeperDatabaseContract private constructor(){

    object CourseInfoEntry : BaseColumns{
        const val TABLE_NAME = "course_info"
        const val COLUMN_COURSE_ID = "course_id"
        const val COLUMN_COURSE_TITLE = "course_title"

        // CREATE INDEX course_info_index1 ON course_info(course_title)
        const val INDEX1 = "${TABLE_NAME}_index1"
        const val SQL_CREATE_INDEX1 = "CREATE INDEX $INDEX1 ON $TABLE_NAME($COLUMN_COURSE_TITLE)"

        fun getQName(columnName: String) : String{
            return "$TABLE_NAME.$columnName"
        }

        // CREATE TABLE course_info (course_id, course_title)
        val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME " +
                    "($_ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_COURSE_ID TEXT UNIQUE NOT NULL, " +
                    "$COLUMN_COURSE_TITLE TEXT NOT NULL)"
    }

    object NoteInfoEntry: BaseColumns {
        const val TABLE_NAME = "note_info"
        const val COLUMN_NOTE_TITLE = "note_title"
        const val COLUMN_NOTE_TEXT = "note_text"
        const val COLUMN_COURSE_ID = "course_id"

        const val INDEX1 = "${TABLE_NAME}_index1"
        const val SQL_CREATE_INDEX1 = "CREATE INDEX $INDEX1 ON ${TABLE_NAME}(${COLUMN_NOTE_TITLE})"

        fun getQName(columnName: String) : String{
            return "${TABLE_NAME}.$columnName"
        }

        val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME " +
                    "($_ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_NOTE_TITLE TEXT NOT NULL, $COLUMN_NOTE_TEXT TEXT, " +
                    "$COLUMN_COURSE_ID TEXT NOT NULL)"
    }
}