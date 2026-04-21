package com.pushnotifier.notification

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import android.os.Build
import com.pushnotifier.data.AppDatabase
import com.pushnotifier.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationListenerService : android.service.notification.NotificationListenerService() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val dao by lazy { database.notificationDao() }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        // Skip our own notifications
        if (sbn.packageName == packageName) return
        
        // Skip ongoing notifications (like music playback, calls)
        if (sbn.isOngoing) return
        
        extractAndSaveNotification(sbn)
    }

    private fun extractAndSaveNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getString(Notification.EXTRA_TEXT)
        val bigText = extras.getString(Notification.EXTRA_BIG_TEXT) ?: text
        
        scope.launch {
            val entity = NotificationEntity(
                packageName = sbn.packageName,
                title = title,
                text = bigText,
                postedTime = System.currentTimeMillis(),
                appName = getAppName(sbn.packageName)
            )
            dao.insert(entity)
        }
    }

    private fun getAppName(packageName: String): String? {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    companion object {
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val componentName = ComponentName(context, NotificationListenerService::class.java)
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return !flat.isNullOrBlank() && flat.contains(componentName.flattenToString())
        }
    }
}
