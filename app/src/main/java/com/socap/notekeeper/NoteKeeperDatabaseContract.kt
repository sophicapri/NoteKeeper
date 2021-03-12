package com.socap.notekeeper

import java.util.*

class NoteKeeperDatabaseContract private constructor(){

    object CourseInfoEntry {
        const val TABLE_NAME = "course_info"
        const val COLUMN_COURSE_ID = "course_id"
        const val COLUMN_COURSE_TITLE = "course_title"
        const val ID = "_id"

        fun getQName(columnName: String) : String{
            return "$TABLE_NAME.$columnName"
        }

        // CREATE TABLE course_info (course_id, course_title)
        const val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME " +
                    "($ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_COURSE_ID TEXT UNIQUE NOT NULL, " +
                    "$COLUMN_COURSE_TITLE TEXT NOT NULL)"
    }

    object NoteInfoEntry {
        const val TABLE_NAME = "note_info"
        const val COLUMN_NOTE_TITLE = "note_title"
        const val COLUMN_NOTE_TEXT = "note_text"
        const val COLUMN_COURSE_ID = "course_id"
        const val ID = "_id"

        fun getQName(columnName: String) : String{
            return "${TABLE_NAME}.$columnName"
        }

        const val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME " +
                    "($ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_NOTE_TITLE TEXT NOT NULL, $COLUMN_NOTE_TEXT TEXT, " +
                    "$COLUMN_COURSE_ID TEXT NOT NULL)"
    }
}