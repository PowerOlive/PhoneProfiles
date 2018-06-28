package sk.henrichg.phoneprofiles;

import android.os.Bundle;
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

        if (ActivateProfileActivity.getInstance() == null) {
            finish();
            return;
        }

        //GlobalGUIRoutines.setTheme(this, true, true);
        ActivateProfileActivity.getInstance().showTargetHelps();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
