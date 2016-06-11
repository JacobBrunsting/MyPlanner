package com.myplanner.myplanner;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

public class NotificationCreator {
    private static String NOTIFICATION = "notification";
    private static String ID = "id";
    private static String TAB = "tab";

    public static void addNotification(final AlarmManager alarmManager, final int id, final String title, final String body, final int drawableID, final long timeInMills, final Context context, final int targetMainTab) {
        // generate the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setSmallIcon(drawableID);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(body);
        final Notification notification = notificationBuilder.build();

        // schedule the notification
        final Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra(ID, id);
        notificationIntent.putExtra(NOTIFICATION, notification);
        notificationIntent.putExtra(TAB, targetMainTab);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMills, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMills, pendingIntent);
        }
    }

    public static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(intent.getIntExtra(ID, 0), (Notification) intent.getParcelableExtra(NOTIFICATION));
            final Intent actionIntent = new Intent(context, Main.class);
            actionIntent.putExtra(Main.TAB_TAG, intent.getIntExtra(TAB, 0));
            context.startActivity(actionIntent);
        }
    }
}
