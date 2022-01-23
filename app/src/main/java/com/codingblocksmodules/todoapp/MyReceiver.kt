package com.codingblocksmodules.todoapp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        createNotification(context, intent, intent.getStringExtra("TodoTitle"))
    }
    private fun createNotification(
        context: Context?,
        intent: Intent,
        title: CharSequence?
    ) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = NotificationCompat.Builder(context!!, "todoAppChannel")
        builder.setContentTitle("todo App")
        builder.setContentText(title)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(200, builder.build())
    }
}