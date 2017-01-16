package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.stericson.rootshell.RootShell;
import com.stericson.roottools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PPApplication extends Application {

    static String PACKAGE_NAME;

    public static final boolean exactAlarms = true;

    public static final String EXPORT_PATH = "/PhoneProfiles";
    public static final String LOG_FILENAME = "log.txt";
    public static final String CRASH_FILENAME = "crash.txt";

    private static boolean logIntoLogCat = false;
    private static boolean logIntoFile = false;
    private static boolean rootToolsDebug = false;
    public static String logFilterTags =     "PhoneProfilesHelper.doUninstallPPHelper"
                                            +"|PhoneProfilesBackupAgent"

                                            +"|BootUpReceiver.onReceive"
                                            +"|PhoneProfilesService"
                                            +"|FirstStartService.onHandleIntent"
                                            +"|ActivateProfileFromExternalApplicationActivity.onStart"
                                            +"|ActivateProfileListFragment.doOnStart"
                                            +"|BackgroundActivateProfileActivity.onStart"
            ;

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";
    static final String EXTRA_RESET_EDITOR = "reset_editor";
    static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
    static final String EXTRA_PREDEFINED_PROFILE_INDEX = "predefined_profile_index";
    //static final String EXTRA_PREFERENCES_STARTUP_SOURCE = "preferences_startup_source";
    static final String EXTRA_LINKUNLINK_VOLUMES = "link_unlink_volumes";
    static final String EXTRA_PROFILE_NAME = "profile_name";
    static final String EXTRA_FOR_PROFILE_ACTIVATION = "for_profile_activation";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_BOOT = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_SERVICE = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    static final int STARTUP_SOURCE_EXTERNAL_APP = 10;

    static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    static final int PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE = 3;

    // request code for startActivityForResult with intent BackgroundActivateProfileActivity
    static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
    // request code for startActivityForResult with intent ProfilePreferencesFragmentActivity
    static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
    // request code for startActivityForResult with intent PhoneProfilesActivity
    static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
    // request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
    static final int REQUEST_CODE_REMOTE_EXPORT = 6250;

    static final int PROFILE_NOTIFICATION_ID = 700420;
    static final int IMPORTANT_INFO_NOTIFICATION_ID = 700422;
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 700423;
    static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 700424;

    static final String PREF_PROFILE_NAME = "prf_pref_profileName";
    static final String PREF_PROFILE_ICON = "prf_pref_profileIcon";
    static final String PREF_PROFILE_VOLUME_RINGER_MODE = "prf_pref_volumeRingerMode";
    static final String PREF_PROFILE_VOLUME_ZEN_MODE = "prf_pref_volumeZenMode";
    static final String PREF_PROFILE_VOLUME_RINGTONE = "prf_pref_volumeRingtone";
    static final String PREF_PROFILE_VOLUME_NOTIFICATION = "prf_pref_volumeNotification";
    static final String PREF_PROFILE_VOLUME_MEDIA = "prf_pref_volumeMedia";
    static final String PREF_PROFILE_VOLUME_ALARM = "prf_pref_volumeAlarm";
    static final String PREF_PROFILE_VOLUME_SYSTEM = "prf_pref_volumeSystem";
    static final String PREF_PROFILE_VOLUME_VOICE = "prf_pref_volumeVoice";
    static final String PREF_PROFILE_SOUND_RINGTONE_CHANGE = "prf_pref_soundRingtoneChange";
    static final String PREF_PROFILE_SOUND_RINGTONE = "prf_pref_soundRingtone";
    static final String PREF_PROFILE_SOUND_NOTIFICATION_CHANGE = "prf_pref_soundNotificationChange";
    static final String PREF_PROFILE_SOUND_NOTIFICATION = "prf_pref_soundNotification";
    static final String PREF_PROFILE_SOUND_ALARM_CHANGE = "prf_pref_soundAlarmChange";
    static final String PREF_PROFILE_SOUND_ALARM = "prf_pref_soundAlarm";
    static final String PREF_PROFILE_DEVICE_AIRPLANE_MODE = "prf_pref_deviceAirplaneMode";
    static final String PREF_PROFILE_DEVICE_WIFI = "prf_pref_deviceWiFi";
    static final String PREF_PROFILE_DEVICE_BLUETOOTH = "prf_pref_deviceBluetooth";
    static final String PREF_PROFILE_DEVICE_SCREEN_TIMEOUT = "prf_pref_deviceScreenTimeout";
    static final String PREF_PROFILE_DEVICE_BRIGHTNESS = "prf_pref_deviceBrightness";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_CHANGE = "prf_pref_deviceWallpaperChange";
    static final String PREF_PROFILE_DEVICE_WALLPAPER = "prf_pref_deviceWallpaper";
    static final String PREF_PROFILE_DEVICE_MOBILE_DATA = "prf_pref_deviceMobileData";
    static final String PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS = "prf_pref_deviceMobileDataPrefs";
    static final String PREF_PROFILE_DEVICE_GPS = "prf_pref_deviceGPS";
    static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE = "prf_pref_deviceRunApplicationChange";
    static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "prf_pref_deviceRunApplicationPackageName";
    static final String PREF_PROFILE_DEVICE_AUTOSYNC = "prf_pref_deviceAutosync";
    static final String PREF_PROFILE_DEVICE_AUTOROTATE = "prf_pref_deviceAutoRotation";
    static final String PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS = "prf_pref_deviceLocationServicePrefs";
    static final String PREF_PROFILE_VOLUME_SPEAKER_PHONE = "prf_pref_volumeSpeakerPhone";
    static final String PREF_PROFILE_DEVICE_NFC = "prf_pref_deviceNFC";
    static final String PREF_PROFILE_DURATION = "prf_pref_duration";
    static final String PREF_PROFILE_AFTER_DURATION_DO = "prf_pref_afterDurationDo";
    static final String PREF_PROFILE_DEVICE_KEYGUARD = "prf_pref_deviceKeyguard";
    static final String PREF_PROFILE_VIBRATION_ON_TOUCH = "prf_pref_vibrationOnTouch";
    static final String PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS = "prf_pref_volumeUnlinkVolumesAppSettings";
    static final String PREF_PROFILE_DEVICE_WIFI_AP = "prf_pref_deviceWiFiAP";
    static final String PREF_PROFILE_DEVICE_POWER_SAVE_MODE = "prf_pref_devicePowerSaveMode";
    //static final String PREF_PROFILE_SHOW_DURATION_BUTTON = "prf_pref_showDurationButton";
    static final String PREF_PROFILE_ASK_FOR_DURATION = "prf_pref_askForDuration";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE = "prf_pref_deviceNetworkType";
    static final String PREF_PROFILE_NOTIFICATION_LED = "prf_pref_notificationLed";
    static final String PREF_PROFILE_VIBRATE_WHEN_RINGING = "prf_pref_vibrateWhenRinging";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_FOR = "prf_pref_deviceWallpaperFor";

    // no preferences, bud checked from isProfilePreferenceAllowed
    static final String PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS = "prf_pref_deviceAdaptiveBrightness";

    static final String PROFILE_ICON_DEFAULT = "ic_profile_default";

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    static final String DEFAULT_PROFILE_PREFS_NAME = "profile_preferences_default_profile"; //PPApplication.APPLICATION_PREFS_NAME;
    static final String PERMISSIONS_PREFS_NAME = "permissions_list";

    public static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    public static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    public static final String PREF_APPLICATION_ALERT = "applicationAlert";
    public static final String PREF_APPLICATION_CLOSE = "applicationClose";
    public static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    public static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    public static final String PREF_APPLICATION_THEME = "applicationTheme";
    public static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    public static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    public static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    public static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    public static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    public static final String PREF_NOTIFICATION_STATUS_BAR  = "notificationStatusBar";
    public static final String PREF_NOTIFICATION_STATUS_BAR_STYLE  = "notificationStatusBarStyle";
    public static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT  = "notificationStatusBarPermanent";
    public static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    public static final String PREF_NOTIFICATION_SHOW_IN_STATUS_BAR  = "notificationShowInStatusBar";
    public static final String PREF_NOTIFICATION_TEXT_COLOR = "notificationTextColor";
    public static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR = "applicationWidgetListPrefIndicator";
    public static final String PREF_APPLICATION_WIDGET_LIST_HEADER = "applicationWidgetListHeader";
    public static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND = "applicationWidgetListBackground";
    public static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B = "applicationWidgetListLightnessB";
    public static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T = "applicationWidgetListLightnessT";
    public static final String PREF_APPLICATION_WIDGET_ICON_COLOR = "applicationWidgetIconColor";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS = "applicationWidgetIconLightness";
    public static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR = "applicationWidgetListIconColor";
    public static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS = "applicationWidgetListIconLightness";
    public static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    public static final String PREF_APPLICATION_BACKGROUND_PROFILE = "applicationBackgroundProfile";
    public static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT= "applicationActivatorGridLayout";
    public static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT= "applicationWidgetListGridLayout";
    public static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    public static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    public static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    public static final String PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN = "notificationHideInLockscreen";
    public static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND = "applicationWidgetIconBackground";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B = "applicationWidgetIconLightnessB";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T = "applicationWidgetIconLightnessT";

    public static final int PREFERENCE_NOT_ALLOWED = 0;
    public static final int PREFERENCE_ALLOWED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_NO_HARDWARE = 0;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_ROOTED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND = 2;
    public static final int PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND = 3;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM = 4;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS = 5;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION = 6;

    public static final long DEFAULT_PROFILE_ID = -999L;  // source profile id
    public static final long PROFILE_NO_ACTIVATE = -999;

    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVATED_PROFILE_FOR_DURATION = "activatedProfileForDuration";
    private static final String PREF_ACTIVATED_PROFILE_END_DURATION_TIME = "activatedProfileEndDurationTime";
    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START = "show_info_notification_on_start";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION = "show_info_notification_on_start_version";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION = "show_request_write_settings_permission";
    private static final String PREF_MERGED_PERRMISSIONS = "merged_permissions";
    private static final String PREF_MERGED_PERRMISSIONS_COUNT = "merged_permissions_count";
    private static final String PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION = "show_request_access_notification_policy_permission";
    private static final String PREF_SCREEN_UNLOCKED = "screen_unlocked";
    private static final String PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION = "show_request_draw_overlays_permission";
    private static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT = "activated_profile_screen_timeout";

    public static boolean applicationStartOnBoot;
    public static boolean applicationActivate;
    public static boolean applicationActivateWithAlert;
    public static boolean applicationClose;
    public static boolean applicationLongClickActivation;
    public static String applicationLanguage;
    public static String applicationTheme;
    public static boolean applicationActivatorPrefIndicator;
    public static boolean applicationEditorPrefIndicator;
    public static boolean applicationActivatorHeader;
    public static boolean applicationEditorHeader;
    public static boolean notificationsToast;
    public static boolean notificationStatusBar;
    public static String notificationStatusBarStyle;
    public static boolean notificationStatusBarPermanent;
    public static String notificationStatusBarCancel;
    public static boolean notificationShowInStatusBar;
    public static boolean notificationHideInLockscreen;
    public static String notificationTextColor;
    public static boolean applicationWidgetListPrefIndicator;
    public static boolean applicationWidgetListHeader;
    public static String applicationWidgetListBackground;
    public static String applicationWidgetListLightnessB;
    public static String applicationWidgetListLightnessT;
    public static String applicationWidgetIconColor;
    public static String applicationWidgetIconLightness;
    public static String applicationWidgetListIconColor;
    public static String applicationWidgetListIconLightness;
    public static boolean notificationPrefIndicator;
    public static String applicationBackgroundProfile;
    public static boolean applicationActivatorGridLayout;
    public static boolean applicationWidgetListGridLayout;
    public static boolean applicationWidgetIconHideProfileName;
    public static boolean applicationUnlinkRingerNotificationVolumes;
    public static boolean applicationShortcutEmblem;
    public static String applicationWidgetIconBackground;
    public static String applicationWidgetIconLightnessB;
    public static String applicationWidgetIconLightnessT;

    public static int notAllowedReason;
    public static  String notAllowedReasonDetail;

    //public static final RootMutex rootMutex = new RootMutex();

    @Override
    public void onCreate()
    {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

    //	Debug.startMethodTracing("phoneprofiles");

        //long nanoTimeStart = startMeasuringRunTime();

        super.onCreate();

        PACKAGE_NAME = getPackageName();

        // initialization
        loadPreferences(this);

        PPApplication.initRoot();

        //Log.d("PPApplication.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());

        //Log.d("PPApplication.onCreate","xxx");

        //getMeasuredRunTime(nanoTimeStart, "PPApplication.onCreate");

    }

    @Override
    public void onTerminate ()
    {
        DatabaseHandler.getInstance(this).closeConnecion();
        super.onTerminate();
    }

    //--------------------------------------------------------------

    static public void loadPreferences(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        applicationStartOnBoot = preferences.getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
        applicationActivate = preferences.getBoolean(PREF_APPLICATION_ACTIVATE, true);
        applicationActivateWithAlert = preferences.getBoolean(PREF_APPLICATION_ALERT, true);
        applicationClose = preferences.getBoolean(PREF_APPLICATION_CLOSE, true);
        applicationLongClickActivation = preferences.getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
        applicationLanguage = preferences.getString(PREF_APPLICATION_LANGUAGE, "system");
        applicationTheme = preferences.getString(PREF_APPLICATION_THEME, "material");
        applicationActivatorPrefIndicator = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
        applicationEditorPrefIndicator = preferences.getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        applicationActivatorHeader = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
        applicationEditorHeader = preferences.getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
        notificationsToast = preferences.getBoolean(PREF_NOTIFICATION_TOAST, true);
        notificationStatusBar = preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
        notificationStatusBarStyle = preferences.getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
        notificationStatusBarPermanent = preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        notificationStatusBarCancel = preferences.getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
        notificationShowInStatusBar = preferences.getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
        notificationTextColor = preferences.getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
        notificationHideInLockscreen = preferences.getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
        applicationWidgetListPrefIndicator = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
        applicationWidgetListHeader = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
        applicationWidgetListBackground = preferences.getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
        applicationWidgetListLightnessB = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
        applicationWidgetListLightnessT = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
        applicationWidgetIconColor = preferences.getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
        applicationWidgetIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
        applicationWidgetListIconColor = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
        applicationWidgetListIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
        notificationPrefIndicator = preferences.getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
        applicationBackgroundProfile = preferences.getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
        applicationActivatorGridLayout = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
        applicationWidgetListGridLayout = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
        applicationWidgetIconHideProfileName = preferences.getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
        applicationUnlinkRingerNotificationVolumes = preferences.getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
        applicationShortcutEmblem = preferences.getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
        applicationWidgetIconBackground = preferences.getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "0");
        applicationWidgetIconLightnessB = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
        applicationWidgetIconLightnessT = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");

        if (applicationTheme.equals("light"))
        {
            applicationTheme = "material";
            Editor editor = preferences.edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.commit();
        }
    }

    private static String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    private static void moveDefaultProfilesPreference(Context context)
    {
        SharedPreferences oldPreferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences newPreferences = context.getSharedPreferences(DEFAULT_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editorNew = newPreferences.edit();
        SharedPreferences.Editor editorOld = oldPreferences.edit();
        Map<String, ?> all = oldPreferences.getAll();
        for (Entry<String, ?> x : all.entrySet()) {

            if (x.getKey().equals(PREF_PROFILE_NAME) ||
                x.getKey().equals(PREF_PROFILE_ICON) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_RINGER_MODE) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_ZEN_MODE) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_RINGTONE) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_NOTIFICATION) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_MEDIA) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_ALARM) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_SYSTEM) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_VOICE) ||
                x.getKey().equals(PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                x.getKey().equals(PREF_PROFILE_SOUND_RINGTONE) ||
                x.getKey().equals(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                x.getKey().equals(PREF_PROFILE_SOUND_NOTIFICATION) ||
                x.getKey().equals(PREF_PROFILE_SOUND_ALARM_CHANGE) ||
                x.getKey().equals(PREF_PROFILE_SOUND_ALARM) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_WIFI) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_BLUETOOTH) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_WALLPAPER) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_GPS) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_AUTOSYNC) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_AUTOROTATE) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                x.getKey().equals(PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_NFC) ||
                x.getKey().equals(PREF_PROFILE_DURATION) ||
                x.getKey().equals(PREF_PROFILE_AFTER_DURATION_DO) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_KEYGUARD) ||
                x.getKey().equals(PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_WIFI_AP) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                x.getKey().equals(PREF_PROFILE_NOTIFICATION_LED) ||
                x.getKey().equals(PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                x.getKey().equals(PREF_PROFILE_DEVICE_WALLPAPER_FOR))
            {
                if      (x.getValue().getClass().equals(Boolean.class)) editorNew.putBoolean(x.getKey(), (Boolean)x.getValue());
                else if (x.getValue().getClass().equals(Float.class))   editorNew.putFloat(x.getKey(),   (Float)x.getValue());
                else if (x.getValue().getClass().equals(Integer.class)) editorNew.putInt(x.getKey(),     (Integer)x.getValue());
                else if (x.getValue().getClass().equals(Long.class))    editorNew.putLong(x.getKey(),    (Long)x.getValue());
                else if (x.getValue().getClass().equals(String.class))  editorNew.putString(x.getKey(),  (String)x.getValue());

                editorOld.remove(x.getKey());
            }
        }
        editorNew.commit();
        editorOld.commit();
    }

    static public Profile getDefaultProfile(Context context)
    {
        // move default profile preferences into new file
        moveDefaultProfilesPreference(context);

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int	maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int	maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int	maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int	maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int	maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int	maximumValueVoicecall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        SharedPreferences preferences = context.getSharedPreferences(DEFAULT_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);

        Profile profile = new Profile();
        profile._id = DEFAULT_PROFILE_ID;
        profile._name = context.getResources().getString(R.string.default_profile_name);
        profile._icon = PROFILE_ICON_DEFAULT + "|1|0|0";
        profile._checked = false;
        profile._porder = 0;
        profile._duration = 0;
        profile._afterDurationDo = Profile.AFTERDURATIONDO_NOTHING;
        profile._volumeRingerMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VOLUME_RINGER_MODE, "1")); // ring
        profile._volumeZenMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VOLUME_ZEN_MODE, "1")); // all
        profile._volumeRingtone = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_RINGTONE, getVolumeLevelString(71, maximumValueRing) + "|0|0");
        profile._volumeNotification = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_NOTIFICATION, getVolumeLevelString(86, maximumValueNotification)+"|0|0");
        profile._volumeMedia = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_MEDIA, getVolumeLevelString(80, maximumValueMusic)+"|0|0");
        profile._volumeAlarm = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_ALARM, getVolumeLevelString(100, maximumValueAlarm)+"|0|0");
        profile._volumeSystem = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_SYSTEM, getVolumeLevelString(70, maximumValueSystem)+"|0|0");
        profile._volumeVoice = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_VOICE, getVolumeLevelString(70, maximumValueVoicecall)+"|0|0");
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
        profile._soundRingtone = preferences.getString(PPApplication.PREF_PROFILE_SOUND_RINGTONE, Settings.System.DEFAULT_RINGTONE_URI.toString());
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
        profile._soundNotification = preferences.getString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
        profile._soundAlarm = preferences.getString(PPApplication.PREF_PROFILE_SOUND_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "2")); // OFF
        profile._deviceWiFi = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WIFI, "2")); // OFF
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_BLUETOOTH, "2")); //OFF
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "2")); // 30 seconds
        profile._deviceBrightness = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|0|1|0");  // automatic on
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        profile._deviceWallpaper = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER, "-|0");
        profile._deviceMobileData = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA, "1")); //ON
        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0"));
        profile._deviceGPS = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_GPS, "2")); //OFF
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
        profile._deviceRunApplicationPackageName = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        profile._deviceAutosync = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_AUTOSYNC, "1")); // ON
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_AUTOROTATE, "1")); // ON
        profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, "0"));
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
        profile._deviceNFC = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_NFC, "0"));
        profile._deviceKeyguard = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_KEYGUARD, "0"));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WIFI_AP, "2")); // OFF
        profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, "0"));
        profile._deviceNetworkType = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
        profile._notificationLed = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_NOTIFICATION_LED, "0"));
        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
        profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_FOR, "0"));

        return profile;
    }

    static public Profile getMappedProfile(Profile profile, Context context)
    {
        if (profile != null)
        {
            Profile defaultProfile = getDefaultProfile(context);

            Profile mappedProfile = new Profile(
                               profile._id,
                               profile._name,
                               profile._icon,
                               profile._checked,
                               profile._porder,
                               profile._volumeRingerMode,
                               profile._volumeRingtone,
                               profile._volumeNotification,
                               profile._volumeMedia,
                               profile._volumeAlarm,
                               profile._volumeSystem,
                               profile._volumeVoice,
                               profile._soundRingtoneChange,
                               profile._soundRingtone,
                               profile._soundNotificationChange,
                               profile._soundNotification,
                               profile._soundAlarmChange,
                               profile._soundAlarm,
                               profile._deviceAirplaneMode,
                               profile._deviceWiFi,
                               profile._deviceBluetooth,
                               profile._deviceScreenTimeout,
                               profile._deviceBrightness,
                               profile._deviceWallpaperChange,
                               profile._deviceWallpaper,
                               profile._deviceMobileData,
                               profile._deviceMobileDataPrefs,
                               profile._deviceGPS,
                               profile._deviceRunApplicationChange,
                               profile._deviceRunApplicationPackageName,
                               profile._deviceAutosync,
                               profile._deviceAutoRotate,
                               profile._deviceLocationServicePrefs,
                               profile._volumeSpeakerPhone,
                               profile._deviceNFC,
                               profile._duration,
                               profile._afterDurationDo,
                               profile._volumeZenMode,
                               profile._deviceKeyguard,
                               profile._vibrationOnTouch,
                               profile._deviceWiFiAP,
                               profile._devicePowerSaveMode,
                               profile._askForDuration,
                               profile._deviceNetworkType,
                               profile._notificationLed,
                               profile._vibrateWhenRinging,
                               profile._deviceWallpaperFor);

            boolean zenModeMapped = false;
            if (profile._volumeRingerMode == 99) {
                mappedProfile._volumeRingerMode = defaultProfile._volumeRingerMode;
                if (mappedProfile._volumeRingerMode == 5) {
                    mappedProfile._volumeZenMode = defaultProfile._volumeZenMode;
                    zenModeMapped = true;
                }
            }
            if ((profile._volumeZenMode == 99) && (!zenModeMapped))
                mappedProfile._volumeZenMode = defaultProfile._volumeZenMode;
            if (profile.getVolumeRingtoneDefaultProfile())
                mappedProfile._volumeRingtone = defaultProfile._volumeRingtone;
            if (profile.getVolumeNotificationDefaultProfile())
                mappedProfile._volumeNotification = defaultProfile._volumeNotification;
            if (profile.getVolumeAlarmDefaultProfile())
                mappedProfile._volumeAlarm = defaultProfile._volumeAlarm;
            if (profile.getVolumeMediaDefaultProfile())
                mappedProfile._volumeMedia = defaultProfile._volumeMedia;
            if (profile.getVolumeSystemDefaultProfile())
                mappedProfile._volumeSystem = defaultProfile._volumeSystem;
            if (profile.getVolumeVoiceDefaultProfile())
                mappedProfile._volumeVoice = defaultProfile._volumeVoice;
            if (profile._soundRingtoneChange == 99)
            {
                mappedProfile._soundRingtoneChange = defaultProfile._soundRingtoneChange;
                mappedProfile._soundRingtone = defaultProfile._soundRingtone;
            }
            if (profile._soundNotificationChange == 99)
            {
                mappedProfile._soundNotificationChange = defaultProfile._soundNotificationChange;
                mappedProfile._soundNotification = defaultProfile._soundNotification;
            }
            if (profile._soundAlarmChange == 99)
            {
                mappedProfile._soundAlarmChange = defaultProfile._soundAlarmChange;
                mappedProfile._soundAlarm = defaultProfile._soundAlarm;
            }
            if (profile._deviceAirplaneMode == 99)
                mappedProfile._deviceAirplaneMode = defaultProfile._deviceAirplaneMode;
            if (profile._deviceAutosync == 99)
                mappedProfile._deviceAutosync = defaultProfile._deviceAutosync;
            if (profile._deviceMobileData == 99)
                mappedProfile._deviceMobileData = defaultProfile._deviceMobileData;
            if (profile._deviceMobileDataPrefs == 99)
                mappedProfile._deviceMobileDataPrefs = defaultProfile._deviceMobileDataPrefs;
            if (profile._deviceWiFi == 99)
                mappedProfile._deviceWiFi = defaultProfile._deviceWiFi;
            if (profile._deviceBluetooth == 99)
                mappedProfile._deviceBluetooth = defaultProfile._deviceBluetooth;
            if (profile._deviceGPS == 99)
                mappedProfile._deviceGPS = defaultProfile._deviceGPS;
            if (profile._deviceLocationServicePrefs == 99)
                mappedProfile._deviceLocationServicePrefs = defaultProfile._deviceLocationServicePrefs;
            if (profile._deviceScreenTimeout == 99)
                mappedProfile._deviceScreenTimeout = defaultProfile._deviceScreenTimeout;
            if (profile.getDeviceBrightnessDefaultProfile())
                mappedProfile._deviceBrightness = defaultProfile._deviceBrightness;
            if (profile._deviceAutoRotate == 99)
                mappedProfile._deviceAutoRotate = defaultProfile._deviceAutoRotate;
            if (profile._deviceRunApplicationChange == 99)
            {
                mappedProfile._deviceRunApplicationChange = defaultProfile._deviceRunApplicationChange;
                mappedProfile._deviceRunApplicationPackageName = defaultProfile._deviceRunApplicationPackageName;
            }
            if (profile._deviceWallpaperChange == 99)
            {
                mappedProfile._deviceWallpaperChange = defaultProfile._deviceWallpaperChange;
                mappedProfile._deviceWallpaper = defaultProfile._deviceWallpaper;
                mappedProfile._deviceWallpaperFor = defaultProfile._deviceWallpaperFor;
            }
            if (profile._volumeSpeakerPhone == 99)
                mappedProfile._volumeSpeakerPhone = defaultProfile._volumeSpeakerPhone;
            if (profile._deviceNFC == 99)
                mappedProfile._deviceNFC = defaultProfile._deviceNFC;
            if (profile._deviceKeyguard == 99)
                mappedProfile._deviceKeyguard = defaultProfile._deviceKeyguard;
            if (profile._vibrationOnTouch == 99)
                mappedProfile._vibrationOnTouch = defaultProfile._vibrationOnTouch;
            if (profile._deviceWiFiAP == 99)
                mappedProfile._deviceWiFiAP = defaultProfile._deviceWiFiAP;
            if (profile._devicePowerSaveMode == 99)
                mappedProfile._devicePowerSaveMode = defaultProfile._devicePowerSaveMode;
            if (profile._deviceNetworkType == 99)
                mappedProfile._deviceNetworkType = defaultProfile._deviceNetworkType;
            if (profile._notificationLed == 99)
                mappedProfile._notificationLed = defaultProfile._notificationLed;
            if (profile._vibrateWhenRinging == 99)
                mappedProfile._vibrateWhenRinging = defaultProfile._vibrateWhenRinging;

            mappedProfile._iconBitmap = profile._iconBitmap;
            mappedProfile._preferencesIndicator = profile._preferencesIndicator;

            return mappedProfile;
        }
        else
            return null;
    }

    static public boolean getApplicationStarted(Context context, boolean testService)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        if (testService)
            return preferences.getBoolean(PREF_APPLICATION_STARTED, false) && (PhoneProfilesService.instance != null);
        else
            return preferences.getBoolean(PREF_APPLICATION_STARTED, false);
    }

    static public void setApplicationStarted(Context context, boolean appStarted)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
        editor.commit();
    }

    static public long getActivatedProfileForDuration(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, 0);
    }

    static public void setActivatedProfileForDuration(Context context, long profileId)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, profileId);
        editor.commit();
    }

    static public long getActivatedProfileEndDurationTime(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, 0);
    }

    static public void setActivatedProfileEndDurationTime(Context context, long time)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, time);
        editor.commit();
    }

    static public boolean getLockscreenDisabled(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_LOCKSCREEN_DISABLED, false);
    }

    static public void setLockscreenDisabled(Context context, boolean disabled)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_LOCKSCREEN_DISABLED, disabled);
        editor.commit();
    }

    static public int getRingerVolume(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_RINGER_VOLUME, -999);
    }

    static public void setRingerVolume(Context context, int volume)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_RINGER_VOLUME, volume);
        editor.commit();
    }

    static public int getNotificationVolume(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_NOTIFICATION_VOLUME, -999);
    }

    static public void setNotificationVolume(Context context, int volume)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_NOTIFICATION_VOLUME, volume);
        editor.commit();
    }

    static public boolean getScreenUnlocked(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SCREEN_UNLOCKED, true);
    }

    static public void setScreenUnlocked(Context context, boolean unlocked)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SCREEN_UNLOCKED, unlocked);
        editor.commit();
    }

    static public int getRingerMode(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_RINGER_MODE, 0);
    }

    static public void setRingerMode(Context context, int mode)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_RINGER_MODE, mode);
        editor.commit();
    }

    static public int getZenMode(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_ZEN_MODE, 0);
    }

    static public void setZenMode(Context context, int mode)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_ZEN_MODE, mode);
        editor.commit();
    }

    static public boolean getShowInfoNotificationOnStart(Context context, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        boolean show = preferences.getBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, true);
        int _version = preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        return ((_version >= version) && show);
    }

    static public void setShowInfoNotificationOnStart(Context context, boolean show, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, show);
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.commit();
    }

    static public int getShowInfoNotificationOnStartVersion(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, 0);
    }

    static public void setShowInfoNotificationOnStartVersion(Context context, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.commit();
    }

    static public boolean getShowRequestWriteSettingsPermission(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, true);
    }

    static public void setShowRequestWriteSettingsPermission(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, value);
        editor.commit();
    }

    static public boolean getShowRequestAccessNotificationPolicyPermission(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, true);
    }

    static public void setShowRequestAccessNotificationPolicyPermission(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, value);
        editor.commit();
    }

    static public boolean getShowRequestDrawOverlaysPermission(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, true);
    }

    static public void setShowRequestDrawOverlaysPermission(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, value);
        editor.commit();
    }

    static public List<Permissions.PermissionType> getMergedPermissions(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);

        List<Permissions.PermissionType> permissions = new ArrayList<>();

        int count = preferences.getInt(PREF_MERGED_PERRMISSIONS_COUNT, 0);

        Gson gson = new Gson();

        for (int i = 0; i < count; i++) {
            String json = preferences.getString(PREF_MERGED_PERRMISSIONS + i, "");
            if (!json.isEmpty()) {
                Permissions.PermissionType permission = gson.fromJson(json, Permissions.PermissionType.class);
                permissions.add(permission);
            }
        }

        return permissions;
    }

    static public void addMergedPermissions(Context context, List<Permissions.PermissionType> permissions)
    {
        List<Permissions.PermissionType> savedPermissions = getMergedPermissions(context);

        for (Permissions.PermissionType permission : permissions) {
            boolean found = false;
            for (Permissions.PermissionType savedPermission : savedPermissions) {

                if (savedPermission.permission.equals(permission.permission)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                savedPermissions.add(new Permissions.PermissionType(permission.preference, permission.permission));
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(PREF_MERGED_PERRMISSIONS_COUNT, savedPermissions.size());

        Gson gson = new Gson();

        for (int i = 0; i < savedPermissions.size(); i++)
        {
            String json = gson.toJson(savedPermissions.get(i));
            editor.putString(PREF_MERGED_PERRMISSIONS+i, json);
        }

        editor.commit();
    }

    static public void clearMergedPermissions(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    static public int getActivatedProfileScreenTimeout(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, 0);
    }

    static public void setActivatedProfileScreenTimeout(Context context, int timeout)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, timeout);
        editor.commit();
    }

    // ----- Check if preference is allowed in device -------------------------------------

    static int isProfilePreferenceAllowed(String preferenceKey, Context context)
    {
        int featurePresented = PREFERENCE_NOT_ALLOWED;

        if (preferenceKey.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 17)
            {
                if (isRooted())
                {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                featurePresented = PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_WIFI))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device ma Wifi
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_BLUETOOTH))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                // device ma bluetooth
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_MOBILE_DATA))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    if (isRooted()) {
                        // zariadenie je rootnute
                        //if (serviceBinaryExists() && telephonyServiceExists(context, PREF_PROFILE_DEVICE_MOBILE_DATA))
                        featurePresented = PREFERENCE_ALLOWED;
                    }
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                {
                    if (canSetMobileData(context))
                        featurePresented = PREFERENCE_ALLOWED;
                    else {
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                featurePresented = PREFERENCE_ALLOWED;
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_GPS))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
            {
                // device ma gps
                /*if (android.os.Build.VERSION.SDK_INT < 17)
                {
                    if (isRooted(false))
                        featurePresented = PREFERENCE_ALLOWED;
                }
                else*/
                if (isRooted())
                {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                if (canExploitGPS(context))
                {
                    featurePresented = PREFERENCE_ALLOWED;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_NFC))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
            {
                logE("PPApplication.hardwareCheck","NFC=presented");

                // device ma nfc
                if (isRooted())
                    featurePresented = PREFERENCE_ALLOWED;
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            {
                logE("PPApplication.hardwareCheck","NFC=not presented");
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                // device ma Wifi
                if (WifiApManager.canExploitWifiAP(context))
                {
                    featurePresented = PREFERENCE_ALLOWED;
                }
                else {
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_VIBRATE_WHEN_RINGING))
        {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (isRooted()) {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                featurePresented = PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                {
                    if (isRooted())
                    {
                        // zariadenie je rootnute
                        if (settingsBinaryExists())
                            featurePresented = PREFERENCE_ALLOWED;
                        else
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                    featurePresented = PREFERENCE_ALLOWED;
            }
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_POWER_SAVE_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (isRooted()) {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_NETWORK_TYPE))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                final int phoneType = telephonyManager.getPhoneType();
                if ((phoneType == TelephonyManager.PHONE_TYPE_GSM) || (phoneType == TelephonyManager.PHONE_TYPE_CDMA)) {
                    if (isRooted()) {
                        // zariadenie je rootnute
                        if (telephonyServiceExists(context, PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (serviceBinaryExists())
                                featurePresented = PREFERENCE_ALLOWED;
                            else
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        }
                        else {
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                        }
                    }
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else {
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_NOTIFICATION_LED))
        {
            int value = Settings.System.getInt(context.getContentResolver(), "notification_light_pulse", -10);
            if ((value != -10) && (android.os.Build.VERSION.SDK_INT >= 23)) {
                if (isRooted()) {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            if (value != -10)
                featurePresented = PREFERENCE_ALLOWED;
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_KEYGUARD))
        {
            boolean secureKeyguard;
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 16)
                secureKeyguard = keyguardManager.isKeyguardSecure();
            else
                secureKeyguard = keyguardManager.inKeyguardRestrictedInputMode();
            if (secureKeyguard) {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_secure_lock);
            }
            else
                featurePresented = PREFERENCE_ALLOWED;
        }
        else
            featurePresented = PREFERENCE_ALLOWED;

        return featurePresented;
    }

    public static String getNotAllowedPreferenceReasonString(Context context) {
        switch (notAllowedReason) {
            case PREFERENCE_NOT_ALLOWED_NO_HARDWARE: return context.getString(R.string.preference_not_allowed_reason_no_hardware);
            case PREFERENCE_NOT_ALLOWED_NOT_ROOTED: return context.getString(R.string.preference_not_allowed_reason_not_rooted);
            case PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_settings_not_found);
            case PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_service_not_found);
            case PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS: return context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM:
                return context.getString(R.string.preference_not_allowed_reason_not_supported) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_by_application) + " (" + notAllowedReasonDetail + ")";
            default: return context.getString(R.string.empty_string);
        }
    }

    static boolean canExploitGPS(Context context)
    {
        // test expoiting power manager widget
        PackageManager pacman = context.getPackageManager();
        try {
            PackageInfo pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);

            if(pacInfo != null){
                for(ActivityInfo actInfo : pacInfo.receivers){
                    //test if recevier is exported. if so, we can toggle GPS.
                    if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                        return true;
                    }
                }
            }
        } catch (NameNotFoundException e) {
            return false; //package not found
        }
        return false;
    }

    static boolean canSetMobileData(Context context)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    static public String getTransactionCode(Context context, String fieldName) throws Exception {
        //try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        //} catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
        //    throw e;
        //}
    }

    static boolean telephonyServiceExists(Context context, String preference) {
        try {
            /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                Log.e("PPApplication.telephonyServiceExists","getActiveSubscriptionInfoCount="+mSubscriptionManager.getActiveSubscriptionInfoCount());
                Log.e("PPApplication.telephonyServiceExists", "subscriptionInfo="+mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0));
                if (mSubscriptionManager.getActiveSubscriptionInfoCount() > 1)
                    // dual sim is not supported
                    return false;
            }*/

            if (preference.equals(PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                getTransactionCode(context, "TRANSACTION_setDataEnabled");
            }
            else
            if (preference.equals(PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    //--------------------------------------------------------------

    static private void resetLog()
    {
        File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    @SuppressLint("SimpleDateFormat")
    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException ignored) {
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] splits = logFilterTags.split("\\|");
        for (String split : splits) {
            if (tag.contains(split)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    static public void logI(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    static public void logW(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    static public void logE(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    static public void logD(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
    }

    //--------------------------------------------------------------

    //static private boolean rootChecked;
    static private boolean rooted;
    //static private boolean settingsBinaryChecked;
    static private boolean settingsBinaryExists;
    //static private boolean isSELinuxEnforcingChecked;
    static private boolean isSELinuxEnforcing;
    //static private String suVersion;
    //static private boolean suVersionChecked;
    //static private boolean serviceBinaryChecked;
    static private boolean serviceBinaryExists;

    static synchronized void initRoot() {
        //synchronized (PPApplication.rootMutex) {
        //rootChecked = false;
        rooted = false;
        //settingsBinaryChecked = false;
        settingsBinaryExists = false;
        //isSELinuxEnforcingChecked = false;
        isSELinuxEnforcing = false;
        //suVersion = null;
        //suVersionChecked = false;
        //serviceBinaryChecked = false;
        serviceBinaryExists = false;
        //}
    }

    private static boolean _isRooted()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!rootChecked)
        if (!rooted)
        {
            PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //rootChecking = true;
            /*try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //if (RootTools.isRootAvailable()) {
            if (RootToolsSmall.isRooted()) {
                // zariadenie je rootnute
                PPApplication.logE("PPApplication._isRooted", "root available");
                //rootChecked = true;
                rooted = true;
            } else {
                PPApplication.logE("PPApplication._isRooted", "root NOT available");
                //rootChecked = true;
                rooted = false;
                settingsBinaryExists = false;
                //settingsBinaryChecked = false;
                //isSELinuxEnforcingChecked = false;
                isSELinuxEnforcing = false;
                //suVersionChecked = false;
                //suVersion = null;
                serviceBinaryExists = false;
                //serviceBinaryChecked = false;
            }
        }
        //if (rooted)
        //	getSUVersion();
        return rooted;
    }

    static boolean isRooted() {
        //synchronized (PPApplication.rootMutex) {
        _isRooted();
        //}
        return rooted;
    }

    static boolean isRootGranted()
    {
        RootShell.debugMode = rootToolsDebug;

        //synchronized (PPApplication.rootMutex) {

        if (_isRooted()) {
            PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
            //grantChecking = true;
                /*try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            if (RootTools.isAccessGiven()) {
                // root grantnuty
                PPApplication.logE("PPApplication.isRootGranted", "root granted");
                return true;
            } else {
                // grant odmietnuty
                PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                return false;
            }
        }
        else {
            PPApplication.logE("PPApplication.isRootGranted", "not rooted");
            return false;
        }
        //}
    }

    static boolean settingsBinaryExists()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!settingsBinaryChecked)
        if (!settingsBinaryExists)
        {
            //synchronized (PPApplication.rootMutex) {
            PPApplication.logE("PPApplication.settingsBinaryExists", "start");
            //settingsBinaryChecking = true;
                /*try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            //List<String> settingsPaths = RootTools.findBinary("settings");
            //settingsBinaryExists = settingsPaths.size() > 0;
            settingsBinaryExists = RootToolsSmall.hasSettingBin();
            //settingsBinaryChecked = true;
            //}
        }
        PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists="+settingsBinaryExists);
        return settingsBinaryExists;
    }

    static boolean serviceBinaryExists()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!serviceBinaryChecked)
        if (!serviceBinaryExists)
        {
            //synchronized (PPApplication.rootMutex) {
            PPApplication.logE("PPApplication.serviceBinaryExists", "start");
            //serviceBinaryChecking = true;
                /*try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            //List<String> servicePaths = RootTools.findBinary("service");
            //serviceBinaryExists = servicePaths.size() > 0;
            serviceBinaryExists = RootToolsSmall.hasServiceBin();
            //serviceBinaryChecked = true;
            //}
        }
        PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists="+serviceBinaryExists);
        return serviceBinaryExists;
    }

    /**
     * Detect if SELinux is set to enforcing, caches result
     *
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     */
    public static boolean isSELinuxEnforcing()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!isSELinuxEnforcingChecked)
        if (!isSELinuxEnforcing)
        {
            //synchronized (PPApplication.rootMutex) {
            boolean enforcing = false;

            // First known firmware with SELinux built-in was a 4.2 (17)
            // leak
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                // Detect enforcing through sysfs, not always present
                File f = new File("/sys/fs/selinux/enforce");
                if (f.exists()) {
                    try {
                        InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                        try {
                            enforcing = (is.read() == '1');
                        } finally {
                            is.close();
                        }
                    } catch (Exception ignore) {
                    }
                }

                /*
                // 4.4+ builds are enforcing by default, take the gamble
                if (!enforcing)
                {
                    enforcing = (android.os.Build.VERSION.SDK_INT >= 19);
                }
                */
            }

            isSELinuxEnforcing = enforcing;
            //isSELinuxEnforcingChecked = true;
            //}
        }

        PPApplication.logE("PPApplication.isSELinuxEnforcing", "isSELinuxEnforcing="+isSELinuxEnforcing);

        return isSELinuxEnforcing;
    }

    /*
    public static String getSELinuxEnforceCommand(String command, Shell.ShellContext context)
    {
        if ((suVersion != null) && suVersion.contains("SUPERSU"))
            return "su --context " + context.getValue() + " -c \"" + command + "\"  < /dev/null";
        else
            return command;
    }

    public static String getSUVersion()
    {
        if (!suVersionChecked)
        {
            Command command = new Command(0, false, "su -v")
            {
                @Override
                public void commandOutput(int id, String line) {
                    suVersion = line;

                    super.commandOutput(id, line);
                }
            }
            ;
            try {
                RootTools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
                //RootTools.closeAllShells();
            } catch (Exception e) {
                Log.e("PPApplication.getSUVersion", "Error on run su");
            }
        }
        return suVersion;
    }
    
    private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("GlobaData.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }
    */

    public static String getJavaCommandFile(Class<?> mainClass, String name, Context context, Object cmdParam) {
        try {
            String cmd =
                    "#!/system/bin/sh\n" +
                            "base=/system\n" +
                            "export CLASSPATH=" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.sourceDir + "\n" +
                            "exec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";

            FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            fos.write(cmd.getBytes());
            fos.close();

            File file = context.getFileStreamPath(name);
            //noinspection ResultOfMethodCallIgnored
            file.setExecutable(true);

            /*
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File outFile = new File(sd, PPApplication.EXPORT_PATH + "/" + name);
            OutputStream out = new FileOutputStream(outFile);
            out.write(cmd.getBytes());
            out.close();

            outFile.setExecutable(true);
            */

            return file.getAbsolutePath();

        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    // Debug -----------------------------------------------------------------

    /*
    public static long startMeasuringRunTime()
    {
        return System.nanoTime();
    }

    public static void getMeasuredRunTime(long nanoTimeStart, String log)
    {
        long nanoTimeEnd = System.nanoTime();
        long measuredTime = (nanoTimeEnd - nanoTimeStart) / 1000000;

        Log.d(log, "MEASURED TIME=" + measuredTime);
    }
    */

    // others ------------------------------------------------------------------

    public static int getResourceId(String pVariableName, String pResourcename, Context context)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourcename, context.getPackageName());
        } catch (Exception e) {
            //e.printStackTrace();
            return -1;
        }
    }

    public static void sleep(long ms) {
        long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);
    }

    public static boolean canChangeZenMode(Context context, boolean notCheckAccess) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60) {
                if (notCheckAccess)
                    return true;
                else
                    return Permissions.checkAccessNotificationPolicy(context);
            }
            else
                return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        }
        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23))
            return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getSystemZenMode(Context context, int defaultValue) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60) {
                NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                int interuptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                switch (interuptionFilter) {
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        return ActivateProfileHelper.ZENMODE_ALL;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        return ActivateProfileHelper.ZENMODE_PRIORITY;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        return ActivateProfileHelper.ZENMODE_NONE;
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        return ActivateProfileHelper.ZENMODE_ALARMS;
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        return ActivateProfileHelper.ZENMODE_ALL;
                }
            }
            else {
                int interuptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);;
                switch (interuptionFilter) {
                    case 0:
                        return ActivateProfileHelper.ZENMODE_ALL;
                    case 1:
                        return ActivateProfileHelper.ZENMODE_PRIORITY;
                    case 2:
                        return ActivateProfileHelper.ZENMODE_NONE;
                    case 3:
                        return ActivateProfileHelper.ZENMODE_ALARMS;
                }
            }
        }
        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) {
            int interuptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
            switch (interuptionFilter) {
                case 0:
                    return ActivateProfileHelper.ZENMODE_ALL;
                case 1:
                    return ActivateProfileHelper.ZENMODE_PRIORITY;
                case 2:
                    return ActivateProfileHelper.ZENMODE_NONE;
                case 3:
                    return ActivateProfileHelper.ZENMODE_ALARMS;
            }
        }
        return defaultValue;
    }

    public static boolean getMergedRingNotificationVolumes(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true);
    }

    // test if ring and notification volumes are merged
    public static void setMergedRingNotificationVolumes(Context context, boolean force) {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        if (!preferences.contains(PREF_MERGED_RING_NOTIFICATION_VOLUMES) || force) {
            try {
                boolean merged;
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = audioManager.getRingerMode();
                int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                int oldRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                int oldNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                if (oldRingVolume == oldNotificationVolume) {
                    int newNotificationVolume;
                    if (oldNotificationVolume == maximumNotificationValue)
                        newNotificationVolume = oldNotificationVolume - 1;
                    else
                        newNotificationVolume = oldNotificationVolume + 1;
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newNotificationVolume, 0);
                    PPApplication.sleep(500);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_RING) == newNotificationVolume)
                        merged = true;
                    else
                        merged = false;
                } else
                    merged = false;
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, 0);
                audioManager.setRingerMode(ringerMode);

                //Log.d("PPApplication.setMergedRingNotificationVolumes", "merged="+merged);

                Editor editor = preferences.edit();
                editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
                editor.commit();
            } catch (Exception ignored) { }
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean vibrationIsOn(Context context, AudioManager audioManager, boolean testRingerMode) {
        int ringerMode = -999;
        if (testRingerMode)
            ringerMode = audioManager.getRingerMode();
        int vibrateType = -999;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
            vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        //int vibrateWhenRinging;
        //if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);
        //else
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

        PPApplication.logE("PPApplication.vibrationIsOn", "ringerMode="+ringerMode);
        PPApplication.logE("PPApplication.vibrationIsOn", "vibrateType="+vibrateType);
        //PPApplication.logE("PPApplication.vibrationIsOn", "vibrateWhenRinging="+vibrateWhenRinging);

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT);// ||
                //(vibrateWhenRinging == 1);
    }

    public static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

}
