package com.socap.notekeeper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.socap.notekeeper.databinding.ActivityNoteListBinding

class NoteListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteListBinding
    private lateinit var noteRecyclerAdapter: NoteRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }
        initializeDisplayContent()
    }

    override fun onResume() {
        super.onResume()
        noteRecyclerAdapter.notifyDataSetChanged()
    }

    private fun initializeDisplayContent() {
        val recyclerNotes = binding.contentNoteList.listNotes
        val notesLayoutManager = LinearLayoutManager(this)
        recyclerNotes.layoutManager = notesLayoutManager

        val notes = DataManager.instance.notes
        noteRecyclerAdapter = NoteRecyclerAdapter(this, notes)
        recyclerNotes.adapter = noteRecyclerAdapter
    }
}