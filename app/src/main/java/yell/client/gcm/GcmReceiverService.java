package yell.client.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONObject;

import yell.client.R;
import yell.client.activities.MessagingActivity;
import yell.client.sqlite.ConversationSqlHelper;
import yell.client.type.Message;
import yell.client.util.Conversations;
import yell.client.util.LastMessages;
import yell.client.util.MessageNotification;

public class GcmReceiverService extends GcmListenerService {

    public GcmReceiverService() {
        super();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Context context = getApplicationContext();

        final String sender = data.getString("sender");
        final String message = data.getString("message_text");
        String messageType = data.getString("messagetype");
        String date = data.getString("date");

        SharedPreferences preferences = context.getSharedPreferences("private_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (preferences.getBoolean(sender + "_active", true)) {
            MessageNotification.notify(context, sender, message);
        }

        Intent intent = new Intent("receive_message")
                .putExtra("sender", sender)
                .putExtra("message", message)
                .putExtra("message_type", messageType)
                .putExtra("date", date);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        ConversationSqlHelper sqlHelper = new ConversationSqlHelper(context, sender);
        sqlHelper.insertMessage(new Message(false, message, messageType, date));
        sqlHelper.close();

        LastMessages.setLastMessage(context, sender, message, false);

        Conversations.addConversation(context, sender);

        Log.i("GCM Message from", from);
    }
}
