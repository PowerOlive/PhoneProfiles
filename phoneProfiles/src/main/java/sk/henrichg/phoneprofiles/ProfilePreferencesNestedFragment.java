package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.Toast;

import static android.content.Context.DEVICE_POLICY_SERVICE;

public class ProfilePreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    int startupSource;

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private Context context;

    private static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    //static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    private static final String PREFS_NAME_SHARED_PROFILE = PPApplication.SHARED_PROFILE_PREFS_NAME;

    private static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;

    private static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 1981;

    private static final String PREF_VOLUME_NOTIFICATION_VOLUME0 = "prf_pref_volumeNotificationVolume0";

    static final String PREF_DEVICE_ADMINISTRATOR_SETTINGS = "prf_pref_lockDevice_deviceAdminSettings";
    private static final int RESULT_DEVICE_ADMINISTRATOR_SETTINGS = 1982;

    private static final String PREF_FORCE_STOP_APPLICATIONS_CATEGORY = "prf_pref_forceStopApplicationsCategory";
    static final String PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER = "prf_pref_deviceForceStopApplicationInstallExtender";
    private static final String PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS = "prf_pref_deviceForceStopApplicationAccessibilitySettings";
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1983;
    private static final String PREF_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettings";

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

        Bundle bundle = this.getArguments();
        if (bundle != null)
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);

        context = getActivity().getBaseContext();

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
    }

    public static String getPreferenceName(int startupSource) {
        String PREFS_NAME;
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        /*else
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;*/
        else
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
            PREFS_NAME = PREFS_NAME_SHARED_PROFILE;
        else
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        return PREFS_NAME;
    }

    void setPreferencesManager() {
        String PREFS_NAME = getPreferenceName(startupSource);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
            Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
            Bundle bundle = getArguments();
            if (bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false))
                toolbar.setSubtitle(getString(R.string.title_activity_default_profile_preferences));
            else
                toolbar.setSubtitle(null);
        }

        setPreferencesManager();
        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);

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
                setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            }
            */

            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/
            final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), false);

            ListPreference zenModePreference = (ListPreference)prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (zenModePreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    zenModePreference.setTitle(R.string.profile_preferences_volumeZenModeM);
                    zenModePreference.setDialogTitle(R.string.profile_preferences_volumeZenModeM);
                }
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
            }

            Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (notificationAccessPreference != null) {
                /*boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {*/
                if (canEnableZenMode) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                    preferenceCategory.removePreference(notificationAccessPreference);
                } else {
                    if (ringerModePreference != null) {
                        CharSequence[] entries = ringerModePreference.getEntries();
                        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
                            entries[5] = "(S) "+getString(R.string.array_pref_ringerModeArray_ZenMode);
                        else
                            entries[6] = "(S) "+getString(R.string.array_pref_ringerModeArray_ZenMode);
                        ringerModePreference.setEntries(entries);
                    }

                    boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                    @SuppressLint("InlinedApi")
                    final boolean showDoNotDisturbPermission =
                            (android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext());
                    if (showDoNotDisturbPermission) {
                        notificationAccessPreference.setTitle(getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions));
                        notificationAccessPreference.setSummary(getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary));
                    }

                    //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            if (showDoNotDisturbPermission) {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                            }
                            else
                            if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                                dialog.show();
                            }
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
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                            );*/
                        final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), true);

                        Preference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                        if (zenModePreference != null) {
                            zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);

                            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            @SuppressLint("InlinedApi")
                            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                            setTitleStyle(zenModePreference, false, false, addS);
                        }

                        return true;
                    }
                });
            }
        }
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
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
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if ((android.os.Build.VERSION.SDK_INT < 23) || (android.os.Build.VERSION.SDK_INT > 23)) {
            Preference preference = prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT == 23) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null) {
                preference.setTitle("(R) " + getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_othersCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            /*Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_deviceWiFiAP));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP, value);
            }*/
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON);
            if (preference != null) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("prf_pref_activationParametersCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
        {
            ListPreference networkTypePreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            if (networkTypePreference != null) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int phoneType = TelephonyManager.PHONE_TYPE_GSM;
                if (telephonyManager != null)
                    phoneType = telephonyManager.getPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));
                    }
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }

                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));
                    }
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }
            }
        }
        Preference preference;
        if (!ActivateProfileHelper.getMergedRingNotificationVolumes(context)) {
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        else {
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
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
        Preference deviceAdminSettings = prefMng.findPreference(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
        if (deviceAdminSettings != null) {
            deviceAdminSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DevicePolicyManager manager = (DevicePolicyManager)getActivity().getSystemService(DEVICE_POLICY_SERVICE);
                    final ComponentName component = new ComponentName(getActivity(), PPDeviceAdminReceiver.class);
                    if ((manager == null) || !manager.isAdminActive(component)) {
                        if (GlobalGUIRoutines.activityActionExists(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN, context)) {
                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);
                            getActivity().startActivityForResult(intent, RESULT_DEVICE_ADMINISTRATOR_SETTINGS);
                        } else {
                            Intent intent = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                            if (GlobalGUIRoutines.activityIntentExists(intent, context)) {
                                getActivity().startActivityForResult(intent, RESULT_DEVICE_ADMINISTRATOR_SETTINGS);
                            } else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                                dialog.show();
                            }
                        }
                    } else {
                        Intent intent = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                        if (GlobalGUIRoutines.activityIntentExists(intent, context)) {
                            getActivity().startActivityForResult(intent, RESULT_DEVICE_ADMINISTRATOR_SETTINGS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                            dialog.show();
                        }
                    }
                    return false;
                }
            });
        }

        InfoDialogPreference infoDialogPreference = (InfoDialogPreference)prefMng.findPreference("prf_pref_preferenceTypesInfo");
        if (infoDialogPreference != null) {
            infoDialogPreference.setInfoText(
                    getString(R.string.important_info_profile_grant)+"\n"+
                    getString(R.string.profile_preferences_typesInfoGrant)+"\n\n"+
                    getString(R.string.important_info_profile_root)+"\n\n"+
                            getString(R.string.important_info_profile_settings)+"\n\n"+
                            getString(R.string.important_info_profile_interactive));
        }

        Preference extenderPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                    return false;
                }
            });
        }
        Preference accessibilityPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                        dialog.show();
                    }
                    return false;
                }
            });
        }
        if (Build.VERSION.SDK_INT >= 26) {
            preference = prefMng.findPreference(PREF_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_activated_profile));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                            dialog.show();
                        }
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    private void setTitleStyle(Preference preference, boolean bold, boolean underline, boolean systemSettings)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (systemSettings) {
                String s = title.toString();
                if (!s.contains("(S)"))
                    title = TextUtils.concat("(S) ", title);
            }
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold)
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (underline)
                    sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(sbt);
            } else {
                preference.setTitle(sbt);
            }
        }
    }

    private String getTitleWhenPreferenceChanged(String key, boolean systemSettings) {
        Preference preference = prefMng.findPreference(key);
        String title = "";
        if ((preference != null) && (preference.isEnabled())) {
            if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
                /*boolean defaultValue =
                        getResources().getBoolean(
                                GlobalGUIRoutines.getResourceId(preference.getKey(), "bool", context));*/
                boolean defaultValue = Profile.defaultValuesBoolean.get(preference.getKey());
                if (preferences.getBoolean(key, defaultValue) != defaultValue)
                    title = preference.getTitle().toString();
            }
            else {
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(preference.getKey(), "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(preference.getKey());
                if (preference instanceof VolumeDialogPreference) {
                    if (VolumeDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else if (preference instanceof BrightnessDialogPreference) {
                    if (BrightnessDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else {
                    if (!preferences.getString(preference.getKey(), defaultValue).equals(defaultValue)) {
                        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) &&
                                (android.os.Build.VERSION.SDK_INT >= 23))
                            title = context.getString(R.string.profile_preferences_volumeZenModeM);
                        else
                            title = preference.getTitle().toString();
                    }
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

        if (key.equals(Profile.PREF_PROFILE_DURATION) ||
            key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION, false);
            String afterDurationDoTitle = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title + " • ";
                summary = summary + afterDurationDoTitle;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            if (bold) {
                // any of duration preferences are set
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, false);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            @SuppressLint("InlinedApi")
            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, addS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_soundProfileCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGTONE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            if ((!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue)) {
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, false);
                if (!title.isEmpty()) {
                    _bold = true;
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MEDIA, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ALARM, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SYSTEM, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_VOICE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_volumeCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(Profile.PREF_PROFILE_SOUND_ALARM)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_RINGTONE);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_ALARM);
            preferenceScreen = prefMng.findPreference("prf_pref_soundsCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
            key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
            key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ON_TOUCH, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_touchEffectsCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_GPS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NFC, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_radiosCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE) ||
                key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED) ||
                key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_KEYGUARD, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_NOTIFICATION_LED, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_screenCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
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

    private void setSummaryForNotificationVolume0() {
        Preference preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
        if (preference != null) {
            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            String uriId = TonesHandler.getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_NOTIFICATION);
            if (notificationToneChange.equals("1") && notificationTone.equals(uriId))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigured);
            else
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigureForVolume0);
        }
    }

    private void setSummary(String key, Object value)
    {
        if (key.equals(Profile.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                setTitleStyle(preference, false, true, false);
                setCategorySummary(preference, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE))
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
        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                        );*/
                final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), false);

                if (!canEnableZenMode)
                {
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        listPreference.setEnabled(false);
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        @SuppressLint("InlinedApi")
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
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
                        if ((iValue != Profile.NO_CHANGE_VALUE) && (iValue != Profile.SHARED_PROFILE_VALUE)) {
                            if (!((iValue == 6) && (android.os.Build.VERSION.SDK_INT < 23))) {
                                String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                                summary = summary + " - " + summaryArray[iValue - 1];
                            }
                        }
                        listPreference.setSummary(summary);

                        final String sRingerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                        int iRingerMode;
                        if (sRingerMode.isEmpty())
                            iRingerMode = 0;
                        else
                            iRingerMode = Integer.parseInt(sRingerMode);

                        if (iRingerMode == 5) {
                            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            @SuppressLint("InlinedApi")
                            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                            setTitleStyle(listPreference, index > 0, false, addS);
                            setCategorySummary(listPreference, index > 0);
                        }
                        listPreference.setEnabled(iRingerMode == 5);
                    }
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
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
            setSummaryForNotificationVolume0();
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
            key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
            key.equals(Profile.PREF_PROFILE_SOUND_ALARM))
        {
            setSummaryForNotificationVolume0();
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            if (key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                // set mobile data preference title
                ListPreference mobileDataPreference = (ListPreference)prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
                if (mobileDataPreference != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
                        mobileDataPreference.setDialogTitle(R.string.profile_preferences_deviceMobileData_21);
                    } else {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
                        mobileDataPreference.setDialogTitle(R.string.profile_preferences_deviceMobileData);
                    }
                }
            }
            int canChange = Profile.isProfilePreferenceAllowed(key, context);
            if (canChange != PPApplication.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + PPApplication.getNotAllowedPreferenceReasonString(getActivity()));
                    setTitleStyle(preference, false, false, false);
                    setCategorySummary(preference, false);
                }
            }
            else
            if (key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    String sValue = value.toString();
                    boolean bold = !sValue.equals(Profile.CONNECTTOSSID_JUSTANY);
                    setTitleStyle(preference, bold, false, false);
                    setCategorySummary(preference, bold);
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
        if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(getActivity()));
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
        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
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
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE))
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
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
            key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
            key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR) ||
            key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
            key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH))
        {
            int canChange = PPApplication.PREFERENCE_ALLOWED;
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
                canChange = Profile.isProfilePreferenceAllowed(key, context);
            if (canChange != PPApplication.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    setTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                }
            }
            else {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    setTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED)) {
            ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
            if (listPreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed_23);
                    listPreference.setDialogTitle(R.string.profile_preferences_notificationLed_23);
                } else {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed);
                    listPreference.setDialogTitle(R.string.profile_preferences_notificationLed);
                }
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(getActivity()));
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
        if (key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE))
        {
            ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
            if (listPreference != null) {
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
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
        if (key.equals(Profile.PREF_PROFILE_DURATION))
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
        if (key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(key, "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(key);
                setTitleStyle(listPreference, value != defaultValue, false, false);
                setCategorySummary(listPreference, /*index > 0*/false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                setTitleStyle(checkBoxPreference, show, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND))
        {
            String sValue = value.toString();
            RingtonePreference ringtonePreference = (RingtonePreference) prefMng.findPreference(key);
            if (ringtonePreference != null) {
                boolean show = !sValue.isEmpty();
                setTitleStyle(ringtonePreference, show, false, false);
                setCategorySummary(ringtonePreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                setTitleStyle(checkBoxPreference, show, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) && (Build.VERSION.SDK_INT < 26))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                setTitleStyle(checkBoxPreference, show, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_VOICE))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                setTitleStyle(preference, change, false, false);
                setCategorySummary(preference, change);
            }
        }
        if (key.equals(PREF_VOLUME_NOTIFICATION_VOLUME0)) {
            setSummaryForNotificationVolume0();
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);
                setTitleStyle(preference, change, false, false);
                setCategorySummary(preference, change);
            }
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = AccessibilityServiceBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.profile_preferences_deviceForceStopApplications_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_2_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)) {
            Preference preferenceScreen = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_CATEGORY);
            if (preferenceScreen != null) {
                int index = 0;
                String sValue = "0";
                CharSequence categorySummary = "";


                ListPreference listPreference =
                        (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
                if (listPreference != null) {

                    boolean ok = true;
                    CharSequence changeSummary = "";
                    int extenderVersion = AccessibilityServiceBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion == 0) {
                        ok = false;
                        changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                        categorySummary = changeSummary;
                    }
                    else
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_2_0) {
                        ok = false;
                        changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                        categorySummary = changeSummary;
                    }
                    else
                    if (!AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                        ok = false;
                        changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                        categorySummary = changeSummary;
                    }
                    if (!ok) {
                        listPreference.setSummary(changeSummary);
                        setTitleStyle(listPreference, false, false, false);
                        setCategorySummary(listPreference, false);
                    }
                    else {
                        sValue = listPreference.getValue();
                        index = listPreference.findIndexOfValue(sValue);
                        changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        categorySummary = changeSummary;
                        listPreference.setSummary(changeSummary);
                        setTitleStyle(listPreference, index > 0, false,false);
                        setCategorySummary(listPreference, index > 0);
                    }
                }

                if (sValue.equals("1")) {
                    ApplicationsMultiSelectDialogPreference appMultiSelectPreference =
                            (ApplicationsMultiSelectDialogPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
                    if (appMultiSelectPreference != null)
                        categorySummary = categorySummary + " • " + appMultiSelectPreference.getSummaryAMSDP();
                }
                preferenceScreen.setSummary(categorySummary);
                setTitleStyle(preferenceScreen, index > 0, false,false);
            }
        }

    }

    void setSummary(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
            key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
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

    private boolean getEnableVolumeNotificationVolume0(boolean notificationEnabled, String notificationValue) {
        return  notificationEnabled && ActivateProfileHelper.getMergedRingNotificationVolumes(context) &&
                ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context) &&
                Profile.getVolumeRingtoneChange(notificationValue) && (Profile.getVolumeRingtoneValue(notificationValue) == 0);
    }

    private void disableDependedPref(String key, Object value)
    {
        String sValue = value.toString();

        final String ON = "1";

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE)) {
            boolean enabled = getEnableVolumeNotificationByRingtone(sValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            String notificationValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
            enabled = getEnableVolumeNotificationVolume0(enabled, notificationValue);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION)) {
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            boolean enabled = (!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            enabled = getEnableVolumeNotificationVolume0(enabled, sValue);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) || sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) || sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) || sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) || sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) || sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (Profile.isProfilePreferenceAllowed(key, context) == PPApplication.PREFERENCE_ALLOWED) {
                boolean enabled = !sValue.equals(ON);
                ListPreference preference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(enabled);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
                boolean enabled = false;
                if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, context) == PPApplication.PREFERENCE_ALLOWED) &&
                        ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
                ListPreference preference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(enabled);
                }
            }
        }

        DevicePolicyManager manager = (DevicePolicyManager) getActivity().getSystemService(DEVICE_POLICY_SERVICE);
        final ComponentName component = new ComponentName(getActivity(), PPDeviceAdminReceiver.class);
        /* THIS NOT WORKING - after enabling device admin, entries are not refreshed in dialog :-/
        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            ListPreference lockDevicePreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (lockDevicePreference != null) {
                CharSequence[] entries = lockDevicePreference.getEntries();
                if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                    if (!manager.isAdminActive(component))
                        entries[1] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_enabled) + ")";
                    if (!PPApplication.isRooted())
                        entries[2] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_rooted) + ")";
                } else {
                    if (!manager.isAdminActive(component))
                        entries[2] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_enabled) + ")";
                    if (!PPApplication.isRooted())
                        entries[3] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_rooted) + ")";
                }
                lockDevicePreference.setEntries(entries);
            }
        }*/
        if (key.equals(PREF_DEVICE_ADMINISTRATOR_SETTINGS)) {
            if ((manager != null) && manager.isAdminActive(component)) {
                Preference preference = prefMng.findPreference(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
                if (preference != null) {
                    preference.setSummary(R.string.profile_preferences_lockDevice_deviceAdminSettings_summary_activated);
                    //preference.setEnabled(false);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
                if (preference != null) {
                    preference.setSummary(R.string.profile_preferences_lockDevice_deviceAdminSettings_summary);
                    //preference.setEnabled(false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
            boolean ok = AccessibilityServiceBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_2_0);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            if (preference != null) {
                preference.setEnabled(ok);
                setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            }
            ApplicationsMultiSelectDialogPreference appPreference =
                    (ApplicationsMultiSelectDialogPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
            if (appPreference != null) {
                appPreference.setEnabled(ok && (!(sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) || sValue.equals(Profile.NO_CHANGE_VALUE_STR))));
                appPreference.setSummaryAMSDP();
            }
        }
    }

    void disableDependedPref(String key) {
        String value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        String value;
        if (key.equals(Profile.PREF_PROFILE_NAME)) {
            value = sharedPreferences.getString(key, "");
            Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
            toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + value);
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
            key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
            value = sharedPreferences.getString(key, "");
        setSummary(key, value);
        // disable depended preferences
        disableDependedPref(key, value);

        if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
            // no save menu for shared profile
            ProfilePreferencesActivity activity = (ProfilePreferencesActivity) getActivity();
            ProfilePreferencesActivity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == EditorProfilesActivity.REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == Activity.RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                //int newProfileMode = data.getIntExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedProfileIndex = data.getIntExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id == Profile.SHARED_PROFILE_ID)
                {
                    Profile sharedProfile = Profile.getSharedProfile(context.getApplicationContext());
                    Permissions.grantProfilePermissions(context.getApplicationContext(), sharedProfile, true,
                            /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, false);
                }
            }
        }
        if (requestCode == WallpaperViewPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception ignored) {
                    }
                //}
                WallpaperViewPreference preference = (WallpaperViewPreference)prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
                if (preference != null)
                    preference.setImageIdentifier(selectedImage.toString());
                /*
                if (ProfilePreferencesFragment.changedWallpaperViewPreference != null) {
                    ProfilePreferencesFragment.changedWallpaperViewPreference.setImageIdentifier(selectedImage.toString());
                    ProfilePreferencesFragment.changedWallpaperViewPreference = null;
                }
                */
            }
        }
        if (requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception ignored) {
                    }
                //}

                Resources resources = context.getResources();
                int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                if (BitmapManipulator.checkBitmapSize(selectedImage.toString(), width, height, context)) {
                    ProfileIconPreference preference = (ProfileIconPreference)prefMng.findPreference(Profile.PREF_PROFILE_ICON);
                    if (preference != null) {
                        preference.dismissDialog();
                        preference.setImageIdentifierAndType(selectedImage.toString(), false, true);
                    }
                    /*if (ProfilePreferencesFragment.changedProfileIconPreference != null) {
                        ProfilePreferencesFragment.changedProfileIconPreference.dismissDialog();
                        ProfilePreferencesFragment.changedProfileIconPreference.setImageIdentifierAndType(selectedImage.toString(), false, true);
                        ProfilePreferencesFragment.changedProfileIconPreference = null;
                    }*/
                }
                else {
                    String text = context.getResources().getString(R.string.profileicon_pref_dialog_custom_icon_image_too_large);
                    text = text + " " + (width * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                    text = text + "x" + (height * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                    Toast msg = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG);
                    msg.show();
                }
            }
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/

            final String sZenModeType = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if (requestCode == ApplicationsDialogPreference.RESULT_APPLICATIONS_EDITOR && resultCode == Activity.RESULT_OK && data != null)
        {
            ApplicationsDialogPreference preference = (ApplicationsDialogPreference)prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null) {
                preference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        /*(Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON),*/
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
            }
            /*if (ProfilePreferencesFragment.applicationsDialogPreference != null) {
                ProfilePreferencesFragment.applicationsDialogPreference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
                ProfilePreferencesFragment.applicationsDialogPreference = null;
            }*/
        }
        if (requestCode == RESULT_UNLINK_VOLUMES_APP_PREFERENCES) {
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        }
        if (requestCode == RESULT_DEVICE_ADMINISTRATOR_SETTINGS) {
            disableDependedPref(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            // show save menu
            ProfilePreferencesActivity activity = (ProfilePreferencesActivity)getActivity();
            ProfilePreferencesActivity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WALLPAPER) {
            WallpaperViewPreference preference = (WallpaperViewPreference)prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.startGallery();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            ProfileIconPreference preference = (ProfileIconPreference)prefMng.findPreference(Profile.PREF_PROFILE_ICON);
            if (preference != null)
                preference.startGallery();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            BrightnessDialogPreference preference = (BrightnessDialogPreference)prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
            if (preference != null)
                preference.enableViews();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            RingtonePreference preference = (RingtonePreference) prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.refreshListView();
            preference = (RingtonePreference) prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.refreshListView();
            preference = (RingtonePreference) prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.refreshListView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        //Log.d("------ ProfilePreferencesFragment.addPreferencesFromResource", "startupSource="+startupSource);
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
            return "SharedProfilePreferencesFragment_PreferenceScreenKey";
        else
            return "ProfilePreferencesFragment_PreferenceScreenKey";
    }

}