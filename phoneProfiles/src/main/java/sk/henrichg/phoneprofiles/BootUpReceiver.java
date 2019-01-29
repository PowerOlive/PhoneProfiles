package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("BootUpReceiver.onReceive", "xxx");

        if (intent == null)
            return;

        String action = intent.getAction();
        boolean okAction = false;
        if (action != null) {
            if (Build.VERSION.SDK_INT >= 24)
                okAction = action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED);
            if (!okAction)
                okAction = action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                        action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                        action.equals("com.htc.intent.action.QUICKBOOT_POWERON");
        }
        if (okAction) {

            // start delayed boot up broadcast
            PPApplication.startedOnBoot = true;
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 30000);

            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot(context));

            //PPApplication.setApplicationStarted(context, false);

            final Context appContext = context.getApplicationContext();

            PPApplication.startHandlerThread();
            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BootUpReceiver.onReceive.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (ApplicationPreferences.applicationStartOnBoot(appContext)) {

                            PPApplication.sleep(3000);
                            if (!PPApplication.getApplicationStarted(appContext, true)) {
                                // start service
                                PPApplication.setApplicationStarted(appContext, true);
                                Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
                                PPApplication.startPPService(appContext, serviceIntent);
                            }
                        } else {
                            PPApplication.exitApp(appContext, /*dataWrapper,*/ null, false/*, true*/);
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
    }

}
