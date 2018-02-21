package yell.client.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Abdulkerim on 3.06.2016.
 */
public class Friends {
    public static boolean addFriend(Context context, String username) {
        SharedPreferences preferences = context.getSharedPreferences("private_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray friends;

        try {
            friends = new JSONArray(preferences.getString("friends", "[]"));

            for (int i = 0; i < friends.length(); i++) {
                String s = friends.getString(i);

                if (s.equals(username)) {
                    return false;
                }
            }

            friends.put(username);
            editor.putString("friends", friends.toString());
            editor.commit();

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
