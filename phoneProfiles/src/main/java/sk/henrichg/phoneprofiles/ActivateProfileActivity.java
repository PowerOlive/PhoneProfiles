package sk.henrichg.phoneprofiles;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class ActivateProfileActivity extends AppCompatActivity {

    private static ActivateProfileActivity instance;

    private float popupWidth;
    private float popupHeight;

    private Toolbar toolbar;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "activate_profiles_activity_start_target_helps";

    @SuppressWarnings({ "deprecation" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        GlobalGUIRoutines.setTheme(this, true, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

    // set window dimensions ----------------------------------------------------------

        Display display = getWindowManager().getDefaultDisplay();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // display dimensions
        popupWidth = display.getWidth();
        float popupMaxHeight = display.getHeight();
        popupHeight = 0;
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
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
        int profileCount = dataWrapper.getDatabaseHandler().getProfilesCount();
        dataWrapper.invalidateDataWrapper();

        if (!ApplicationPreferences.applicationActivatorGridLayout(getApplicationContext()))
        {
            // add list items height
            popupHeight = popupHeight + (60f * scale * profileCount); // item
            popupHeight = popupHeight + (1f * scale * profileCount); // divider

            popupHeight = popupHeight + (20f * scale); // listview padding
        }
        else
        {
            // add grid items height
            int modulo = profileCount % 3;
            profileCount = profileCount / 3;
            if (modulo > 0)
                ++profileCount;
            popupHeight = popupHeight + (85f * scale * profileCount); // item
            popupHeight = popupHeight + (1f * scale * (profileCount-1)); // divider

            popupHeight = popupHeight + (24f * scale); // gridview margin
        }

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

        toolbar = findViewById(R.id.act_prof_tollbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activator);
        }

    //-----------------------------------------------------------------------------------------		

    }

    public static ActivateProfileActivity getInstance()
    {
        return instance;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (instance == this)
            instance = null;
        ActivatorTargetHelpsActivity.activatorActivity = null;
    }

    @Override
    protected void onResume()
    {
        //Debug.stopMethodTracing();
        super.onResume();

        if (instance == null)
        {
            instance = this;
            refreshGUI(false);
        }
    }

    @Override
    protected void onDestroy()
    {
    //	Debug.stopMethodTracing();

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

    public void refreshGUI(boolean refreshIcons)
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

            ActivatorTargetHelpsActivity.activatorActivity = this;
            Intent intent = new Intent(this, ActivatorTargetHelpsActivity.class);
            startActivity(intent);

        }
    }

    public void showTargetHelps() {
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

                TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                final Display display = getWindowManager().getDefaultDisplay();
                @SuppressWarnings("deprecation") int popupLeft = (int) (display.getWidth() - popupWidth) / 2;
                @SuppressWarnings("deprecation") int popupTop = (int) (display.getHeight() - popupHeight) / 2;

                getTheme().resolveAttribute(R.attr.actionEditProfilesIcon, tv, true);
                final Drawable actionEditProfilesIcon = ContextCompat.getDrawable(this, tv.resourceId);
                final Rect actionEditProfilesTarget = new Rect(0, 0, actionEditProfilesIcon.getIntrinsicWidth(), actionEditProfilesIcon.getIntrinsicHeight());
                actionEditProfilesTarget.offset((popupLeft+(int)popupWidth) - (/*iconWidth + */GlobalGUIRoutines.dpToPx(50))/* - GlobalGUIRoutines.dpToPx(30)*/, popupTop+GlobalGUIRoutines.dpToPx(35));
                actionEditProfilesIcon.setBounds(0, 0, GlobalGUIRoutines.dpToPx(35), GlobalGUIRoutines.dpToPx(35));

                int circleColor = 0xFFFFFF;
                if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("dark"))
                    circleColor = 0x7F7F7F;

                final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);
                sequence.targets(
                        TapTarget.forView(toolbar.findViewById(R.id.menu_edit_profiles), getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_pp))
                                .icon(actionEditProfilesIcon, true)
                                .targetCircleColorInt(circleColor)
                                .textColorInt(0xFFFFFF)
                                .drawShadow(true)
                                .id(1)
                );
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
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null)
                        {
                            ((ActivateProfileListFragment)fragment).showTargetHelps();
                        }
                    }
                }, 500);
            }
        }
        else {
            if (ActivatorTargetHelpsActivity.activity != null) {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                ActivatorTargetHelpsActivity.activity.finish();
                ActivatorTargetHelpsActivity.activity = null;
            }
        }
    }

}
