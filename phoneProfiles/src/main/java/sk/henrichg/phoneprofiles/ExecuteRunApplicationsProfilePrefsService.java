package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ExecuteRunApplicationsProfilePrefsService extends IntentService
{
    public ExecuteRunApplicationsProfilePrefsService() {
        super("ExecuteRunApplicationsProfilePrefsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PPApplication.logE("$$$ ExecuteRunApplicationsProfilePrefsService.onHandleIntent", "-- START ----------");

        Context context = getApplicationContext();

        //PPApplication.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);

        // run execute radios from ActivateProfileHelper
        profile = Profile.getMappedProfile(profile, context);
        if (profile != null)
        {
            ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
            aph.initialize(dataWrapper, context);
            aph.executeForRunApplications(profile);
        }

        dataWrapper.invalidateDataWrapper();

        PPApplication.logE("$$$ ExecuteWallpaperProfilePrefsService.onHandleIntent","-- END ----------");

    }
}
