package yell.client.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Abdulkerim on 2.06.2016.
 */
public class LastMessages {

    public static String getLastMessage(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences("private_preferences", Context.MODE_PRIVATE);
        return preferences.getString(name + "_last", null);
    }

    public static void setLastMessage(Context context, String name, String message, boolean isSentByThis) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("private_preferences", context.MODE_PRIVATE).edit();

            JSONObject lastMessage = new JSONObject();

            lastMessage.put("is_sent_by_this", isSentByThis);
            lastMessage.put("message", message);
            lastMessage.put("message_type", "txt");
            lastMessage.put("date", DateFormat.format(new Date()));

            editor.putString(name + "_last", lastMessage.toString());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
