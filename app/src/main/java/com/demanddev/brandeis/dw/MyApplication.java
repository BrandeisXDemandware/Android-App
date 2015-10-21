package com.demanddev.brandeis.dw;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {

    private BeaconManager beaconManager;
    private long start;
    private long elapsedTime;

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());

        // set callback/listener
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                start = System.currentTimeMillis();
                showNotification(
                        "Welcome To DW SHOP!",
                        "Current on sale items are: ...");
            }
            @Override
            public void onExitedRegion(Region region) {
                elapsedTime = System.currentTimeMillis()-start;
                showNotification(
                        "Thank you for coming!",
                        "You shop "+elapsedTime/1000+" seconds!");
            }
        });

        // start monitoring
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString(getResources().getString(R.string.beacon_UUID)),
                        getResources().getInteger(R.integer.beacon_2_major), getResources().getInteger(R.integer.beacon_2_minor)));
            }
        });
    }

    // helper function
    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

        Drawable myDrawable = getResources().getDrawable(R.drawable.dw);
        Bitmap anImage      = ((BitmapDrawable) myDrawable).getBitmap();

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(anImage)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}