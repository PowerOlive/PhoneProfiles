package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{
        Intent serviceIntent = new Intent(context.getApplicationContext(), PackageReplacedService.class);
        WakefulIntentService.sendWakefulWork(context.getApplicationContext(), serviceIntent);
        //}
    }

}
