package com.socap.notekeeper

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.socap.notekeeper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteRecyclerAdapter: NoteRecyclerAdapter
    private lateinit var courseRecyclerAdapter: CourseRecyclerAdapter
    private lateinit var recyclerItems: RecyclerView
    private lateinit var notesLayoutManager: LinearLayoutManager
    private lateinit var courseLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        configureDrawerLayout()
        initializeDisplayContent()
    }

    private fun configureDrawerLayout() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.appBarMain.toolbar,
            R.string.open_navigation_drawer, R.string.close_navigation_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        noteRecyclerAdapter.notifyDataSetChanged()
    }

    private fun initializeDisplayContent() {
        recyclerItems = binding.appBarMain.contentMain.listItems
        notesLayoutManager = LinearLayoutManager(this)
        courseLayoutManager = GridLayoutManager(this, resources.getInteger(R.integer.course_grid_span))

        val notes = DataManager.instance.notes
        noteRecyclerAdapter = NoteRecyclerAdapter(this, notes)

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_notes -> displayNotes()
            R.id.nav_courses -> displayCourses()
            R.id.nav_share -> handleSelection(R.string.nav_share_message)
            R.id.nav_send -> handleSelection(R.string.nav_send_message)
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleSelection(message_id: Int) {
        val view = binding.appBarMain.contentMain.listItems
        Snackbar.make(view, message_id, Snackbar.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}