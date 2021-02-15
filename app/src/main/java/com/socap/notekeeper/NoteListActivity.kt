package com.socap.notekeeper

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.socap.notekeeper.databinding.ActivityNoteBinding
import com.socap.notekeeper.databinding.ActivityNoteListBinding

class NoteListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

       /* findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        */

        initializeDisplayContent()
    }

    private fun initializeDisplayContent() {
        val listNotes: ListView = binding.contentNoteList.listNotes
        val notes : List<NoteInfo> = DataManager.instance.notes

        val adapterNotes : ArrayAdapter<NoteInfo> =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, notes)

        listNotes.adapter = adapterNotes
    }
}