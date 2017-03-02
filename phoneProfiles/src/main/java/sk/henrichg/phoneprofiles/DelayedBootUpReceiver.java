package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DelayedBootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("DelayedBootUpReceiver.onReceive", "xxx");

        PPApplication.startedOnBoot = false;
    }
}
