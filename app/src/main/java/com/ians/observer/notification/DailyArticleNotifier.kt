package com.ians.observer.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ians.observer.MainActivity
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.presentation.helper.shortenSnippet
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyArticleNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.daily_news_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.daily_news_channel_description)
        }

        context
            .getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showArticle(article: Article) {
        if (!context.canPostNotifications()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = context
                .getSystemService(NotificationManager::class.java)
                .getNotificationChannel(CHANNEL_ID)

            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_ARTICLE
            putExtra(MainActivity.EXTRA_ARTICLE_ID, article.id)

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = article.description
            ?.takeIf(String::isNotBlank)
            ?.let(::shortenSnippet)
            ?: context.getString(R.string.daily_article_notification_fallback_text)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_newspaper_56)
            .setContentTitle(article.title)
            .setSubText(article.publisher.name)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(text)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat
                .from(context)
                .notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            return
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_news"
        const val NOTIFICATION_ID = 1001
    }
}

private fun Context.canPostNotifications(): Boolean {
    val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    return runtimePermissionGranted &&
            NotificationManagerCompat.from(this).areNotificationsEnabled()
}