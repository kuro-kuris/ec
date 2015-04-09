package com.example.edibus;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.os.PowerManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

public class ReceiveTransitionsIntentService extends IntentService {

    private final String TAG = this.getClass().getCanonicalName();

    public ReceiveTransitionsIntentService() {
        super("GeofenceIntentService");
        Log.v(TAG, "Constructor.");
    }

    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.v(TAG, "onHandleIntent");
        if(!geofencingEvent.hasError()) {
            int transition = geofencingEvent.getGeofenceTransition();
            String notificationTitle;

            switch(transition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    notificationTitle = "Geofence Entered";
                    Log.v(TAG, "Geofence Entered");
                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    notificationTitle = "Geofence Dwell";
                    Log.v(TAG, "Dwelling in Geofence");
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    notificationTitle = "Geofence Exit";
                    Log.v(TAG, "Geofence Exited");
                    break;
                default:
                    notificationTitle = "Geofence Unknown";
            }

            sendNotification(this, getTriggeringGeofences(intent), notificationTitle);
        }
    }

    private void sendNotification(Context context, String notificationText,
                                  String notificationTitle) {

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();
        /*
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.drawable.button_idle_state_selector)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(false);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
        */
        System.out.println("Triggered wakeLock: "+notificationText+" "+notificationTitle);
        wakeLock.release();
    }

    private String getTriggeringGeofences(Intent intent) {
        GeofencingEvent geofenceEvent = GeofencingEvent.fromIntent(intent);
        List<Geofence> geofences = geofenceEvent
                .getTriggeringGeofences();

        String[] geofenceIds = new String[geofences.size()];

        for (int i = 0; i < geofences.size(); i++) {
            geofenceIds[i] = geofences.get(i).getRequestId();
        }

        return TextUtils.join(", ", geofenceIds);
    }
}