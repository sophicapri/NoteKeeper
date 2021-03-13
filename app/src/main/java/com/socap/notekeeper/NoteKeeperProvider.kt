package com.socap.notekeeper

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.socap.notekeeper.NoteKeeperProviderContract.Courses
import com.socap.notekeeper.NoteKeeperProviderContract.Notes
import java.util.*

class NoteKeeperProvider : ContentProvider() {
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper

    companion object {
        private var uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private const val COURSES = 0
        private const val NOTES = 1
        private const val NOTES_EXPANDED = 2

        init {
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("Implement this to handle requests to delete one or more rows")
    }

    override fun getType(uri: Uri): String? {
        TODO(
            "Implement this to handle requests for the MIME type of the data" +
                    "at the given URI"
        )
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Implement this to handle requests to insert a new row.")
    }

    override fun onCreate(): Boolean {
        context?.let { dbOpenHelper = NoteKeeperOpenHelper(it) }
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val cursor: Cursor?
        val db = dbOpenHelper.readableDatabase

        val uriMatch = uriMatcher.match(uri)

        cursor = when (uriMatch) {
            COURSES -> db.query(
                CourseInfoEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder
            )
            else -> db.query(
                NoteInfoEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder
            )
        }
        return cursor
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        TODO("Implement this to handle requests to update one or more rows.")
    }
}