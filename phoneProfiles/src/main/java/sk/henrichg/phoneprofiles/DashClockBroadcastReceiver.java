package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = PPApplication.PACKAGE_NAME + ".REFRESH_DASHCLOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
                if (dashClockExtension != null)
                {
                    dashClockExtension.updateExtension();
                }
            }
        });
    }

}
