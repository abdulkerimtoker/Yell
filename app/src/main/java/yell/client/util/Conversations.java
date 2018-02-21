package yell.client.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Abdulkerim on 2.06.2016.
 */
public class Conversations {

    public static void addConversation(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences("private_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray conversations;

        try {
            conversations = new JSONArray(preferences.getString("conversations", "[]"));

            for (int i = 0; i < conversations.length(); i++) {
                String username = conversations.getString(i);

                if (username.equals(name)) {
                    return;
                }
            }

            conversations.put(name);
            editor.putString("conversations", conversations.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void removeConversation(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences("private_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray conversations;

        try {
            conversations = new JSONArray(preferences.getString("conversations", "[]"));

            for (int i = 0; i < conversations.length(); i++) {
                String username = conversations.getString(i);

                if (username.equals(name)) {
                    conversations.remove(i);
                }
            }

            editor.putString("conversations", conversations.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
