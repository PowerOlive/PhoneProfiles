package sk.henrichg.phoneprofiles;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

public class ActivateProfileFromExternalApplicationActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private long profile_id = 0;

    private static final String EXTRA_PROFILE_NAME = "profile_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        String profileName = intent.getStringExtra(EXTRA_PROFILE_NAME);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0);

        if (profileName != null) {
            profileName = profileName.trim();

            if (!profileName.isEmpty()) {
                dataWrapper.fillProfileList(false, false);
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._name.trim().equals(profileName.trim())) {
                        profile_id = profile._id;
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true)) {
            PPApplication.logE("ActivateProfileFromExternalApplicationActivity.onStart","application not started");
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            PPApplication.startPPService(this, serviceIntent);
        }

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id, false, false);
            if (Permissions.grantProfilePermissions(getApplicationContext(), profile, true,
                    /*false, false, 0,*/ PPApplication.STARTUP_SOURCE_EXTERNAL_APP, /*true,*/ this, true)) {
                dataWrapper._activateProfile(profile, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, /*true,*/ this);
            }
        }
        else {
            showNotification(getString(R.string.action_for_external_application_notification_title),
                    getString(R.string.action_for_external_application_notification_no_profile_text));

            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    private void showNotification(String title, String text) {
        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = getString(R.string.app_name);
            nText = title+": "+text;
        }
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        /*Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);*/
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
