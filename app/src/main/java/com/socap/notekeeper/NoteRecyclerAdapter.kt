package com.socap.notekeeper

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry

class NoteRecyclerAdapter(private val context: Context, private var cursor: Cursor?) :
    RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)
    private var coursePos: Int = POSITION_NOT_SET
    private var noteTitlePos: Int = POSITION_NOT_SET
    private var idPos: Int = POSITION_NOT_SET
    val TAG = "com.socap.notekeeper.NoteRecyclerAdapter"

    init {
        populateColumnPositions()
    }

    private fun populateColumnPositions() {
        if (cursor == null)
            return

        // get column indexes from cursor
        cursor?.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE)?.let { coursePos = it }
        cursor?.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)?.let { noteTitlePos = it }
        cursor?.getColumnIndex(NoteInfoEntry.ID)?.let { idPos = it }
    }

    fun changeCursor(cursor: Cursor?) {
        this.cursor?.close()
        this.cursor = cursor
        populateColumnPositions()
        Log.d(TAG, "changeCursor: value = $cursor")

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_note_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: value cursor = $cursor")
        cursor?.moveToPosition(position)
        val course = cursor?.getString(coursePos)
        val noteTitle = cursor?.getString(noteTitlePos)
        val id = cursor?.getInt(idPos)

        holder.textCourse.text = course
        holder.textTitle.text = noteTitle
        holder.id = id
    }

    override fun getItemCount() : Int {
        return if (cursor != null) cursor?.count!! else 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCourse: TextView = itemView.findViewById(R.id.text_course)
        val textTitle: TextView = itemView.findViewById(R.id.text_title)
        var id : Int? = -1

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, NoteActivity::class.java)
                intent.putExtra(NoteActivity.NOTE_ID, id)
                context.startActivity(intent)
            }
        }
    }
}