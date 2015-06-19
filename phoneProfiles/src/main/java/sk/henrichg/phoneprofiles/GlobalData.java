package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.stericson.RootShell.RootShell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GlobalData extends Application {

	static String PACKAGE_NAME;
	
	public static final String EXPORT_PATH = "/PhoneProfiles";
	public static final String LOG_FILENAME = "log.txt";

	private static boolean logIntoLogCat = false;
	private static boolean logIntoFile = false;
	private static boolean rootToolsDebug = false;
	public static String logFilterTags = "ActivateProfileListFragment"
			;
	
	static final String EXTRA_PROFILE_ID = "profile_id";
	static final String EXTRA_START_APP_SOURCE = "start_app_source";
	static final String EXTRA_RESET_EDITOR = "reset_editor";
	static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
	static final String EXTRA_PREFERENCES_STARTUP_SOURCE = "preferences_startup_source";

	static final int STARTUP_SOURCE_NOTIFICATION = 1;
	static final int STARTUP_SOURCE_WIDGET = 2;
	static final int STARTUP_SOURCE_SHORTCUT = 3;
	static final int STARTUP_SOURCE_BOOT = 4;
	static final int STARTUP_SOURCE_ACTIVATOR = 5;
	static final int STARTUP_SOURCE_SERVICE = 6;
	static final int STARTUP_SOURCE_EDITOR = 8;
	static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
	
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
	
	// musi byt tu, pouziva t ActivateProfileHelper
	static final int NOTIFICATION_ID = 700420;

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

	static final String PROFILE_ICON_DEFAULT = "ic_profile_default";
	
	static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
	static final String DEFAULT_PROFILE_PREFS_NAME = "profile_preferences_default_profile"; //GlobalData.APPLICATION_PREFS_NAME;
	
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

    public static final int HARDWARE_CHECK_NOT_ALLOWED = 0;
    public static final int HARDWARE_CHECK_ALLOWED = 1;
    public static final int HARDWARE_CHECK_INSTALL_PPHELPER = 2;
    public static final int HARDWARE_CHECK_UPGRADE_PPHELPER = 3;
    
	public static final long DEFAULT_PROFILE_ID = -999;  // source profile id
	public static final long PROFILE_NO_ACTIVATE = -999;
	
	private static final String PREF_APPLICATION_STARTED = "applicationStarted";
	private static final String PREF_ACTIVATED_PROFILE_FOR_DURATION = "activatedProfileForDuration";
    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
	private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START = "show_info_notification_on_start";

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
    
	public void onCreate()
	{
	//	Debug.startMethodTracing("phoneprofiles");
		
		//long nanoTimeStart = startMeasuringRunTime();
		
		super.onCreate();
		
		PACKAGE_NAME = getPackageName();
		
		// initialization
		loadPreferences(this);
		
		//Log.d("GlobalData.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());
		
		//Log.d("GlobalData.onCreate","xxx");
		
		//getMeasuredRunTime(nanoTimeStart, "GlobalData.onCreate");
		
	}
	
	public void onTerminate ()
	{
		DatabaseHandler.getInstance(this).closeConnecion();
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
	    applicationWidgetListPrefIndicator = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
	    applicationWidgetListHeader = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
	    applicationWidgetListBackground = preferences.getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
	    applicationWidgetListLightnessB = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
	    applicationWidgetListLightnessT = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
	    applicationWidgetIconColor = preferences.getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
	    applicationWidgetIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");;
	    applicationWidgetListIconColor = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
	    applicationWidgetListIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");;
	    notificationPrefIndicator = preferences.getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
	    applicationBackgroundProfile = preferences.getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
	    applicationActivatorGridLayout = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, false);
	    applicationWidgetListGridLayout = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, false);
	    applicationWidgetIconHideProfileName = preferences.getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
		applicationUnlinkRingerNotificationVolumes = preferences.getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);

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
                x.getKey().equals(PREF_PROFILE_DEVICE_WIFI_AP))
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
    	profile._volumeRingerMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "1")); // ring
    	profile._volumeZenMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "1")); // all
    	profile._volumeRingtone = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, getVolumeLevelString(71, maximumValueRing) + "|0|0");
    	profile._volumeNotification = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, getVolumeLevelString(86, maximumValueNotification)+"|0|0");
    	profile._volumeMedia = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, getVolumeLevelString(80, maximumValueMusic)+"|0|0");
    	profile._volumeAlarm = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ALARM, getVolumeLevelString(100, maximumValueAlarm)+"|0|0");
    	profile._volumeSystem = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, getVolumeLevelString(70, maximumValueSystem)+"|0|0");
    	profile._volumeVoice = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_VOICE, getVolumeLevelString(70, maximumValueVoicecall)+"|0|0");
    	profile._soundRingtoneChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
    	profile._soundRingtone = preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, Settings.System.DEFAULT_RINGTONE_URI.toString());
    	profile._soundNotificationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
    	profile._soundNotification = preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    	profile._soundAlarmChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
    	profile._soundAlarm = preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
    	profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "2")); // OFF
    	profile._deviceWiFi = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI, "2")); // OFF
    	profile._deviceBluetooth = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, "2")); //OFF
    	profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "2")); // 30 seconds
    	profile._deviceBrightness = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|0|1|0");  // automatic on
    	profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
   		profile._deviceWallpaper = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, "-|0");
    	profile._deviceMobileData = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, "1")); //ON
    	profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0"));
    	profile._deviceGPS = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_GPS, "2")); //OFF
    	profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
   		profile._deviceRunApplicationPackageName = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
    	profile._deviceAutosync = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, "1")); // ON
    	profile._deviceAutoRotate = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, "1")); // ON
    	profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, "0"));
    	profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
    	profile._deviceNFC = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NFC, "0"));
    	profile._deviceKeyguard = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, "0"));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, "2")); // OFF

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
							   profile._deviceWiFiAP);
		
			if (profile._volumeRingerMode == 99)
				mappedProfile._volumeRingerMode = defaultProfile._volumeRingerMode;
			if (profile._volumeZenMode == 99)
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

			mappedProfile._iconBitmap = profile._iconBitmap;
			mappedProfile._preferencesIndicator = profile._preferencesIndicator;
			
			return mappedProfile;
		}
		else
			return profile;
	}

	static public boolean getApplicationStarted(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_APPLICATION_STARTED, false);
	}

	static public void setApplicationStarted(Context context, boolean globalEventsStarted)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_APPLICATION_STARTED, globalEventsStarted);
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

	static public int getRingerMode(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getInt(PREF_RINGER_MODE, 0);
	}

	static public void setRingerMode(Context context, int volume)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(PREF_RINGER_MODE, volume);
		editor.commit();
	}

	static public boolean getShowInfoNotificationOnStart(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, true);
	}

	static public void setShowInfoNotificationOnStart(Context context, boolean volume)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, volume);
		editor.commit();
	}


	// ----- Hardware check -------------------------------------
	
	static int hardwareCheck(String preferenceKey, Context context)
	{
		//long nanoTimeStart = startMeasuringRunTime();
		
		int featurePresented = HARDWARE_CHECK_NOT_ALLOWED;

		if (preferenceKey.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE))
		{	
			if (android.os.Build.VERSION.SDK_INT >= 17)
			{
				if (PhoneProfilesHelper.isPPHelperInstalled(context, 7))
				{
					// je nainstalovany PhonProfilesHelper
					featurePresented = HARDWARE_CHECK_ALLOWED;
				}
				else
				if (isRooted(false))
				{
					// zariadenie je rootnute
					if (settingsBinaryExists())
						featurePresented = HARDWARE_CHECK_ALLOWED;
					else
					{
						// "settings" binnary not exists 
						if (PhoneProfilesHelper.PPHelperVersion == -1)
							featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
						else
							featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
					}
				}
			}
			else
				featurePresented = HARDWARE_CHECK_ALLOWED;
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_WIFI))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
				// device ma Wifi
				featurePresented = HARDWARE_CHECK_ALLOWED;
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_BLUETOOTH))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
				// device ma bluetooth
				featurePresented = HARDWARE_CHECK_ALLOWED;
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_MOBILE_DATA))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
			{
				if (android.os.Build.VERSION.SDK_INT >= 21)
				{
					if (PhoneProfilesHelper.isPPHelperInstalled(context, 22))
					{
						// je nainstalovany PhonProfilesHelper
						featurePresented = HARDWARE_CHECK_ALLOWED;
				    }
					else
					{
						if (isRooted(false))
						{
							if (PhoneProfilesHelper.PPHelperVersion == -1)
								featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
							else
								featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
						}
					}
				}
				else
				{
					if (canSetMobileData(context))
						featurePresented = HARDWARE_CHECK_ALLOWED;
				}
			}
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS))
		{
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
			{
				featurePresented = HARDWARE_CHECK_ALLOWED;
			}
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_GPS))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
			{
				// device ma gps
				if (canExploitGPS(context))
				{
					featurePresented = HARDWARE_CHECK_ALLOWED;
			    }
				else
				if (android.os.Build.VERSION.SDK_INT < 17)
				{
					if (PhoneProfilesHelper.isPPHelperInstalled(context, 7))
					{
						// je nainstalovany PhonProfilesHelper
						featurePresented = HARDWARE_CHECK_ALLOWED;
				    }
					else
					{
						if (isRooted(false))
						{
							if (PhoneProfilesHelper.PPHelperVersion == -1)
								featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
							else
								featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
						}
					}
				}
				else
				if (isRooted(false))
				{
					// zariadenie je rootnute
					if (settingsBinaryExists())
						featurePresented = HARDWARE_CHECK_ALLOWED;
					else
					{
						// "settings" binnary not exists 
						if (PhoneProfilesHelper.PPHelperVersion == -1)
							featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
						else
							featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
					}
				}
			}
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_NFC))
		{
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
			{
				// device ma nfc
				if (PhoneProfilesHelper.isPPHelperInstalled(context, 7))
				{
					// je nainstalovany PhonProfilesHelper
					featurePresented = HARDWARE_CHECK_ALLOWED;
			    }
				else
				{
					if (isRooted(false))
					{
						if (PhoneProfilesHelper.PPHelperVersion == -1)
							featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
						else
							featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
					}
				}
			}
		}
        else
        if (preferenceKey.equals(PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device ma Wifi
                featurePresented = HARDWARE_CHECK_ALLOWED;
        }
		else
			featurePresented = HARDWARE_CHECK_ALLOWED;
		
		//getMeasuredRunTime(nanoTimeStart, "GlobalData.hardwareCheck for "+preferenceKey);
		
		return featurePresented;
	}
	
	static boolean canExploitGPS(Context context)
	{
		// test expoiting power manager widget
	    PackageManager pacman = context.getPackageManager();
	    PackageInfo pacInfo = null;
	    try {
	        pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);

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
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
	}

	//--------------------------------------------------------------
	
	static private void resetLog()
	{
		File sd = Environment.getExternalStorageDirectory();
		File exportDir = new File(sd, GlobalData.EXPORT_PATH);
		if (!(exportDir.exists() && exportDir.isDirectory()))
			exportDir.mkdirs();
		
		File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
		logFile.delete();
	}

	@SuppressLint("SimpleDateFormat")
	static private void logIntoFile(String type, String tag, String text)
	{
		if (!logIntoFile)
			return;

		File sd = Environment.getExternalStorageDirectory();
		File exportDir = new File(sd, GlobalData.EXPORT_PATH);
		if (!(exportDir.exists() && exportDir.isDirectory()))
			exportDir.mkdirs();
		
		File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);

		if (logFile.length() > 1024 * 10000)
			resetLog();

	    if (!logFile.exists())
	    {
	        try
	        {
	            logFile.createNewFile();
	        } 
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	    }
	    try
	    {
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
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }		
	}

	private static boolean logContainsFilterTag(String tag)
	{
		boolean contains = false;
		String[] splits = logFilterTags.split("\\|");
		for (int i = 0; i < splits.length; i++)
		{
			if (tag.contains(splits[i]))
			{
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
	
	static private boolean rootChecked = false;
	static private boolean rooted = false;
	static private boolean grantChecked = false;
	static private boolean rootGranted = false;
	static private boolean settingsBinaryExists = false;
	static private boolean settingsBinaryChecked = false;
	static private boolean isSELinuxEnforcingChecked = false;
	static private boolean isSELinuxEnforcing = false;
	//static private String suVersion = null;
	//static private boolean suVersionChecked = false;
	
	static boolean isRooted(boolean onlyCheckFlags)
	{
		RootShell.debugMode = rootToolsDebug;
		
		if (!rootChecked)
		{
			settingsBinaryExists = false;
			settingsBinaryChecked = false;
			isSELinuxEnforcingChecked = false;
			isSELinuxEnforcing = false;
			//suVersionChecked = false;
			//suVersion = null;
			if (!onlyCheckFlags)
			{
				if (RootTools.isRootAvailable())
				{
					// zariadenie je rootnute
					rootChecked = true;
					rooted = true;
				}
				else
				{
					rootChecked = true;
					rooted = false;
				}
			}
			else
			{
				rootChecked = false;
				rooted = false;
			}
		}
		//if (rooted)
		//	getSUVersion();
		return rooted; 
	}
	
	static boolean grantRoot(boolean force)
	{
		RootShell.debugMode = rootToolsDebug;
		
		if ((!grantChecked) || force)
		{
			settingsBinaryExists = false;
			settingsBinaryChecked = false;
			isSELinuxEnforcingChecked = false;
			isSELinuxEnforcing = false;
			//suVersionChecked = false;
			//suVersion = null;
			if (RootTools.isAccessGiven())
			{
				// root grantnuty
				rootChecked = true;
				rooted = true;
				grantChecked = true;
				rootGranted = true;
			}
			else
			{
				// grant odmietnuty
				rootChecked = true;
				rooted = false;
				grantChecked = true;
				rootGranted = false;
			}
		}

		//if (rooted)
		//	getSUVersion();
		return rootGranted;
	}
	
	static boolean settingsBinaryExists()
	{
		RootShell.debugMode = rootToolsDebug;
		
		if (!settingsBinaryChecked)
		{
			List<String> settingsPaths = RootTools.findBinary("settings"); 
			settingsBinaryExists = settingsPaths.size() > 0;
			settingsBinaryChecked = true;
		}
		return settingsBinaryExists;
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
    	
        if (!isSELinuxEnforcingChecked)
        {
            boolean enforcing = false;

            // First known firmware with SELinux built-in was a 4.2 (17)
            // leak
            if (android.os.Build.VERSION.SDK_INT >= 17)
            {
                // Detect enforcing through sysfs, not always present
                File f = new File("/sys/fs/selinux/enforce");
                if (f.exists())
                {
                    try {
                        InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                        try {
                            enforcing = (is.read() == '1');
                        } finally {
                            is.close();
                        }
                    } catch (Exception e) {
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
            isSELinuxEnforcingChecked = true; 
            
        }
        
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
				Log.e("GlobalData.getSUVersion", "Error on run su");
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
	
	// Debug -----------------------------------------------------------------
	
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
	
}
