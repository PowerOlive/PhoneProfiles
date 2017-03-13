package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ExecuteRadioProfilePrefsService extends IntentService //WakefulIntentService 
{

    public ExecuteRadioProfilePrefsService() {
        super("ExecuteRadioProfilePrefsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //@Override
    //protected void doWakefulWork(Intent intent) {
    protected void onHandleIntent(Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        Context context = getApplicationContext();

        PPApplication.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);

        profile = Profile.getMappedProfile(profile, context);
        if (profile != null) {
            if (Permissions.checkProfileRadioPreferences(context, profile)) {
                // run execute radios from ActivateProfileHelper
                ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
                aph.initialize(dataWrapper, context);
                aph.executeForRadios(profile);
            }
        }

        dataWrapper.invalidateDataWrapper();
    }

}
