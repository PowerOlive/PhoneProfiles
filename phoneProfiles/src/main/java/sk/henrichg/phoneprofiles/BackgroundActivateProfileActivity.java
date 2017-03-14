package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BackgroundActivateProfileActivity extends Activity {

    private DataWrapper dataWrapper;

    private int startupSource = 0;
    private long profile_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
        profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {

            //PPApplication.loadPreferences(getApplicationContext());

            dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true)) {
            PPApplication.logE("BackgroundActivateProfileActivity.onStart","application not started");
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));
        }

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT))
            dataWrapper.activateProfile(profile_id, startupSource, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
