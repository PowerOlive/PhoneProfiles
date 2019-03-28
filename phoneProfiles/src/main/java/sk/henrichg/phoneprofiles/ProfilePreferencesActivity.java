package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

public class ProfilePreferencesActivity extends PreferenceActivity
                            implements PreferenceFragment.OnCreateNestedPreferenceFragment
{
    private long profile_id = 0;
    private int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
    private int predefinedProfileIndex = 0;

    private int resultCode = RESULT_CANCELED;

    public static boolean showSaveMenu = false;

    //public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "profile_preferences_activity_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_SAVE = "profile_preferences_activity_start_target_helps_save";

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true);
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_profile_preferences);

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_id = getIntent().getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        if (profile_id == Profile.SHARED_PROFILE_ID)
            resultCode = RESULT_OK;
        newProfileMode = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
        predefinedProfileIndex = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);


        /*
        if (profile_id == PPApplication.SHARED_PROFILE_ID)
            getSupportActionBar().setTitle(R.string.title_activity_default_profile_preferences);
        else
            getSupportActionBar().setTitle(R.string.title_activity_profile_preferences);
        */

        ProfilePreferencesNestedFragment fragment = createFragment(false);

        if (savedInstanceState == null) {
            loadPreferences(newProfileMode, predefinedProfileIndex);
        }

        setPreferenceFragment(fragment);
    }

    private ProfilePreferencesNestedFragment createFragment(boolean nested) {
        ProfilePreferencesNestedFragment fragment;
        if (nested)
            fragment = new ProfilePreferencesNestedFragment();
        else
            fragment = new ProfilePreferencesFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        arguments.putInt(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        arguments.putInt(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        if (profile_id == Profile.SHARED_PROFILE_ID)
            arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE);
        else
            arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        arguments.putBoolean(PreferenceFragment.EXTRA_NESTED, nested);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void finish() {
        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
        returnIntent.putExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        returnIntent.putExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        returnIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_RESET_EDITOR, sk.henrichg.phoneprofiles.Permissions.grantRootChanged);
        sk.henrichg.phoneprofiles.Permissions.grantRootChanged = false;
        setResult(resultCode, returnIntent);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (showSaveMenu) {
            // for shared profile is not needed, for shared profile is used PPApplication.SHARED_PROFILE_PREFS_NAME
            // and this is used in Profile.getSharedProfile()
            if (profile_id != Profile.SHARED_PROFILE_ID) {
                toolbar.inflateMenu(R.menu.profile_preferences_save);
            }
        }
        else {
            // no menu for shared profile
            if (profile_id != Profile.SHARED_PROFILE_ID) {
                toolbar.inflateMenu(R.menu.profile_preferences);
            }
        }
        return true;
    }

    private static void onNextLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver trueObserver;

                if (observer.isAlive()) {
                    trueObserver = observer;
                } else {
                    trueObserver = view.getViewTreeObserver();
                }

                trueObserver.removeOnGlobalLayoutListener(this);

                runnable.run();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        if (profile_id != Profile.SHARED_PROFILE_ID) {
            // no menu for shared profile

            onNextLayout(toolbar, new Runnable() {
                @Override
                public void run() {
                    showTargetHelps();
                }
            });
        }

        /*
        final Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        }, 1000);
        */

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.profile_preferences_shared_profile:
                // start preferences activity for default profile
                Intent intent = new Intent(this, ProfilePreferencesActivity.class);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.SHARED_PROFILE_ID);
                intent.putExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
                intent.putExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                startActivityForResult(intent, EditorProfilesActivity.REQUEST_CODE_PROFILE_PREFERENCES);
                return true;
            case R.id.profile_preferences_save:
                savePreferences(newProfileMode, predefinedProfileIndex);
                resultCode = RESULT_OK;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!showSaveMenu)
            super.onBackPressed();
        else {
            boolean nested = false;
            PreferenceFragment fragment = getFragment();
            if (fragment != null) {
                Bundle bundle = fragment.getArguments();
                if (bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false))
                    nested = true;
            }
            if (!nested) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.not_saved_changes_alert_title);
                dialogBuilder.setMessage(R.string.not_saved_changes_alert_message);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        savePreferences(newProfileMode, predefinedProfileIndex);
                        resultCode = RESULT_OK;
                        finish();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ProfilePreferencesActivity.super.onBackPressed();
                    }
                });
                AlertDialog dialog = dialogBuilder.create();
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
                if (!isFinishing())
                    dialog.show();
            }
            else
                ProfilePreferencesActivity.super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PreferenceFragment fragment = getFragment();
        if (fragment != null)
            ((ProfilePreferencesNestedFragment)fragment).doOnActivityResult(requestCode, resultCode, data);
    }

    private Profile createProfile(int startupSource, long profile_id, int new_profile_mode,
                                  int predefinedProfileIndex, boolean leaveSaveMenu) {
        Profile profile;
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
            // no change this in shared profile
            if (!leaveSaveMenu)
                showSaveMenu = false;
        }

        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
        {
            profile = Profile.getSharedProfile(getApplicationContext());
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT)
        {
            // create new profile
            if (predefinedProfileIndex == 0) {
                profile = DataWrapper.getNonInitializedProfile(
                        getBaseContext().getResources().getString(R.string.profile_name_default),
                        Profile.PROFILE_ICON_DEFAULT, 0);
            }
            else {
                profile = dataWrapper.getPredefinedProfile(predefinedProfileIndex-1, false, getBaseContext());
            }
            showSaveMenu = true;
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)
        {
            // duplicate profile
            Profile origProfile = dataWrapper.getProfileById(profile_id, false, false);
            if (origProfile != null) {
                profile = new Profile(
                        origProfile._name + "_d",
                        origProfile._icon,
                        false,
                        origProfile._porder,
                        origProfile._volumeRingerMode,
                        origProfile._volumeRingtone,
                        origProfile._volumeNotification,
                        origProfile._volumeMedia,
                        origProfile._volumeAlarm,
                        origProfile._volumeSystem,
                        origProfile._volumeVoice,
                        origProfile._soundRingtoneChange,
                        origProfile._soundRingtone,
                        origProfile._soundNotificationChange,
                        origProfile._soundNotification,
                        origProfile._soundAlarmChange,
                        origProfile._soundAlarm,
                        origProfile._deviceAirplaneMode,
                        origProfile._deviceWiFi,
                        origProfile._deviceBluetooth,
                        origProfile._deviceScreenTimeout,
                        origProfile._deviceBrightness,
                        origProfile._deviceWallpaperChange,
                        origProfile._deviceWallpaper,
                        origProfile._deviceMobileData,
                        origProfile._deviceMobileDataPrefs,
                        origProfile._deviceGPS,
                        origProfile._deviceRunApplicationChange,
                        origProfile._deviceRunApplicationPackageName,
                        origProfile._deviceAutoSync,
                        origProfile._deviceAutoRotate,
                        origProfile._deviceLocationServicePrefs,
                        origProfile._volumeSpeakerPhone,
                        origProfile._deviceNFC,
                        origProfile._duration,
                        origProfile._afterDurationDo,
                        origProfile._volumeZenMode,
                        origProfile._deviceKeyguard,
                        origProfile._vibrationOnTouch,
                        origProfile._deviceWiFiAP,
                        origProfile._devicePowerSaveMode,
                        origProfile._askForDuration,
                        origProfile._deviceNetworkType,
                        origProfile._notificationLed,
                        origProfile._vibrateWhenRinging,
                        origProfile._deviceWallpaperFor,
                        origProfile._hideStatusBarIcon,
                        origProfile._lockDevice,
                        origProfile._deviceConnectToSSID,
                        origProfile._durationNotificationSound,
                        origProfile._durationNotificationVibrate,
                        origProfile._deviceWiFiAPPrefs,
                        origProfile._headsUpNotifications,
                        origProfile._deviceForceStopApplicationChange,
                        origProfile._deviceForceStopApplicationPackageName,
                        origProfile._activationByUserCount,
                        origProfile._deviceNetworkTypePrefs,
                        origProfile._deviceCloseAllApplications,
                        origProfile._screenNightMode,
                        origProfile._dtmfToneWhenDialing,
                        origProfile._soundOnTouch);
                showSaveMenu = true;
            }
            else
                profile = null;
        }
        else
            profile = dataWrapper.getProfileById(profile_id, false, false);

        return profile;
    }

    private void loadPreferences(int new_profile_mode, int predefinedProfileIndex) {
        int startupSource;
        if (profile_id == Profile.SHARED_PROFILE_ID)
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE;
        else
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;

        Profile profile = createProfile(startupSource, profile_id, new_profile_mode, predefinedProfileIndex, false);
        if (profile == null)
            profile  = createProfile(startupSource, profile_id, EditorProfileListFragment.EDIT_MODE_INSERT, predefinedProfileIndex, false);

        if (profile != null)
        {
            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                Toolbar toolbar = findViewById(R.id.mp_toolbar);
                toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + profile._name);
            }

            String PREFS_NAME = ProfilePreferencesNestedFragment.getPreferenceName(startupSource);

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
            {
                /*
                editor.remove(Profile.PREF_PROFILE_NAME).putString(Profile.PREF_PROFILE_NAME, profile._name);
                editor.remove(Profile.PREF_PROFILE_ICON).putString(Profile.PREF_PROFILE_ICON, profile._icon);
                editor.remove(Profile.PREF_PROFILE_DURATION).editor.putString(Profile.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.remove(Profile.PREF_PROFILE_AFTER_DURATION_DO).editor.putString(Profile.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
                */
                editor.putString(Profile.PREF_PROFILE_NAME, profile._name);
                editor.putString(Profile.PREF_PROFILE_ICON, profile._icon);
                editor.putString(Profile.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.putString(Profile.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
                editor.putBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, profile._askForDuration);
                editor.putString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                editor.putBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, profile._hideStatusBarIcon);
            }
            editor.putString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
            editor.putString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
            editor.putString(Profile.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
            editor.putString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
            editor.putString(Profile.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
            editor.putString(Profile.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
            editor.putString(Profile.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
            editor.putString(Profile.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
            editor.putString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
            editor.putString(Profile.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
            editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
            editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
            editor.putString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
            editor.putString(Profile.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
            editor.putString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
            editor.putString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
            editor.putString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
            editor.putString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
            editor.putString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
            editor.putString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
            editor.putString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
            editor.putString(Profile.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
            editor.putString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
            editor.putString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
            editor.putString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutoSync));
            editor.putString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
            editor.putString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
            editor.putString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._volumeSpeakerPhone));
            editor.putString(Profile.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
            editor.putString(Profile.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
            editor.putString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, Integer.toString(profile._vibrationOnTouch));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, Integer.toString(profile._deviceWiFiAP));
            editor.putString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, Integer.toString(profile._devicePowerSaveMode));
            editor.putString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, Integer.toString(profile._deviceNetworkType));
            editor.putString(Profile.PREF_PROFILE_NOTIFICATION_LED, Integer.toString(profile._notificationLed));
            editor.putString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, Integer.toString(profile._vibrateWhenRinging));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR, Integer.toString(profile._deviceWallpaperFor));
            editor.putString(Profile.PREF_PROFILE_LOCK_DEVICE, Integer.toString(profile._lockDevice));
            editor.putString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
            editor.putString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, Integer.toString(profile._deviceWiFiAPPrefs));
            editor.putString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, Integer.toString(profile._headsUpNotifications));
            editor.putString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, Integer.toString(profile._deviceForceStopApplicationChange));
            editor.putString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
            editor.putString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, Integer.toString(profile._deviceNetworkTypePrefs));
            editor.putString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, Integer.toString(profile._deviceCloseAllApplications));
            editor.putString(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, Integer.toString(profile._screenNightMode));
            editor.putString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, Integer.toString(profile._dtmfToneWhenDialing));
            editor.putString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, Integer.toString(profile._soundOnTouch));
            editor.apply();
        }
    }

    Profile getProfileFromPreferences(long profile_id, int new_profile_mode, int predefinedProfileIndex) {
        int startupSource;
        if (profile_id == Profile.SHARED_PROFILE_ID)
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE;
        else
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;

        Profile profile = createProfile(startupSource, profile_id, new_profile_mode, predefinedProfileIndex, true);

        if (profile != null) {
            String PREFS_NAME = ProfilePreferencesNestedFragment.getPreferenceName(startupSource);

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

            // save preferences into profile
            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                profile._name = preferences.getString(Profile.PREF_PROFILE_NAME, "");
                profile._icon = preferences.getString(Profile.PREF_PROFILE_ICON, "");

                profile._duration = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DURATION, ""));
                profile._afterDurationDo = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, ""));
                profile._askForDuration = preferences.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);
                profile._durationNotificationSound = preferences.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, "");
                profile._durationNotificationVibrate = preferences.getBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false);

                profile._hideStatusBarIcon = preferences.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false);
            }
            profile._volumeRingerMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, ""));
            profile._volumeZenMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, ""));
            profile._volumeRingtone = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            profile._volumeNotification = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
            profile._volumeMedia = preferences.getString(Profile.PREF_PROFILE_VOLUME_MEDIA, "");
            profile._volumeAlarm = preferences.getString(Profile.PREF_PROFILE_VOLUME_ALARM, "");
            profile._volumeSystem = preferences.getString(Profile.PREF_PROFILE_VOLUME_SYSTEM, "");
            profile._volumeVoice = preferences.getString(Profile.PREF_PROFILE_VOLUME_VOICE, "");
            profile._soundRingtoneChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, ""));
            profile._soundRingtone = preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE, "");
            profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
            profile._soundNotification = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
            profile._soundAlarm = preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM, "");
            profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
            profile._deviceWiFi = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI, ""));
            profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
            profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
            profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
            profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
            if (profile._deviceWallpaperChange == 1) {
                profile._deviceWallpaper = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER, "");
                profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR, ""));
            } else {
                profile._deviceWallpaper = "-|0";
                profile._deviceWallpaperFor = 0;
            }
            profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, ""));
            profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, ""));
            profile._deviceGPS = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_GPS, ""));
            profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, ""));
            if (profile._deviceRunApplicationChange == 1)
                profile._deviceRunApplicationPackageName = preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
            else
                profile._deviceRunApplicationPackageName = "-";
            profile._deviceAutoSync = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, ""));
            profile._deviceAutoRotate = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, ""));
            profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, ""));
            profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, ""));
            profile._deviceNFC = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NFC, ""));
            profile._deviceKeyguard = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD, ""));
            profile._vibrationOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, ""));
            profile._deviceWiFiAP = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, ""));
            profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, ""));
            profile._deviceNetworkType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, ""));
            profile._notificationLed = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, ""));
            profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, ""));
            profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, ""));
            profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, "");
            profile._deviceWiFiAPPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, ""));
            profile._headsUpNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, ""));
            profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, ""));
            if (profile._deviceForceStopApplicationChange == 1)
                profile._deviceForceStopApplicationPackageName = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, "-");
            else
                profile._deviceForceStopApplicationPackageName = "-";
            profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, ""));
            profile._deviceCloseAllApplications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, ""));
            profile._screenNightMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, ""));
            profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, ""));
            profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, ""));
        }

        return profile;
    }

    private void savePreferences(int new_profile_mode, int predefinedProfileIndex)
    {
        Profile profile = getProfileFromPreferences(profile_id, new_profile_mode, predefinedProfileIndex);
        if (profile != null) {
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext().getApplicationContext(), false, 0, false);

            int startupSource;
            if (profile_id == Profile.SHARED_PROFILE_ID)
                startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE;
            else
                startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;

            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);
                if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                    // reset alarm for profile duration
                    ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, getApplicationContext());
                    Profile.setActivatedProfileForDuration(getApplicationContext(), profile._id);
                }

                if ((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                        (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)) {
                    DatabaseHandler.getInstance(getApplicationContext()).addProfile(profile);
                    profile_id = profile._id;
                } else if (profile_id > 0) {
                    // update profile
                    DatabaseHandler.getInstance(getApplicationContext()).updateProfile(profile);
                }

                if ((activatedProfile != null) && (activatedProfile._id == profile._id))
                    PPApplication.setBlockProfileEventActions(true);
                    //dataWrapper.activateProfileFromMainThread(profile, false, startupSource, false, null);
                    dataWrapper._activateProfile(profile, startupSource, false, null);

            }
        }
    }

    @Override
    public PreferenceFragment onCreateNestedPreferenceFragment() {
        return createFragment(true);
    }

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        ApplicationPreferences.getSharedPreferences(this);

        String applicationTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);

        if (!showSaveMenu) {
            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                //Log.d("ProfilePreferencesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                Toolbar toolbar = findViewById(R.id.mp_toolbar);

                //TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                //final Display display = getWindowManager().getDefaultDisplay();

                int circleColor = R.color.tabTargetHelpCircleColor;
                if (applicationTheme.equals("dark"))
                    circleColor = R.color.tabTargetHelpCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
                if (applicationTheme.equals("white"))
                    textColor = R.color.tabTargetHelpTextColor_white;
                boolean tintTarget = !applicationTheme.equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(this);
                List<TapTarget> targets = new ArrayList<>();
                int id = 1;
                try {
                    targets.add(
                            TapTarget.forToolbarMenuItem(toolbar, R.id.profile_preferences_shared_profile, getString(R.string.title_activity_default_profile_preferences), getString(R.string.profile_preferences_sourceProfileInfo_summary))
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
                        //targetHelpsSequenceStarted = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;
                sequence.start();
            }
        }
        else {
            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_SAVE, true)) {
                //Log.d("ProfilePreferencesActivity.showTargetHelps", "PREF_START_TARGET_HELPS_SAVE=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS_SAVE, false);
                editor.apply();

                Toolbar toolbar = findViewById(R.id.mp_toolbar);

                //TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                //final Display display = getWindowManager().getDefaultDisplay();

                int circleColor = R.color.tabTargetHelpCircleColor;
                if (applicationTheme.equals("dark"))
                    circleColor = R.color.tabTargetHelpCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
                if (applicationTheme.equals("white"))
                    textColor = R.color.tabTargetHelpTextColor_white;
                boolean tintTarget = !applicationTheme.equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(this);
                if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {

                    editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(PREF_START_TARGET_HELPS, false);
                    editor.apply();

                    List<TapTarget> targets = new ArrayList<>();
                    int id = 1;
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(toolbar, R.id.profile_preferences_shared_profile, getString(R.string.title_activity_default_profile_preferences), getString(R.string.profile_preferences_sourceProfileInfo_summary))
                                        .targetCircleColor(circleColor)
                                        .textColor(textColor)
                                        .tintTarget(tintTarget)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(toolbar, R.id.profile_preferences_save, getString(R.string.profile_preference_activity_targetHelps_save_title), getString(R.string.profile_preference_activity_targetHelps_save_description))
                                        .targetCircleColor(circleColor)
                                        .textColor(textColor)
                                        .tintTarget(tintTarget)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?

                    sequence.targets(targets);
                }
                else {
                    List<TapTarget> targets = new ArrayList<>();
                    int id = 1;
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(toolbar, R.id.profile_preferences_save, getString(R.string.profile_preference_activity_targetHelps_save_title), getString(R.string.profile_preference_activity_targetHelps_save_description))
                                        .targetCircleColor(circleColor)
                                        .textColor(textColor)
                                        .tintTarget(tintTarget)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?

                    sequence.targets(targets);
                }
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;
                sequence.start();
            }
        }
    }

}