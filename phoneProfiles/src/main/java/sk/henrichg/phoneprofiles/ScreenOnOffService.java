package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;

public class ScreenOnOffService extends IntentService {

    public ScreenOnOffService() {
        super("ScreenOnOffService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            Context appContext = getApplicationContext();
            
            boolean lockDeviceEnabled = false;
            if (PPApplication.lockDeviceActivity != null) {
                lockDeviceEnabled = true;
                PPApplication.lockDeviceActivity.finish();
                PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
            }

            //PPApplication.loadPreferences(context);

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen on");
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen off");
                ActivateProfileHelper.setScreenUnlocked(appContext, false);

                if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                        ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                    DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen unlock");
                ActivateProfileHelper.setScreenUnlocked(appContext, true);

                DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);

                if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                        ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                }

                // change screen timeout
                if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
                int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
                if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext)))
                    dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);

                dataWrapper.invalidateDataWrapper();

                // enable/disable keyguard
                Intent keyguardService = new Intent(appContext, KeyguardService.class);
                appContext.startService(keyguardService);
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen on");
                if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                        ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                    DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }
    }

}