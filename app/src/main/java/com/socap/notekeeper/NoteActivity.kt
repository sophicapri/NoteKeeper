package com.socap.notekeeper

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.socap.notekeeper.NoteKeeperProviderContract.Companion.AUTHORITY
import com.socap.notekeeper.NoteKeeperProviderContract.Courses
import com.socap.notekeeper.databinding.ActivityNoteBinding
import java.util.concurrent.Executors


class NoteActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var binding: ActivityNoteBinding
    private var note: NoteInfo = NoteInfo()
    private var isNewNote = false
    private lateinit var spinnerCourses: Spinner
    private lateinit var textNoteTitle: EditText
    private lateinit var textNoteText: EditText
    private var noteId = 0
    private var isCancelling = false
    private lateinit var viewModel: NoteActivityViewModel
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper
    private lateinit var noteCursor: Cursor
    private var courseIdPos = 0
    private var noteTitlePos = 0
    private var noteTextPos = 0
    private lateinit var adapterCourses: SimpleCursorAdapter
    private var coursesQueryFinished = false
    private var noteQueryFinished = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        dbOpenHelper = NoteKeeperOpenHelper(this)
        val viewModelProvider = ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )

        viewModel = viewModelProvider.get(NoteActivityViewModel::class.java)

        if (viewModel.isNewlyCreated && savedInstanceState != null)
            viewModel.restoreState(savedInstanceState)

        viewModel.isNewlyCreated = false

        adapterCourses =
            SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item, null,
                arrayOf(CourseInfoEntry.COLUMN_COURSE_TITLE),
                IntArray(1) { android.R.id.text1 }, 0
            )
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses = binding.contentNote.spinnerCourses
        spinnerCourses.adapter = adapterCourses

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this)

        textNoteTitle = binding.contentNote.textNoteTitle
        textNoteText = binding.contentNote.textNoteText

        readDisplayStateValues()
        if (!isNewNote) {
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        var loader: Loader<Cursor> = CursorLoader(this)
        if (id == LOADER_NOTES)
            loader = createLoaderNote()
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses()
        return loader
    }

    private fun createLoaderCourses(): Loader<Cursor> {
        coursesQueryFinished = false
        val uri = Courses.CONTENT_URI
        val courseColumns = arrayOf(
            Courses.COLUMN_COURSE_TITLE,
            Courses.COLUMN_COURSE_ID,
            Courses._ID
        )
        return CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE)
    }

    private fun createLoaderNote(): Loader<Cursor> {
        noteQueryFinished = false
        return object : CursorLoader(this) {
            override fun loadInBackground(): Cursor? {
                val db = dbOpenHelper.readableDatabase

                val selection = "${NoteInfoEntry._ID} = ?"
                val selectionArgs: Array<String> = arrayOf(noteId.toString())

                val noteColumns: Array<String> = arrayOf(
                    NoteInfoEntry.COLUMN_COURSE_ID,
                    NoteInfoEntry.COLUMN_NOTE_TITLE,
                    NoteInfoEntry.COLUMN_NOTE_TEXT
                )

                return db.run {
                    query(
                        NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs, null,
                        null, null
                    )
                }
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        if (loader.id == LOADER_NOTES)
            loadFinishedNote(data)
        else if (loader.id == LOADER_COURSES){
            adapterCourses.changeCursor(data)
            coursesQueryFinished = true
            displayNoteWhenQueriesFinished()
        }
    }

    private fun loadFinishedNote(data: Cursor) {
        noteCursor = data
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT)
        noteCursor.moveToNext()
        noteQueryFinished = true
        displayNoteWhenQueriesFinished()
    }

    private fun displayNoteWhenQueriesFinished() {
        if(noteQueryFinished && coursesQueryFinished) {
            displayNote()
            saveOriginalNoteValues()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTES)
            noteCursor.close()
        else if (loader.id == LOADER_COURSES)
            adapterCourses.changeCursor(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_send_email -> sendEmail()
            R.id.action_cancel -> {
                isCancelling = true
                finish()
            }
            R.id.action_next -> moveNext()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item: MenuItem = menu.findItem(R.id.action_next)
        val lastNoteIndex = DataManager.instance.notes.size - 1
        item.isEnabled = noteId < lastNoteIndex
        return super.onPrepareOptionsMenu(menu)
    }

    private fun moveNext() {
        saveNote()

        ++noteId
        note = DataManager.instance.notes[noteId]

        saveOriginalNoteValues()
        displayNote()
        invalidateOptionsMenu()
    }

    private fun sendEmail() {
        val course = spinnerCourses.selectedItem as CourseInfo
        val subject = textNoteTitle.text.toString()
        val text = "Check out what I learned in the Pluralsight course \"" +
                "${course.title}\"\n${textNoteText.text}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc2822"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(intent)
    }

    private fun readDisplayStateValues() {
        val intent: Intent = intent
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET)
        isNewNote = noteId == ID_NOT_SET
        if (isNewNote)
            createNewNote()

        Log.i(TAG, "readDisplayStateValues: - notePosition = $noteId")
    }

    private fun createNewNote() {
        val values = ContentValues()
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "")
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "")
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "")
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val db = dbOpenHelper.writableDatabase
            noteId = db.insert(NoteInfoEntry.TABLE_NAME, null, values).toInt()
        }
    }

    private fun saveOriginalNoteValues() {
        if (isNewNote)
            return
        note.course?.courseId?.let { viewModel.originalNoteCourseId = it }
        note.title?.let { viewModel.originalNoteTitle = it }
        note.text?.let { viewModel.originalNoteText = it }

    }

    private fun displayNote() {
        val courseId = noteCursor.getString(courseIdPos)
        val noteTitle = noteCursor.getString(noteTitlePos)
        val noteText = noteCursor.getString(noteTextPos)

        val courseIndex = getIndexOfCourseId(courseId)
        spinnerCourses.setSelection(courseIndex)
        val courseTitle = spinnerCourses.selectedItem.toString()
        textNoteTitle.setText(noteTitle)
        textNoteText.setText(noteText)


        note = NoteInfo(
            course = CourseInfo(courseId = courseId, title = courseTitle),
            title = noteTitle, text = noteText
        )
    }

    private fun getIndexOfCourseId(courseId: String): Int {
        val cursor = adapterCourses.cursor
        val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
        var courseRowIndex = 0

        var more = cursor.moveToFirst()
        while (more) {
            val cursorCourseId = cursor.getString(courseIdPos)
            if (courseId == cursorCourseId)
                break

            courseRowIndex++
            more = cursor.moveToNext()
        }
        return courseRowIndex
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position $noteId")
            if (isNewNote)
                deleteNoteFromDatabase()
            else
                storePreviousNoteValues()
        } else
            saveNote()
        Log.d(TAG, "onPause")
    }

    private fun deleteNoteFromDatabase() {
        val selection = "${NoteInfoEntry._ID} = ?"
        val args = arrayOf(noteId.toString())
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val db = dbOpenHelper.writableDatabase
            db.delete(NoteInfoEntry.TABLE_NAME, selection, args)
        }
    }

    private fun storePreviousNoteValues() {
        val course = viewModel.originalNoteCourseId.let { DataManager.instance.getCourse(it) }
        course?.let { note.course = it }
        note.title = viewModel.originalNoteTitle
        note.text = viewModel.originalNoteText
    }

    private fun saveNote() {
        val courseId = selectedCourseId()
        val noteTitle = textNoteTitle.text.toString()
        val noteText = textNoteText.text.toString()
        saveNoteToDatabase(courseId, noteTitle, noteText)
    }

    private fun selectedCourseId(): String {
        val selectedPosition = spinnerCourses.selectedItemPosition
        val cursor = adapterCourses.cursor
        cursor.moveToPosition(selectedPosition)
        val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
        return cursor.getString(courseIdPos)
    }

    private fun saveNoteToDatabase(courseId: String, noteTitle: String, noteText: String){
        val selection = "${NoteInfoEntry._ID} = ?"
        val args = arrayOf(noteId.toString())

        val values = ContentValues()
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId)
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle)
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText)

        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val db = dbOpenHelper.writableDatabase
            db.update(NoteInfoEntry.TABLE_NAME, values, selection, args)
        }
    }

    override fun onDestroy() {
        dbOpenHelper.close()
        super.onDestroy()
    }

    companion object {
        const val NOTE_ID = "com.socap.notekeeper.NOTE_POSITION"
        const val ID_NOT_SET = -1
        const val LOADER_NOTES = 0
        const val LOADER_COURSES = 1
        private val TAG = NoteActivity::class.java.simpleName
    }
}