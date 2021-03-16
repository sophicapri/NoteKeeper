package com.socap.notekeeper

import android.app.job.JobService
import android.app.job.JobParameters
import android.net.Uri
import java.util.concurrent.Executors

class NoteUploaderJobService : JobService() {
    private lateinit var noteUploader: NoteUploader

    override fun onStartJob(params: JobParameters): Boolean {
        noteUploader = NoteUploader(contentResolver)
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val stringDataUri = params.extras.getString(EXTRA_DATA_URI)
            val dataUri = Uri.parse(stringDataUri)
            noteUploader.doUpload(dataUri)
            if (!noteUploader.isCanceled)
                jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        noteUploader.cancel()
        return true
    }

    companion object {
        const val EXTRA_DATA_URI = "com.socap.notekeeper.extras.DATA_URI"
    }
}