package com.socap.notekeeper

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard



@RunWith(AndroidJUnit4::class)
class NoteCreationTest{

    @get:Rule
    var activityRule: ActivityScenarioRule<NoteListActivity>
            = ActivityScenarioRule(NoteListActivity::class.java)

/*    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
        }
    }*/

    @Test
    fun createNewNote(){
        /*val fabNewNote = onView(withId(R.id.fab))
        fabNewNote.perform(click())*/
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.text_note_title)).perform(typeText("Test note title"))
        onView(withId(R.id.text_note_text)).perform(typeText("This is the body of our test note"),
        closeSoftKeyboard())
    }
}