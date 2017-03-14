package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class ActivateProfileFromExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private long profile_id = 0;

    private static final String EXTRA_PROFILE_NAME = "profile_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String profileName = intent.getStringExtra(EXTRA_PROFILE_NAME);
        profileName = profileName.trim();

        if (!profileName.isEmpty()) {
            //PPApplication.loadPreferences(getApplicationContext());

            dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());

            List<Profile> profileList = dataWrapper.getProfileList();
            for (Profile profile : profileList) {
                if (profile._name.trim().equals(profileName.trim())) {
                    profile_id = profile._id;
                    break;
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
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));
        }

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id);
            if (Permissions.grantProfilePermissions(getApplicationContext(), profile, true,
                    true, false, 0, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, true, this, true)) {
                dataWrapper._activateProfile(profile, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, true, this);
            }
        }
        else
            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
