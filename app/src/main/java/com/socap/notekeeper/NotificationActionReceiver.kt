package com.socap.notekeeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.setClass(context, NoteBackupService::class.java)
        NoteBackupService.enqueueWork(context, intent)
    }

    companion object{
        private val TAG: String = NotificationActionReceiver::class.java.name
    }
}