package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		//int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
		//int myUid = android.os.Process.myUid();
		//if (intentUid == myUid)
		//{
			GlobalData.loadPreferences(context);
			
			if (GlobalData.getApplicationStarted(context))
			{
				// must by false for avoiding starts/pause events before restart events
				GlobalData.setApplicationStarted(context, false); 
				
				// start ReceiverService
				context.startService(new Intent(context.getApplicationContext(), ReceiversService.class));
			}

		//}
	}

}
