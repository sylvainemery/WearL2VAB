package com.hackncheese.wearl2vab;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class WearNotifyService extends IntentService {
    private static final String ACTION_SENDNOTIF = "com.hackncheese.wearl2vab.action.SENDNOTIF";
    private static final String ACTION_REMOVENOTIF = "com.hackncheese.wearl2vab.action.REMOVENOTIF";
    private static final String EXTRA_EMAIL = "com.hackncheese.wearl2vab.extra.EMAIL";
    private static final String EXTRA_PASSWORD = "com.hackncheese.wearl2vab.extra.PASSWORD";
    private static final String EXTRA_SALT = "com.hackncheese.wearl2vab.extra.SALT";
    private static final String EXTRA_BALANCE = "com.hackncheese.wearl2vab.extra.BALANCE";

    private static final int NOTIFICATION_ID = 1;

    public static void startActionSendNotif(Context context, String email, String password, String salt, String balance) {
        Intent intent = new Intent(context, WearNotifyService.class);
        intent.setAction(ACTION_SENDNOTIF);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_SALT, salt);
        intent.putExtra(EXTRA_BALANCE, balance);
        context.startService(intent);
    }

    public static void startActionRemoveNotif(Context context) {
        Intent intent = new Intent(context, WearNotifyService.class);
        intent.setAction(ACTION_REMOVENOTIF);
        context.startService(intent);
    }

    public WearNotifyService() {
        super("WearNotifyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SENDNOTIF.equals(action)) {
                final String email = intent.getStringExtra(EXTRA_EMAIL);
                final String password = intent.getStringExtra(EXTRA_PASSWORD);
                final String salt = intent.getStringExtra(EXTRA_SALT);
                final String balance = intent.getStringExtra(EXTRA_BALANCE);
                handleActionSendNotif(email, password, salt, balance);
            } else if (ACTION_REMOVENOTIF.equals(action)) {
                handleActionRemoveNotif();
            }
        }
    }

    private void handleActionSendNotif(String email, String password, String salt, String balance) {
        long[] vibrationPattern = {0,500};//,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500};//star wars imperial march

        Intent openFridgeIntent = new Intent(this, L2VABApiService.class);
        openFridgeIntent.setAction(L2VABApiService.ACTION_OPENFRIDGE);
        openFridgeIntent.putExtra(L2VABApiService.EXTRA_EMAIL, email);
        openFridgeIntent.putExtra(L2VABApiService.EXTRA_PASSWORD, password);
        openFridgeIntent.putExtra(L2VABApiService.EXTRA_SALT, salt);
        PendingIntent openFridgePendingIntent = PendingIntent.getService(this, 0, openFridgeIntent, 0);

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_lock_open_white,
                        getString(R.string.open_the_fridge), openFridgePendingIntent)
                        .build();

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.l2v_300))
                .addAction(action);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.balance_value, balance))
                .extend(wearableExtender)
                .setContentIntent(openFridgePendingIntent)/////
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vibrationPattern);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void handleActionRemoveNotif() {
        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
