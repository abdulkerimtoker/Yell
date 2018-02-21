package yell.client.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import yell.client.R;
import yell.client.activities.MessagingActivity;

/**
 * Created by Abdulkerim on 2.06.2016.
 */
public class MessageNotification {

    public static void notify(Context context, String sender, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.yell_logo)
                .setContentTitle(sender)
                .setContentText(message)
                .setAutoCancel(true);

        Intent intent = new Intent(context, MessagingActivity.class)
                .putExtra("username", sender);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(sender, 0, mBuilder.build());

        Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }
}
