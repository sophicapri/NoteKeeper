package com.socap.notekeeper

import android.content.Context
import androidx.core.app.JobIntentService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class NoteBackupService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        Log.i(TAG, "Executing work: $intent")
        val backupCourseId = intent.getStringExtra(EXTRA_COURSE_ID)
        if (backupCourseId != null)
            NoteBackup.doBackup(contentResolver, backupCourseId)
        else
            Log.e(TAG, "onHandleWork: Extra not found")
        Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime())
    }

    companion object {
        const val EXTRA_COURSE_ID = "com.socap.notekeeper.extra.COURSE_ID"
        private val TAG = NoteBackupService::class.java.name

        /**
         * Unique job ID for this service.
         */
        private const val JOB_ID = 1000

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, NoteBackupService::class.java, JOB_ID, work)
        }
    }
}