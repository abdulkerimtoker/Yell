package yell.client.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import yell.client.R;
import yell.client.util.HttpRequester;

/**
 * Created by abdulkerim on 26.03.2016.
 */
public class GcmRegistrationIntentService extends IntentService {

    public GcmRegistrationIntentService() {
        super("GcmRegistrationIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String token = null;

        InstanceID instanceID = InstanceID.getInstance(this);

        try {
            token = instanceID.getToken(getString(R.string.gcm_sender_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write the sender id to the token file so it can be accessed other time
        writeToPreferences("gcm_token", token);

        SharedPreferences preferences = getSharedPreferences("private_preferences", MODE_PRIVATE);
        Editor editor = preferences.edit();

        String response = "error";
        String sessionKey = intent.getStringExtra("session_key");

        Log.i("Session Key Extra", sessionKey);

        try {
            response = HttpRequester.registerGcmToken(sessionKey, token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response.equals("done")) {
            editor.putBoolean("gcm_token_registered", true);
            Log.i("GCM Session", "saved");
        } else {
            editor.putBoolean("gcm_token_registered", false);
            Log.i("GCM Session", "error");
        }

        editor.commit();

        Log.i("GCM Token", token);
    }

    private void writeToPreferences(String key, String value) {
        SharedPreferences privatePreferences = getSharedPreferences("private_preferences", MODE_PRIVATE);
        Editor editor = privatePreferences.edit();

        editor.putString(key, value);
        editor.commit();
    }

}

