package com.socap.notekeeper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.socap.notekeeper.databinding.ActivityNoteBinding


class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private lateinit var note: NoteInfo
    private var isNewNote = false
    private lateinit var spinnerCourses: Spinner
    private lateinit var textNoteTitle: EditText
    private lateinit var textNoteText: EditText
    private var notePosition = 0
    private var isCancelling = false
    lateinit var viewModel: NoteActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

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
            displayNote(
                spinnerCourses,
                textNoteTitle,
                textNoteText,
            )
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
        }
        return super.onOptionsItemSelected(item)
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
//        note = dm.notes[notePosition]
    }

    private fun displayNote(
        spinnerCourses: Spinner,
        textNoteTitle: EditText,
        textNoteText: EditText
    ) {
        val courses = DataManager.instance.courses
        val courseIndex = courses.indexOf(note.course)
        spinnerCourses.setSelection(courseIndex)
        textNoteTitle.setText(note.title)
        textNoteText.setText(note.text)
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            if (isNewNote)
                DataManager.instance.removeNote(notePosition)
            else
                storePreviousNoteValues()
        } else
            saveNote()
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

    companion object {
        const val NOTE_POSITION = "com.socap.notekeeper.NOTE_POSITION"
        const val POSITION_NOT_SET = -1
    }
}