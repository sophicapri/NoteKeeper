package com.socap.notekeeper

import android.database.Cursor
import com.socap.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.socap.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import kotlin.collections.ArrayList

class DataManager private constructor() {
    private val mCourses: MutableList<CourseInfo> = ArrayList()
    private val mNotes: MutableList<NoteInfo> = ArrayList()
    val currentUserName: String
        get() = "Jim Wilson"
    val currentUserEmail: String
        get() = "jimw@jwhh.com"
    val notes: MutableList<NoteInfo>
        get() = mNotes

    fun createNewNote(): Int {
        val note = NoteInfo()
        mNotes.add(note)
        return mNotes.size - 1
    }

    fun findNote(note: NoteInfo): Int {
        for (index in mNotes.indices) {
            if (note == mNotes[index]) return index
        }
        return -1
    }

    fun removeNote(index: Int) {
        mNotes.removeAt(index)
    }

    val courses: MutableList<CourseInfo>
        get() = mCourses

    fun getCourse(id: String): CourseInfo? {
        for (course in mCourses) {
            if (id == course.courseId) return course
        }
        return null
    }

    fun getNotes(course: CourseInfo): List<NoteInfo> {
        val notes = ArrayList<NoteInfo>()
        for (note in mNotes) {
            if (course == note.course) notes.add(note)
        }
        return notes
    }

    fun getNoteCount(course: CourseInfo): Int {
        var count = 0
        for (note in mNotes) {
            if (course == note.course) count++
        }
        return count
    }

    //region Initialization code
    private fun initializeCourses() {
        mCourses.add(initializeCourse1())
        mCourses.add(initializeCourse2())
        mCourses.add(initializeCourse3())
        mCourses.add(initializeCourse4())
    }

    fun initializeExampleNotes() {
        val dm = instance
        var course = dm.getCourse("android_intents")
        course?.let {
            it.getModule("android_intents_m01")?.isComplete = true
            it.getModule("android_intents_m02")?.isComplete = true
            it.getModule("android_intents_m03")?.isComplete = true
            mNotes.add(
                NoteInfo(
                    course = it, title = "Dynamic intent resolution",
                    text = "Wow, intents allow components to be resolved at runtime"
                )
            )
            mNotes.add(
                NoteInfo(
                    course = it, title = "Delegating intents",
                    text = "PendingIntents are powerful; they delegate much more than just a component invocation"
                )
            )
        }
        course = dm.getCourse("android_async")
        course?.let {
            it.getModule("android_async_m01")?.isComplete = true
            it.getModule("android_async_m02")?.isComplete = true
            mNotes.add(
                NoteInfo(
                    course = it, title =  "Service default threads",
                    text = "Did you know that by default an Android Service will tie up the UI thread?"
                )
            )
            mNotes.add(
                NoteInfo(
                    course = it, title = "Long running operations",
                    text = "Foreground Services can be tied to a notification icon"
                )
            )
        }
        course = dm.getCourse("java_lang")
        course?.let {
            it.getModule("java_lang_m01")?.isComplete = true
            it.getModule("java_lang_m02")?.isComplete = true
            it.getModule("java_lang_m03")?.isComplete = true
            it.getModule("java_lang_m04")?.isComplete = true
            it.getModule("java_lang_m05")?.isComplete = true
            it.getModule("java_lang_m06")?.isComplete = true
            it.getModule("java_lang_m07")?.isComplete = true

            mNotes.add(
                NoteInfo(
                    course = it, title = "Parameters",
                    text = "Leverage variable-length parameter lists"
                )
            )
            mNotes.add(
                NoteInfo(
                    course = it, title = "Anonymous classes",
                    text = "Anonymous classes simplify implementing one-use types"
                )
            )
        }
        course = dm.getCourse("java_core")
        course?.let {
            it.getModule("java_core_m01")?.isComplete = true
            it.getModule("java_core_m02")?.isComplete = true
            it.getModule("java_core_m03")?.isComplete = true
            mNotes.add(
                NoteInfo(
                    course = it, title = "Compiler options",
                    text = "The -jar option isn't compatible with with the -cp option"
                )
            )
            mNotes.add(
                NoteInfo(
                    course = course, title = "Serialization",
                    text = "Remember to include SerialVersionUID to assure version compatibility"
                )
            )
        }
    }

    private fun initializeCourse1(): CourseInfo {
        val modules: MutableList<ModuleInfo> = ArrayList()
        modules.add(ModuleInfo("android_intents_m01", "Android Late Binding and Intents"))
        modules.add(ModuleInfo("android_intents_m02", "Component activation with intents"))
        modules.add(
            ModuleInfo(
                "android_intents_m03",
                "Delegation and Callbacks through PendingIntents"
            )
        )
        modules.add(ModuleInfo("android_intents_m04", "IntentFilter data tests"))
        modules.add(
            ModuleInfo(
                "android_intents_m05",
                "Working with Platform Features Through Intents"
            )
        )
        return CourseInfo("android_intents", "Android Programming with Intents", modules)
    }

    private fun initializeCourse2(): CourseInfo {
        val modules: MutableList<ModuleInfo> = ArrayList()
        modules.add(ModuleInfo("android_async_m01", "Challenges to a responsive user experience"))
        modules.add(
            ModuleInfo(
                "android_async_m02",
                "Implementing long-running operations as a service"
            )
        )
        modules.add(ModuleInfo("android_async_m03", "Service lifecycle management"))
        modules.add(ModuleInfo("android_async_m04", "Interacting with services"))
        return CourseInfo("android_async", "Android Async Programming and Services", modules)
    }

    private fun initializeCourse3(): CourseInfo {
        val modules: MutableList<ModuleInfo> = ArrayList()
        modules.add(ModuleInfo("java_lang_m01", "Introduction and Setting up Your Environment"))
        modules.add(ModuleInfo("java_lang_m02", "Creating a Simple App"))
        modules.add(ModuleInfo("java_lang_m03", "Variables, Data Types, and Math Operators"))
        modules.add(ModuleInfo("java_lang_m04", "Conditional Logic, Looping, and Arrays"))
        modules.add(ModuleInfo("java_lang_m05", "Representing Complex Types with Classes"))
        modules.add(ModuleInfo("java_lang_m06", "Class Initializers and Constructors"))
        modules.add(ModuleInfo("java_lang_m07", "A Closer Look at Parameters"))
        modules.add(ModuleInfo("java_lang_m08", "Class Inheritance"))
        modules.add(ModuleInfo("java_lang_m09", "More About Data Types"))
        modules.add(ModuleInfo("java_lang_m10", "Exceptions and Error Handling"))
        modules.add(ModuleInfo("java_lang_m11", "Working with Packages"))
        modules.add(ModuleInfo("java_lang_m12", "Creating Abstract Relationships with Interfaces"))
        modules.add(
            ModuleInfo(
                "java_lang_m13",
                "Static Members, Nested Types, and Anonymous Classes"
            )
        )
        return CourseInfo("java_lang", "Java Fundamentals: The Java Language", modules)
    }

    private fun initializeCourse4(): CourseInfo {
        val modules: MutableList<ModuleInfo> = ArrayList()
        modules.add(ModuleInfo("java_core_m01", "Introduction"))
        modules.add(ModuleInfo("java_core_m02", "Input and Output with Streams and Files"))
        modules.add(ModuleInfo("java_core_m03", "String Formatting and Regular Expressions"))
        modules.add(ModuleInfo("java_core_m04", "Working with Collections"))
        modules.add(ModuleInfo("java_core_m05", "Controlling App Execution and Environment"))
        modules.add(
            ModuleInfo(
                "java_core_m06",
                "Capturing Application Activity with the Java Log System"
            )
        )
        modules.add(ModuleInfo("java_core_m07", "Multithreading and Concurrency"))
        modules.add(ModuleInfo("java_core_m08", "Runtime Type Information and Reflection"))
        modules.add(ModuleInfo("java_core_m09", "Adding Type Metadata with Annotations"))
        modules.add(ModuleInfo("java_core_m10", "Persisting Objects with Serialization"))
        return CourseInfo("java_core", "Java Fundamentals: The Core Platform", modules)
    } //endregion

    fun createNewNote(course: CourseInfo, noteTitle: String, noteText: String): Int {
        val index = createNewNote()
        val note = notes[index]
        note.course = course
        note.title = noteTitle
        note.text = noteText
        return index
    }

    companion object {
        private lateinit var ourInstance: DataManager
        val instance: DataManager
            get() {
                if (!::ourInstance.isInitialized) {
                    ourInstance = DataManager()
                    /*ourInstance.initializeCourses()
                    ourInstance.initializeExampleNotes()*/
                }
                return ourInstance
            }

        fun loadFromDatabase(dbHelper: NoteKeeperOpenHelper) {
            val db = dbHelper.readableDatabase
            val courseColumns: Array<String> = arrayOf(
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE
            )
            val courseCursor: Cursor = db.run {
                query(CourseInfoEntry.TABLE_NAME, courseColumns,
                    null, null, null, null,
                    "${CourseInfoEntry.COLUMN_COURSE_TITLE} DESC")
            }

            loadCoursesFromDatabase(courseCursor)

            val noteColumns: Array<String> = arrayOf(
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID
            )
            val noteOrderBy = "${NoteInfoEntry.COLUMN_COURSE_ID},${NoteInfoEntry.COLUMN_NOTE_TITLE}"
            val noteCursor: Cursor = db.run {
                query(NoteInfoEntry.TABLE_NAME, noteColumns, null,
                    null, null, null, noteOrderBy)
            }

            loadNotesFromDatabase(noteCursor)
        }

        private fun loadCoursesFromDatabase(cursor: Cursor) {
            val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
            val courseTitlePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE)

            val dm = instance
            dm.courses.clear()
            while (cursor.moveToNext()){
                val courseId = cursor.getString(courseIdPos)
                val courseTitle = cursor.getString(courseTitlePos)
                val course = CourseInfo(courseId, courseTitle)

                dm.courses.add(course)
            }
            cursor.close()
        }

        private fun loadNotesFromDatabase(cursor: Cursor) {
            val noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
            val noteTextPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT)
            val courseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
            val idPos = cursor.getColumnIndex(NoteInfoEntry._ID)

            val dm = instance
            dm.notes.clear()
            while (cursor.moveToNext()){
                val noteTitle = cursor.getString(noteTitlePos)
                val noteText = cursor.getString(noteTextPos)
                val courseId = cursor.getString(courseIdPos)
                val id = cursor.getInt(idPos)

                val noteCourse = dm.getCourse(courseId)
                noteCourse?.let {
                    val note = NoteInfo(id, it, noteTitle, noteText)
                    dm.notes.add(note)
                }
            }
            cursor.close()
        }
    }
}