package sk.henrichg.phoneprofiles;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

public class ImportantInfoNotification {

    public static final int VERSION_CODE_FOR_NEWS = 9999; // news off
    public static final int API_LEVEL_FOR_NEWS = 14; //21;

    static public void showInfoNotification(Context context) {

        PackageInfo pinfo = null;
        int versionCode = 0;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pinfo.versionCode;
            if (versionCode > GlobalData.getShowInfoNotificationOnStartVersion(context)) {
                boolean show = (versionCode >= VERSION_CODE_FOR_NEWS) &&
                        (android.os.Build.VERSION.SDK_INT >= API_LEVEL_FOR_NEWS);
                GlobalData.setShowInfoNotificationOnStart(context, show, versionCode);
            }
            else
                GlobalData.setShowInfoNotificationOnStartVersion(context, versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }

        if (GlobalData.getShowInfoNotificationOnStart(context, versionCode)) {

            showNotificationForUnlinkRingerNotificationVolumes(context,
                    context.getString(R.string.info_notification_title),
                    context.getString(R.string.info_notification_text));

            GlobalData.setShowInfoNotificationOnStart(context, false, versionCode);
        }
    }

    static private void showNotificationForUnlinkRingerNotificationVolumes(Context context, String title, String text) {
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_pphelper_upgrade_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(context.getString(R.string.app_name) + ": " + text) // message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        if (android.os.Build.VERSION.SDK_INT >= 16)
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GlobalData.IMPORTANT_INFO_NOTIFICATION_ID, mBuilder.build());
    }

    public static void removeNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalData.IMPORTANT_INFO_NOTIFICATION_ID);
    }

}
