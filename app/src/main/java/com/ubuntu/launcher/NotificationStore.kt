package com.ubuntu.launcher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** সিস্টেমের নোটিফিকেশন পড়ে নোটিফিকেশন শেডে দেখানোর জন্য জমা রাখে। */
class NotificationStore : NotificationListenerService() {

    override fun onListenerConnected() {
        instance = this
        refresh()
    }

    override fun onListenerDisconnected() {
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) = refresh()
    override fun onNotificationRemoved(sbn: StatusBarNotification) = refresh()

    private fun refresh() {
        items = try {
            (activeNotifications ?: emptyArray())
                .filter { it.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0 }
                .sortedByDescending { it.postTime }
        } catch (e: Exception) {
            emptyList()
        }
        listener?.invoke()
    }

    companion object {
        var instance: NotificationStore? = null
        var items: List<StatusBarNotification> = emptyList()
        var listener: (() -> Unit)? = null

        fun cancel(key: String) { instance?.cancelNotification(key) }
        fun cancelAll() { instance?.cancelAllNotifications() }
    }
}
