package com.socap.notekeeper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.socap.notekeeper.databinding.ActivityNoteBinding


class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var note: NoteInfo = NoteInfo()
    private var isNewNote = false
    private lateinit var spinnerCourses: Spinner
    private lateinit var textNoteTitle: EditText
    private lateinit var textNoteText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        val courses: List<CourseInfo> = DataManager.instance.courses
        val adapterCourses: ArrayAdapter<CourseInfo> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses = binding.contentNote.spinnerCourses
        spinnerCourses.adapter = adapterCourses
        textNoteTitle = binding.contentNote.textNoteTitle
        textNoteText = binding.contentNote.textNoteText

        readDisplayStateValues()
        if (!isNewNote)
            displayNote(
                spinnerCourses,
                textNoteTitle,
                textNoteText,
            )
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
        return when (item.itemId) {
            R.id.action_send_email -> {
                sendEmail()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        val position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET)
        isNewNote = position == POSITION_NOT_SET
        if (isNewNote) {
//            createNewNote()
        } else
            note = DataManager.instance.notes[position]
    }

/*    private fun createNewNote() {
        val dm : DataManager = DataManager.instance
        val notePosition = dm.createNewNote()
    }*/

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
        saveNote()
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