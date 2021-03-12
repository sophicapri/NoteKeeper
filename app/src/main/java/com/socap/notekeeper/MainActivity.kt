package com.socap.notekeeper

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.socap.notekeeper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteRecyclerAdapter: NoteRecyclerAdapter
    private lateinit var courseRecyclerAdapter: CourseRecyclerAdapter
    private lateinit var recyclerItems: RecyclerView
    private lateinit var notesLayoutManager: LinearLayoutManager
    private lateinit var courseLayoutManager: GridLayoutManager
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper
    var notRestart = true
    val TAG = "com.socap.notekeeper.MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        dbOpenHelper = NoteKeeperOpenHelper(this)
        binding.appBarMain.fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        configureDrawerLayout()
        initializeDisplayContent()
    }

    private fun configureDrawerLayout() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.appBarMain.toolbar,
            R.string.open_navigation_drawer, R.string.close_navigation_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onRestart() {
        super.onRestart()
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this)
        notRestart = false
        Log.d(TAG, "onRestart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        if (notRestart)
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this)
        updateNavHeader()
    }

    private fun updateNavHeader() {
        val navigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)
        val textUserName = headerView.findViewById<TextView>(R.id.text_user_name)
        val textEmailAddress = headerView.findViewById<TextView>(R.id.text_email_address)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val userName = sharedPref.getString(
            getString(R.string.key_user_display_name),
            getString(R.string.pref_default_display_name)
        )
        val emailAddress = sharedPref.getString(
            getString(R.string.key_user_email_address),
            getString(R.string.pref_default_email_address)
        )

        textUserName.text = userName
        textEmailAddress.text = emailAddress
    }

    private fun initializeDisplayContent() {
        DataManager.loadFromDatabase(dbOpenHelper)

        recyclerItems = binding.appBarMain.contentMain.listItems
        notesLayoutManager = LinearLayoutManager(this)
        courseLayoutManager =
            GridLayoutManager(this, resources.getInteger(R.integer.course_grid_span))

        noteRecyclerAdapter = NoteRecyclerAdapter(this, cursor = null)

        val courses = DataManager.instance.courses
        courseRecyclerAdapter = CourseRecyclerAdapter(this, courses)

        displayNotes()
    }

    private fun displayNotes() {
        recyclerItems.layoutManager = notesLayoutManager
        recyclerItems.adapter = noteRecyclerAdapter

        selectNavigationMenuItem(R.id.nav_notes)
    }

    private fun displayCourses() {
        recyclerItems.layoutManager = courseLayoutManager
        recyclerItems.adapter = courseRecyclerAdapter
        selectNavigationMenuItem(R.id.nav_courses)
    }

    private fun selectNavigationMenuItem(id: Int) {
        binding.navView.menu.findItem(id).isChecked = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_notes -> displayNotes()
            R.id.nav_courses -> displayCourses()
            R.id.nav_share -> handleShare()
            R.id.nav_send -> handleSelection(R.string.nav_send_message)
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleShare() {
        Snackbar.make(
            recyclerItems, "Share to - ${
                PreferenceManager
                    .getDefaultSharedPreferences(this).getString(
                        getString(R.string.key_user_favorite_social),
                        getString(R.string.pref_default_favorite_social)
                    )
            }", Snackbar.LENGTH_LONG
        ).show()
    }

    private fun handleSelection(message_id: Int) {
        Snackbar.make(recyclerItems, message_id, Snackbar.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        dbOpenHelper.close()
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        notRestart = false
        Log.d(TAG, "onPause: ")
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        var loader: Loader<Cursor> = CursorLoader(this)
        if (id == LOADER_NOTES) {
            loader = object : CursorLoader(this) {
                override fun loadInBackground(): Cursor? {
                    val db = dbOpenHelper.readableDatabase
                    val noteColumns: Array<String> = arrayOf(
                        NoteInfoEntry.getQName(NoteInfoEntry.ID),
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_TITLE
                    )
                    val noteOrderBy =
                        "${CourseInfoEntry.COLUMN_COURSE_TITLE},${NoteInfoEntry.COLUMN_NOTE_TITLE}"

                    // note_info JOIN course_info ON note_info.course_id = course_info.course_id
                    val tablesWithJoin =
                        "${NoteInfoEntry.TABLE_NAME} JOIN ${CourseInfoEntry.TABLE_NAME} ON " +
                                "${NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)} = " +
                                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID)
                    return db.run {
                        query(
                            tablesWithJoin, noteColumns, null,
                            null, null, null, noteOrderBy
                        )
                    }
                }
            }
        }
        return loader
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        Log.d(TAG, "onLoadFinished: ")
        if (loader.id == LOADER_NOTES) {
            noteRecyclerAdapter.changeCursor(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Log.d(TAG, "onLoaderReset: ")
        if (loader.id == LOADER_NOTES)
            noteRecyclerAdapter.changeCursor(null)
    }

    companion object {
        const val LOADER_NOTES = 3
    }
}