package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.fnp.materialpreferences.PreferenceActivity;
import com.fnp.materialpreferences.PreferenceFragment;

public class ProfilePreferencesFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private DataWrapper dataWrapper;
    private Profile profile;
    //private boolean first_start_activity;
    private int new_profile_mode;
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
        Log.e("******** ProfilePreferenceFragment", "profile_id=" + profile_id);

        profile = ProfilePreferencesFragmentActivity.createProfile(context.getApplicationContext(), profile_id, new_profile_mode, true);

        //prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();

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
        Log.e("******** ProfilePreferenceFragment", "startupSource=" + startupSource);

        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            return R.xml.default_profile_preferences;
        else
            return R.xml.profile_preferences;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferences.registerOnSharedPreferenceChangeListener(this);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);

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
                    String sNewValue = (String)newValue;
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
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }

            // set mobile data preference title
            Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
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

    /*
    private boolean isBold(String key) {
        Preference preference = prefMng.findPreference(key);
        if (preference != null) {
            CharSequence title = preference.getTitle();
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            return spansToRemove.length > 0;
        }
        else
            return true;
    }
    */

    private boolean preferenceChanged(String key) {
        Preference preference = prefMng.findPreference(key);
        if (preference != null) {
            String defaultValue =
                    getResources().getString(
                            GlobalData.getResourceId(preference.getKey(), "string", context));
            //Log.e("------ ProfilePreferencesFragment","preferenceChanged  key="+key);
            //Log.e("------ ProfilePreferencesFragment","preferenceChanged  defaultValue="+defaultValue);
            //Log.e("------ ProfilePreferencesFragment","preferenceChanged  value="+preferences.getString(preference.getKey(), defaultValue));
            if (preference instanceof VolumeDialogPreference) {
                return VolumeDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue));
            }
            else
            if (preference instanceof BrightnessDialogPreference) {
                return BrightnessDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue));
            }
            else
            return !preferences.getString(preference.getKey(), defaultValue).equals(defaultValue);
        }
        else
            return false;
    }

    private void setCategoryTitleStyle(Preference preference, boolean bold) {
        String key = preference.getKey();
        boolean _bold = bold;
        Preference preferenceScreen = null;

        if (key.equals(GlobalData.PREF_PROFILE_DURATION) ||
            key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO)) {
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DURATION);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_AFTER_DURATION_DO);
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH)) {
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH);
            preferenceScreen = prefMng.findPreference("prf_pref_soundProfileCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGTONE);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_MEDIA);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ALARM);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SYSTEM);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_VOICE);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
            preferenceScreen = prefMng.findPreference("prf_pref_volumeCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
            //key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
            //key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM)) {
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
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
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_GPS);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_NFC);
            preferenceScreen = prefMng.findPreference("prf_pref_radiosCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE)) {
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE);
            preferenceScreen = prefMng.findPreference("prf_pref_screenCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            //key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE)) {
            //key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER)) {
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            //_bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            _bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            //_bold = _bold || preferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER);
            preferenceScreen = prefMng.findPreference("prf_pref_othersCategory");
        }

        if (preferenceScreen != null)
            setTitleStyle(preferenceScreen, _bold, false);
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
            setCategoryTitleStyle(listPreference, index > 0);
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
                    setCategoryTitleStyle(listPreference, false);
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
                        setCategoryTitleStyle(listPreference, index > 0);
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
            setCategoryTitleStyle(listPreference, index > 0);
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
                setCategoryTitleStyle(listPreference, false);
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false);
                setCategoryTitleStyle(listPreference, index > 0);
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
                setCategoryTitleStyle(listPreference, false);
                listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
            }
            else {
                String sValue = value.toString();
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false);
                setCategoryTitleStyle(listPreference, index > 0);
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
            setCategoryTitleStyle(listPreference, index > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
            setCategoryTitleStyle(listPreference, index > 0);
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
            setCategoryTitleStyle(listPreference, index > 0);
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
            setCategoryTitleStyle(preference, iValue > 0);
        }
        if (key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(sValue);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
            setTitleStyle(listPreference, index > 0, false);
            setCategoryTitleStyle(listPreference, index > 0);
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
            setCategoryTitleStyle(preference, noChange != 1);
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
            setCategoryTitleStyle(preference, noChange != 1);
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
            setSummary(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);

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