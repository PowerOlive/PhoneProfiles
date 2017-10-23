package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteRootProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteRootProfilePrefsJob";

    private static final String EXTRA_ACTION = "action";
    static final String ACTION_ADAPTIVE_BRIGHTNESS = "action_adaptive_brightness";
    static final String ACTION_LOCK_DEVICE = "action_lock_device";
    static final String ACTION_POWER_SAVE_MODE = "action_power_save_mode";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ExecuteRootProfilePrefsJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);

        Bundle bundle = params.getTransientExtras();

        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);

        // run execute radios from ActivateProfileHelper
        profile = Profile.getMappedProfile(profile, appContext);
        if (profile != null) {
            ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
            aph.initialize(dataWrapper, appContext);

            String action = bundle.getString(EXTRA_ACTION, "");
            if (action.equals(ACTION_ADAPTIVE_BRIGHTNESS))
                aph.executeRootForAdaptiveBrightness(profile);
            else
            if (action.equals(ACTION_LOCK_DEVICE))
                aph.lockDevice(profile);
            else
            if (action.equals(ACTION_POWER_SAVE_MODE))
                aph.setPowerSaveMode(profile);
        }

        dataWrapper.invalidateDataWrapper();

        return Result.SUCCESS;
    }

    static void start(String action, long profile_id) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, action);
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);

        try {
            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .setTransientExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } catch (Exception ignored) { }
    }

}
