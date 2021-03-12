package com.socap.notekeeper

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import java.util.*

class NoteKeeperProvider : ContentProvider() {
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper

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
        cursor = db.query(
            CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
            null, null, sortOrder
        )
        return cursor
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        TODO("Implement this to handle requests to update one or more rows.")
    }

    companion object {
        const val AUTHORITY = "com.socap.notekeeper.provider"
    }
}