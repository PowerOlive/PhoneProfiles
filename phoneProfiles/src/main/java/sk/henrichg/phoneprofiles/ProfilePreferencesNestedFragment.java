package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.fnp.materialpreferences.PreferenceFragment;

public class ProfilePreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    protected PreferenceManager prefMng;
    protected SharedPreferences preferences;
    private Context context;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    static final String PREFS_NAME_DEFAULT_PROFILE = GlobalData.DEFAULT_PROFILE_PREFS_NAME;

    static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;
    static final String PREF_UNLINK_VOLUMES_APP_PREFERENCES = "prf_pref_volumeUnlinkVolumesAppSettings";
    static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 1981;

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(false);

        context = getActivity().getBaseContext();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String PREFS_NAME;
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        else
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        else
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            PREFS_NAME = PREFS_NAME_DEFAULT_PROFILE;
        else
            PREFS_NAME = PREFS_NAME_FRAGMENT;

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

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
            final boolean canEnableZenMode = GlobalData.canChangeZenMode(context.getApplicationContext(), true);

            Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (zenModePreference != null) {
                String value = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
            }

            Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (notificationAccessPreference != null) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60)) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                    preferenceCategory.removePreference(notificationAccessPreference);
                } else {
                    ListPreference listPreference = (ListPreference) prefMng.findPreference("prf_pref_volumeRingerMode");
                    if (listPreference != null) {
                        CharSequence[] entries = listPreference.getEntries();
                        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
                            entries[5] = "(S) "+getString(R.string.array_pref_ringerModeArray_ZenMode);
                        else
                            entries[6] = "(S) "+getString(R.string.array_pref_ringerModeArray_ZenMode);
                        ringerModePreference.setEntries(entries);
                    }
                    /*Preference preference = prefMng.findPreference("prf_pref_volumeZenMode");
                    if (preference != null) {
                        preference.setTitle("(S) "+getString(R.string.profile_preferences_volumeZenMode));
                    }*/
                    //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                            return false;
                        }
                    });
                }
            }

            //Preference volumeUnlinkPreference = prefMng.findPreference(PREF_UNLINK_VOLUMES_APP_PREFERENCES);
            //volumeUnlinkPreference.setWidgetLayoutResource(R.layout.start_activity_preference);

            if (ringerModePreference != null) {
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
                        final boolean canEnableZenMode = GlobalData.canChangeZenMode(context.getApplicationContext(), true);

                        Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);

                        zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);

                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60));
                        setTitleStyle(zenModePreference, false, false, addS);

                        return true;
                    }
                });
            }
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
            preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 23) {
            Preference preference = prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        else {
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null) {
                preference.setTitle("(R) " + getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_othersCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
        {
            ListPreference networkTypePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            if (networkTypePreference != null) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                final int phoneType = telephonyManager.getPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));
                    }
                    String value = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }

                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));
                    }
                    String value = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }
            }
        }
        Preference preference = prefMng.findPreference("prf_pref_sourceProfileInfo");
        if (preference != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // start preferences activity for default profile
                    Intent intent = new Intent(getActivity().getBaseContext(), ProfilePreferencesFragmentActivity.class);
                    intent.putExtra(GlobalData.EXTRA_PROFILE_ID, GlobalData.DEFAULT_PROFILE_ID);
                    intent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
                    intent.putExtra(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                    getActivity().startActivityForResult(intent, GlobalData.REQUEST_CODE_PROFILE_PREFERENCES);
                    return false;
                }
            });
        }
        if (!GlobalData.getMergedRingNotificationVolumes(context)) {
            preference = prefMng.findPreference(PREF_UNLINK_VOLUMES_APP_PREFERENCES);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        else {
            preference = prefMng.findPreference(PREF_UNLINK_VOLUMES_APP_PREFERENCES);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // start preferences activity for default profile
                        Intent intent = new Intent(getActivity().getBaseContext(), PhoneProfilesPreferencesActivity.class);
                        intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "categorySystem");
                        intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                        getActivity().startActivityForResult(intent, RESULT_UNLINK_VOLUMES_APP_PREFERENCES);
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this); 
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == GlobalData.REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == Activity.RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
                //int newProfileMode = data.getIntExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedProfileIndex = data.getIntExtra(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
                {
                    Profile defaultProfile = GlobalData.getDefaultProfile(context.getApplicationContext());
                    Permissions.grantProfilePermissions(context.getApplicationContext(), defaultProfile, true,
                            true, false, 0, GlobalData.STARTUP_SOURCE_EDITOR, true, null, false);
                }
            }
        }
        if (requestCode == ImageViewPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String picturePath = ImageViewPreference.getPath(context, selectedImage);

            if (ProfilePreferencesFragment.changedImageViewPreference != null)
                // nastavime image identifikatoru na ziskanu cestu ku obrazku
                ProfilePreferencesFragment.changedImageViewPreference.setImageIdentifierAndType(picturePath, false);
        }
        if (requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String picturePath = ImageViewPreference.getPath(context, selectedImage);

            if (ProfilePreferencesFragment.changedProfileIconPreference != null)
                // nastavime image identifikatoru na ziskanu cestu ku obrazku
                ProfilePreferencesFragment.changedProfileIconPreference.setImageIdentifierAndType(picturePath, false, true);
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );*/

            final String sZenModeType = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if (requestCode == ApplicationsDialogPreference.RESULT_APPLICATIONS_EDITOR && resultCode == Activity.RESULT_OK && data != null)
        {
            if (ProfilePreferencesFragment.applicationsDialogPreference != null) {
                ProfilePreferencesFragment.applicationsDialogPreference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        /*(Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON),*/
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1));
            }
        }
        if (requestCode == RESULT_UNLINK_VOLUMES_APP_PREFERENCES) {
            disableDependedPref(GlobalData.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    private void setTitleStyle(Preference preference, boolean bold, boolean underline, boolean systemSettings)
    {
        CharSequence title = preference.getTitle();
        Spannable sbt = new SpannableString(title);
        Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        if (systemSettings) {
            String s = title.toString();
            if (!s.contains("(S)"))
                title = "(S) " + title;
        }
        sbt = new SpannableString(title);
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

    private String getTitleWhenPreferenceChanged(String key, boolean systemSettings) {
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
            if (systemSettings) {
                if (!title.isEmpty() && !title.contains("(S)"))
                    title = "(S) " + title;
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
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DURATION, false);
            String afterDurationDoTitle = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, false);
            if ((!afterDurationDoTitle.isEmpty()) && (!title.isEmpty())) {
                _bold = true;
                summary = summary + title + " • ";
                summary = summary + afterDurationDoTitle;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_ASK_FOR_DURATION, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60));
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, addS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, false);
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
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            if (!GlobalData.getMergedRingNotificationVolumes(context) || GlobalData.applicationUnlinkRingerNotificationVolumes) {
                title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, false);
                if (!title.isEmpty()) {
                    _bold = true;
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_MEDIA, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ALARM, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_VOICE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, false);
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
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, false);
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
                key.equals(GlobalData.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_GPS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_NFC, false);
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
                key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE) ||
                key.equals(GlobalData.PREF_PROFILE_NOTIFICATION_LED)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_NOTIFICATION_LED, false);
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
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER);
            preferenceScreen = prefMng.findPreference("prf_pref_othersCategory");
        }

        if (preferenceScreen != null) {
            setTitleStyle(preferenceScreen, _bold, false, false);
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
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.menu_settings) + ": " +
                        context.getResources().getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes));
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                setTitleStyle(preference, false, true, false);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                        );*/
                final boolean canEnableZenMode = GlobalData.canChangeZenMode(context.getApplicationContext(), true);

                if (!canEnableZenMode)
                {
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        listPreference.setEnabled(false);
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60));
                        setTitleStyle(listPreference, false, false, addS);
                        setCategorySummary(listPreference, false);
                    }
                }
                else
                {
                    String sValue = value.toString();
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        int iValue = Integer.parseInt(sValue);
                        int index = listPreference.findIndexOfValue(sValue);
                        CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        if ((iValue != 0) && (iValue != 99)) {
                            if (!((iValue == 6) && (android.os.Build.VERSION.SDK_INT < 23))) {
                                String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                                summary = summary + " - " + summaryArray[iValue - 1];
                            }
                        }
                        listPreference.setSummary(summary);

                        final String sRingerMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                        int iRingerMode;
                        if (sRingerMode.isEmpty())
                            iRingerMode = 0;
                        else
                            iRingerMode = Integer.parseInt(sRingerMode);

                        if (iRingerMode == 5) {
                            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60));
                            setTitleStyle(listPreference, index > 0, false, addS);
                            setCategorySummary(listPreference, index > 0);
                        }
                        listPreference.setEnabled(iRingerMode == 5);
                    }
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM))
        {
            String ringtoneUri = value.toString();

            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (ringtoneUri.isEmpty())
                    preference.setSummary(R.string.preferences_notificationSound_None);
                else {
                    Uri uri = Uri.parse(ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    String ringtoneName;
                    if (ringtone == null)
                        ringtoneName = "";
                    else
                        ringtoneName = ringtone.getTitle(context);
                    preference.setSummary(ringtoneName);
                }
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
            key.equals(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE))
        {
            if (key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                // set mobile data preference title
                Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
                if (mobileDataPreference != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
                    } else {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
                    }
                }
            }
            int canChange = GlobalData.isProfilePreferenceAllowed(key, context);
            if (canChange != GlobalData.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    if (canChange == GlobalData.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+GlobalData.getNotAllowedPreferenceReasonString(getActivity()));
                    setTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    setTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }

        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int canChange = GlobalData.isProfilePreferenceAllowed(key, context);
                if (canChange != GlobalData.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == GlobalData.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+GlobalData.getNotAllowedPreferenceReasonString(getActivity()));
                    setTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    setTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
            key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
            key.equals(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_NOTIFICATION_LED)) {
            ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
            if (listPreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed_23);
                } else {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed);
                }
                int canChange = GlobalData.isProfilePreferenceAllowed(key, context);
                if (canChange != GlobalData.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == GlobalData.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+GlobalData.getNotAllowedPreferenceReasonString(getActivity()));
                    setTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    setTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                int iValue = 0;
                if (!sValue.isEmpty())
                    iValue = Integer.valueOf(sValue);
                //preference.setSummary(sValue);
                setTitleStyle(preference, iValue > 0, false, false);
                setCategorySummary(preference, iValue > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                setTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                setTitleStyle(checkBoxPreference, show, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                setTitleStyle(preference, change, false, false);
                setCategorySummary(preference, change);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);
                setTitleStyle(preference, change, false, false);
                setCategorySummary(preference, change);
            }
        }

    }

    public void setSummary(String key) {
        String value;
        if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
    }

    private boolean getEnableVolumeNotificationByRingtone(String ringtoneValue) {
        boolean enabled = Profile.getVolumeRingtoneChange(ringtoneValue);
        if (enabled) {
            int volume = Profile.getVolumeRingtoneValue(ringtoneValue);
            return volume > 0;
        }
        else
            return true;
    }

    private void disableDependedPref(String key, Object value)
    {
        String sValue = value.toString();

        final String NO_CHANGE = "0";
        final String DEFAULT_PROFILE = "99";
        final String ON = "1";

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE)) {
            boolean enabled = getEnableVolumeNotificationByRingtone(sValue);
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION)) {
            String ringtoneValue = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, "");
            boolean enabled = (!GlobalData.getMergedRingNotificationVolumes(context) || GlobalData.applicationUnlinkRingerNotificationVolumes) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue);
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
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
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME).setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            boolean enabled = !sValue.equals(ON);
            ListPreference preference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(NO_CHANGE);
                preference.setEnabled(enabled);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                String ringerMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                String zenMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
                boolean enabled = false;
                if (ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
                ListPreference preference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(NO_CHANGE);
                    preference.setEnabled(enabled);
                }
            }
        }

    }

    public void disableDependedPref(String key) {
        String value = preferences.getString(key, "");
        disableDependedPref(key, value);
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

}