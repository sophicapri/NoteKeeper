package com.socap.notekeeper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry


class NoteKeeperOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE)
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE)
        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1)
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1)

        val worker = DatabaseDataWorker(db)
        worker.insertCourses()
        worker.insertSampleNotes()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2){
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1)
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1)
        }
    }

    companion object {
        const val DATABASE_NAME = "NoteKeeper.db"
        const val DATABASE_VERSION = 2
    }
}