# Android-App
Proof of Concept: Beacon and Personalized Notification/ Andriod-App with Demandware @ Brandeis University

####SDK
[Estimote SDK](http://developer.estimote.com/android/tutorial/part-2-background-monitoring/) and [com.savagelook.android](https://github.com/tonylukasavage/com.savagelook.android)

####Monitoring System
After import the necessary library from Estimote in `MyApplication.java`, we can set up a BeaconManager Object as a static variable
```
public class MyApplication extends Application {

    protected static BeaconManager beaconManager;

...
```
Then in the `onCreate()` function we can set up our `beaconManager` and define a listener for entering the beacon region and exiting the beacon region:

```
public void onCreate() {
    super.onCreate();
    beaconManager = new BeaconManager(getApplicationContext());

    // set callback/listener
    beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
        @Override
        public void onEnteredRegion(Region region, List<Beacon> list) {
            Log.e("MyApplication", "be enter");
            start = System.currentTimeMillis();
            showNotification(
                    "Welcome!",
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
    
    ...
```
Notice that, in the `onEnteredRegion()` trigger function, I call a `showNotification()` helper function to show the notification and start a timer, which later is used in the `onExitedRegion()` trigger function to get the tottal elapsed time in the beacon region.

Once we have this setup, we can start monitorting but connecting to `ServiceReadyCallback()` in the same `onCreate()` function:
```    
public void onCreate() {
  ...

  // start monitoring
  beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
          beaconManager.startMonitoring(new Region(
                  "monitored region",
                  UUID.fromString(getResources().getString(R.string.beacon_UUID)),
                  null, null));
      }
  });
}
  ```

Moreover, to custmoize your notification, you can create the following helper function with some customized configuration in `MyApplication.java` or a utility class you like:
```
public void showNotification(String title, String message) {
  Intent notifyIntent = new Intent(this, MainActivity.class);
  notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
          new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

  Drawable myDrawable = getResources().getDrawable(R.drawable.dw);
  Bitmap anImage      = ((BitmapDrawable) myDrawable) != null ? ((BitmapDrawable) myDrawable).getBitmap() : null;

  Notification notification = new Notification.Builder(this)
          .setSmallIcon(R.mipmap.ic_launcher) // setup small icon show in the status bar
          .setLargeIcon(anImage)  // setup the notificaiton image shown
          .setContentTitle(title) // notification title
          .setContentText(message)  // notification message
          .setAutoCancel(true)
          .setContentIntent(pendingIntent)
          .build();
  notification.defaults |= Notification.DEFAULT_SOUND;
  NotificationManager notificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
  notificationManager.notify(1, notification);
}
```

#### Ranging System
Ranging system is different than monitor as monitor is only interested in a single beacon, while ranging system is interested in all the beacons but sorted them in an array by signal strength.

Since the ranging system setup is very similar to the monitor system, we are not going to go over it in detail, but you can check out this [get started guide](http://developer.estimote.com/android/tutorial/part-3-ranging-beacons/).

#### Screenshoot

<img src="http://i.imgur.com/SSfprWp.png" align="left" height="600" Hspace="30" Vspace="10">
