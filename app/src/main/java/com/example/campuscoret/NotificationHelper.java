package com.example.campuscoret;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public final class NotificationHelper {
    private static final String CHANNEL_ID = "classroom_updates";
    private static final String CHANNEL_NAME = "Classroom Updates";
    private static final String CHANNEL_DESCRIPTION = "Notifications for session activity and attendance events.";
    private static String lastStudentNotifiedSessionId;

    private NotificationHelper() {
    }

    public static void notifySessionStarted(Context context, SessionRecord sessionRecord) {
        ensureChannel(context);
        if (!canPostNotifications(context)) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Session started")
                .setContentText(sessionRecord.getSubjectName() + " for " + sessionRecord.getClassName() + " is now active.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Attendance is active for " + sessionRecord.getSubjectName()
                                + " in " + sessionRecord.getClassName()
                                + ". Session ID: " + sessionRecord.getSessionId()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        postNotification(context, 1001, builder);
    }

    public static void notifySessionEnded(Context context, SessionRecord sessionRecord) {
        ensureChannel(context);
        if (!canPostNotifications(context) || sessionRecord == null) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Session ended")
                .setContentText(sessionRecord.getSubjectName() + " for " + sessionRecord.getClassName() + " has been closed.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Attendance has been closed for session " + sessionRecord.getSessionId()
                                + " in " + sessionRecord.getClassName() + "."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        postNotification(context, 1002, builder);
    }

    public static void notifyStudentSessionAvailable(Context context, SessionRecord sessionRecord) {
        ensureChannel(context);
        if (!canPostNotifications(context) || sessionRecord == null) {
            return;
        }

        if (sessionRecord.getSessionId().equals(lastStudentNotifiedSessionId)) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Class session is live")
                .setContentText(sessionRecord.getSubjectName() + " for " + sessionRecord.getClassName() + " is ready to join.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your class session is active. Join " + sessionRecord.getSubjectName()
                                + " for " + sessionRecord.getClassName()
                                + " and mark attendance while the session is open."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        lastStudentNotifiedSessionId = sessionRecord.getSessionId();
        postNotification(context, 1003, builder);
    }

    private static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(CHANNEL_DESCRIPTION);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private static boolean canPostNotifications(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private static void postNotification(
            Context context,
            int notificationId,
            NotificationCompat.Builder builder
    ) {
        if (!canPostNotifications(context)) {
            return;
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
