package com.socap.notekeeper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import com.socap.notekeeper.databinding.ActivityNoteBinding


class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var note: NoteInfo? = null

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
        val spinnerCourses = binding.contentNote.spinnerCourses
        spinnerCourses.adapter = adapterCourses

        readDisplayStateValues()
        displayNote(
            spinnerCourses,
            binding.contentNote.textNoteTitle,
            binding.contentNote.textNoteText
        )
    }

    private fun displayNote(
        spinnerCourses: Spinner,
        textNoteTitle: EditText,
        textNoteText: EditText
    ) {
        val courses = DataManager.instance.courses
        val courseIndex = courses.indexOf(note?.course)
        spinnerCourses.setSelection(courseIndex)
        textNoteTitle.setText(note?.title)
        textNoteText.setText(note?.text)
    }

    private fun readDisplayStateValues() {
        val intent: Intent = intent
        note = intent.getParcelableExtra(NOTE_INFO)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val NOTE_INFO = "com.socap.notekeeper.NOTE_INFO"
    }
}