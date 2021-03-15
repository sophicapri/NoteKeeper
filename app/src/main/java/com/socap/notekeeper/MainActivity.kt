package com.socap.notekeeper

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.socap.notekeeper.NoteKeeperProviderContract.Notes
import com.socap.notekeeper.databinding.ActivityMainBinding
import java.util.concurrent.Executors

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        enableStrictMode()

        dbOpenHelper = NoteKeeperOpenHelper(this)
        binding.appBarMain.fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        configureDrawerLayout()
        initializeDisplayContent()
    }

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            val policy = StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            StrictMode.setThreadPolicy(policy)
        }
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

    /**
     * Using #notRestart flag Because onRestart() gets called before onResume() and loaderManager
     * crashes if not loaded properly
     */
    override fun onRestart() {
        super.onRestart()
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this)
        notRestart = false
        Log.d(TAG, "onRestart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        if (notRestart) {
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this)
        }
        notRestart = true
        updateNavHeader()
        openDrawer()
    }

    private fun updateNavHeader() {
        val navigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)
        val textUserName = headerView.findViewById<TextView>(R.id.text_user_name)
        val textEmailAddress = headerView.findViewById<TextView>(R.id.text_email_address)
        var userName: String?
        var emailAddress: String?
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            userName = sharedPref.getString(
                getString(R.string.key_user_display_name),
                getString(R.string.pref_default_display_name)
            )
            emailAddress = sharedPref.getString(
                getString(R.string.key_user_email_address),
                getString(R.string.pref_default_email_address)
            )
            val uiThread = Handler(Looper.getMainLooper())
            uiThread.post {
                textUserName.text = userName
                textEmailAddress.text = emailAddress
            }
        }
    }

    private fun openDrawer() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val drawer = binding.drawerLayout
            drawer.openDrawer(Gravity.START)
        }, 1000)
    }

    private fun initializeDisplayContent() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            DataManager.loadFromDatabase(dbOpenHelper)
        }
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
        Log.d(Companion.TAG, "onDestroy: ")
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        notRestart = false
        Log.d(Companion.TAG, "onPause: ")
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        var loader: Loader<Cursor> = CursorLoader(this)
        if (id == LOADER_NOTES) {
            val noteColumns: Array<String> = arrayOf(
                Notes._ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_COURSE_TITLE
            )
            val noteOrderBy = "${Notes.COLUMN_COURSE_TITLE},${Notes.COLUMN_NOTE_TITLE}"

            loader = CursorLoader(
                this, Notes.CONTENT_EXPANDED_URI, noteColumns,
                null, null, noteOrderBy
            )
        }
        return loader
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (loader.id == LOADER_NOTES) {
            noteRecyclerAdapter.changeCursor(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTES)
            noteRecyclerAdapter.changeCursor(null)
    }

    companion object {
        const val LOADER_NOTES = 3
        private val TAG = MainActivity::class.java.name
    }
}