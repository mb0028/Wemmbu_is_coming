package mb28.WemmbuIsComing

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import mb28.WemmbuIsComing.MainActivity.Companion.WEMMBU
import mb28.WemmbuIsComing.MainActivity.Companion.WEMMBU_INTERVAL

fun scheduleAlarms(context: Context) {
    val sp = context.getSharedPreferences(WEMMBU, 0)
    val alertInterval = sp.getInt(WEMMBU_INTERVAL, 13)
    val sec = ((alertInterval * 60) * 60)

    val alarmManager = context.getSystemService<AlarmManager>()
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 28, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    alarmManager?.set(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis() + (sec * 1000),
        pendingIntent
    )
}

@SuppressLint("FullScreenIntentPolicy")
fun fireNow(context: Context) {
    val nm = NotificationManagerCompat.from(context)
    val channel = NotificationChannelCompat.Builder(
        "WEMMBU", NotificationManager.IMPORTANCE_HIGH)
        .setName("Wemmbu alerts")
        .setDescription("Shows wemmbu alerts")
        .setVibrationEnabled(true)
        .setVibrationPattern(longArrayOf(2000, 1000, 1000, 1000, 1000, 1000, 1000))
        .build()
    nm.createNotificationChannel(channel)

    val intent = Intent(context, WemmbuAlarmActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    val pendingIntent = PendingIntent.getActivity(context, 28, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(context, "WEMMBU")
        .setContentTitle("Wemmbu is flying above your area")
        .setContentText("Please don't panic asn stay calm")
        .setSmallIcon(R.drawable.wemmbu3d)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setFullScreenIntent(pendingIntent, true)
        .setAutoCancel(true)
        .setTimeoutAfter(30000)
        .build()

    if (ActivityCompat.checkSelfPermission(context,
            Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        nm.notify(60, notification)
    }
}

fun cancelAllAlarms(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        context.getSystemService<AlarmManager>()?.cancelAll()
    } else {
        context.getSystemService<AlarmManager>()?.cancel {

        }
    }
    context.getSystemService<NotificationManagerCompat>()?.cancelAll()
}