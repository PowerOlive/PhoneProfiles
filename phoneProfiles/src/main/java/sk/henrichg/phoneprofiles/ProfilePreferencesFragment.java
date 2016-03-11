package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.fnp.materialpreferences.PreferenceFragment;

public class ProfilePreferencesFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private DataWrapper dataWrapper;
    private Profile profile;
    //private boolean first_start_activity;
    private int new_profile_mode;
    private int predefineProfileIndex;
    public static int startupSource;
    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private Context context;

    private static ImageViewPreference changedImageViewPreference;
    private static ProfileIconPreference changedProfileIconPreference;
    private static Activity preferencesActivity = null;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    static final String PREFS_NAME_DEFAULT_PROFILE = GlobalData.DEFAULT_PROFILE_PREFS_NAME;
    private String PREFS_NAME;

    static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;
    static final String PREF_UNLINK_VOLUMES_APP_PREFERENCES = "prf_pref_volumeUnlinkVolumesAppSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(false);

        preferencesActivity = getActivity();
        context = getActivity().getBaseContext();

        dataWrapper = new DataWrapper(context.getApplicationContext(), true, false, 0);

        long profile_id = 0;

        // getting attached fragment data
        if (getArguments().containsKey(GlobalData.EXTRA_NEW_PROFILE_MODE))
            new_profile_mode = getArguments().getInt(GlobalData.EXTRA_NEW_PROFILE_MODE);
        if (getArguments().containsKey(GlobalData.EXTRA_PROFILE_ID))
            profile_id = getArguments().getLong(GlobalData.EXTRA_PROFILE_ID);
        predefineProfileIndex = getArguments().getInt(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX);
        //Log.e("******** ProfilePreferenceFragment", "profile_id=" + profile_id);

        profile = ProfilePreferencesFragmentActivity.createProfile(context.getApplicationContext(), profile_id, new_profile_mode, predefineProfileIndex, true);

        //prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        //Log.e("********  ProfilePreferenceFragment","startupSource="+startupSource);
        //if (savedInstanceState == null)
        //    loadPreferences();

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
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

        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        //Log.e("******** ProfilePreferenceFragment", "startupSource=" + startupSource);

        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            return R.xml.default_profile_preferences;
        else
            return R.xml.profile_preferences;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);

            /*
            // add zen mode option to preference Ringer mode
            if (ringerModePreference.findIndexOfValue("5") < 0) {
                CharSequence[] entries = ringerModePreference.getEntries();
                CharSequence[] entryValues = ringerModePreference.getEntryValues();

                CharSequence[] newEntries = new CharSequence[entries.length + 1];
                CharSequence[] newEntryValues = new CharSequence[entries.length + 1];

                for (int i = 0; i < entries.length; i++) {
                    newEntries[i] = entries[i];
                    newEntryValues[i] = entryValues[i];
                }

                newEntries[entries.length] = context.getString(R.string.array_pref_ringerModeArray_ZenMode);
                newEntryValues[entries.length] = "5";

                ringerModePreference.setEntries(newEntries);
                ringerModePreference.setEntryValues(newEntryValues);
                ringerModePreference.setValue(Integer.toString(profile._volumeRingerMode));
                setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            }
            */

            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );*/
            final boolean canEnableZenMode =
                    PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext());

            Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            zenModePreference.setEnabled((profile._volumeRingerMode == 5) && canEnableZenMode);

            Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                    return false;
                }
            });

            Preference volumeUnlinkPreference = prefMng.findPreference(PREF_UNLINK_VOLUMES_APP_PREFERENCES);
            //volumeUnlinkPreference.setWidgetLayoutResource(R.layout.start_activity_preference);

            ringerModePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 0;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    /*final boolean canEnableZenMode =
                            (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                                    (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                            );*/
                    final boolean canEnableZenMode =
                            PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext());

                    Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);

                    zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);
                    setTitleStyle(zenModePreference, false, false);

                    return true;
                }
            });
        }
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 23) {
            Preference preference = (Preference) prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
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
            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );*/

            final String sZenModeType = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    private String getTitleWhenPreferenceChanged(String key) {
        Preference preference = prefMng.findPreference(key);
        String title = "";
        if ((preference != null) && (preference.isEnabled())) {
            if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
                boolean defaultValue =
                        getResources().getBoolean(
                                GlobalData.getResourceId(preference.getKey(), "bool", context));
                if (preferences.getBoolean(key, defaultValue) != defaultValue)
                    title = preference.getTitle().toString();
            }
            else {
                String defaultValue =
                        getResources().getString(
                                GlobalData.getResourceId(preference.getKey(), "string", context));
                //Log.e("------ ProfilePreferencesFragment","preferenceChanged  key="+key);
                //Log.e("------ ProfilePreferencesFragment","preferenceChanged  defaultValue="+defaultValue);
                //Log.e("------ ProfilePreferencesFragment","preferenceChanged  value="+preferences.getString(preference.getKey(), defaultValue));
                if (preference instanceof VolumeDialogPreference) {
                    if (VolumeDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else if (preference instanceof BrightnessDialogPreference) {
                    if (BrightnessDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else {
                    if (!preferences.getString(preference.getKey(), defaultValue).equals(defaultValue))
                        title = preference.getTitle().toString();
                }
            }
            return title;
        }
        else
            return title;
    }

    private void setCategorySummary(Preference preference, boolean bold) {
        String key = preference.getKey();
        boolean _bold = bold;
        Preference preferenceScreen = null;
        String summary = "";

        if (key.equals(GlobalData.PREF_PROFILE_DURATION) ||
            key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO) ||
            key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DURATION);
            String afterDurationDoTitle = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_AFTER_DURATION_DO);
            if ((!afterDurationDoTitle.isEmpty()) && (!title.isEmpty())) {
                _bold = true;
                summary = summary + title + " • ";
                summary = summary + afterDurationDoTitle;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_ASK_FOR_DURATION);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_soundProfileCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGTONE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_MEDIA);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ALARM);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SYSTEM);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_VOICE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_volumeCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                //key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                //key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_ALARM);
            preferenceScreen = prefMng.findPreference("prf_pref_soundsCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_NFC)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_GPS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_NFC);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_radiosCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_screenCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                //key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE)) {
            //key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER);
            preferenceScreen = prefMng.findPreference("prf_pref_othersCategory");
        }

        if (preferenceScreen != null) {
            setTitleStyle(preferenceScreen, _bold, false);
            if (_bold)
                preferenceScreen.setSummary(summary);
            else
                preferenceScreen.setSummary("");
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
            setCategorySummary(listPreference, index > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                        );*/
                final boolean canEnableZenMode =
                        PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext());

                if (!canEnableZenMode)
                {
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    listPreference.setEnabled(false);
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
                    setTitleStyle(listPreference, false, false);
                    setCategorySummary(listPreference, false);
                }
                else
                {
                    String sValue = value.toString();
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    int iValue = Integer.parseInt(sValue);
                    if ((iValue != 0) && (iValue != 99)) {
                        String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                        summary = summary + " - " + summaryArray[iValue-1];
                    }
                    listPreference.setSummary(summary);

                    final String sRingerMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                    int iRingerMode;
                    if (sRingerMode.isEmpty())
                        iRingerMode = 0;
                    else
                        iRingerMode = Integer.parseInt(sRingerMode);

                    if (iRingerMode == 5) {
                        setTitleStyle(listPreference, index > 0, false);
                        setCategorySummary(listPreference, index > 0);
                    }
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
            setCategorySummary(listPreference, index > 0);
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
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE))
        {
            if (key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                // set mobile data preference title
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
                    mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
                }
                else {
                    Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
                    mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
                }
            }
            int canChange = GlobalData.isPreferenceAllowed(key, context);
            if (canChange != GlobalData.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                listPreference.setEnabled(false);
                if (canChange == GlobalData.PREFERENCE_NOT_ALLOWED)
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
                else
                if (canChange == GlobalData.PREFERENCE_INSTALL_PPHELPER)
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_install_pphelper));
                else
                if (canChange == GlobalData.PREFERENCE_UPGRADE_PPHELPER)
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_upgrade_pphelper));
                setTitleStyle(listPreference, false, false);
                setCategorySummary(listPreference, false);
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false);
                setCategorySummary(listPreference, index > 0);
            }

        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            boolean secureKeyguard;
            KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Activity.KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 16)
                secureKeyguard = keyguardManager.isKeyguardSecure();
            else
                secureKeyguard = keyguardManager.inKeyguardRestrictedInputMode();
            listPreference.setEnabled(!secureKeyguard);
            if (secureKeyguard) {
                setTitleStyle(listPreference, false, false);
                setCategorySummary(listPreference, false);
                listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
            }
            else {
                String sValue = value.toString();
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
            setCategorySummary(listPreference, index > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
            setCategorySummary(listPreference, index > 0);
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
            setCategorySummary(listPreference, index > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            String sValue = value.toString();
            int iValue = 0;
            if (!sValue.isEmpty())
                iValue = Integer.valueOf(sValue);
            //preference.setSummary(sValue);
            setTitleStyle(preference, iValue > 0, false);
            setCategorySummary(preference, iValue > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
            setCategorySummary(listPreference, index > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            boolean show = sValue.equals("true");
            setTitleStyle(checkBoxPreference, show, false);
            setCategorySummary(checkBoxPreference, show);
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
            boolean change = VolumeDialogPreference.changeEnabled(sValue);
            setTitleStyle(preference, change, false);
            setCategorySummary(preference, change);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            String sValue = value.toString();
            boolean change = BrightnessDialogPreference.changeEnabled(sValue);
            setTitleStyle(preference, change, false);
            setCategorySummary(preference, change);
        }

    }

    private void setSummary(String key) {
        String value;
        if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
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

    private void disableDependedPref(String key) {
        String value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    private void updateSharedPreference()
    {
        if (profile != null) 
        {	

            // updating activity with selected profile preferences

            setSummary(GlobalData.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);

            if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                setSummary(GlobalData.PREF_PROFILE_NAME);
                setSummary(GlobalData.PREF_PROFILE_DURATION);
                setSummary(GlobalData.PREF_PROFILE_AFTER_DURATION_DO);
                setSummary(GlobalData.PREF_PROFILE_ASK_FOR_DURATION);
            }
            setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_GPS);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_NFC);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGTONE);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_MEDIA);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ALARM);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_SYSTEM);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_VOICE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS);
            setSummary(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);

            // disable depended preferences
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);

        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        String value;
        if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
            value = sharedPreferences.getString(key, "");
        setSummary(key, value);
        // disable depended preferences
        disableDependedPref(key, value);

        //Activity activity = getActivity();
        //boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
        //canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof ProfilePreferencesFragmentActivity));
        //if (canShow)
        //    showActionMode();
        ProfilePreferencesFragmentActivity activity = (ProfilePreferencesFragmentActivity)getActivity();
        ProfilePreferencesFragmentActivity.showSaveMenu = true;
        activity.invalidateOptionsMenu();
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