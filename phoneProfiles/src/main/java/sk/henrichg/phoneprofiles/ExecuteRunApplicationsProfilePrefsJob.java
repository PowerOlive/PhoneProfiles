package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteRunApplicationsProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteRunApplicationsProfilePrefsJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ExecuteRunApplicationsProfilePrefsJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);

        Bundle bundle = params.getTransientExtras();

        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);

        // run execute radios from ActivateProfileHelper
        profile = Profile.getMappedProfile(profile, appContext);
        if (profile != null)
        {
            ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
            aph.initialize(dataWrapper, appContext);
            aph.executeForRunApplications(profile);
        }

        dataWrapper.invalidateDataWrapper();

        return Result.SUCCESS;
    }

    static void start(long profile_id) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
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
