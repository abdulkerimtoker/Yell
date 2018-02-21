package yell.client.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import yell.client.R;

public class MainActivity extends AppCompatActivity {

    private MixpanelAPI mixpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mixpanel = MixpanelAPI.getInstance(this, getString(R.string.mixpanel_token));
        mixpanel.track("Home Activity is created");
    }

    @Override
    protected void onStart() {
        super.onStart();

        String sessionKey = getSharedPreferences("private_preferences", MODE_PRIVATE).getString("session_key", null);

        if (sessionKey == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }

        finish();
    }
}
