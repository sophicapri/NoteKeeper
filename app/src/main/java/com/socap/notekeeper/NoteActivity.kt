package com.socap.notekeeper

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.socap.notekeeper.NoteKeeperProviderContract.Courses
import com.socap.notekeeper.NoteKeeperProviderContract.Notes
import com.socap.notekeeper.databinding.ActivityNoteBinding
import java.util.concurrent.Executors
import android.view.View
import android.widget.ProgressBar
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception


class NoteActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var binding: ActivityNoteBinding
    private var note: NoteInfo = NoteInfo()
    private var isNewNote = false
    private lateinit var spinnerCourses: Spinner
    private lateinit var textNoteTitle: EditText
    private lateinit var textNoteText: EditText
    private var noteId = 0
    private lateinit var viewModel: NoteActivityViewModel
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper
    private lateinit var noteCursor: Cursor
    private var courseIdPos = 0
    private var noteTitlePos = 0
    private var noteTextPos = 0
    private lateinit var adapterCourses: SimpleCursorAdapter
    private var coursesQueryFinished = false
    private var noteQueryFinished = false
    private lateinit var noteUri: Uri
    private var isActivityRecreated = false
    private lateinit var viewModuleStatus: ModuleStatusView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        dbOpenHelper = NoteKeeperOpenHelper(this)
        val viewModelProvider = ViewModelProvider(viewModelStore,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )

        viewModel = viewModelProvider.get(NoteActivityViewModel::class.java)
        if (viewModel.isNewlyCreated && savedInstanceState != null) {
            viewModel.restoreState(savedInstanceState)
            noteUri = Uri.parse(viewModel.originalNoteUri)
        }
        viewModel.isNewlyCreated = false

        if (savedInstanceState != null) isActivityRecreated = true

        adapterCourses =
            SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item, null,
                arrayOf(CourseInfoEntry.COLUMN_COURSE_TITLE),
                IntArray(1) { android.R.id.text1 }, 0
            )
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses = binding.contentNote.spinnerCourses
        spinnerCourses.adapter = adapterCourses

        LoaderManager.getInstance(this).restartLoader(LOADER_COURSES, null, this)

        textNoteTitle = binding.contentNote.textNoteTitle
        textNoteText = binding.contentNote.textNoteText

        readDisplayStateValues()
        if (!isNewNote)
            LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this)
        viewModuleStatus = findViewById(R.id.module_status)
        loadModuleStatusValues()
    }

    private fun loadModuleStatusValues() {
        // In real life we'd lookup the selected course's module statuses from the content provider
        val totalNumberOfModules = 11
        val completedNumberOfModules = 7
        val moduleStatus = BooleanArray(totalNumberOfModules)
        for (moduleIndex in 0 until completedNumberOfModules) moduleStatus[moduleIndex] = true
        viewModuleStatus.moduleStatus = moduleStatus
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        Log.i(TAG, "onCreateLoader: ")
        var loader: Loader<Cursor> = CursorLoader(this)
        if (id == LOADER_NOTES)
            loader = createLoaderNote()
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses()
        return loader
    }

    private fun createLoaderCourses(): Loader<Cursor> {
        coursesQueryFinished = false
        val uri = Courses.CONTENT_URI
        val courseColumns = arrayOf(
            Courses.COLUMN_COURSE_TITLE,
            Courses.COLUMN_COURSE_ID,
            Courses._ID
        )
        return CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE)
    }

    private fun createLoaderNote(): Loader<Cursor> {
        noteQueryFinished = false
        val noteColumns: Array<String> = arrayOf(
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT
        )
        Log.i(TAG, "createLoaderNote: ")
        noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, noteId.toLong())
        return CursorLoader(this, noteUri, noteColumns, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        Log.i(TAG, "onLoadFinished: ")
        if (loader.id == LOADER_NOTES)
            loadFinishedNote(data)
        else if (loader.id == LOADER_COURSES) {
            adapterCourses.changeCursor(data)
            coursesQueryFinished = true
            displayNoteWhenQueriesFinished()
        }
    }

    // TODO: fix issue with backstack
    private fun loadFinishedNote(data: Cursor) {
        noteCursor = data
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT)

        noteCursor.moveToFirst()
        noteQueryFinished = true
        displayNoteWhenQueriesFinished()
    }

    private fun displayNoteWhenQueriesFinished() {
        if (noteQueryFinished && coursesQueryFinished) {
            if (!isActivityRecreated) {
                displayNote()
                saveOriginalNoteValues()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTES)
            noteCursor.close()
        else if (loader.id == LOADER_COURSES)
            adapterCourses.changeCursor(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NoteActivityViewModel.ORIGINAL_NOTE_URI, noteUri.toString())
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
            R.id.action_cancel -> cancelNoteCreation()
          //R.id.action_next -> moveNext()
            R.id.action_set_reminder -> showReminderNotification()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cancelNoteCreation() {
        if (isNewNote)
            deleteNoteFromDatabase()
        else
            storePreviousNoteValues()
        finish()
    }

    @SuppressLint("InlinedApi")
    private fun showReminderNotification() {
        val noteTitle: String = textNoteTitle.text.toString()
        val noteText: String = textNoteText.text.toString()
        // val noteId = ContentUris.parseId(noteUri).toInt()

        val intent = Intent(this, NoteReminderReceiver::class.java)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId)

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R || Build.VERSION.CODENAME == "S")
                PendingIntent.FLAG_MUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val currentTimeInMilliseconds = SystemClock.elapsedRealtime()
        val ONE_HOUR = (60 * 60 * 1000).toLong()
        val TEN_SECONDS = (10 * 1000)
        val alarmTime = currentTimeInMilliseconds + TEN_SECONDS
        alarmManager[AlarmManager.ELAPSED_REALTIME, alarmTime] = pendingIntent
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item: MenuItem = menu.findItem(R.id.action_next)
        val lastNoteIndex = DataManager.instance.notes.size - 1
        item.isEnabled = noteId < lastNoteIndex
        return super.onPrepareOptionsMenu(menu)
    }

    /*private fun moveNext() {
        saveNote()
        ++noteId
        note = DataManager.instance.notes[noteId]
        saveOriginalNoteValues()
        displayNote()
        invalidateOptionsMenu()
    }*/

    private fun sendEmail() {
        //val course = spinnerCourses.selectedItem as CourseInfo
        val subject = textNoteTitle.text.toString()
        val text = "Check out what I learned in the Pluralsight course \"" //+
        //  "${course.title}\"\n${textNoteText.text}"

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
    }

    private fun createNewNote() {
        val values = ContentValues()
        values.put(Notes.COLUMN_COURSE_ID, "")
        values.put(Notes.COLUMN_NOTE_TITLE, "")
        values.put(Notes.COLUMN_NOTE_TEXT, "")

        val executor = Executors.newSingleThreadExecutor()
        val uiThread = Handler(Looper.getMainLooper())
        val progressBar = binding.contentNote.progressBar
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 1
        executor.execute {
            try {
                noteUri = contentResolver.insert(Notes.CONTENT_URI, values)!!
                simulateLongRunningWork() // simulate slow database work
                uiThread.post { progressBar.progress = 2 }
                simulateLongRunningWork() // simulate slow work with data
                uiThread.post { progressBar.progress = 3 }
                uiThread.post { displaySnackbar(noteUri.toString(), progressBar) }
            } catch (e: NullPointerException) {
                Log.e(TAG, "createNewNote: ${e.message}", e.cause)
            }
        }
    }

    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(2000)
        } catch (ex: Exception) {
        }
    }

    private fun displaySnackbar(message: String, progressBar: ProgressBar) {
        val view = findViewById<View>(R.id.spinner_courses)
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
    }

    private fun saveOriginalNoteValues() {
        Log.i(TAG, "saveOriginalNoteValues: ")
        if (isNewNote)
            return
        viewModel.originalNoteCourseId = note.course.courseId
        viewModel.originalNoteTitle = note.title
        viewModel.originalNoteText = note.text
        viewModel.originalNoteUri = noteUri.toString()
    }

    private fun displayNote() {
        val courseId = noteCursor.getString(courseIdPos)
        val noteTitle = noteCursor.getString(noteTitlePos)
        val noteText = noteCursor.getString(noteTextPos)

        val courseIndex = getIndexOfCourseId(courseId)
        spinnerCourses.setSelection(courseIndex)
        textNoteTitle.setText(noteTitle)
        textNoteText.setText(noteText)

        sendBroadcast(CourseEventBroadcastHelper.getEventBroadcastIntent(courseId, "Editing Note"))
    }

    private fun getIndexOfCourseId(courseId: String): Int {
        val cursor = adapterCourses.cursor
        val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
        var courseRowIndex = 0

        var more = cursor.moveToFirst()
        while (more) {
            val cursorCourseId = cursor.getString(courseIdPos)
            if (courseId == cursorCourseId)
                break

            courseRowIndex++
            more = cursor.moveToNext()
        }
        return courseRowIndex
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.i(TAG, "onBackPressed: ")
        saveNote()
    }

    private fun deleteNoteFromDatabase() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            contentResolver.delete(noteUri, null, null)
        }
    }

    private fun storePreviousNoteValues() {
        val course = viewModel.originalNoteCourseId.let { DataManager.instance.getCourse(it) }
        course?.let { note.course = it }
        note.title = viewModel.originalNoteTitle
        note.text = viewModel.originalNoteText
    }

    private fun saveNote() {
        val courseId = selectedCourseId()
        val noteTitle = textNoteTitle.text.toString()
        val noteText = textNoteText.text.toString()
        saveNoteToDatabase(courseId, noteTitle, noteText)
    }

    private fun selectedCourseId(): String {
        val selectedPosition = spinnerCourses.selectedItemPosition
        val cursor = adapterCourses.cursor
        cursor.moveToPosition(selectedPosition)
        val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
        return cursor.getString(courseIdPos)
    }

    private fun saveNoteToDatabase(courseId: String, noteTitle: String, noteText: String) {
        val values = ContentValues()
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId)
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle)
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText)

        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            contentResolver.update(noteUri, values, null, null)
        }
    }

    override fun onDestroy() {
        dbOpenHelper.close()
        super.onDestroy()
    }

    companion object {
        const val NOTE_ID = "com.socap.notekeeper.NOTE_POSITION"
        const val ID_NOT_SET = -1
        const val LOADER_NOTES = 0
        const val LOADER_COURSES = 1
        private val TAG = NoteActivity::class.java.name
    }
}