package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
 
public class ProfilePreferencesFragment extends PreferenceFragment 
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private DataWrapper dataWrapper;
    private Profile profile;
    public long profile_id;
    //private boolean first_start_activity;
    private int new_profile_mode;
    private int startupSource;
    public boolean profileNonEdited = true;
    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private Context context;
    private ActionMode actionMode;
    private Callback actionModeCallback;

    private int actionModeButtonClicked = BUTTON_UNDEFINED;

    private static ImageViewPreference changedImageViewPreference;
    private static ProfileIconPreference changedProfileIconPreference;
    private static Activity preferencesActivity = null;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    static final String PREFS_NAME_DEFAULT_PROFILE = GlobalData.DEFAULT_PROFILE_PREFS_NAME;
    private String PREFS_NAME;

    static final String SP_ACTION_MODE_SHOWED = "action_mode_showed";

    static final int BUTTON_UNDEFINED = 0;
    static final int BUTTON_CANCEL = 1;
    static final int BUTTON_SAVE = 2;

    static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;

    private OnShowActionMode onShowActionModeCallback = sDummyOnShowActionModeCallback;
    private OnHideActionMode onHideActionModeCallback = sDummyOnHideActionModeCallback;
    private OnRestartProfilePreferences onRestartProfilePreferencesCallback = sDummyOnRestartProfilePreferencesCallback;
    private OnRedrawProfileListFragment onRedrawProfileListFragmentCallback = sDummyOnRedrawProfileListFragmentCallback;

    // invokes when action mode shows
    public interface OnShowActionMode {
        public void onShowActionMode();
    }

    private static OnShowActionMode sDummyOnShowActionModeCallback = new OnShowActionMode() {
        public void onShowActionMode() {
        }
    };

    // invokes when action mode hides
    public interface OnHideActionMode {
        public void onHideActionMode();
    }

    private static OnHideActionMode sDummyOnHideActionModeCallback = new OnHideActionMode() {
        public void onHideActionMode() {
        }
    };

    // invokes when restart of profile preferences fragment needed (undo preference changes)
    public interface OnRestartProfilePreferences {
        /**
         * Callback for restart fragment.
         */
        public void onRestartProfilePreferences(Profile profile, int newProfileMode);
    }

    private static OnRestartProfilePreferences sDummyOnRestartProfilePreferencesCallback = new OnRestartProfilePreferences() {
        public void onRestartProfilePreferences(Profile profile, int newProfileMode) {
        }
    };

    // invokes when profile list fragment redraw needed (preference changes accepted)
    public interface OnRedrawProfileListFragment {
        /**
         * Callback for redraw profile list fragment.
         */
        public void onRedrawProfileListFragment(Profile profile, int newProfileMode);
    }

    private static OnRedrawProfileListFragment sDummyOnRedrawProfileListFragmentCallback = new OnRedrawProfileListFragment() {
        public void onRedrawProfileListFragment(Profile profile, int newProfileMode) {
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof OnShowActionMode)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onShowActionModeCallback = (OnShowActionMode) activity;

        if (!(activity instanceof OnHideActionMode)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onHideActionModeCallback = (OnHideActionMode) activity;

        if (!(activity instanceof OnRestartProfilePreferences)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onRestartProfilePreferencesCallback = (OnRestartProfilePreferences) activity;

        if (!(activity instanceof OnRedrawProfileListFragment)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onRedrawProfileListFragmentCallback = (OnRedrawProfileListFragment) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onShowActionModeCallback = sDummyOnShowActionModeCallback;
        onHideActionModeCallback = sDummyOnHideActionModeCallback;
        onRestartProfilePreferencesCallback = sDummyOnRestartProfilePreferencesCallback;
        onRedrawProfileListFragmentCallback = sDummyOnRedrawProfileListFragmentCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(false);

        preferencesActivity = getActivity();
        context = getActivity().getBaseContext();

        dataWrapper = new DataWrapper(context.getApplicationContext(), true, false, 0);
        
        startupSource = getArguments().getInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        else
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        else
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            PREFS_NAME = PREFS_NAME_DEFAULT_PROFILE;
        else
            PREFS_NAME = PREFS_NAME_FRAGMENT;

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        // getting attached fragment data
        if (getArguments().containsKey(GlobalData.EXTRA_NEW_PROFILE_MODE))
            new_profile_mode = getArguments().getInt(GlobalData.EXTRA_NEW_PROFILE_MODE);
        if (getArguments().containsKey(GlobalData.EXTRA_PROFILE_ID))
            profile_id = getArguments().getLong(GlobalData.EXTRA_PROFILE_ID);

        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            profile = GlobalData.getDefaultProfile(context);
            profile_id = profile._id;
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT)
        {
            // create new profile
            profile = dataWrapper.getNoinitializedProfile(
                        getResources().getString(R.string.profile_name_default),
                        GlobalData.PROFILE_ICON_DEFAULT, 0);
            profile_id = 0;
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)
        {
            // duplicate profile
            Profile origProfile = dataWrapper.getProfileById(profile_id);
            profile = new Profile(
                           origProfile._name+"_d",
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
                           origProfile._deviceAutosync,
                           origProfile._deviceAutoRotate,
                           origProfile._deviceLocationServicePrefs,
                           origProfile._volumeSpeakerPhone,
                           origProfile._deviceNFC,
                           origProfile._duration,
                           origProfile._afterDurationDo,
                           origProfile._volumeZenMode,
                           origProfile._deviceKeyguard,
                           origProfile._vibrationOnTouch,
                           origProfile._deviceWiFiAP);
            profile_id = 0;
        }
        else
            profile = dataWrapper.getProfileById(profile_id);

        preferences = prefMng.getSharedPreferences();

        if (savedInstanceState == null)
            loadPreferences();

        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            addPreferencesFromResource(R.xml.default_profile_preferences);
        else
            addPreferencesFromResource(R.xml.profile_preferences);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            // add zen mode option to preference Ringer mode
            ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            CharSequence[] entries = ringerModePreference.getEntries();
            CharSequence[] entryValues = ringerModePreference.getEntryValues();

            CharSequence[] newEntries = new CharSequence[entries.length+1];
            CharSequence[] newEntryValues = new CharSequence[entries.length+1];

            for (int i = 0; i < entries.length; i++)
            {
                newEntries[i] = entries[i];
                newEntryValues[i] = entryValues[i];
            }

            newEntries[entries.length] = context.getString(R.string.array_pref_ringerModeArray_ZenMode);
            newEntryValues[entries.length] = "5";

            ringerModePreference.setEntries(newEntries);
            ringerModePreference.setEntryValues(newEntryValues);
            ringerModePreference.setValue(Integer.toString(profile._volumeRingerMode));

            final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );

            Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            zenModePreference.setEnabled((profile._volumeRingerMode == 5) && canEnableZenMode);

            Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                    return false;
                }
            });

            ringerModePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String)newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 0;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    final boolean canEnableZenMode =
                            (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                                    (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                            );

                    Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);

                    zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);
                    setTitleStyle(zenModePreference, false, false);

                    return true;
                }
            });

            // set mobile data preference title
            Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);

        }
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (preference != null)
            {
                PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (preference != null)
            {
                PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }

            // set mobile data preference title
            Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
        }

        preferences.registerOnSharedPreferenceChangeListener(this);  
        
        createActionModeCallback();

        if (savedInstanceState == null)
        {
            SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.remove(SP_ACTION_MODE_SHOWED);
            editor.commit();
        }

        updateSharedPreference();

    }

    @Override
    public void onStart()
    {
        super.onStart();


        // must by in onStart(), in ocCreate() crashed
        SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        int actionModeShowed = preferences.getInt(SP_ACTION_MODE_SHOWED, 0);
        if (actionModeShowed == 2)
            showActionMode();
        else
        if (((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
            (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            && (actionModeShowed == 0))
            showActionMode();
    }

    @Override
    public void onPause()
    {
        super.onPause();

    /*	if (actionMode != null)
        {
            restart = false; // nerestartovat fragment
            actionMode.finish();
        } */
    }

    @Override
    public void onDestroy()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this); 
        profile = null;
        
        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
        
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImageViewPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String picturePath = ImageViewPreference.getPath(context, selectedImage);

            if (changedImageViewPreference != null)
                // nastavime image identifikatoru na ziskanu cestu ku obrazku
                changedImageViewPreference.setImageIdentifierAndType(picturePath, false);
        }
        if (requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String picturePath = ImageViewPreference.getPath(context, selectedImage);

            if (changedProfileIconPreference != null)
                // nastavime image identifikatoru na ziskanu cestu ku obrazku
                changedProfileIconPreference.setImageIdentifierAndType(picturePath, false, true);
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );

            final String sZenModeType = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (actionMode != null)
            editor.putInt(SP_ACTION_MODE_SHOWED, 2);
        else
            editor.putInt(SP_ACTION_MODE_SHOWED, 1);
        editor.commit();

    }

    private void loadPreferences()
    {
        if (profile != null)
        {
            //SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

            Editor editor = preferences.edit();
            if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                /*
                editor.remove(GlobalData.PREF_PROFILE_NAME).putString(GlobalData.PREF_PROFILE_NAME, profile._name);
                editor.remove(GlobalData.PREF_PROFILE_ICON).putString(GlobalData.PREF_PROFILE_ICON, profile._icon);
                editor.remove(GlobalData.PREF_PROFILE_DURATION).editor.putString(GlobalData.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.remove(GlobalData.PREF_PROFILE_AFTER_DURATION_DO).editor.putString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
                */
                editor.putString(GlobalData.PREF_PROFILE_NAME, profile._name);
                editor.putString(GlobalData.PREF_PROFILE_ICON, profile._icon);
                editor.putString(GlobalData.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.putString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
            }
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
            editor.putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
            editor.putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
            editor.putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
            editor.putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
            editor.putString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
            editor.putString(GlobalData.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutosync));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._volumeSpeakerPhone));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
            editor.putString(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, Integer.toString(profile._vibrationOnTouch));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, Integer.toString(profile._deviceWiFiAP));
            editor.commit();
        }

    }

    private void savePreferences()
    {
        // save preferences into profile
        if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            profile._name = preferences.getString(GlobalData.PREF_PROFILE_NAME, "");
            profile._icon = preferences.getString(GlobalData.PREF_PROFILE_ICON, "");

            profile._duration = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DURATION, ""));
            profile._afterDurationDo = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, ""));

            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, context);
                GlobalData.setActivatedProfileForDuration(context, profile._id);
            }
        }
        profile._volumeRingerMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, ""));
        profile._volumeZenMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, ""));
        profile._volumeRingtone = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, "");
        profile._volumeNotification = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, "");
        profile._volumeMedia = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, "");
        profile._volumeAlarm = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ALARM, "");
        profile._volumeSystem = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, "");
        profile._volumeVoice = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_VOICE, "");
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, ""));
        profile._soundRingtone = preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, "");
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
        profile._soundNotification = preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, "");
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
        profile._soundAlarm = preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM, "");
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
        profile._deviceWiFi = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI, ""));
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
        profile._deviceBrightness = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
        if (profile._deviceWallpaperChange == 1)
            profile._deviceWallpaper = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, "");
        else
            profile._deviceWallpaper = "-|0";
        profile._deviceMobileData = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, ""));
        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, ""));
        profile._deviceGPS = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_GPS, ""));
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, ""));
        if (profile._deviceRunApplicationChange == 1)
            profile._deviceRunApplicationPackageName = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        else
            profile._deviceRunApplicationPackageName = "-";
        profile._deviceAutosync = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, ""));
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, ""));
        profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, ""));
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, ""));
        profile._deviceNFC = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NFC, ""));
        profile._deviceKeyguard = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, ""));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, ""));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, ""));

        if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            // update bitmaps
            profile.generateIconBitmap(context, false, 0);
            profile.generatePreferencesIndicator(context, false, 0);

            if ((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            {
                dataWrapper.getDatabaseHandler().addProfile(profile);
                profile_id = profile._id;
            }
            else
            if (profile_id > 0)
            {
                // udate profile
                dataWrapper.getDatabaseHandler().updateProfile(profile);
            }
        }

        onRedrawProfileListFragmentCallback.onRedrawProfileListFragment(profile, new_profile_mode);
    }

    private void setTitleStyle(Preference preference, boolean bold, boolean underline)
    {
        CharSequence title = preference.getTitle();
        Spannable sbt = new SpannableString(title);
        Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        if (bold || underline)
        {
            if (bold)
                sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (underline)
                sbt.setSpan(new UnderlineSpan(), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preference.setTitle(sbt);
        }
        else
        {
            preference.setTitle(sbt);
        }
    }

    private void setSummary(String key, Object value)
    {
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(context.getResources().getString(R.string.menu_settings)+": "+
                    context.getResources().getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes));
        }
        if (key.equals(GlobalData.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(value.toString());
            setTitleStyle(preference, false, true);
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                        );

                if (!canEnableZenMode)
                {
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    listPreference.setEnabled(false);
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
                    setTitleStyle(listPreference, false, false);
                }
                else
                {
                    String sValue = value.toString();
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    final String sRingerMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                    int iRingerMode;
                    if (sRingerMode.isEmpty())
                        iRingerMode = 0;
                    else
                        iRingerMode = Integer.parseInt(sRingerMode);

                    if (iRingerMode == 5)
                        setTitleStyle(listPreference, index > 0, false);
                    listPreference.setEnabled(iRingerMode == 5);
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM))
        {
            String ringtoneUri = value.toString();

            if (ringtoneUri.isEmpty())
                prefMng.findPreference(key).setSummary(R.string.preferences_notificationSound_None);
            else
            {
                Uri uri = Uri.parse(ringtoneUri);
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                String ringtoneName;
                if (ringtone == null)
                    ringtoneName = "";
                else
                    ringtoneName = ringtone.getTitle(context);
                prefMng.findPreference(key).setSummary(ringtoneName);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_GPS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_NFC) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            int canChange = GlobalData.hardwareCheck(key, context);
            if (canChange != GlobalData.HARDWARE_CHECK_ALLOWED)
            {
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                listPreference.setEnabled(false);
                if (canChange == GlobalData.HARDWARE_CHECK_NOT_ALLOWED)
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
                else
                if (canChange == GlobalData.HARDWARE_CHECK_INSTALL_PPHELPER)
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_install_pphelper));
                else
                if (canChange == GlobalData.HARDWARE_CHECK_UPGRADE_PPHELPER)
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_upgrade_pphelper));
                setTitleStyle(listPreference, false, false);
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false);
            }

        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
            key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            String sValue = value.toString();
            int iValue = 0;
            if (!sValue.isEmpty())
                iValue = Integer.valueOf(sValue);
            preference.setSummary(sValue);
            setTitleStyle(preference, iValue > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE))
        {
            Preference preference = prefMng.findPreference(key);
            String sValue = value.toString();
            String[] splits = sValue.split("\\|");
            int noChange;
            try {
                noChange = Integer.parseInt(splits[1]);
            } catch (Exception e) {
                noChange = 1;
            }
            setTitleStyle(preference, noChange != 1, false);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            String sValue = value.toString();
            String[] splits = sValue.split("\\|");
            int noChange;
            try {
                noChange = Integer.parseInt(splits[1]);
            } catch (Exception e) {
                noChange = 1;
            }
            setTitleStyle(preference, noChange != 1, false);
        }

    }

    private void disableDependedPref(String key, Object value)
    {
        String sValue = value.toString();

        final String NO_CHANGE = "0";
        final String DEFAULT_PROFILE = "99";
        final String ON = "1";

        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_RINGTONE).setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION).setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_ALARM).setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER).setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME).setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            boolean enabled = !sValue.equals(ON);
            if (!enabled) {
                Editor editor = preferences.edit();
                editor.putString(GlobalData.PREF_PROFILE_DEVICE_WIFI, NO_CHANGE);
                editor.commit();
            }
            prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WIFI).setEnabled(enabled);
        }

    }

    private void updateSharedPreference()
    {
        if (profile != null) 
        {	

            // updating activity with selected profile preferences

            setSummary(GlobalData.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS, "");

            if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                setSummary(GlobalData.PREF_PROFILE_NAME, profile._name);
                setSummary(GlobalData.PREF_PROFILE_DURATION, profile._duration);
                setSummary(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, profile._afterDurationDo);
            }
            setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, profile._volumeZenMode);
            setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
            setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
            setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
            setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
            setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
            setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI, profile._deviceWiFi);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, profile._deviceBluetooth);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, profile._deviceMobileData);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_GPS, profile._deviceGPS);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, profile._deviceAutosync);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_NFC, profile._deviceNFC);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, profile._deviceKeyguard);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
            setSummary(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, profile._vibrationOnTouch);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, profile._deviceWiFiAP);

            // disable depended preferences
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, profile._deviceWiFiAP);

        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        /*if (!(key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE)
                ))*/
                setSummary(key, sharedPreferences.getString(key, ""));

        // disable depended preferences
        disableDependedPref(key, sharedPreferences.getString(key, ""));

        Activity activity = getActivity();
        boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
        canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof ProfilePreferencesFragmentActivity));
        if (canShow)
            showActionMode();
    }

    private void createActionModeCallback()
    {
        actionModeCallback = new ActionMode.Callback() {

            /** Invoked whenever the action mode is shown. This is invoked immediately after onCreateActionMode */
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem menuItem = menu.findItem(R.id.profile_preferences_action_mode_save);
                menuItem.setTitle(menuItem.getTitle() + "    ");
                return true;
            }
 
            /** Called when user exits action mode */
            public void onDestroyActionMode(ActionMode mode) {
               actionMode = null;
               if (actionModeButtonClicked == BUTTON_CANCEL)
                   onRestartProfilePreferencesCallback.onRestartProfilePreferences(profile, new_profile_mode);
            }
 
            /** This is called when the action mode is created. This is called by startActionMode() */
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.profile_preferences_action_mode, menu);
                return true;
            }
 
            /** This is called when an item in the context menu is selected */
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.profile_preferences_action_mode_save:
                        savePreferences();
                        finishActionMode(BUTTON_SAVE);
                        return true;
                    default:
                        finishActionMode(BUTTON_CANCEL);
                        return false;                        
                }
            }

        };		
    }

    @SuppressLint("InflateParams")
    private void showActionMode()
    {
        profileNonEdited = false;

        if (actionMode != null)
            actionMode.finish();

        actionModeButtonClicked = BUTTON_UNDEFINED;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View actionView = inflater.inflate(R.layout.profile_preferences_action_mode, null);
        
        actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        actionMode.setCustomView(actionView);
        
        onShowActionModeCallback.onShowActionMode();        
        
        actionMode.getCustomView().findViewById(R.id.profile_preferences_action_menu_cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                finishActionMode(BUTTON_CANCEL);

            }
        });

    }

    public boolean isActionModeActive()
    {
        return (actionMode != null);
    }

    public void finishActionMode(int button)
    {
        int _button = button;

        if (_button == BUTTON_SAVE)
            new_profile_mode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;

        if (getActivity() instanceof ProfilePreferencesFragmentActivity)
        {
            actionModeButtonClicked = BUTTON_UNDEFINED;
            getActivity().finish(); // finish activity;
        }
        else
        if (actionMode != null)
        {
            actionModeButtonClicked = _button;
            actionMode.finish();
        }

        onHideActionModeCallback.onHideActionMode();        
    }

    static public Activity getPreferencesActivity()
    {
        return preferencesActivity;
    }

    static public void setChangedImageViewPreference(ImageViewPreference changedImageViewPref)
    {
        changedImageViewPreference = changedImageViewPref;
    }

    static public void setChangedProfileIconPreference(ProfileIconPreference changedProfileIconPref)
    {
        changedProfileIconPreference = changedProfileIconPref;
    }

}