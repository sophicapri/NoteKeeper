package com.socap.notekeeper

import android.content.Context
import android.util.Log
import com.socap.notekeeper.NoteKeeperProviderContract.Notes

object NoteBackup {
    const val ALL_COURSES = "ALL_COURSES"
    private val TAG = NoteBackup::class.java.name

    fun doBackup(context: Context, backupCourseId: String) {
        val columns = arrayOf(
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT
        )
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (backupCourseId != ALL_COURSES) {
            selection = Notes.COLUMN_COURSE_ID + " = ?"
            selectionArgs = arrayOf(backupCourseId)
        }
        val cursor = context.contentResolver.query(
            Notes.CONTENT_URI,
            columns,
            selection,
            selectionArgs,
            null
        )
        try {
            val courseIdPos = cursor!!.getColumnIndex(Notes.COLUMN_COURSE_ID)
            val noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE)
            val noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT)
            Log.i(TAG, ">>>***   BACKUP START - Thread: " + Thread.currentThread().id + "   ***<<<")
            while (cursor.moveToNext()) {
                val courseId = cursor.getString(courseIdPos)
                val noteTitle = cursor.getString(noteTitlePos)
                val noteText = cursor.getString(noteTextPos)
                if (noteTitle != "") {
                    Log.i(TAG, ">>>Backing Up Note<<< $courseId|$noteTitle|$noteText")
                    simulateLongRunningWork()
                }
            }
            Log.i(TAG, ">>>***   BACKUP COMPLETE   ***<<<")
            cursor.close()
        } catch (e: NullPointerException) {
            Log.e(TAG, "doBackup: ", e.cause)
        }
    }

    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(1000)
        } catch (ex: Exception) {
        }
    }
}