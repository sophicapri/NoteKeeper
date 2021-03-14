package com.socap.notekeeper

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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
        private const val NOTES_ROW = 3
        private const val COURSES_ROW = 4
        private const val NOTES_EXPANDED_ROW = 5
        private const val MIME_VENDOR_TYPE = "vnd.${NoteKeeperProviderContract.AUTHORITY}."

        init {
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, "${Notes.PATH}/#", NOTES_ROW)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH + "/#", COURSES_ROW)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED + "/#",
                NOTES_EXPANDED_ROW)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val rowId: Long
        val rowSelection: String?
        val rowSelectionArgs: Array<String>?
        var rows = -1
        val db: SQLiteDatabase = dbOpenHelper.readableDatabase

        when (uriMatcher.match(uri)) {
            COURSES -> rows = db.delete(CourseInfoEntry.TABLE_NAME, selection, selectionArgs)
            NOTES -> rows = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs)
            NOTES_EXPANDED -> {
                // throw exception saying that this is a read-only table
            }
            COURSES_ROW -> {
                rowId = ContentUris.parseId(uri)
                rowSelection = CourseInfoEntry._ID + " = ?"
                rowSelectionArgs = arrayOf(rowId.toString())
                rows = db.delete(CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs)
            }
            NOTES_ROW -> {
                rowId = ContentUris.parseId(uri)
                rowSelection = NoteInfoEntry._ID + " = ?"
                rowSelectionArgs = arrayOf(rowId.toString())
                rows = db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs)
            }
            NOTES_EXPANDED_ROW -> {
                // throw exception saying that this is a read-only table
            }
        }
        return rows
    }

    override fun getType(uri: Uri): String? {
        var mimeType: String? = null
        when (uriMatcher.match(uri)) {
            COURSES -> // vnd.android.cursor.dir/vnd.com.socap.notekeeper.provider.courses
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + Courses.PATH
            NOTES -> mimeType =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH
            NOTES_EXPANDED -> mimeType =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH_EXPANDED
            COURSES_ROW -> mimeType =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH
            NOTES_ROW -> mimeType =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH
            NOTES_EXPANDED_ROW -> mimeType =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH_EXPANDED
        }
        return mimeType
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbOpenHelper.writableDatabase
        val rowId: Long
        var rowUri: Uri? = null
        when (uriMatcher.match(uri)) {
            NOTES -> {
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values)
                // content://com.socap.notekeeper.provider/notes/1
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId)
            }
            COURSES -> {
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values)
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId)
            }
            NOTES_EXPANDED -> {
                // throw exception --> read-only table
            }
        }
        return rowUri
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
            NOTES -> db.query(
                NoteInfoEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder
            )
            NOTES_EXPANDED -> notesExpandedQuery(
                db, projection, selection,
                selectionArgs, sortOrder
            )
            NOTES_ROW -> {
                val rowId = ContentUris.parseId(uri)
                val rowSelection = "${NoteInfoEntry._ID} = ?"
                val rowSelectionArgs = arrayOf(rowId.toString())
                db.query(
                    NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null,
                    null, null
                )
            }
            else -> null
        }
        return cursor
    }

    private fun notesExpandedQuery(
        db: SQLiteDatabase,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val columns: Array<String?> = arrayOfNulls(projection!!.size)
        (projection.indices).forEach { idx ->
            columns[idx] =
                if (projection[idx] == Notes._ID || projection[idx] == Notes.COLUMN_COURSE_ID)
                    NoteInfoEntry.getQName(projection[idx])
                else
                    projection[idx]
        }

        val tablesWithJoin =
            "${NoteInfoEntry.TABLE_NAME} JOIN ${CourseInfoEntry.TABLE_NAME} ON " +
                    "${NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)} = " +
                    CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID)

        return db.query(
            tablesWithJoin,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val rowId: Long
        val rowSelection: String?
        val rowSelectionArgs: Array<String>?
        var rows = -1
        val db: SQLiteDatabase = dbOpenHelper.readableDatabase

        when (uriMatcher.match(uri)) {
            COURSES -> rows =
                db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs)
            NOTES -> rows = db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs)
            NOTES_EXPANDED -> {
                // throw exception saying that this is a read-only table
            }
            COURSES_ROW -> {
                rowId = ContentUris.parseId(uri)
                rowSelection = CourseInfoEntry._ID + " = ?"
                rowSelectionArgs = arrayOf(java.lang.Long.toString(rowId))
                rows =
                    db.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs)
            }
            NOTES_ROW -> {
                rowId = ContentUris.parseId(uri)
                rowSelection = NoteInfoEntry._ID + " = ?"
                rowSelectionArgs = arrayOf(java.lang.Long.toString(rowId))
                rows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs)
            }
            NOTES_EXPANDED_ROW -> {
                // throw exception saying that this is a read-only table
            }
        }
        return rows
    }
}