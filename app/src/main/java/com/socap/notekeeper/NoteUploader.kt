package com.socap.notekeeper

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.socap.notekeeper.NoteKeeperProviderContract.Notes

class NoteUploader(private val contentResolver: ContentResolver) {
    var isCanceled = false
        private set

    fun cancel() {
        isCanceled = true
    }

    fun doUpload(dataUri: Uri) {
        val columns = arrayOf(
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT
        )
        val cursor = contentResolver.query(dataUri, columns, null, null, null)
        val courseIdPos = cursor!!.getColumnIndex(Notes.COLUMN_COURSE_ID)
        val noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE)
        val noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT)

        Log.i(TAG, ">>>*** UPLOAD START - $dataUri ***<<<")
        isCanceled = false
        while (!isCanceled && cursor.moveToNext()) {
            val courseId = cursor.getString(courseIdPos)
            val noteTitle = cursor.getString(noteTitlePos)
            val noteText = cursor.getString(noteTextPos)
            if (noteTitle != "") {
                Log.i(TAG, ">>>Uploading Note<<< $courseId|$noteTitle|$noteText")
                simulateLongRunningWork()
            }
        }

        if (isCanceled) {
            Log.i(TAG, ">>>*** UPLOAD !!CANCELED!! - $dataUri ***<<<")
        } else
            Log.i(TAG, ">>>*** UPLOAD COMPLETE - $dataUri ***<<<")
        cursor.close()
    }

    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(3000)
        } catch (ex: Exception) {
        }
    }

    companion object {
        private val TAG = NoteUploader::class.java.name
    }
}