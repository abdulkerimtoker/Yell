package yell.client.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GcmIdListenerService extends InstanceIDListenerService {

    public GcmIdListenerService() {
        super();
    }

    @Override
    public void onTokenRefresh() {
        startService(new Intent(this, GcmRegistrationIntentService.class));
    }
}
