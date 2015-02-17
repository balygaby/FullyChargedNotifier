package hu.balygaby.projects.fullychargednotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()==Intent.ACTION_POWER_CONNECTED){
            context.startService(new Intent(context, BatteryStateMonitorService.class));
        }
        if (intent.getAction()==Intent.ACTION_POWER_DISCONNECTED){
            context.stopService(new Intent(context, BatteryStateMonitorService.class));
        }
    }
}