package com.socap.notekeeper

import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import com.socap.notekeeper.NoteKeeperDatabaseContract

/**
 * Created by Jim.
 */
class DatabaseDataWorker(private val mDb: SQLiteDatabase) {
    fun insertCourses() {
        insertCourse("android_intents", "Android Programming with Intents")
        insertCourse("android_async", "Android Async Programming and Services")
        insertCourse("java_lang", "Java Fundamentals: The Java Language")
        insertCourse("java_core", "Java Fundamentals: The Core Platform")
    }

    fun insertSampleNotes() {
        insertNote(
            "android_intents",
            "Dynamic intent resolution",
            "Wow, intents allow components to be resolved at runtime"
        )
        insertNote(
            "android_intents",
            "Delegating intents",
            "PendingIntents are powerful; they delegate much more than just a component invocation"
        )
        insertNote(
            "android_async",
            "Service default threads",
            "Did you know that by default an Android Service will tie up the UI thread?"
        )
        insertNote(
            "android_async",
            "Long running operations",
            "Foreground Services can be tied to a notification icon"
        )
        insertNote("java_lang", "Parameters", "Leverage variable-length parameter lists?")
        insertNote(
            "java_lang",
            "Anonymous classes",
            "Anonymous classes simplify implementing one-use types"
        )
        insertNote(
            "java_core",
            "Compiler options",
            "The -jar option isn't compatible with with the -cp option"
        )
        insertNote(
            "java_core",
            "Serialization",
            "Remember to include SerialVersionUID to assure version compatibility"
        )
    }

    private fun insertCourse(courseId: String, title: String) {
        val values = ContentValues()
        values.put(NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_ID, courseId)
        values.put(NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_TITLE, title)
        val newRowId =
            mDb.insert(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, null, values)
    }

    private fun insertNote(courseId: String, title: String, text: String) {
        val values = ContentValues()
        values.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_COURSE_ID, courseId)
        values.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TITLE, title)
        values.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TEXT, text)
        val newRowId = mDb.insert(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, null, values)
    }
}