package com.socap.notekeeper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) : ContextWrapper(context) {
    private val _manager: NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val manager: NotificationManager
        get() = _manager

    init {
        createChannel()
    }

    //@TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, REMINDER, NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun getChannelNotification(
        noteTitle: String,
        noteText: String,
        noteId: Int
    ): NotificationCompat.Builder {
        val noteActivityIntent = Intent(context, NoteActivity::class.java)
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId)

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        val picture = BitmapFactory.decodeResource(context.resources, R.drawable.logo)


        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Title review note")
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSmallIcon(R.drawable.ic_stat_note_reminder)
            // Provide a large icon, shown with the notification in the
            // notification drawer on devices running Android 3.0 or later.
            .setLargeIcon(picture)
            // Set ticker text (preview) information for this notification.
            //.setTicker("Review note")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(noteText)
                    .setBigContentTitle(noteTitle)
                    .setSummaryText("Summary Review note")
            )
            // Set the pending intent to be initiated when the user touches
            // the notification.
            .setContentIntent(
                PendingIntent.getActivity(
                    context, 0, noteActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                0,
                "View all notes",
                PendingIntent.getActivity(
                    context,
                    0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setAutoCancel(true)
    }

    companion object {
        const val NOTIFICATION_ID: Int = 1
        const val CHANNEL_ID = "channelID"
        const val REMINDER = "Reminder"
    }
}