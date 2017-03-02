package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.os.Bundle;

public class ActivatorTargetHelpsActivity extends Activity {

    public static ActivatorTargetHelpsActivity activity;
    public static ActivateProfileActivity activatorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        activity = this;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        GlobalGUIRoutines.setTheme(this, true, true);
        activatorActivity.showTargetHelps();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
