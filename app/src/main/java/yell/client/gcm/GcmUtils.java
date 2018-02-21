package yell.client.gcm;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

/**
 * Created by abdulkerim on 27.03.2016.
 */
public class GcmUtils {

    public static void subTopics(@NonNull Context context, @NonNull String token, String... topics) {
        GcmPubSub pubSub = GcmPubSub.getInstance(context);
        for (String topic : topics) {
            try {
                pubSub.subscribe(token, "/topics/" + topic, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
