package com.socap.notekeeper

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.socap.notekeeper.NoteActivity.Companion.NOTE_INFO
import com.socap.notekeeper.databinding.ActivityNoteListBinding

class NoteListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            startActivity(Intent(this, NoteActivity::class.java))
        }
        initializeDisplayContent()
    }

    private fun initializeDisplayContent() {
        val listNotes: ListView = binding.contentNoteList.listNotes
        val notes : List<NoteInfo> = DataManager.instance.notes
        val adapterNotes : ArrayAdapter<NoteInfo> =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, notes)

        listNotes.adapter = adapterNotes
        listNotes.setOnItemClickListener { _, _, position, l ->
            val intent = Intent(this, NoteActivity::class.java)
            val note : NoteInfo = listNotes.getItemAtPosition(position) as NoteInfo
            intent.putExtra(NOTE_INFO, note)
            startActivity(intent)
        }
    }
}