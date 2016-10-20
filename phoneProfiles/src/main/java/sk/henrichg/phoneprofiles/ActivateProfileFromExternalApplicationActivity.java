package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class ActivateProfileFromExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private String profileName;
    private long profile_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        profileName = intent.getStringExtra(GlobalData.EXTRA_PROFILE_NAME);
        profileName.trim();

        if (!profileName.isEmpty()) {
            GlobalData.loadPreferences(getApplicationContext());

            dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getApplicationContext());

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

        if (!GlobalData.getApplicationStarted(getApplicationContext()))
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id);
            if (Permissions.grantProfilePermissions(getApplicationContext(), profile, true,
                    true, false, 0, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this, true, true)) {
                dataWrapper._activateProfile(profile, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this);
            }
        }
        else
            dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
