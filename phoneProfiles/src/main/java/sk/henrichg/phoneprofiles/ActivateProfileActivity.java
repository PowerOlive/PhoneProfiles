package sk.henrichg.phoneprofiles;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

public class ActivateProfileActivity extends AppCompatActivity {

    //private static volatile ActivateProfileActivity instance;

    private Toolbar toolbar;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "activate_profiles_activity_start_target_helps";

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent ) {
            boolean refreshIcons = intent.getBooleanExtra(RefreshGUIBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            refreshGUI(refreshIcons);
        }
    };

    static final String EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY = "show_target_helps_for_activity";
    private final BroadcastReceiver showTargetHelpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            boolean forActivity = intent.getBooleanExtra(EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, false);
            if (forActivity)
                ActivateProfileActivity.this.showTargetHelps();
            else {
                Fragment fragment = ActivateProfileActivity.this.getFragmentManager().findFragmentById(R.id.activate_profile_list);
                if (fragment != null) {
                    ((ActivateProfileListFragment) fragment).showTargetHelps();
                }
            }
        }
    };

    private final BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            ActivateProfileActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*synchronized (ActivateProfileActivity.class) {
            instance = this;
        }*/

        GlobalGUIRoutines.setTheme(this, true, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

    // set window dimensions ----------------------------------------------------------

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // display dimensions
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float popupWidth = displaymetrics.widthPixels;
        float popupMaxHeight = displaymetrics.heightPixels;
        //Display display = getWindowManager().getDefaultDisplay();
        //float popupWidth = display.getWidth();
        //float popupMaxHeight = display.getHeight();
        float popupHeight = 0;
        float actionBarHeight = 0;

        // action bar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        // set max. dimensions for display orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //popupWidth = Math.round(popupWidth / 100f * 50f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 50f;
            popupMaxHeight = popupMaxHeight / 100f * 90f;
        }
        else
        {
            //popupWidth = Math.round(popupWidth / 100f * 70f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 80f;
            popupMaxHeight = popupMaxHeight / 100f * 90f;
        }

        // add action bar height
        popupHeight = popupHeight + actionBarHeight;

        final float scale = getResources().getDisplayMetrics().density;

        // add header height
        if (ApplicationPreferences.applicationActivatorHeader(getApplicationContext())) {
            if (!ApplicationPreferences.applicationActivatorGridLayout(getApplicationContext()))
                popupHeight = popupHeight + 62f * scale;
            else
                popupHeight = popupHeight + 74f * scale;
        }

        // add list items height
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0);
        int profileCount = DatabaseHandler.getInstance(getApplicationContext()).getProfilesCount();
        dataWrapper.invalidateDataWrapper();

        if (profileCount > 0) {
            if (!ApplicationPreferences.applicationActivatorGridLayout(getApplicationContext())) {
                // add list items height
                popupHeight = popupHeight + (60f * scale * profileCount); // item
                popupHeight = popupHeight + (1f * scale * profileCount); // divider

                popupHeight = popupHeight + (20f * scale); // listview padding
            } else {
                // add grid items height
                int modulo = profileCount % 3;
                profileCount = profileCount / 3;
                if (modulo > 0)
                    ++profileCount;
                popupHeight = popupHeight + (85f * scale * profileCount); // item
                popupHeight = popupHeight + (1f * scale * (profileCount - 1)); // divider

                popupHeight = popupHeight + (24f * scale); // gridview margin
            }
        }
        else
            popupHeight = popupHeight + 60f * scale; // for empty TextView

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));

    //-----------------------------------------------------------------------------------

        //Debug.startMethodTracing("phoneprofiles");

    // Layout ---------------------------------------------------------------------------------

        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        setContentView(R.layout.activity_activate_profile);

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onCreate - setContentView");

        if (Build.VERSION.SDK_INT >= 21) {
            View toolbarShadow = findViewById(R.id.activate_profile_toolbar_shadow);
            if (toolbarShadow != null)
                toolbarShadow.setVisibility(View.GONE);
        }

        toolbar = findViewById(R.id.act_prof_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activator);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter("RefreshActivatorGUIBroadcastReceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                new IntentFilter("ShowActivatorTargetHelpsBroadcastReceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(finishBroadcastReceiver,
                new IntentFilter("FinishActivatorBroadcastReceiver"));

        //-----------------------------------------------------------------------------------------

    }

    /*public static ActivateProfileActivity getInstance()
    {
        return instance;
    }*/

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true))
        {
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
            PPApplication.startPPService(this, serviceIntent);
        }
        else
        {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                PPApplication.startPPService(this, serviceIntent);
            }
        }
    }

    /*
    @Override
    protected void onStop()
    {
        super.onStop();
        synchronized (ActivateProfileActivity.class) {
            instance = null;
        }
        //ActivatorTargetHelpsActivity.activatorActivity = null;
    }*/

    /*
    @Override
    protected void onResume()
    {
        //Debug.stopMethodTracing();
        super.onResume();

        if (ActivateProfileActivity.getInstance() == null)
        {
            synchronized (ActivateProfileActivity.class) {
                instance = this;
            }
            refreshGUI(false);
        }
    }*/

    @Override
    protected void onDestroy()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        toolbar.inflateMenu(R.menu.activity_activate_profile);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_edit_profiles:
            Intent intent = new Intent(getApplicationContext(), EditorProfilesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
            getApplicationContext().startActivity(intent);

            finish();

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        //setContentView(R.layout.activity_phone_profiles);

        GlobalGUIRoutines.reloadActivity(this, false);
    }
    */

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile && (getDataWrapper() != null)) {
                    Profile profile = getDataWrapper().getProfileById(profileId, false, false);
                    getDataWrapper()._activateProfile(profile, startupSource, this);
                }
            }
        }
    }
    */

    private void refreshGUI(boolean refreshIcons)
    {
        final boolean _refreshIcons = refreshIcons;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
                if (fragment != null)
                    ((ActivateProfileListFragment)fragment).refreshGUI(_refreshIcons);
            }
        });
    }

    /*
    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
        if (fragment != null)
            return ((ActivateProfileListFragment)fragment).activityDataWrapper;
        else
            return null;
    }
    */

    public void startTargetHelpsActivity() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        //Log.d("ActivateProfilesActivity.startTargetHelpsActivity", "(1)");

        ApplicationPreferences.getSharedPreferences(this);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true)) {

            //Log.d("ActivateProfilesActivity.startTargetHelpsActivity", "(2)");

            //ActivatorTargetHelpsActivity.activatorActivity = this;
            Intent intent = new Intent(this, ActivatorTargetHelpsActivity.class);
            startActivity(intent);

        }
    }

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        ApplicationPreferences.getSharedPreferences(this);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true)) {

            //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                int circleColor = R.color.tabTargetHelpCircleColor;
                if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("dark"))
                    circleColor = R.color.tabTargetHelpCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
                if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("white"))
                    textColor = R.color.tabTargetHelpTextColor_white;
                boolean tintTarget = !ApplicationPreferences.applicationTheme(getApplicationContext()).equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);
                List<TapTarget> targets = new ArrayList<>();
                int id = 1;
                try {
                    View editorActionView = toolbar.findViewById(R.id.menu_edit_profiles);
                    targets.add(
                            TapTarget.forView(editorActionView, getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_pp))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(id)
                    );
                    ++id;
                } catch (Exception ignored) {} // not in action bar?

                sequence.targets(targets);
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        targetHelpsSequenceStarted = false;
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null)
                        {
                            ((ActivateProfileListFragment)fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        targetHelpsSequenceStarted = false;
                        final Handler handler = new Handler(getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (ActivatorTargetHelpsActivity.activity != null) {
                                    //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                                    ActivatorTargetHelpsActivity.activity.finish();
                                    ActivatorTargetHelpsActivity.activity = null;
                                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                                }
                            }
                        }, 500);

                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.apply();
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                //final Context context = getApplicationContext();
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("ShowActivatorTargetHelpsBroadcastReceiver");
                        intent.putExtra(ActivateProfileActivity.EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, false);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        /*if (ActivateProfileActivity.getInstance() != null) {
                            Fragment fragment = ActivateProfileActivity.getInstance().getFragmentManager().findFragmentById(R.id.activate_profile_list);
                            if (fragment != null) {
                                ((ActivateProfileListFragment) fragment).showTargetHelps();
                            }
                        }*/
                    }
                }, 500);
            }
        }
        else {
            if (ActivatorTargetHelpsActivity.activity != null) {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                ActivatorTargetHelpsActivity.activity.finish();
                ActivatorTargetHelpsActivity.activity = null;
                //ActivatorTargetHelpsActivity.activatorActivity = null;
            }
        }
    }

}
