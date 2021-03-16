package com.socap.notekeeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NoteReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE)?: "<no title>"
        val noteText = intent.getStringExtra(EXTRA_NOTE_TEXT)?: "<no text>"
        val noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0)

        val notificationHelper = NotificationHelper(context)
        val nb: NotificationCompat.Builder = notificationHelper
            .getChannelNotification(noteTitle, noteText, noteId)
        notificationHelper.manager.notify(NotificationHelper.NOTIFICATION_ID, nb.build())
    }

    companion object {
        const val EXTRA_NOTE_TITLE = "com.socap.notekeeper.extra.NOTE_TITLE"
        const val EXTRA_NOTE_TEXT = "com.socap.notekeeper.extra.NOTE_TEXT"
        const val EXTRA_NOTE_ID = "com.socap.notekeeper.extra.NOTE_ID"
    }
}