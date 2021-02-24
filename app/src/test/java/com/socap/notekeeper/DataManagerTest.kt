package com.socap.notekeeper

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class DataManagerTest {
    private val dm: DataManager = DataManager.instance

    @Before
    fun setUp() {
        dm.notes.clear()
        dm.initializeExampleNotes()
    }

    @Test
    fun createNewNote() {
        val course = dm.getCourse("android_async")
        val noteTitle = "Test note title"
        val noteText = "This is the boy text of my note"

        val noteIndex = dm.createNewNote()
        val newNote = dm.notes[noteIndex]
        newNote.course = course
        newNote.title = noteTitle
        newNote.text = noteText

        val compareNote = dm.notes[noteIndex]
        assertEquals(course, compareNote.course)
        assertEquals(noteTitle, compareNote.title)
        assertEquals(noteText, compareNote.text)
    }

    @Test
    fun findSimilarNotes() {
        val dm: DataManager = DataManager.instance
        val course = dm.getCourse("android_async")
        val noteTitle = "Test note title"
        val noteText1 = "This is the body text of my test note"
        val noteText2 = "This is the body of my second test note"

        val noteIndex1 = dm.createNewNote()
        val newNote1 = dm.notes[noteIndex1]
        newNote1.course = course
        newNote1.title = noteTitle
        newNote1.text = noteText1

        val noteIndex2 = dm.createNewNote()
        val newNote2 = dm.notes[noteIndex2]
        newNote2.course = course
        newNote2.title = noteTitle
        newNote2.text = noteText2

        val foundIndex1 = dm.findNote(newNote1)
        assertEquals(noteIndex1, foundIndex1)

        val foundIndex2 = dm.findNote(newNote2)
        assertEquals(noteIndex2, foundIndex2)
    }
}