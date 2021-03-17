package com.socap.notekeeper

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModel

class NoteActivityViewModel : ViewModel() {
    lateinit var originalNoteUri: String
    lateinit var originalNoteCourseId : String
    lateinit var originalNoteTitle : String
    lateinit var originalNoteText : String
    var isNewlyCreated = true

    fun saveState(outState: Bundle) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID, originalNoteCourseId)
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle)
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText)
    }

    fun restoreState(inState: Bundle){
        inState.getString(ORIGINAL_NOTE_COURSE_ID)?.let { originalNoteCourseId = it }
        inState.getString(ORIGINAL_NOTE_TITLE)?.let { originalNoteTitle = it }
        inState.getString(ORIGINAL_NOTE_TEXT)?.let { originalNoteText = it }
        inState.getString(ORIGINAL_NOTE_URI)?.let { originalNoteUri = it }
    }

    companion object{
        const val ORIGINAL_NOTE_COURSE_ID = "package com.socap.notekeeper.ORIGINAL_NOTE_COURSE_ID"
        const val ORIGINAL_NOTE_TITLE = "package com.socap.notekeeper.ORIGINAL_NOTE_TITLE"
        const val ORIGINAL_NOTE_TEXT = "package com.socap.notekeeper.ORIGINAL_NOTE_TEXT"
        const val ORIGINAL_NOTE_URI = "package com.socap.notekeeper.ORIGINAL_NOTE_URI"
    }
}