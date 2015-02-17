package hu.balygaby.projects.fullychargednotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

public class BatteryStateMonitorService extends Service {

    private static final int ID_FLASH_LED = 123;
    private static final int LED_COLOR = 0xff0000;
    private static final int DURATION_ON = 1000;
    private static final int DURATION_OFF = 6000;
    BroadcastReceiver batteryChangeReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                float batteryPct = level / (float)scale * 100;
                if ((batteryPct>99)&&(isCharging)){
                    sendNotification();
                }
            }
        };
        registerReceiver(batteryChangeReceiver, intentFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(getResources().getString(R.string.fully_charged))
                                .setContentText(getResources().getString(R.string.fully_charged_text))
                                .setLights(LED_COLOR, DURATION_ON, DURATION_OFF)
                                .setSound(uri)
                                .setAutoCancel(true);
                Notification notification = mBuilder.build();
                notification.flags|=Notification.FLAG_SHOW_LIGHTS|Notification.FLAG_AUTO_CANCEL;
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(ID_FLASH_LED, notification);
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (!pm.isScreenOn()) {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Wakelock");
                    wl.acquire(8000);//milliseconds timeout
                }
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryChangeReceiver);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID_FLASH_LED);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
