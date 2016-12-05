package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ExecuteWallpaperProfilePrefsService extends IntentService
{
    public ExecuteWallpaperProfilePrefsService() {
        super("ExecuteWallpaperProfilePrefsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GlobalData.logE("ExecuteWallpaperProfilePrefsService.onHandleIntent","-- START ----------");

        Context context = getApplicationContext();

        GlobalData.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);

        // run execute radios from ActivateProfileHelper
        profile = GlobalData.getMappedProfile(profile, context);
        if (profile != null)
        {
            ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
            aph.initialize(dataWrapper, context);
            aph.executeForWallpaper(profile);
        }

        dataWrapper.invalidateDataWrapper();

        GlobalData.logE("ExecuteWallpaperProfilePrefsService.onHandleIntent","-- END ----------");

    }
}
