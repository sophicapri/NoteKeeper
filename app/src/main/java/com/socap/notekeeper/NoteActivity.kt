package com.socap.notekeeper

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.socap.notekeeper.databinding.ActivityNoteBinding


class NoteActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityNoteBinding
    private lateinit var note: NoteInfo
    private var isNewNote = false
    private lateinit var spinnerCourses: Spinner
    private lateinit var textNoteTitle: EditText
    private lateinit var textNoteText: EditText
    private var notePosition = 0
    private var isCancelling = false
    lateinit var viewModel: NoteActivityViewModel
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper
    private lateinit var noteCursor: Cursor
    private var courseIdPos = 0
    private var noteTitlePos = 0
    private var noteTextPos = 0

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

        val courses: List<CourseInfo> = DataManager.instance.courses
        val adapterCourses: ArrayAdapter<CourseInfo> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerCourses = binding.contentNote.spinnerCourses

        spinnerCourses.adapter = adapterCourses
        textNoteTitle = binding.contentNote.textNoteTitle
        textNoteText = binding.contentNote.textNoteText

        readDisplayStateValues()
        saveOriginalNoteValues()
        if (!isNewNote)
            loadNoteData()

        Log.d(TAG, "onCreate")
    }

    private fun loadNoteData() {
        val db = dbOpenHelper.readableDatabase

        val courseId = "android_intents"
        val titleStart = "dynamic"

        val selection =
            "${NoteInfoEntry.COLUMN_COURSE_ID} = ? AND ${NoteInfoEntry.COLUMN_NOTE_TITLE} LIKE ?"
        val selectionArgs: Array<String> = arrayOf(courseId, "$titleStart%")

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
        item.isEnabled = notePosition < lastNoteIndex
        return super.onPrepareOptionsMenu(menu)
    }

    private fun moveNext() {
        saveNote()

        ++notePosition
        note = DataManager.instance.notes[notePosition]

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
        notePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET)
        isNewNote = notePosition == POSITION_NOT_SET
        if (isNewNote)
            createNewNote()

        Log.i(TAG, "readDisplayStateValues: - notePosition = $notePosition")
        note = DataManager.instance.notes[notePosition]
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
        notePosition = dm.createNewNote()
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
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position $notePosition")
            if (isNewNote)
                DataManager.instance.removeNote(notePosition)
            else
                storePreviousNoteValues()
        } else
            saveNote()
        Log.d(TAG, "onPause")
    }

    private fun storePreviousNoteValues() {
        val course = DataManager.instance.getCourse(viewModel.originalNoteCourseId)
        note.course = course
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
        const val NOTE_POSITION = "com.socap.notekeeper.NOTE_POSITION"
        const val POSITION_NOT_SET = -1
    }
}