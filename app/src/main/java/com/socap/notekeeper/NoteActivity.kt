package com.socap.notekeeper

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.socap.notekeeper.databinding.ActivityNoteBinding


class NoteActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityNoteBinding
    private var note: NoteInfo = NoteInfo()
    private var isNewNote = false
    private lateinit var spinnerCourses: Spinner
    private lateinit var textNoteTitle: EditText
    private lateinit var textNoteText: EditText
    private var noteId = 0
    private var isCancelling = false
    lateinit var viewModel: NoteActivityViewModel
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper
    private lateinit var noteCursor: Cursor
    private var courseIdPos = 0
    private var noteTitlePos = 0
    private var noteTextPos = 0
    private lateinit var adapterCourses: SimpleCursorAdapter

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

        loadCourseData()

        textNoteTitle = binding.contentNote.textNoteTitle
        textNoteText = binding.contentNote.textNoteText

        readDisplayStateValues()
        if (!isNewNote)
            loadNoteData()
        saveOriginalNoteValues()

        Log.d(TAG, "onCreate")
    }

    private fun loadCourseData() {
        val db = dbOpenHelper.readableDatabase
        val courseColumns = arrayOf(
            CourseInfoEntry.COLUMN_COURSE_TITLE,
            CourseInfoEntry.COLUMN_COURSE_ID,
            CourseInfoEntry.ID
        )

        val cursor = db.run {
            query(
                CourseInfoEntry.TABLE_NAME,
                courseColumns, null, null, null, null,
                CourseInfoEntry.COLUMN_COURSE_TITLE
            )
        }
        adapterCourses.changeCursor(cursor)
    }

    private fun loadNoteData() {
        val db = dbOpenHelper.readableDatabase

        val selection = "${NoteInfoEntry.ID} = ?"
        val selectionArgs: Array<String> = arrayOf(noteId.toString())

        val noteColumns: Array<String> = arrayOf(
            NoteInfoEntry.COLUMN_COURSE_ID,
            NoteInfoEntry.COLUMN_NOTE_TITLE,
            NoteInfoEntry.COLUMN_NOTE_TEXT
        )

        noteCursor = db.run {
            query(
                NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs, null,
                null, null
            )
        }

        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT)

        noteCursor.moveToNext()
        displayNote()
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
        //note = DataManager.instance.notes[noteId]
    }

    private fun saveOriginalNoteValues() {
        if (isNewNote)
            return
        note.course?.courseId?.let { viewModel.originalNoteCourseId = it }
        note.title?.let { viewModel.originalNoteTitle = it }
        note.text?.let { viewModel.originalNoteText = it }

    }

    private fun createNewNote() {
        val dm: DataManager = DataManager.instance
        noteId = dm.createNewNote()
    }

    private fun displayNote() {
        val courseId = noteCursor.getString(courseIdPos)
        val noteTitle = noteCursor.getString(noteTitlePos)
        val noteText = noteCursor.getString(noteTextPos)
        val courses = DataManager.instance.courses
        val course = DataManager.instance.getCourse(courseId)
        val courseIndex = courses.indexOf(course)
        spinnerCourses.setSelection(courseIndex)
        textNoteTitle.setText(noteTitle)
        textNoteText.setText(noteText)

        note = NoteInfo(course = course, title = noteTitle, text = noteText)
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position $noteId")
            if (isNewNote)
                DataManager.instance.removeNote(noteId)
            else
                storePreviousNoteValues()
        } else
            saveNote()
        Log.d(TAG, "onPause")
    }

    private fun storePreviousNoteValues() {
        val course = viewModel.originalNoteCourseId.let { DataManager.instance.getCourse(it) }
        course?.let { note.course = it }
        note.title = viewModel.originalNoteTitle
        note.text = viewModel.originalNoteText
    }

    private fun saveNote() {
        note.course = spinnerCourses.selectedItem as CourseInfo
        note.title = textNoteTitle.text.toString()
        note.text = textNoteText.text.toString()
    }

    override fun onDestroy() {
        dbOpenHelper.close()
        super.onDestroy()
    }

    companion object {
        const val NOTE_ID = "com.socap.notekeeper.NOTE_POSITION"
        const val ID_NOT_SET = -1
    }
}