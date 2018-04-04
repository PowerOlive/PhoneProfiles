package sk.henrichg.phoneprofiles;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BackgroundActivateProfileActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private int startupSource = 0;
    private long profile_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
        profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {

            dataWrapper = new DataWrapper(getApplicationContext(), false, 0);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true)) {
            PPApplication.logE("BackgroundActivateProfileActivity.onStart","application not started");
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            PPApplication.startPPService(this, serviceIntent);
        }

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {
            dataWrapper.activateProfile(profile_id, startupSource, this);
            DatabaseHandler.getInstance(dataWrapper.context).increaseActivationByUserCount(profile_id);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
