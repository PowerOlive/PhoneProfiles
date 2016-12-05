package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecreateNotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

        ActivateProfileHelper activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(dataWrapper, context);

        Profile activatedProfile = dataWrapper.getActivatedProfile();
        activateProfileHelper.showNotification(activatedProfile);

    }
}
