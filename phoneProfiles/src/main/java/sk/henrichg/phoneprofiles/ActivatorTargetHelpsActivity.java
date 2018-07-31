package sk.henrichg.phoneprofiles;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

public class ActivatorTargetHelpsActivity extends AppCompatActivity {

    public static ActivatorTargetHelpsActivity activity;
    //public static ActivateProfileActivity activatorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        activity = this;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        /*if (ActivateProfileActivity.getInstance() == null) {
            finish();
            return;
        }*/

        GlobalGUIRoutines.setTheme(this, true, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        Intent intent = new Intent("ShowActivatorTargetHelpsBroadcastReceiver");
        intent.putExtra(ActivateProfileActivity.EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //ActivateProfileActivity.getInstance().showTargetHelps();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
