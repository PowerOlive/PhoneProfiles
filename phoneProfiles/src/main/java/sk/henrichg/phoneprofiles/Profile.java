package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Profile {

    //private variables
    long _id;
    String _name;
    String _icon;
    boolean _checked;
    int _porder;
    int _duration;
    int _afterDurationDo;
    boolean _askForDuration;
    String _durationNotificationSound;
    boolean _durationNotificationVibrate;
    int _volumeRingerMode;
    int _volumeZenMode;
    String _volumeRingtone;
    String _volumeNotification;
    String _volumeMedia;
    String _volumeAlarm;
    String _volumeSystem;
    String _volumeVoice;
    int _soundRingtoneChange;
    String _soundRingtone;
    int _soundNotificationChange;
    String _soundNotification;
    int _soundAlarmChange;
    String _soundAlarm;
    int _deviceAirplaneMode;
    int _deviceMobileData;
    int _deviceMobileDataPrefs;
    int _deviceWiFi;
    int _deviceBluetooth;
    int _deviceGPS;
    int _deviceLocationServicePrefs;
    int _deviceScreenTimeout;
    String _deviceBrightness;
    int _deviceWallpaperChange;
    String _deviceWallpaper;
    int _deviceRunApplicationChange;
    String _deviceRunApplicationPackageName;
    int _deviceAutoSync;
    int _deviceAutoRotate;
    int _volumeSpeakerPhone;
    int _deviceNFC;
    int _deviceKeyguard;
    int _vibrationOnTouch;
    int _deviceWiFiAP;
    int _devicePowerSaveMode;
    int _deviceNetworkType;
    int _notificationLed;
    int _vibrateWhenRinging;
    int _deviceWallpaperFor;
    boolean _hideStatusBarIcon;
    int _lockDevice;
    String _deviceConnectToSSID;
    int _deviceWiFiAPPrefs;
    int _headsUpNotifications;
    int _deviceForceStopApplicationChange;
    String _deviceForceStopApplicationPackageName;
    long _activationByUserCount;
    int _deviceNetworkTypePrefs;
    int _deviceCloseAllApplications;
    int _screenNightMode;
    int _dtmfToneWhenDialing;
    int _soundOnTouch;
    String _volumeDTMF;
    String _volumeAccessibility;
    String _volumeBluetoothSCO;

    Bitmap _iconBitmap;
    Bitmap _preferencesIndicator;
    int _ringerModeForZenMode;

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
    static final String PREF_PROFILE_ASK_FOR_DURATION = "prf_pref_askForDuration";
    static final String PREF_PROFILE_DURATION_NOTIFICATION_SOUND = "prf_pref_durationNotificationSound";
    static final String PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE = "prf_pref_durationNotificationVibrate";
    static final String PREF_PROFILE_DEVICE_KEYGUARD = "prf_pref_deviceKeyguard";
    static final String PREF_PROFILE_VIBRATION_ON_TOUCH = "prf_pref_vibrationOnTouch";
    static final String PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS = "prf_pref_volumeUnlinkVolumesAppSettings";
    static final String PREF_PROFILE_DEVICE_WIFI_AP = "prf_pref_deviceWiFiAP";
    static final String PREF_PROFILE_DEVICE_POWER_SAVE_MODE = "prf_pref_devicePowerSaveMode";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE = "prf_pref_deviceNetworkType";
    static final String PREF_PROFILE_NOTIFICATION_LED = "prf_pref_notificationLed";
    static final String PREF_PROFILE_VIBRATE_WHEN_RINGING = "prf_pref_vibrateWhenRinging";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_FOR = "prf_pref_deviceWallpaperFor";
    static final String PREF_PROFILE_HIDE_STATUS_BAR_ICON = "prf_pref_hideStatusBarIcon";
    static final String PREF_PROFILE_LOCK_DEVICE = "prf_pref_lockDevice";
    static final String PREF_PROFILE_DEVICE_CONNECT_TO_SSID = "prf_pref_deviceConnectToSSID";
    static final String PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS = "prf_pref_deviceAdaptiveBrightness";
    static final String PREF_PROFILE_DEVICE_WIFI_AP_PREFS = "prf_pref_deviceWiFiAPPrefs";
    static final String PREF_PROFILE_HEADS_UP_NOTIFICATIONS = "prf_pref_headsUpNotifications";
    static final String PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE = "prf_pref_deviceForceStopApplicationChange";
    static final String PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = "prf_pref_deviceForceStopApplicationPackageName";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS = "prf_pref_deviceNetworkTypePrefs";
    static final String PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS = "prf_pref_deviceCloseAllApplications";
    static final String PREF_PROFILE_SCREEN_NIGHT_MODE = "prf_pref_screenNightMode";
    static final String PREF_PROFILE_DTMF_TONE_WHEN_DIALING = "prf_pref_dtmfToneWhenDialing";
    static final String PREF_PROFILE_SOUND_ON_TOUCH = "prf_pref_soundOnTouch";
    static final String PREF_PROFILE_VOLUME_DTMF = "prf_pref_volumeDTMF";
    static final String PREF_PROFILE_VOLUME_ACCESSIBILITY = "prf_pref_volumeAccessibility";
    static final String PREF_PROFILE_VOLUME_BLUETOOTH_SCO = "prf_pref_volumeBluetoothSCO";

    static final HashMap<String, Boolean> defaultValuesBoolean;
    static {
        defaultValuesBoolean = new HashMap<>();
        defaultValuesBoolean.put("prf_pref_askForDuration", false);
        defaultValuesBoolean.put("prf_pref_durationNotificationVibrate", false);
        defaultValuesBoolean.put("prf_pref_hideStatusBarIcon", false);
    }
    static final HashMap<String, String> defaultValuesString;
    static {
        defaultValuesString = new HashMap<>();
        defaultValuesString.put("prf_pref_profileIcon", "ic_profile_default|1|0|0");
        defaultValuesString.put("prf_pref_profileIcon_withoutIcon", "|1|0|0");
        defaultValuesString.put("prf_pref_duration", "0");
        defaultValuesString.put("prf_pref_afterDurationDo", "0");
        defaultValuesString.put("prf_pref_durationNotificationSound", "");
        defaultValuesString.put("prf_pref_volumeRingerMode", "0");
        defaultValuesString.put("prf_pref_volumeZenMode", "0");
        defaultValuesString.put("prf_pref_vibrationOnTouch", "0");
        defaultValuesString.put("prf_pref_volumeRingtone", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeNotification", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeMedia", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeAlarm", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeSystem", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeVoice", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeSpeakerPhone", "0");
        defaultValuesString.put("prf_pref_soundRingtoneChange", "0");
        defaultValuesString.put("prf_pref_soundRingtone", "");
        defaultValuesString.put("prf_pref_soundNotificationChange", "0");
        defaultValuesString.put("prf_pref_soundNotification", "");
        defaultValuesString.put("prf_pref_soundAlarmChange", "0");
        defaultValuesString.put("prf_pref_soundAlarm", "");
        defaultValuesString.put("prf_pref_deviceAirplaneMode", "0");
        defaultValuesString.put("prf_pref_deviceAutosync", "0");
        defaultValuesString.put("prf_pref_deviceMobileData", "0");
        defaultValuesString.put("prf_pref_deviceMobileDataPrefs", "0");
        defaultValuesString.put("prf_pref_deviceWiFi", "0");
        defaultValuesString.put("prf_pref_deviceWiFiAP", "0");
        defaultValuesString.put("prf_pref_deviceBluetooth", "0");
        defaultValuesString.put("prf_pref_deviceGPS", "0");
        defaultValuesString.put("prf_pref_deviceLocationServicePrefs", "0");
        defaultValuesString.put("prf_pref_deviceNFC", "0");
        defaultValuesString.put("prf_pref_deviceScreenTimeout", "0");
        defaultValuesString.put("prf_pref_deviceKeyguard", "0");
        defaultValuesString.put("prf_pref_deviceBrightness", "50|1|1|0");
        defaultValuesString.put("prf_pref_deviceBrightness_withoutLevel", "|1|1|0");
        defaultValuesString.put("prf_pref_deviceAutoRotation", "0");
        defaultValuesString.put("prf_pref_devicePowerSaveMode", "0");
        defaultValuesString.put("prf_pref_deviceRunApplicationChange", "0");
        defaultValuesString.put("prf_pref_deviceRunApplicationPackageName", "-");
        defaultValuesString.put("prf_pref_deviceWallpaperChange", "0");
        defaultValuesString.put("prf_pref_deviceWallpaper", "-");
        defaultValuesString.put("prf_pref_deviceNetworkType", "0");
        defaultValuesString.put("prf_pref_notificationLed", "0");
        defaultValuesString.put("prf_pref_vibrateWhenRinging", "0");
        defaultValuesString.put("prf_pref_deviceWallpaperFor", "0");
        defaultValuesString.put("prf_pref_lockDevice", "0");
        defaultValuesString.put("prf_pref_deviceConnectToSSID", "^just_any^");
        defaultValuesString.put("prf_pref_deviceWiFiAPPrefs", "0");
        defaultValuesString.put("prf_pref_headsUpNotifications", "0");
        defaultValuesString.put("prf_pref_deviceForceStopApplicationChange", "0");
        defaultValuesString.put("prf_pref_deviceForceStopApplicationPackageName", "-");
        defaultValuesString.put("prf_pref_deviceNetworkTypePrefs", "0");
        defaultValuesString.put("prf_pref_deviceCloseAllApplications", "0");
        defaultValuesString.put("prf_pref_screenNightMode", "0");
        defaultValuesString.put("prf_pref_dtmfToneWhenDialing", "0");
        defaultValuesString.put("prf_pref_soundOnTouch", "0");
        defaultValuesString.put("prf_pref_volumeDTMF", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeAccessibility", "-1|1|0");
        defaultValuesString.put("prf_pref_volumeBluetoothSCO", "-1|1|0");
    }

    static final int RINGERMODE_RING = 1;
    static final int RINGERMODE_RING_AND_VIBRATE = 2;
    static final int RINGERMODE_VIBRATE = 3;
    static final int RINGERMODE_SILENT = 4;
    static final int RINGERMODE_ZENMODE = 5;

    static final int ZENMODE_ALL = 1;
    static final int ZENMODE_PRIORITY = 2;
    static final int ZENMODE_NONE = 3;
    static final int ZENMODE_ALL_AND_VIBRATE = 4;
    static final int ZENMODE_PRIORITY_AND_VIBRATE = 5;
    static final int ZENMODE_ALARMS = 6;

    static final int AFTERDURATIONDO_NOTHING = 0;
    static final int AFTERDURATIONDO_UNDOPROFILE = 1;
    static final int AFTERDURATIONDO_BACKGROUNPROFILE = 2;

    static final int BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET = -99;

    static final String CONNECTTOSSID_JUSTANY = "^just_any^";
    //static final String CONNECTTOSSID_SHAREDPROFILE = "^default_profile^";

    static final String PROFILE_ICON_DEFAULT = "ic_profile_default";
    //static final long SHARED_PROFILE_ID = -999L;
    static final long PROFILE_NO_ACTIVATE = -999;

    static final int NO_CHANGE_VALUE = 0;
    //static final int SHARED_PROFILE_VALUE = 99;
    static final String NO_CHANGE_VALUE_STR = "0";
    //static final String SHARED_PROFILE_VALUE_STR = "99";

    private static final String PREF_ACTIVATED_PROFILE_FOR_DURATION = "activatedProfileForDuration";
    private static final String PREF_ACTIVATED_PROFILE_END_DURATION_TIME = "activatedProfileEndDurationTime";

    static final int[] profileIconId = {
            R.drawable.ic_profile_default,

            R.drawable.ic_profile_home, R.drawable.ic_profile_home_2, R.drawable.ic_profile_home_3,
            R.drawable.ic_profile_home_4, R.drawable.ic_profile_home_5, R.drawable.ic_profile_home_6,

            R.drawable.ic_profile_outdoors_1, R.drawable.ic_profile_outdoors_2, R.drawable.ic_profile_outdoors_3,
            R.drawable.ic_profile_outdoors_4, R.drawable.ic_profile_outdoors_5, R.drawable.ic_profile_outdoors_6,
            R.drawable.ic_profile_outdoors_7, R.drawable.ic_profile_outdoors_8, R.drawable.ic_profile_outdoors_9,

            R.drawable.ic_profile_meeting, R.drawable.ic_profile_meeting_2, R.drawable.ic_profile_meeting_3,
            R.drawable.ic_profile_mute, R.drawable.ic_profile_mute_2, R.drawable.ic_profile_volume_4,
            R.drawable.ic_profile_volume_1, R.drawable.ic_profile_volume_2, R.drawable.ic_profile_volume_3,
            R.drawable.ic_profile_vibrate_1,

            R.drawable.ic_profile_work_1, R.drawable.ic_profile_work_2, R.drawable.ic_profile_work_12,
            R.drawable.ic_profile_work_3, R.drawable.ic_profile_work_4, R.drawable.ic_profile_work_5,
            R.drawable.ic_profile_work_6, R.drawable.ic_profile_work_7, R.drawable.ic_profile_work_8,
            R.drawable.ic_profile_work_9, R.drawable.ic_profile_work_10, R.drawable.ic_profile_work_11,
            R.drawable.ic_profile_work_13, R.drawable.ic_profile_work_14, R.drawable.ic_profile_work_15,

            R.drawable.ic_profile_sleep, R.drawable.ic_profile_sleep_2, R.drawable.ic_profile_night,
            R.drawable.ic_profile_call_1, R.drawable.ic_profile_food_1, R.drawable.ic_profile_food_2,
            R.drawable.ic_profile_food_3, R.drawable.ic_profile_food_4, R.drawable.ic_profile_food_5,
            R.drawable.ic_profile_alarm,

            R.drawable.ic_profile_car_1, R.drawable.ic_profile_car_2, R.drawable.ic_profile_car_3,
            R.drawable.ic_profile_car_4, R.drawable.ic_profile_car_5, R.drawable.ic_profile_car_6,
            R.drawable.ic_profile_car_7, R.drawable.ic_profile_car_8, R.drawable.ic_profile_car_9,
            R.drawable.ic_profile_car_10, R.drawable.ic_profile_airplane_1, R.drawable.ic_profile_airplane_2,
            R.drawable.ic_profile_airplane_3, R.drawable.ic_profile_ship_1, R.drawable.ic_profile_ship_2,
            R.drawable.ic_profile_ship_3, R.drawable.ic_profile_tram_1, R.drawable.ic_profile_tickets_1,
            R.drawable.ic_profile_tickets_2,

            R.drawable.ic_profile_culture_1, R.drawable.ic_profile_culture_6, R.drawable.ic_profile_culture_7,
            R.drawable.ic_profile_culture_2, R.drawable.ic_profile_culture_8, R.drawable.ic_profile_culture_9,
            R.drawable.ic_profile_culture_3, R.drawable.ic_profile_culture_10, R.drawable.ic_profile_culture_11,
            R.drawable.ic_profile_culture_12, R.drawable.ic_profile_culture_13, R.drawable.ic_profile_culture_5,
            R.drawable.ic_profile_culture_14, R.drawable.ic_profile_culture_4, R.drawable.ic_profile_culture_15,
            R.drawable.ic_profile_culture_16,

            R.drawable.ic_profile_battery_1, R.drawable.ic_profile_battery_2, R.drawable.ic_profile_battery_3,

            R.drawable.ic_profile_lock
    };

    static final int[] profileIconColor = {
            // default
            0xff1c9cd7,

            // home
            0xff99cc00, 0xff99cc00, 0xff99cc00, 0xff99cc00, 0xff99cc00,
            0xff99cc00,

            // outdoors
            0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33,
            0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33,
            0xffffbc33,

            // meeting, volume
            0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000,
            0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000,

            // work
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,

            // sleep, food, alarm
            0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc,
            0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc,

            // car, airplane, ship, tickets
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174,

            // culture
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,

            // battery
            0xffdb3514, 0xffdb9714, 0xff2aa561,

            //lock
            0xff6a3e18
    };

    static final HashMap<String, Integer> profileIconIdMap;
    static {
        profileIconIdMap = new HashMap<>();
        profileIconIdMap.put("ic_profile_default", R.drawable.ic_profile_default);
        profileIconIdMap.put("ic_profile_home", R.drawable.ic_profile_home);
        profileIconIdMap.put("ic_profile_home_2", R.drawable.ic_profile_home_2);
        profileIconIdMap.put("ic_profile_home_3", R.drawable.ic_profile_home_3);
        profileIconIdMap.put("ic_profile_home_4", R.drawable.ic_profile_home_4);
        profileIconIdMap.put("ic_profile_home_5", R.drawable.ic_profile_home_5);
        profileIconIdMap.put("ic_profile_home_6", R.drawable.ic_profile_home_6);
        profileIconIdMap.put("ic_profile_outdoors_1", R.drawable.ic_profile_outdoors_1);
        profileIconIdMap.put("ic_profile_outdoors_2", R.drawable.ic_profile_outdoors_2);
        profileIconIdMap.put("ic_profile_outdoors_3", R.drawable.ic_profile_outdoors_3);
        profileIconIdMap.put("ic_profile_outdoors_4", R.drawable.ic_profile_outdoors_4);
        profileIconIdMap.put("ic_profile_outdoors_5", R.drawable.ic_profile_outdoors_5);
        profileIconIdMap.put("ic_profile_outdoors_6", R.drawable.ic_profile_outdoors_6);
        profileIconIdMap.put("ic_profile_outdoors_7", R.drawable.ic_profile_outdoors_7);
        profileIconIdMap.put("ic_profile_outdoors_8", R.drawable.ic_profile_outdoors_8);
        profileIconIdMap.put("ic_profile_outdoors_9", R.drawable.ic_profile_outdoors_9);
        profileIconIdMap.put("ic_profile_meeting", R.drawable.ic_profile_meeting);
        profileIconIdMap.put("ic_profile_meeting_2", R.drawable.ic_profile_meeting_2);
        profileIconIdMap.put("ic_profile_meeting_3", R.drawable.ic_profile_meeting_3);
        profileIconIdMap.put("ic_profile_mute", R.drawable.ic_profile_mute);
        profileIconIdMap.put("ic_profile_mute_2", R.drawable.ic_profile_mute_2);
        profileIconIdMap.put("ic_profile_volume_4", R.drawable.ic_profile_volume_4);
        profileIconIdMap.put("ic_profile_volume_1", R.drawable.ic_profile_volume_1);
        profileIconIdMap.put("ic_profile_volume_2", R.drawable.ic_profile_volume_2);
        profileIconIdMap.put("ic_profile_volume_3", R.drawable.ic_profile_volume_3);
        profileIconIdMap.put("ic_profile_vibrate_1", R.drawable.ic_profile_vibrate_1);
        profileIconIdMap.put("ic_profile_work_1", R.drawable.ic_profile_work_1);
        profileIconIdMap.put("ic_profile_work_2", R.drawable.ic_profile_work_2);
        profileIconIdMap.put("ic_profile_work_12", R.drawable.ic_profile_work_12);
        profileIconIdMap.put("ic_profile_work_3", R.drawable.ic_profile_work_3);
        profileIconIdMap.put("ic_profile_work_4", R.drawable.ic_profile_work_4);
        profileIconIdMap.put("ic_profile_work_5", R.drawable.ic_profile_work_5);
        profileIconIdMap.put("ic_profile_work_6", R.drawable.ic_profile_work_6);
        profileIconIdMap.put("ic_profile_work_7", R.drawable.ic_profile_work_7);
        profileIconIdMap.put("ic_profile_work_8", R.drawable.ic_profile_work_8);
        profileIconIdMap.put("ic_profile_work_9", R.drawable.ic_profile_work_9);
        profileIconIdMap.put("ic_profile_work_10", R.drawable.ic_profile_work_10);
        profileIconIdMap.put("ic_profile_work_11", R.drawable.ic_profile_work_11);
        profileIconIdMap.put("ic_profile_work_13", R.drawable.ic_profile_work_13);
        profileIconIdMap.put("ic_profile_work_14", R.drawable.ic_profile_work_14);
        profileIconIdMap.put("ic_profile_work_15", R.drawable.ic_profile_work_15);
        profileIconIdMap.put("ic_profile_sleep", R.drawable.ic_profile_sleep);
        profileIconIdMap.put("ic_profile_sleep_2", R.drawable.ic_profile_sleep_2);
        profileIconIdMap.put("ic_profile_night", R.drawable.ic_profile_night);
        profileIconIdMap.put("ic_profile_call_1", R.drawable.ic_profile_call_1);
        profileIconIdMap.put("ic_profile_food_1", R.drawable.ic_profile_food_1);
        profileIconIdMap.put("ic_profile_food_2", R.drawable.ic_profile_food_2);
        profileIconIdMap.put("ic_profile_food_3", R.drawable.ic_profile_food_3);
        profileIconIdMap.put("ic_profile_food_4", R.drawable.ic_profile_food_4);
        profileIconIdMap.put("ic_profile_food_5", R.drawable.ic_profile_food_5);
        profileIconIdMap.put("ic_profile_alarm", R.drawable.ic_profile_alarm);
        profileIconIdMap.put("ic_profile_car_1", R.drawable.ic_profile_car_1);
        profileIconIdMap.put("ic_profile_car_2", R.drawable.ic_profile_car_2);
        profileIconIdMap.put("ic_profile_car_3", R.drawable.ic_profile_car_3);
        profileIconIdMap.put("ic_profile_car_4", R.drawable.ic_profile_car_4);
        profileIconIdMap.put("ic_profile_car_5", R.drawable.ic_profile_car_5);
        profileIconIdMap.put("ic_profile_car_6", R.drawable.ic_profile_car_6);
        profileIconIdMap.put("ic_profile_car_7", R.drawable.ic_profile_car_7);
        profileIconIdMap.put("ic_profile_car_8", R.drawable.ic_profile_car_8);
        profileIconIdMap.put("ic_profile_car_9", R.drawable.ic_profile_car_9);
        profileIconIdMap.put("ic_profile_car_10", R.drawable.ic_profile_car_10);
        profileIconIdMap.put("ic_profile_airplane_1", R.drawable.ic_profile_airplane_1);
        profileIconIdMap.put("ic_profile_airplane_2", R.drawable.ic_profile_airplane_2);
        profileIconIdMap.put("ic_profile_airplane_3", R.drawable.ic_profile_airplane_3);
        profileIconIdMap.put("ic_profile_ship_1", R.drawable.ic_profile_ship_1);
        profileIconIdMap.put("ic_profile_ship_2", R.drawable.ic_profile_ship_2);
        profileIconIdMap.put("ic_profile_ship_3", R.drawable.ic_profile_ship_3);
        profileIconIdMap.put("ic_profile_tram_1", R.drawable.ic_profile_tram_1);
        profileIconIdMap.put("ic_profile_tickets_1", R.drawable.ic_profile_tickets_1);
        profileIconIdMap.put("ic_profile_tickets_2", R.drawable.ic_profile_tickets_2);
        profileIconIdMap.put("ic_profile_culture_1", R.drawable.ic_profile_culture_1);
        profileIconIdMap.put("ic_profile_culture_6", R.drawable.ic_profile_culture_6);
        profileIconIdMap.put("ic_profile_culture_7", R.drawable.ic_profile_culture_7);
        profileIconIdMap.put("ic_profile_culture_2", R.drawable.ic_profile_culture_2);
        profileIconIdMap.put("ic_profile_culture_8", R.drawable.ic_profile_culture_8);
        profileIconIdMap.put("ic_profile_culture_9", R.drawable.ic_profile_culture_9);
        profileIconIdMap.put("ic_profile_culture_3", R.drawable.ic_profile_culture_3);
        profileIconIdMap.put("ic_profile_culture_10", R.drawable.ic_profile_culture_10);
        profileIconIdMap.put("ic_profile_culture_11", R.drawable.ic_profile_culture_11);
        profileIconIdMap.put("ic_profile_culture_12", R.drawable.ic_profile_culture_12);
        profileIconIdMap.put("ic_profile_culture_13", R.drawable.ic_profile_culture_13);
        profileIconIdMap.put("ic_profile_culture_5", R.drawable.ic_profile_culture_5);
        profileIconIdMap.put("ic_profile_culture_14", R.drawable.ic_profile_culture_14);
        profileIconIdMap.put("ic_profile_culture_4", R.drawable.ic_profile_culture_4);
        profileIconIdMap.put("ic_profile_culture_15", R.drawable.ic_profile_culture_15);
        profileIconIdMap.put("ic_profile_culture_16", R.drawable.ic_profile_culture_16);
        profileIconIdMap.put("ic_profile_battery_1", R.drawable.ic_profile_battery_1);
        profileIconIdMap.put("ic_profile_battery_2", R.drawable.ic_profile_battery_2);
        profileIconIdMap.put("ic_profile_battery_3", R.drawable.ic_profile_battery_3);
        profileIconIdMap.put("ic_profile_lock", R.drawable.ic_profile_lock);
    }

    static final HashMap<String, Integer> profileIconNotifyId;
    static {
        profileIconNotifyId = new HashMap<>();
        profileIconNotifyId.put("ic_profile_default", R.drawable.ic_profile_default_notify);
        profileIconNotifyId.put("ic_profile_home", R.drawable.ic_profile_home_notify);
        profileIconNotifyId.put("ic_profile_home_2", R.drawable.ic_profile_home_2_notify);
        profileIconNotifyId.put("ic_profile_home_3", R.drawable.ic_profile_home_3_notify);
        profileIconNotifyId.put("ic_profile_home_4", R.drawable.ic_profile_home_4_notify);
        profileIconNotifyId.put("ic_profile_home_5", R.drawable.ic_profile_home_5_notify);
        profileIconNotifyId.put("ic_profile_home_6", R.drawable.ic_profile_home_6_notify);
        profileIconNotifyId.put("ic_profile_outdoors_1", R.drawable.ic_profile_outdoors_1_notify);
        profileIconNotifyId.put("ic_profile_outdoors_2", R.drawable.ic_profile_outdoors_2_notify);
        profileIconNotifyId.put("ic_profile_outdoors_3", R.drawable.ic_profile_outdoors_3_notify);
        profileIconNotifyId.put("ic_profile_outdoors_4", R.drawable.ic_profile_outdoors_4_notify);
        profileIconNotifyId.put("ic_profile_outdoors_5", R.drawable.ic_profile_outdoors_5_notify);
        profileIconNotifyId.put("ic_profile_outdoors_6", R.drawable.ic_profile_outdoors_6_notify);
        profileIconNotifyId.put("ic_profile_outdoors_7", R.drawable.ic_profile_outdoors_7_notify);
        profileIconNotifyId.put("ic_profile_outdoors_8", R.drawable.ic_profile_outdoors_8_notify);
        profileIconNotifyId.put("ic_profile_outdoors_9", R.drawable.ic_profile_outdoors_9_notify);
        profileIconNotifyId.put("ic_profile_meeting", R.drawable.ic_profile_meeting_notify);
        profileIconNotifyId.put("ic_profile_meeting_2", R.drawable.ic_profile_meeting_2_notify);
        profileIconNotifyId.put("ic_profile_meeting_3", R.drawable.ic_profile_meeting_3_notify);
        profileIconNotifyId.put("ic_profile_mute", R.drawable.ic_profile_mute_notify);
        profileIconNotifyId.put("ic_profile_mute_2", R.drawable.ic_profile_mute_2_notify);
        profileIconNotifyId.put("ic_profile_volume_4", R.drawable.ic_profile_volume_4_notify);
        profileIconNotifyId.put("ic_profile_volume_1", R.drawable.ic_profile_volume_1_notify);
        profileIconNotifyId.put("ic_profile_volume_2", R.drawable.ic_profile_volume_2_notify);
        profileIconNotifyId.put("ic_profile_volume_3", R.drawable.ic_profile_volume_3_notify);
        profileIconNotifyId.put("ic_profile_vibrate_1", R.drawable.ic_profile_vibrate_1_notify);
        profileIconNotifyId.put("ic_profile_work_1", R.drawable.ic_profile_work_1_notify);
        profileIconNotifyId.put("ic_profile_work_2", R.drawable.ic_profile_work_2_notify);
        profileIconNotifyId.put("ic_profile_work_12", R.drawable.ic_profile_work_12_notify);
        profileIconNotifyId.put("ic_profile_work_3", R.drawable.ic_profile_work_3_notify);
        profileIconNotifyId.put("ic_profile_work_4", R.drawable.ic_profile_work_4_notify);
        profileIconNotifyId.put("ic_profile_work_5", R.drawable.ic_profile_work_5_notify);
        profileIconNotifyId.put("ic_profile_work_6", R.drawable.ic_profile_work_6_notify);
        profileIconNotifyId.put("ic_profile_work_7", R.drawable.ic_profile_work_7_notify);
        profileIconNotifyId.put("ic_profile_work_8", R.drawable.ic_profile_work_8_notify);
        profileIconNotifyId.put("ic_profile_work_9", R.drawable.ic_profile_work_9_notify);
        profileIconNotifyId.put("ic_profile_work_10", R.drawable.ic_profile_work_10_notify);
        profileIconNotifyId.put("ic_profile_work_11", R.drawable.ic_profile_work_11_notify);
        profileIconNotifyId.put("ic_profile_work_13", R.drawable.ic_profile_work_13_notify);
        profileIconNotifyId.put("ic_profile_work_14", R.drawable.ic_profile_work_14_notify);
        profileIconNotifyId.put("ic_profile_work_15", R.drawable.ic_profile_work_15_notify);
        profileIconNotifyId.put("ic_profile_sleep", R.drawable.ic_profile_sleep_notify);
        profileIconNotifyId.put("ic_profile_sleep_2", R.drawable.ic_profile_sleep_2_notify);
        profileIconNotifyId.put("ic_profile_night", R.drawable.ic_profile_night_notify);
        profileIconNotifyId.put("ic_profile_call_1", R.drawable.ic_profile_call_1_notify);
        profileIconNotifyId.put("ic_profile_food_1", R.drawable.ic_profile_food_1_notify);
        profileIconNotifyId.put("ic_profile_food_2", R.drawable.ic_profile_food_2_notify);
        profileIconNotifyId.put("ic_profile_food_3", R.drawable.ic_profile_food_3_notify);
        profileIconNotifyId.put("ic_profile_food_4", R.drawable.ic_profile_food_4_notify);
        profileIconNotifyId.put("ic_profile_food_5", R.drawable.ic_profile_food_5_notify);
        profileIconNotifyId.put("ic_profile_alarm", R.drawable.ic_profile_alarm_notify);
        profileIconNotifyId.put("ic_profile_car_1", R.drawable.ic_profile_car_1_notify);
        profileIconNotifyId.put("ic_profile_car_2", R.drawable.ic_profile_car_2_notify);
        profileIconNotifyId.put("ic_profile_car_3", R.drawable.ic_profile_car_3_notify);
        profileIconNotifyId.put("ic_profile_car_4", R.drawable.ic_profile_car_4_notify);
        profileIconNotifyId.put("ic_profile_car_5", R.drawable.ic_profile_car_5_notify);
        profileIconNotifyId.put("ic_profile_car_6", R.drawable.ic_profile_car_6_notify);
        profileIconNotifyId.put("ic_profile_car_7", R.drawable.ic_profile_car_7_notify);
        profileIconNotifyId.put("ic_profile_car_8", R.drawable.ic_profile_car_8_notify);
        profileIconNotifyId.put("ic_profile_car_9", R.drawable.ic_profile_car_9_notify);
        profileIconNotifyId.put("ic_profile_car_10", R.drawable.ic_profile_car_10_notify);
        profileIconNotifyId.put("ic_profile_airplane_1", R.drawable.ic_profile_airplane_1_notify);
        profileIconNotifyId.put("ic_profile_airplane_2", R.drawable.ic_profile_airplane_2_notify);
        profileIconNotifyId.put("ic_profile_airplane_3", R.drawable.ic_profile_airplane_3_notify);
        profileIconNotifyId.put("ic_profile_ship_1", R.drawable.ic_profile_ship_1_notify);
        profileIconNotifyId.put("ic_profile_ship_2", R.drawable.ic_profile_ship_2_notify);
        profileIconNotifyId.put("ic_profile_ship_3", R.drawable.ic_profile_ship_3_notify);
        profileIconNotifyId.put("ic_profile_tram_1", R.drawable.ic_profile_tram_1_notify);
        profileIconNotifyId.put("ic_profile_tickets_1", R.drawable.ic_profile_tickets_1_notify);
        profileIconNotifyId.put("ic_profile_tickets_2", R.drawable.ic_profile_tickets_2_notify);
        profileIconNotifyId.put("ic_profile_culture_1", R.drawable.ic_profile_culture_1_notify);
        profileIconNotifyId.put("ic_profile_culture_6", R.drawable.ic_profile_culture_6_notify);
        profileIconNotifyId.put("ic_profile_culture_7", R.drawable.ic_profile_culture_7_notify);
        profileIconNotifyId.put("ic_profile_culture_2", R.drawable.ic_profile_culture_2_notify);
        profileIconNotifyId.put("ic_profile_culture_8", R.drawable.ic_profile_culture_8_notify);
        profileIconNotifyId.put("ic_profile_culture_9", R.drawable.ic_profile_culture_9_notify);
        profileIconNotifyId.put("ic_profile_culture_3", R.drawable.ic_profile_culture_3_notify);
        profileIconNotifyId.put("ic_profile_culture_10", R.drawable.ic_profile_culture_10_notify);
        profileIconNotifyId.put("ic_profile_culture_11", R.drawable.ic_profile_culture_11_notify);
        profileIconNotifyId.put("ic_profile_culture_12", R.drawable.ic_profile_culture_12_notify);
        profileIconNotifyId.put("ic_profile_culture_13", R.drawable.ic_profile_culture_13_notify);
        profileIconNotifyId.put("ic_profile_culture_5", R.drawable.ic_profile_culture_5_notify);
        profileIconNotifyId.put("ic_profile_culture_14", R.drawable.ic_profile_culture_14_notify);
        profileIconNotifyId.put("ic_profile_culture_4", R.drawable.ic_profile_culture_4_notify);
        profileIconNotifyId.put("ic_profile_culture_15", R.drawable.ic_profile_culture_15_notify);
        profileIconNotifyId.put("ic_profile_culture_16", R.drawable.ic_profile_culture_16_notify);
        profileIconNotifyId.put("ic_profile_battery_1", R.drawable.ic_profile_battery_1_notify);
        profileIconNotifyId.put("ic_profile_battery_2", R.drawable.ic_profile_battery_2_notify);
        profileIconNotifyId.put("ic_profile_battery_3", R.drawable.ic_profile_battery_3_notify);
        profileIconNotifyId.put("ic_profile_lock", R.drawable.ic_profile_lock_notify);
    }

    static final HashMap<String, Integer> profileIconNotifyColorId;
    static {
        profileIconNotifyColorId = new HashMap<>();
        profileIconNotifyColorId.put("ic_profile_default", R.drawable.ic_profile_default_notify_color);
        profileIconNotifyColorId.put("ic_profile_home", R.drawable.ic_profile_home_notify_color);
        profileIconNotifyColorId.put("ic_profile_home_2", R.drawable.ic_profile_home_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_home_3", R.drawable.ic_profile_home_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_home_4", R.drawable.ic_profile_home_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_home_5", R.drawable.ic_profile_home_5_notify_color);
        profileIconNotifyColorId.put("ic_profile_home_6", R.drawable.ic_profile_home_6_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_1", R.drawable.ic_profile_outdoors_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_2", R.drawable.ic_profile_outdoors_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_3", R.drawable.ic_profile_outdoors_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_4", R.drawable.ic_profile_outdoors_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_5", R.drawable.ic_profile_outdoors_5_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_6", R.drawable.ic_profile_outdoors_6_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_7", R.drawable.ic_profile_outdoors_7_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_8", R.drawable.ic_profile_outdoors_8_notify_color);
        profileIconNotifyColorId.put("ic_profile_outdoors_9", R.drawable.ic_profile_outdoors_9_notify_color);
        profileIconNotifyColorId.put("ic_profile_meeting", R.drawable.ic_profile_meeting_notify_color);
        profileIconNotifyColorId.put("ic_profile_meeting_2", R.drawable.ic_profile_meeting_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_meeting_3", R.drawable.ic_profile_meeting_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_mute", R.drawable.ic_profile_mute_notify_color);
        profileIconNotifyColorId.put("ic_profile_mute_2", R.drawable.ic_profile_mute_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_volume_4", R.drawable.ic_profile_volume_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_volume_1", R.drawable.ic_profile_volume_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_volume_2", R.drawable.ic_profile_volume_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_volume_3", R.drawable.ic_profile_volume_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_vibrate_1", R.drawable.ic_profile_vibrate_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_1", R.drawable.ic_profile_work_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_2", R.drawable.ic_profile_work_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_12", R.drawable.ic_profile_work_12_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_3", R.drawable.ic_profile_work_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_4", R.drawable.ic_profile_work_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_5", R.drawable.ic_profile_work_5_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_6", R.drawable.ic_profile_work_6_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_7", R.drawable.ic_profile_work_7_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_8", R.drawable.ic_profile_work_8_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_9", R.drawable.ic_profile_work_9_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_10", R.drawable.ic_profile_work_10_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_11", R.drawable.ic_profile_work_11_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_13", R.drawable.ic_profile_work_13_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_14", R.drawable.ic_profile_work_14_notify_color);
        profileIconNotifyColorId.put("ic_profile_work_15", R.drawable.ic_profile_work_15_notify_color);
        profileIconNotifyColorId.put("ic_profile_sleep", R.drawable.ic_profile_sleep_notify_color);
        profileIconNotifyColorId.put("ic_profile_sleep_2", R.drawable.ic_profile_sleep_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_night", R.drawable.ic_profile_night_notify_color);
        profileIconNotifyColorId.put("ic_profile_call_1", R.drawable.ic_profile_call_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_food_1", R.drawable.ic_profile_food_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_food_2", R.drawable.ic_profile_food_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_food_3", R.drawable.ic_profile_food_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_food_4", R.drawable.ic_profile_food_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_food_5", R.drawable.ic_profile_food_5_notify_color);
        profileIconNotifyColorId.put("ic_profile_alarm", R.drawable.ic_profile_alarm_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_1", R.drawable.ic_profile_car_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_2", R.drawable.ic_profile_car_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_3", R.drawable.ic_profile_car_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_4", R.drawable.ic_profile_car_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_5", R.drawable.ic_profile_car_5_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_6", R.drawable.ic_profile_car_6_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_7", R.drawable.ic_profile_car_7_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_8", R.drawable.ic_profile_car_8_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_9", R.drawable.ic_profile_car_9_notify_color);
        profileIconNotifyColorId.put("ic_profile_car_10", R.drawable.ic_profile_car_10_notify_color);
        profileIconNotifyColorId.put("ic_profile_airplane_1", R.drawable.ic_profile_airplane_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_airplane_2", R.drawable.ic_profile_airplane_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_airplane_3", R.drawable.ic_profile_airplane_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_ship_1", R.drawable.ic_profile_ship_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_ship_2", R.drawable.ic_profile_ship_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_ship_3", R.drawable.ic_profile_ship_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_tram_1", R.drawable.ic_profile_tram_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_tickets_1", R.drawable.ic_profile_tickets_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_tickets_2", R.drawable.ic_profile_tickets_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_1", R.drawable.ic_profile_culture_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_6", R.drawable.ic_profile_culture_6_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_7", R.drawable.ic_profile_culture_7_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_2", R.drawable.ic_profile_culture_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_8", R.drawable.ic_profile_culture_8_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_9", R.drawable.ic_profile_culture_9_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_3", R.drawable.ic_profile_culture_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_10", R.drawable.ic_profile_culture_10_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_11", R.drawable.ic_profile_culture_11_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_12", R.drawable.ic_profile_culture_12_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_13", R.drawable.ic_profile_culture_13_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_5", R.drawable.ic_profile_culture_5_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_14", R.drawable.ic_profile_culture_14_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_4", R.drawable.ic_profile_culture_4_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_15", R.drawable.ic_profile_culture_15_notify_color);
        profileIconNotifyColorId.put("ic_profile_culture_16", R.drawable.ic_profile_culture_16_notify_color);
        profileIconNotifyColorId.put("ic_profile_battery_1", R.drawable.ic_profile_battery_1_notify_color);
        profileIconNotifyColorId.put("ic_profile_battery_2", R.drawable.ic_profile_battery_2_notify_color);
        profileIconNotifyColorId.put("ic_profile_battery_3", R.drawable.ic_profile_battery_3_notify_color);
        profileIconNotifyColorId.put("ic_profile_lock", R.drawable.ic_profile_lock_notify_color);
    }

    // Empty constructor
    public Profile(){
        this._iconBitmap = null;
    }

    // constructor
    public Profile(long id,
                   String name,
                   String icon,
                   Boolean checked,
                   int porder,
                   int volumeRingerMode,
                   String volumeRingtone,
                   String volumeNotification,
                   String volumeMedia,
                   String volumeAlarm,
                   String volumeSystem,
                   String volumeVoice,
                   int soundRingtoneChange,
                   String soundRingtone,
                   int soundNotificationChange,
                   String soundNotification,
                   int soundAlarmChange,
                   String soundAlarm,
                   int deviceAirplaneMode,
                   int deviceWiFi,
                   int deviceBluetooth,
                   int deviceScreenTimeout,
                   String deviceBrightness,
                   int deviceWallpaperChange,
                   String deviceWallpaper,
                   int deviceMobileData,
                   int deviceMobileDataPrefs,
                   int deviceGPS,
                   int deviceRunApplicationChange,
                   String deviceRunApplicationPackageName,
                   int deviceAutoSync,
                   int deviceAutoRotate,
                   int deviceLocationServicePrefs,
                   int volumeSpeakerPhone,
                   int deviceNFC,
                   int duration,
                   int afterDurationDo,
                   int volumeZenMode,
                   int deviceKeyguard,
                   int vibrationOnTouch,
                   int deviceWiFiAP,
                   int devicePowerSaveMode,
                   boolean askForDuration,
                   int deviceNetworkType,
                   int notificationLed,
                   int vibrateWhenRinging,
                   int deviceWallpaperFor,
                   boolean hideStatusBarIcon,
                   int lockDevice,
                   String deviceConnectToSSID,
                   String durationNotificationSound,
                   boolean durationNotificationVibrate,
                   int deviceWiFiAPPrefs,
                   int headsUpNotifications,
                   int deviceForceStopApplicationChange,
                   String deviceForceStopApplicationPackageName,
                   long activationByUserCount,
                   int deviceNetworkTypePrefs,
                   int deviceCloseAllApplications,
                   int screenNightMode,
                   int dtmfToneWhenDialing,
                   int soundOnTouch,
                   String volumeDTMF,
                   String volumeAccessibility,
                   String volumeBluetoothSCO)
    {
        this._id = id;
        this._name = name;
        this._icon = icon;
        this._checked = checked;
        this._porder = porder;
        this._volumeRingerMode = volumeRingerMode;
        this._volumeZenMode = volumeZenMode;
        this._volumeRingtone = volumeRingtone;
        this._volumeNotification = volumeNotification;
        this._volumeMedia = volumeMedia;
        this._volumeAlarm = volumeAlarm;
        this._volumeSystem = volumeSystem;
        this._volumeVoice = volumeVoice;
        this._soundRingtoneChange = soundRingtoneChange;
        this._soundRingtone = soundRingtone;
        this._soundNotificationChange = soundNotificationChange;
        this._soundNotification = soundNotification;
        this._soundAlarmChange = soundAlarmChange;
        this._soundAlarm = soundAlarm;
        this._deviceAirplaneMode = deviceAirplaneMode;
        this._deviceMobileData = deviceMobileData;
        this._deviceMobileDataPrefs = deviceMobileDataPrefs;
        this._deviceWiFi = deviceWiFi;
        this._deviceBluetooth = deviceBluetooth;
        this._deviceGPS = deviceGPS;
        this._deviceScreenTimeout = deviceScreenTimeout;
        this._deviceBrightness = deviceBrightness;
        this._deviceWallpaperChange = deviceWallpaperChange;
        this._deviceWallpaper = deviceWallpaper;
        this._deviceRunApplicationChange = deviceRunApplicationChange;
        this._deviceRunApplicationPackageName = deviceRunApplicationPackageName;
        this._deviceAutoSync = deviceAutoSync;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._askForDuration = askForDuration;
        this._durationNotificationSound = durationNotificationSound;
        this._durationNotificationVibrate = durationNotificationVibrate;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWiFiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;
        this._hideStatusBarIcon = hideStatusBarIcon;
        this._lockDevice = lockDevice;
        this._deviceConnectToSSID = deviceConnectToSSID;
        this._deviceWiFiAPPrefs = deviceWiFiAPPrefs;
        this._headsUpNotifications = headsUpNotifications;
        this._deviceForceStopApplicationChange = deviceForceStopApplicationChange;
        this._deviceForceStopApplicationPackageName = deviceForceStopApplicationPackageName;
        this._deviceNetworkTypePrefs = deviceNetworkTypePrefs;
        this._deviceCloseAllApplications = deviceCloseAllApplications;
        this._screenNightMode = screenNightMode;
        this._dtmfToneWhenDialing = dtmfToneWhenDialing;
        this._soundOnTouch = soundOnTouch;
        this._volumeDTMF = volumeDTMF;
        this._volumeAccessibility = volumeAccessibility;
        this._volumeBluetoothSCO = volumeBluetoothSCO;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
        this._activationByUserCount = activationByUserCount;
    }

    // constructor
    public Profile(String name,
                   String icon,
                   Boolean checked,
                   int porder,
                   int volumeRingerMode,
                   String volumeRingtone,
                   String volumeNotification,
                   String volumeMedia,
                   String volumeAlarm,
                   String volumeSystem,
                   String volumeVoice,
                   int soundRingtoneChange,
                   String soundRingtone,
                   int soundNotificationChange,
                   String soundNotification,
                   int soundAlarmChange,
                   String soundAlarm,
                   int deviceAirplaneMode,
                   int deviceWiFi,
                   int deviceBluetooth,
                   int deviceScreenTimeout,
                   String deviceBrightness,
                   int deviceWallpaperChange,
                   String deviceWallpaper,
                   int deviceMobileData,
                   int deviceMobileDataPrefs,
                   int deviceGPS,
                   int deviceRunApplicationChange,
                   String deviceRunApplicationPackageName,
                   int deviceAutoSync,
                   int deviceAutoRotate,
                   int deviceLocationServicePrefs,
                   int volumeSpeakerPhone,
                   int deviceNFC,
                   int duration,
                   int afterDurationDo,
                   int volumeZenMode,
                   int deviceKeyguard,
                   int vibrationOnTouch,
                   int deviceWiFiAP,
                   int devicePowerSaveMode,
                   boolean askForDuration,
                   int deviceNetworkType,
                   int notificationLed,
                   int vibrateWhenRinging,
                   int deviceWallpaperFor,
                   boolean hideStatusBarIcon,
                   int lockDevice,
                   String deviceConnectToSSID,
                   String durationNotificationSound,
                   boolean durationNotificationVibrate,
                   int deviceWiFiAPPrefs,
                   int headsUpNotifications,
                   int deviceForceStopApplicationChange,
                   String deviceForceStopApplicationPackageName,
                   long activationByUserCount,
                   int deviceNetworkTypePrefs,
                   int deviceCloseAllApplications,
                   int screenNightMode,
                   int dtmfToneWhenDialing,
                   int soundOnTouch,
                   String volumeDTMF,
                   String volumeAccessibility,
                   String volumeBluetoothSCO)
    {
        this._name = name;
        this._icon = icon;
        this._checked = checked;
        this._porder = porder;
        this._volumeRingerMode = volumeRingerMode;
        this._volumeZenMode = volumeZenMode;
        this._volumeRingtone = volumeRingtone;
        this._volumeNotification = volumeNotification;
        this._volumeMedia = volumeMedia;
        this._volumeAlarm = volumeAlarm;
        this._volumeSystem = volumeSystem;
        this._volumeVoice = volumeVoice;
        this._soundRingtoneChange = soundRingtoneChange;
        this._soundRingtone = soundRingtone;
        this._soundNotificationChange = soundNotificationChange;
        this._soundNotification = soundNotification;
        this._soundAlarmChange = soundAlarmChange;
        this._soundAlarm = soundAlarm;
        this._deviceAirplaneMode = deviceAirplaneMode;
        this._deviceMobileData = deviceMobileData;
        this._deviceMobileDataPrefs = deviceMobileDataPrefs;
        this._deviceWiFi = deviceWiFi;
        this._deviceBluetooth = deviceBluetooth;
        this._deviceGPS = deviceGPS;
        this._deviceScreenTimeout = deviceScreenTimeout;
        this._deviceBrightness = deviceBrightness;
        this._deviceWallpaperChange = deviceWallpaperChange;
        this._deviceWallpaper = deviceWallpaper;
        this._deviceRunApplicationChange = deviceRunApplicationChange;
        this._deviceRunApplicationPackageName = deviceRunApplicationPackageName;
        this._deviceAutoSync = deviceAutoSync;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._askForDuration = askForDuration;
        this._durationNotificationSound = durationNotificationSound;
        this._durationNotificationVibrate = durationNotificationVibrate;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWiFiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;
        this._hideStatusBarIcon = hideStatusBarIcon;
        this._lockDevice = lockDevice;
        this._deviceConnectToSSID = deviceConnectToSSID;
        this._deviceWiFiAPPrefs = deviceWiFiAPPrefs;
        this._headsUpNotifications = headsUpNotifications;
        this._deviceForceStopApplicationChange = deviceForceStopApplicationChange;
        this._deviceForceStopApplicationPackageName = deviceForceStopApplicationPackageName;
        this._deviceNetworkTypePrefs = deviceNetworkTypePrefs;
        this._deviceCloseAllApplications = deviceCloseAllApplications;
        this._screenNightMode = screenNightMode;
        this._dtmfToneWhenDialing = dtmfToneWhenDialing;
        this._soundOnTouch = soundOnTouch;
        this._volumeDTMF = volumeDTMF;
        this._volumeAccessibility = volumeAccessibility;
        this._volumeBluetoothSCO = volumeBluetoothSCO;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
        this._activationByUserCount = activationByUserCount;
    }

    void copyProfile(Profile profile)
    {
        this._id = profile._id;
        this._name = profile._name;
        this._icon = profile._icon;
        this._checked = profile._checked;
        this._porder = profile._porder;
        this._volumeRingerMode = profile._volumeRingerMode;
        this._volumeZenMode = profile._volumeZenMode;
        this._volumeRingtone = profile._volumeRingtone;
        this._volumeNotification = profile._volumeNotification;
        this._volumeMedia = profile._volumeMedia;
        this._volumeAlarm = profile._volumeAlarm;
        this._volumeSystem = profile._volumeSystem;
        this._volumeVoice = profile._volumeVoice;
        this._soundRingtoneChange = profile._soundRingtoneChange;
        this._soundRingtone = profile._soundRingtone;
        this._soundNotificationChange = profile._soundNotificationChange;
        this._soundNotification = profile._soundNotification;
        this._soundAlarmChange = profile._soundAlarmChange;
        this._soundAlarm = profile._soundAlarm;
        this._deviceAirplaneMode = profile._deviceAirplaneMode;
        this._deviceMobileData = profile._deviceMobileData;
        this._deviceMobileDataPrefs = profile._deviceMobileDataPrefs;
        this._deviceWiFi = profile._deviceWiFi;
        this._deviceBluetooth = profile._deviceBluetooth;
        this._deviceGPS = profile._deviceGPS;
        this._deviceScreenTimeout = profile._deviceScreenTimeout;
        this._deviceBrightness = profile._deviceBrightness;
        this._deviceWallpaperChange = profile._deviceWallpaperChange;
        this._deviceWallpaper = profile._deviceWallpaper;
        this._deviceRunApplicationChange = profile._deviceRunApplicationChange;
        this._deviceRunApplicationPackageName = profile._deviceRunApplicationPackageName;
        this._deviceAutoSync = profile._deviceAutoSync;
        this._deviceAutoRotate = profile._deviceAutoRotate;
        this._deviceLocationServicePrefs = profile._deviceLocationServicePrefs;
        this._volumeSpeakerPhone = profile._volumeSpeakerPhone;
        this._deviceNFC = profile._deviceNFC;
        this._duration = profile._duration;
        this._afterDurationDo = profile._afterDurationDo;
        this._askForDuration = profile._askForDuration;
        this._durationNotificationSound = profile._durationNotificationSound;
        this._durationNotificationVibrate = profile._durationNotificationVibrate;
        this._deviceKeyguard = profile._deviceKeyguard;
        this._vibrationOnTouch = profile._vibrationOnTouch;
        this._deviceWiFiAP = profile._deviceWiFiAP;
        this._devicePowerSaveMode = profile._devicePowerSaveMode;
        this._deviceNetworkType = profile._deviceNetworkType;
        this._notificationLed = profile._notificationLed;
        this._vibrateWhenRinging = profile._vibrateWhenRinging;
        this._deviceWallpaperFor = profile._deviceWallpaperFor;
        this._hideStatusBarIcon = profile._hideStatusBarIcon;
        this._lockDevice = profile._lockDevice;
        this._deviceConnectToSSID = profile._deviceConnectToSSID;
        this._deviceWiFiAPPrefs = profile._deviceWiFiAPPrefs;
        this._headsUpNotifications = profile._headsUpNotifications;
        this._deviceForceStopApplicationChange = profile._deviceForceStopApplicationChange;
        this._deviceForceStopApplicationPackageName = profile._deviceForceStopApplicationPackageName;
        this._deviceNetworkTypePrefs = profile._deviceNetworkTypePrefs;
        this._deviceCloseAllApplications = profile._deviceCloseAllApplications;
        this._screenNightMode = profile._screenNightMode;
        this._dtmfToneWhenDialing = profile._dtmfToneWhenDialing;
        this._soundOnTouch = profile._soundOnTouch;
        this._volumeDTMF = profile._volumeDTMF;
        this._volumeAccessibility = profile._volumeAccessibility;
        this._volumeBluetoothSCO = profile._volumeBluetoothSCO;

        this._iconBitmap = profile._iconBitmap;
        this._preferencesIndicator = profile._preferencesIndicator;
        this._activationByUserCount = profile._activationByUserCount;
    }

    // getting icon identifier
    public String getIconIdentifier()
    {
        String value;
        try {
            String[] splits = _icon.split("\\|");
            value = splits[0];
        } catch (Exception e) {
            value = "ic_profile_default";
        }
        return value;
    }

    // getting where icon is resource id
    public boolean getIsIconResourceID()
    {
        boolean value;
        try {
            String[] splits = _icon.split("\\|");
            value = splits[1].equals("1");

        } catch (Exception e) {
            value = true;
        }
        return value;
    }

    // getting where icon has custom color
    boolean getUseCustomColorForIcon() {
        boolean value;
        try {
            String[] splits = _icon.split("\\|");
            value = splits[2].equals("1");

        } catch (Exception e) {
            value = false;
        }
        return value;
    }

    // getting icon custom color
    private int getIconCustomColor() {
        int value;
        try {
            String[] splits = _icon.split("\\|");
            value = Integer.valueOf(splits[3]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    static int getVolumeRingtoneValue(String volumeRingtone)
    {
        int value;
        try {
            String[] splits = volumeRingtone.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    int getVolumeRingtoneValue() {
        return getVolumeRingtoneValue(_volumeRingtone);
    }

    static boolean getVolumeRingtoneChange(String volumeRingtone)
    {
        int value;
        try {
            String[] splits = volumeRingtone.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeRingtoneChange()
    {
        return getVolumeRingtoneChange(_volumeRingtone);
    }

    private boolean getVolumeRingtoneSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeRingtone.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void setVolumeRingtoneValue(@SuppressWarnings("SameParameterValue") int value) {

        try {
            String[] splits = _volumeRingtone.split("\\|");
            splits[0] = String.valueOf(value);
            _volumeRingtone = "";
            for (String split : splits) {
                if (!_volumeRingtone.isEmpty())
                    _volumeRingtone = _volumeRingtone + "|";
                _volumeRingtone = _volumeRingtone + split;
            }
        } catch (Exception ignore) {
        }
    }

    int getVolumeNotificationValue()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeNotificationChange()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeNotificationSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void setVolumeNotificationValue(@SuppressWarnings("SameParameterValue") int value) {

        try {
            String[] splits = _volumeNotification.split("\\|");
            splits[0] = String.valueOf(value);
            _volumeNotification = "";
            for (String split : splits) {
                if (!_volumeNotification.isEmpty())
                    _volumeNotification = _volumeNotification + "|";
                _volumeNotification = _volumeNotification + split;
            }
        } catch (Exception ignore) {
        }
    }

    int getVolumeMediaValue()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeMediaChange()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeMediaSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeAlarmValue()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeAlarmChange()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeAlarmSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeSystemValue()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeSystemChange()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeSystemSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeVoiceValue()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeVoiceChange()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeVoiceSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeDTMFValue()
    {
        int value;
        try {
            String[] splits = _volumeDTMF.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeDTMFChange()
    {
        int value;
        try {
            String[] splits = _volumeDTMF.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeDTMFSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeDTMF.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeAccessibilityValue()
    {
        int value;
        try {
            String[] splits = _volumeAccessibility.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeAccessibilityChange()
    {
        int value;
        try {
            String[] splits = _volumeAccessibility.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeAccessibilitySharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeAccessibility.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeBluetoothSCOValue()
    {
        int value;
        try {
            String[] splits = _volumeBluetoothSCO.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeBluetoothSCOChange()
    {
        int value;
        try {
            String[] splits = _volumeBluetoothSCO.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeBluetoothSCOSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeBluetoothSCO.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    private int getDeviceBrightnessValue()
    {
        int maximumValue = 100;
        int defaultValue = 50;
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[0]);
            if ((value < 0) || (value > maximumValue))
                value = defaultValue;
        } catch (Exception e) {
            value = defaultValue;
        }
        return value;
    }

    boolean getDeviceBrightnessChange()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private static boolean getDeviceBrightnessChange(String _deviceBrightness)
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getDeviceBrightnessSharedProfile()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[3]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    boolean getDeviceBrightnessAutomatic()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 1;
    }

    private static boolean getDeviceBrightnessAutomatic(String _deviceBrightness)
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 1;
    }

    boolean getDeviceBrightnessChangeLevel()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[4]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 1;
    }

    /*
    private static int getMinimumScreenBrightnessSetting ()
    {
        final Resources res = Resources.getSystem();
        int id = res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"); // API17+
        if (id == 0)
            id = res.getIdentifier("config_screenBrightnessDim", "integer", "android"); // lower API levels
        if (id != 0)
        {
            try {
                return res.getInteger(id);
            }
            catch (Exception ignored) {}
        }
        return 0;
    }

    private static int getMaximumScreenBrightnessSetting ()
    {*/
        /*final Resources res = Resources.getSystem();
        final int id = res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");  // API17+
        if (id != 0)
        {
            try {
                int value = res.getInteger(id);
                if (value > 255)
                    value = 255;
                return value;
            }
            catch (Resources.NotFoundException e) {
                // ignore
            }
        }*/
    /*    return 255;
    }
    */

    ////// from AOSP and changed for PP
    private static final int GAMMA_SPACE_MAX = 1023;

    private static int getBrightnessPercentage_A9(int settingsValue, int minValue, int maxValue) {
        final int value;

        value = convertLinearToGamma(settingsValue, minValue, maxValue);

        return Math.round(getPercentage(value, 0, GAMMA_SPACE_MAX) * 100);
    }

    private static int getBrightnessValue_A9(int percentage, int minValue, int maxValue) {
        int value = Math.round((GAMMA_SPACE_MAX+1) / 100f * (float)(percentage + 1));
        int systemValue = convertGammaToLinear(value, minValue, maxValue);
        if (systemValue > 255)
            systemValue = 255;
        return systemValue;
    }

    @SuppressWarnings("SameParameterValue")
    private static float getPercentage(int value, int min, int max) {
        if (value > max) {
            return 1.0f;
        }
        if (value < min) {
            return 0.0f;
        }
        return ((float)value - min) / (max - min);
    }

    // Hybrid Log Gamma constant values
    private static final float _R = 0.5f;
    private static final float _A = 0.17883277f;
    private static final float _B = 0.28466892f;
    private static final float _C = 0.55991073f;

    @SuppressWarnings("SameParameterValue")
    private static int convertLinearToGamma(int val, int min, int max) {
        // For some reason, HLG normalizes to the range [0, 12] rather than [0, 1]
        final float normalizedVal = MathUtils.norm(min, max, val) * 12;
        final float ret;
        if (normalizedVal <= 1f) {
            ret = MathUtils.sqrt(normalizedVal) * _R;
        } else {
            ret = _A * MathUtils.log(normalizedVal - _B) + _C;
        }
        return Math.round(MathUtils.lerp(0, GAMMA_SPACE_MAX, ret));
    }

    @SuppressWarnings("SameParameterValue")
    private static int convertGammaToLinear(int val, int min, int max) {
        final float normalizedVal = MathUtils.norm(0, GAMMA_SPACE_MAX, val);
        final float ret;
        if (normalizedVal <= _R) {
            ret = MathUtils.sq(normalizedVal / _R);
        } else {
            ret = MathUtils.exp((normalizedVal - _C) / _A) + _B;
        }
        // HLG is normalized to the range [0, 12], so we need to re-normalize to the range [0, 1]
        // in order to derive the correct setting value.
        return Math.round(MathUtils.lerp(min, max, ret / 12));
    }
    ///////////////

    static int convertPercentsToBrightnessManualValue(int percentage, Context context)
    {
        int maximumValue;// = getMaximumScreenBrightnessSetting();
        int minimumValue;// = getMinimumScreenBrightnessSetting();

        //PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "maximumValue="+maximumValue);
        //PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "minimumValue="+minimumValue);

        //if (maximumValue-minimumValue > 255) {
        minimumValue = 0;
        maximumValue = 255;
        //}

        int value;

        if (percentage == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET) {
            // brightness is not set, change it to default manual brightness value
            int defaultValue = 128;
            if ((Build.VERSION.SDK_INT >= 28) && (!PPApplication.romIsSamsung))
                defaultValue = getBrightnessValue_A9(50, minimumValue, maximumValue);
            value = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, defaultValue);
        }
        else {
            if (PPApplication.logEnabled()) {
                try {
                    int oldValue = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "oldValue=" + oldValue);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if ((Build.VERSION.SDK_INT < 28) || PPApplication.romIsSamsung)
                value = Math.round((float) (maximumValue - minimumValue) / 100 * percentage) + minimumValue;
            else {
                value = getBrightnessValue_A9(percentage, minimumValue, maximumValue);
            }
        }

        PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "value="+value);
        return value;
    }

    int getDeviceBrightnessManualValue(Context context)
    {
        int percentage = getDeviceBrightnessValue();
        return convertPercentsToBrightnessManualValue(percentage, context);
    }

    static float convertPercentsToBrightnessAdaptiveValue(int percentage, Context context)
    {
        float value;

        if (percentage == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            // brightness is not set, change it to default adaptive brightness value
            value = Settings.System.getFloat(context.getContentResolver(),
                    ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, 0f);
        else {
            if ((Build.VERSION.SDK_INT < 28) || PPApplication.romIsSamsung)
                value = (percentage - 50) / 50f;
            else {
                int maximumValue;// = getMaximumScreenBrightnessSetting();
                int minimumValue;// = getMinimumScreenBrightnessSetting();

                //PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "maximumValue="+maximumValue);
                //PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "minimumValue="+minimumValue);

                //if (maximumValue-minimumValue > 255) {
                minimumValue = 0;
                maximumValue = 255;
                //}

                value = (getBrightnessValue_A9(percentage, minimumValue, maximumValue) - 128) / 128f;
            }
        }

        return value;
    }

    float getDeviceBrightnessAdaptiveValue(Context context)
    {
        int percentage = getDeviceBrightnessValue();
        return convertPercentsToBrightnessAdaptiveValue(percentage, context);
    }

    @SuppressWarnings("SameParameterValue")
    static long convertBrightnessToPercents(int value, int maxValue, int minValue)
    {
        long percentage;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            percentage = value; // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else {
            if ((Build.VERSION.SDK_INT < 28) || PPApplication.romIsSamsung)
                percentage = Math.round((float) (value - minValue) / (maxValue - minValue) * 100.0);
            else
                percentage = getBrightnessPercentage_A9(value, minValue, maxValue);
        }

        return percentage;
    }

    /*
    public void setDeviceBrightnessManualValue(int value, Context context)
    {
        int maxValue = getMaximumScreenBrightnessSetting();
        int minValue = getMinimumScreenBrightnessSetting();

        if (maxValue-minValue > 65535) {
            minValue = 0;
            maxValue = 65535;
        }

        long percentage = convertBrightnessToPercents(value, maxValue, minValue, context);

        //value|noChange|automatic|sharedProfile
        String[] splits = _deviceBrightness.split("\\|");
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */
    /*
    public void setDeviceBrightnessAdaptiveValue(float value)
    {
        long percentage;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            percentage = Math.round(value); // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else
            percentage = Math.round(value * 50 + 50);

        //value|noChange|automatic|sharedProfile
        String[] splits = _deviceBrightness.split("\\|");
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */

    //----------------------------------

    void generateIconBitmap(Context context, boolean monochrome, int monochromeValue, boolean useMonochromeValueForCustomIcon)
    {
        if (!getIsIconResourceID())
        {
            releaseIconBitmap();

            Resources resources = context.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            _iconBitmap = BitmapManipulator.resampleBitmapUri(getIconIdentifier(), width, height, true, false, context);

            if (_iconBitmap == null)
            {
                // no icon found, set default icon
                _icon = "ic_profile_default|1|0|0";
                if (monochrome)
                {
                    //int iconResource = context.getResources().getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
                    int iconResource = getIconResource(getIconIdentifier());
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                    _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                    // getIsIconResourceID must return false
                    //_icon = getIconIdentifier() + "|0";
                }
            }
            else
            if (monochrome) {
                float monoValue = 255f;
                if (monochromeValue == 0x00) monoValue = -255f;
                if (monochromeValue == 0x40) monoValue = -128f;
                if (monochromeValue == 0x80) monoValue = 0f;
                if (monochromeValue == 0xC0) monoValue = 128f;
                //if (monochromeValue == 0xFF) monoValue = 255f;
                _iconBitmap = BitmapManipulator.grayScaleBitmap(_iconBitmap);
                if (useMonochromeValueForCustomIcon)
                    _iconBitmap = BitmapManipulator.setBitmapBrightness(_iconBitmap, monoValue);
            }
        }
        else
        if (monochrome)
        {
            Resources resources = context.getResources();
            //int iconResource = resources.getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
            int iconResource = getIconResource(getIconIdentifier());
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            Bitmap bitmap = BitmapManipulator.resampleResource(resources, iconResource, width, height);
            _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue/*, context*/);
            // getIsIconResourceID must return false
            //_icon = getIconIdentifier() + "|0";
            /*Drawable drawable;
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                drawable = context.getResources().getDrawable(iconResource, context.getTheme());
            } else {
                drawable = context.getResources().getDrawable(iconResource);
            }
            _iconDrawable = BitmapManipulator.tintDrawableByValue(drawable, monochromeValue);
            _iconBitmap = null;*/
        }
        else
        if (getUseCustomColorForIcon()) {
            Resources resources = context.getResources();
            //int iconResource = resources.getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
            int iconResource = getIconResource(getIconIdentifier());
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            Bitmap bitmap = BitmapManipulator.resampleResource(resources, iconResource, width, height);
            _iconBitmap = BitmapManipulator.recolorBitmap(bitmap, getIconCustomColor());
            // getIsIconResourceID must return false
            //_icon = getIconIdentifier() + "|0";
        }
        else
            _iconBitmap = null;
    }

    void generatePreferencesIndicator(Context context, boolean monochrome, int monochromeValue)
    {
        releasePreferencesIndicator();

        _preferencesIndicator = ProfilePreferencesIndicator.paint(this, monochrome, context);
        if (_preferencesIndicator != null) {
            if (monochrome)
                _preferencesIndicator = BitmapManipulator.monochromeBitmap(_preferencesIndicator, monochromeValue);
        }
    }

    void releaseIconBitmap()
    {
        if (_iconBitmap != null)
        {
            //if (!_iconBitmap.isRecycled())
            //    _iconBitmap.recycle();
            _iconBitmap = null;
        }
    }

    void releasePreferencesIndicator()
    {
        if (_preferencesIndicator != null)
        {
            //if (!_preferencesIndicator.isRecycled())
            //    _preferencesIndicator.recycle();
            _preferencesIndicator = null;
        }
    }

    public Spannable getProfileNameWithDuration(String indicators, boolean multiLine, Context context) {
        String profileName = _name;
        String durationString = "";
        if ((_duration > 0) && (_afterDurationDo != Profile.AFTERDURATIONDO_NOTHING)) {
            boolean showEndTime = false;
            if (_checked) {
                long endDurationTime = getActivatedProfileEndDurationTime(context);
                if (endDurationTime > 0) {
                    durationString = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ")";
                    showEndTime = true;
                }
            }
            if (!showEndTime) {
                durationString = "[" + GlobalGUIRoutines.getDurationString(_duration) + "]";
            }
        }
        int startSpan = profileName.length();
        if (!(indicators.isEmpty() && durationString.isEmpty())) {
            if (multiLine)
                profileName = profileName + "\n";
            else
                profileName = profileName + " ";
        }
        if (!indicators.isEmpty())
            profileName = profileName + indicators;
        if (!durationString.isEmpty()) {
            if (!indicators.isEmpty())
                profileName = profileName + " ";
            profileName = profileName + durationString;
        }
        Spannable sbt = new SpannableString(profileName);
        sbt.setSpan(new RelativeSizeSpan(0.8f), startSpan, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sbt;
    }
    /*public String getProfileNameWithDuration(boolean multiLine, Context context) {
        String profileName = _name;
        if ((_duration > 0) && (_afterDurationDo != Profile.AFTERDURATIONDO_NOTHING)) {
            boolean showEndTime = false;
            if (_checked) {
                long endDurationTime = getActivatedProfileEndDurationTime(context);
                if (endDurationTime > 0) {
                    if (multiLine)
                        profileName = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ")\n" + profileName;
                    else
                        profileName = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ") " + profileName;
                    showEndTime = true;
                }
            }
            if (!showEndTime) {
                //profileName = "[" + _duration + "] " + profileName;
                if (multiLine)
                    profileName = "[" + GlobalGUIRoutines.getDurationString(_duration) + "]\n" + profileName;
                else
                    profileName = "[" + GlobalGUIRoutines.getDurationString(_duration) + "] " + profileName;
            }
        }
        return profileName;
    }*/

    @SuppressLint("SimpleDateFormat")
    private static String timeDateStringFromTimestamp(Context applicationContext, long timestamp){
        String timeDate;
        String timestampDate = android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp));
        Calendar calendar = Calendar.getInstance();
        String currentDate = android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(calendar.getTimeInMillis()));
        String androidDateTime;
        if (timestampDate.equals(currentDate))
            androidDateTime=android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        else
            androidDateTime=android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp))+" "+
                    android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        String javaDateTime = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        String AmPm="";
        if(!Character.isDigit(androidDateTime.charAt(androidDateTime.length()-1))) {
            if(androidDateTime.contains(new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM])){
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM];
            }else{
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM];
            }
            androidDateTime=androidDateTime.replace(AmPm, "");
        }
        if(!Character.isDigit(javaDateTime.charAt(javaDateTime.length()-1))){
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM], "");
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM], "");
        }
        javaDateTime=javaDateTime.substring(javaDateTime.length()-3);
        timeDate=androidDateTime.concat(javaDateTime);
        return timeDate.concat(AmPm);
    }

    private static String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    static Profile getSharedProfile(Context context)
    {
        int	maximumValueRing = 7;
        int	maximumValueNotification = 7;
        int	maximumValueMusic = 15;
        int	maximumValueAlarm = 7;
        int	maximumValueSystem = 7;
        int	maximumValueVoiceCall = 7;
        int maximumValueDTMF = 7;
        int maximumValueAccessibility = 7;
        int maximumValueBluetoothSCO = 7;
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            maximumValueVoiceCall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            maximumValueDTMF = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
            if (Build.VERSION.SDK_INT >= 26)
                maximumValueAccessibility = audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);
            maximumValueBluetoothSCO = audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO);
        }

        SharedPreferences preferences = context.getSharedPreferences("profile_preferences_default_profile", Context.MODE_PRIVATE);

        Profile profile = new Profile();
        profile._id = -999L;
        profile._name = "Profile";
        profile._icon = PROFILE_ICON_DEFAULT + "|1|0|0";
        profile._checked = false;
        profile._porder = 0;
        profile._duration = 0;
        profile._afterDurationDo = Profile.AFTERDURATIONDO_UNDOPROFILE;
        profile._durationNotificationSound = "";
        profile._durationNotificationVibrate = false;
        profile._activationByUserCount = 0;
        profile._volumeRingerMode = Integer.parseInt(preferences.getString(PREF_PROFILE_VOLUME_RINGER_MODE, "1")); // ring
        profile._volumeZenMode = Integer.parseInt(preferences.getString(PREF_PROFILE_VOLUME_ZEN_MODE, "1")); // all
        profile._volumeRingtone = preferences.getString(PREF_PROFILE_VOLUME_RINGTONE, getVolumeLevelString(71, maximumValueRing) + "|0|0");
        profile._volumeNotification = preferences.getString(PREF_PROFILE_VOLUME_NOTIFICATION, getVolumeLevelString(86, maximumValueNotification)+"|0|0");
        profile._volumeMedia = preferences.getString(PREF_PROFILE_VOLUME_MEDIA, getVolumeLevelString(80, maximumValueMusic)+"|0|0");
        profile._volumeAlarm = preferences.getString(PREF_PROFILE_VOLUME_ALARM, getVolumeLevelString(100, maximumValueAlarm)+"|0|0");
        profile._volumeSystem = preferences.getString(PREF_PROFILE_VOLUME_SYSTEM, getVolumeLevelString(70, maximumValueSystem)+"|0|0");
        profile._volumeVoice = preferences.getString(PREF_PROFILE_VOLUME_VOICE, getVolumeLevelString(70, maximumValueVoiceCall)+"|0|0");
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
        profile._soundRingtone = preferences.getString(PREF_PROFILE_SOUND_RINGTONE, Settings.System.DEFAULT_RINGTONE_URI.toString());
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
        profile._soundNotification = preferences.getString(PREF_PROFILE_SOUND_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
        profile._soundAlarm = preferences.getString(PREF_PROFILE_SOUND_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_AIRPLANE_MODE, "2")); // OFF
        profile._deviceWiFi = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WIFI, "2")); // OFF
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_BLUETOOTH, "2")); //OFF
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "2")); // 30 seconds
        profile._deviceBrightness = preferences.getString(PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|0|1|0");  // automatic on
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        profile._deviceWallpaper = preferences.getString(PREF_PROFILE_DEVICE_WALLPAPER, "-");
        profile._deviceMobileData = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_MOBILE_DATA, "1")); //ON
        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0"));
        profile._deviceGPS = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_GPS, "2")); //OFF
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
        profile._deviceRunApplicationPackageName = preferences.getString(PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        profile._deviceAutoSync = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_AUTOSYNC, "1")); // ON
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_AUTOROTATE, "1")); // ON
        profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, "0"));
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
        profile._deviceNFC = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_NFC, "0"));
        profile._deviceKeyguard = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_KEYGUARD, "0"));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WIFI_AP, "2")); // OFF
        profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_POWER_SAVE_MODE, "0"));
        profile._deviceNetworkType = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
        profile._notificationLed = Integer.parseInt(preferences.getString(PREF_PROFILE_NOTIFICATION_LED, "0"));
        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
        profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WALLPAPER_FOR, "0"));
        profile._lockDevice = Integer.parseInt(preferences.getString(PREF_PROFILE_LOCK_DEVICE, "0"));
        profile._deviceConnectToSSID = preferences.getString(PREF_PROFILE_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
        profile._deviceWiFiAPPrefs = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WIFI_AP_PREFS, "0"));
        profile._headsUpNotifications = Integer.parseInt(preferences.getString(PREF_PROFILE_HEADS_UP_NOTIFICATIONS, "0"));
        profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));
        profile._deviceForceStopApplicationPackageName = preferences.getString(PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, "-");
        profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, "0"));
        profile._deviceCloseAllApplications = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, "0"));
        profile._screenNightMode = Integer.parseInt(preferences.getString(PREF_PROFILE_SCREEN_NIGHT_MODE, "0"));
        profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(PREF_PROFILE_DTMF_TONE_WHEN_DIALING, "0"));
        profile._soundOnTouch = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_ON_TOUCH, "0"));
        profile._volumeDTMF = preferences.getString(PREF_PROFILE_VOLUME_DTMF, getVolumeLevelString(70, maximumValueDTMF)+"|0|0");
        profile._volumeAccessibility = preferences.getString(PREF_PROFILE_VOLUME_ACCESSIBILITY, getVolumeLevelString(80, maximumValueAccessibility)+"|0|0");
        profile._volumeBluetoothSCO = preferences.getString(PREF_PROFILE_VOLUME_BLUETOOTH_SCO, getVolumeLevelString(80, maximumValueBluetoothSCO)+"|0|0");

        return profile;
    }

    static Profile getMappedProfile(Profile profile, Profile sharedProfile/*, Context context*/)
    {
        final int SHARED_PROFILE_VALUE = 99;
        final String CONNECTTOSSID_SHAREDPROFILE = "^default_profile^";

        if (profile != null)
        {
            //Profile sharedProfile = getSharedProfile(context);

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
                    profile._deviceAutoSync,
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
                    profile._deviceWallpaperFor,
                    profile._hideStatusBarIcon,
                    profile._lockDevice,
                    profile._deviceConnectToSSID,
                    profile._durationNotificationSound,
                    profile._durationNotificationVibrate,
                    profile._deviceWiFiAPPrefs,
                    profile._headsUpNotifications,
                    profile._deviceForceStopApplicationChange,
                    profile._deviceForceStopApplicationPackageName,
                    profile._activationByUserCount,
                    profile._deviceNetworkTypePrefs,
                    profile._deviceCloseAllApplications,
                    profile._screenNightMode,
                    profile._dtmfToneWhenDialing,
                    profile._soundOnTouch,
                    profile._volumeDTMF,
                    profile._volumeAccessibility,
                    profile._volumeBluetoothSCO);

            boolean zenModeMapped = false;
            if (profile._volumeRingerMode == SHARED_PROFILE_VALUE) {
                mappedProfile._volumeRingerMode = sharedProfile._volumeRingerMode;
                if (mappedProfile._volumeRingerMode == RINGERMODE_ZENMODE) {
                    mappedProfile._volumeZenMode = sharedProfile._volumeZenMode;
                    zenModeMapped = true;
                }
            }
            if ((profile._volumeZenMode == SHARED_PROFILE_VALUE) && (!zenModeMapped))
                mappedProfile._volumeZenMode = sharedProfile._volumeZenMode;
            if (profile.getVolumeRingtoneSharedProfile())
                mappedProfile._volumeRingtone = sharedProfile._volumeRingtone;
            if (profile.getVolumeNotificationSharedProfile())
                mappedProfile._volumeNotification = sharedProfile._volumeNotification;
            if (profile.getVolumeAlarmSharedProfile())
                mappedProfile._volumeAlarm = sharedProfile._volumeAlarm;
            if (profile.getVolumeMediaSharedProfile())
                mappedProfile._volumeMedia = sharedProfile._volumeMedia;
            if (profile.getVolumeSystemSharedProfile())
                mappedProfile._volumeSystem = sharedProfile._volumeSystem;
            if (profile.getVolumeVoiceSharedProfile())
                mappedProfile._volumeVoice = sharedProfile._volumeVoice;
            if (profile._soundRingtoneChange == SHARED_PROFILE_VALUE)
            {
                mappedProfile._soundRingtoneChange = sharedProfile._soundRingtoneChange;
                mappedProfile._soundRingtone = sharedProfile._soundRingtone;
            }
            if (profile._soundNotificationChange == SHARED_PROFILE_VALUE)
            {
                mappedProfile._soundNotificationChange = sharedProfile._soundNotificationChange;
                mappedProfile._soundNotification = sharedProfile._soundNotification;
            }
            if (profile._soundAlarmChange == SHARED_PROFILE_VALUE)
            {
                mappedProfile._soundAlarmChange = sharedProfile._soundAlarmChange;
                mappedProfile._soundAlarm = sharedProfile._soundAlarm;
            }
            if (profile._deviceAirplaneMode == SHARED_PROFILE_VALUE)
                mappedProfile._deviceAirplaneMode = sharedProfile._deviceAirplaneMode;
            if (profile._deviceAutoSync == SHARED_PROFILE_VALUE)
                mappedProfile._deviceAutoSync = sharedProfile._deviceAutoSync;
            if (profile._deviceMobileData == SHARED_PROFILE_VALUE)
                mappedProfile._deviceMobileData = sharedProfile._deviceMobileData;
            if (profile._deviceMobileDataPrefs == SHARED_PROFILE_VALUE)
                mappedProfile._deviceMobileDataPrefs = sharedProfile._deviceMobileDataPrefs;
            if (profile._deviceWiFi == SHARED_PROFILE_VALUE)
                mappedProfile._deviceWiFi = sharedProfile._deviceWiFi;
            if (profile._deviceBluetooth == SHARED_PROFILE_VALUE)
                mappedProfile._deviceBluetooth = sharedProfile._deviceBluetooth;
            if (profile._deviceGPS == SHARED_PROFILE_VALUE)
                mappedProfile._deviceGPS = sharedProfile._deviceGPS;
            if (profile._deviceLocationServicePrefs == SHARED_PROFILE_VALUE)
                mappedProfile._deviceLocationServicePrefs = sharedProfile._deviceLocationServicePrefs;
            if (profile._deviceScreenTimeout == SHARED_PROFILE_VALUE)
                mappedProfile._deviceScreenTimeout = sharedProfile._deviceScreenTimeout;
            if (profile.getDeviceBrightnessSharedProfile())
                mappedProfile._deviceBrightness = sharedProfile._deviceBrightness;
            if (profile._deviceAutoRotate == SHARED_PROFILE_VALUE)
                mappedProfile._deviceAutoRotate = sharedProfile._deviceAutoRotate;
            if (profile._deviceRunApplicationChange == SHARED_PROFILE_VALUE)
            {
                mappedProfile._deviceRunApplicationChange = sharedProfile._deviceRunApplicationChange;
                mappedProfile._deviceRunApplicationPackageName = sharedProfile._deviceRunApplicationPackageName;
            }
            if (profile._deviceWallpaperChange == SHARED_PROFILE_VALUE)
            {
                mappedProfile._deviceWallpaperChange = sharedProfile._deviceWallpaperChange;
                mappedProfile._deviceWallpaper = sharedProfile._deviceWallpaper;
                mappedProfile._deviceWallpaperFor = sharedProfile._deviceWallpaperFor;
            }
            if (profile._volumeSpeakerPhone == SHARED_PROFILE_VALUE)
                mappedProfile._volumeSpeakerPhone = sharedProfile._volumeSpeakerPhone;
            if (profile._deviceNFC == SHARED_PROFILE_VALUE)
                mappedProfile._deviceNFC = sharedProfile._deviceNFC;
            if (profile._deviceKeyguard == SHARED_PROFILE_VALUE)
                mappedProfile._deviceKeyguard = sharedProfile._deviceKeyguard;
            if (profile._vibrationOnTouch == SHARED_PROFILE_VALUE)
                mappedProfile._vibrationOnTouch = sharedProfile._vibrationOnTouch;
            if (profile._deviceWiFiAP == SHARED_PROFILE_VALUE)
                mappedProfile._deviceWiFiAP = sharedProfile._deviceWiFiAP;
            if (profile._devicePowerSaveMode == SHARED_PROFILE_VALUE)
                mappedProfile._devicePowerSaveMode = sharedProfile._devicePowerSaveMode;
            if (profile._deviceNetworkType == SHARED_PROFILE_VALUE)
                mappedProfile._deviceNetworkType = sharedProfile._deviceNetworkType;
            if (profile._notificationLed == SHARED_PROFILE_VALUE)
                mappedProfile._notificationLed = sharedProfile._notificationLed;
            if (profile._vibrateWhenRinging == SHARED_PROFILE_VALUE)
                mappedProfile._vibrateWhenRinging = sharedProfile._vibrateWhenRinging;
            if (profile._lockDevice == SHARED_PROFILE_VALUE)
                mappedProfile._lockDevice = sharedProfile._lockDevice;
            if ((profile._deviceConnectToSSID != null) && (profile._deviceConnectToSSID.equals(CONNECTTOSSID_SHAREDPROFILE)))
                mappedProfile._deviceConnectToSSID = sharedProfile._deviceConnectToSSID;
            if (profile._deviceWiFiAPPrefs == SHARED_PROFILE_VALUE)
                mappedProfile._deviceWiFiAPPrefs = sharedProfile._deviceWiFiAPPrefs;
            if (profile._headsUpNotifications == SHARED_PROFILE_VALUE)
                mappedProfile._headsUpNotifications = sharedProfile._headsUpNotifications;
            if (profile._deviceForceStopApplicationChange == SHARED_PROFILE_VALUE)
            {
                mappedProfile._deviceForceStopApplicationChange = sharedProfile._deviceForceStopApplicationChange;
                mappedProfile._deviceForceStopApplicationPackageName = sharedProfile._deviceForceStopApplicationPackageName;
            }
            if (profile._deviceNetworkTypePrefs == SHARED_PROFILE_VALUE)
                mappedProfile._deviceNetworkTypePrefs = sharedProfile._deviceNetworkTypePrefs;
            if (profile._deviceCloseAllApplications == SHARED_PROFILE_VALUE)
                mappedProfile._deviceCloseAllApplications = sharedProfile._deviceCloseAllApplications;
            if (profile._screenNightMode == SHARED_PROFILE_VALUE)
                mappedProfile._screenNightMode = sharedProfile._screenNightMode;
            if (profile._dtmfToneWhenDialing == SHARED_PROFILE_VALUE)
                mappedProfile._dtmfToneWhenDialing = sharedProfile._dtmfToneWhenDialing;
            if (profile._soundOnTouch == SHARED_PROFILE_VALUE)
                mappedProfile._soundOnTouch = sharedProfile._soundOnTouch;
            if (profile.getVolumeDTMFSharedProfile())
                mappedProfile._volumeDTMF = sharedProfile._volumeDTMF;
            if (profile.getVolumeAccessibilitySharedProfile())
                mappedProfile._volumeAccessibility = sharedProfile._volumeAccessibility;
            if (profile.getVolumeBluetoothSCOSharedProfile())
                mappedProfile._volumeBluetoothSCO = sharedProfile._volumeBluetoothSCO;

            mappedProfile._iconBitmap = profile._iconBitmap;
            mappedProfile._preferencesIndicator = profile._preferencesIndicator;

            return mappedProfile;
        }
        else
            return null;
    }

    // ----- Check if preference is allowed in device -------------------------------------

    static PreferenceAllowed isProfilePreferenceAllowed(String preferenceKey, Profile profile,
                                                        SharedPreferences sharedPreferences,
                                                        boolean fromUIThread, Context context)
    {
        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();

        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;

        boolean checked = false;

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot(context);

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 17)
            //{
                if (PPApplication.isRooted(fromUIThread))
                {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._deviceAirplaneMode != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    if (PPApplication.settingsBinaryExists(fromUIThread))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            /*}
            else
                featurePresented = PreferenceAllowed.PREFERENCE_ALLOWED;*/
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI))
                // device has Wifi
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH))
                // device has bluetooth
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA))
        {
            boolean mobileDataSupported = false;
            if (!PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
                ConnectivityManager connManager = null;
                try {
                    connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                } catch (Exception ignored) {
                    // java.lang.NullPointerException: missing IConnectivityManager
                    // Dual SIM?? Bug in Android ???
                }
                if (connManager != null) {
                    //if (android.os.Build.VERSION.SDK_INT >= 21) {
                        Network[] networks = connManager.getAllNetworks();
                        if ((networks != null) && (networks.length > 0)) {
                            for (Network network : networks) {
                                try {
                                    if (Build.VERSION.SDK_INT < 28) {
                                        NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                        if (ntkInfo != null) {
                                            if (ntkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                                                mobileDataSupported = true;
                                                break;
                                            }
                                        }
                                    }
                                    else {
                                        NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                        if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                            mobileDataSupported = true;
                                            break;
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //} else {
                    //    NetworkInfo ni = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    //    mobileDataSupported = ni != null;
                    //}
                }
                //else
                //    mobileDataSupported = false;
            }
            else
                mobileDataSupported = true;
            if (mobileDataSupported) {
                //if (android.os.Build.VERSION.SDK_INT >= 21)
                //{
                    // adb shell pm grant sk.henrichg.phoneprofiles android.permission.MODIFY_PHONE_STATE
                    // not working :-/
                    if (Permissions.hasPermission(context, Manifest.permission.MODIFY_PHONE_STATE)) {
                        if (ActivateProfileHelper.canSetMobileData(context))
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                    else
                    if (PPApplication.isRooted(fromUIThread)) {
                        // device is rooted

                        if (profile != null) {
                            // test if grant root is disabled
                            if (profile._deviceMobileData != 0) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                    // not needed to test all parameters
                                    return preferenceAllowed;
                                }
                            }
                        }
                        else
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                    // not needed to test all parameters
                                    return preferenceAllowed;
                                }
                            }
                        }

                        //if (serviceBinaryExists() && telephonyServiceExists(context, PREF_PROFILE_DEVICE_MOBILE_DATA))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                /*}
                else
                {
                    if (ActivateProfileHelper.canSetMobileData(context))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else {
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }*/
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
            {
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_GPS))
        {
            PPApplication.logE("Profile.isProfilePreferenceAllowed", "GPS");

            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_LOCATION_GPS))
            {
                PPApplication.logE("Profile.isProfilePreferenceAllowed", "device has GPS");

                // device has gps
                // adb shell pm grant sk.henrichg.phoneprofiles android.permission.WRITE_SECURE_SETTINGS
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    PPApplication.logE("Profile.isProfilePreferenceAllowed", "WRITE_SECURE_SETTINGS granted");

                    if (ActivateProfileHelper.canSetMobileData(context))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                if (PPApplication.isRooted(fromUIThread))
                {
                    PPApplication.logE("Profile.isProfilePreferenceAllowed", "device is rooted");

                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._deviceGPS != 0) {
                            PPApplication.logE("Profile.isProfilePreferenceAllowed", "applicationNeverAskForGrantRoot="+applicationNeverAskForGrantRoot);
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    if (PPApplication.settingsBinaryExists(fromUIThread)) {
                        PPApplication.logE("Profile.isProfilePreferenceAllowed", "settingsBinaryExists=true");
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                    else {
                        PPApplication.logE("Profile.isProfilePreferenceAllowed", "settingsBinaryExists=false");
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                }
                else
                if (ActivateProfileHelper.canExploitGPS(context))
                {
                    PPApplication.logE("Profile.isProfilePreferenceAllowed", "can exploit GPS");
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else {
                    PPApplication.logE("Profile.isProfilePreferenceAllowed", "WRITE_SECURE_SETTINGS NOT granted");
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                }
            }
            else {
                PPApplication.logE("Profile.isProfilePreferenceAllowed", "device NOT has GPS");
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_NFC))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_NFC))
            {
                PPApplication.logE("PPApplication.hardwareCheck","NFC=presented");

                // device has nfc
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                if (PPApplication.isRooted(fromUIThread)) {

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._deviceNFC != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            }
            else
            {
                PPApplication.logE("PPApplication.hardwareCheck","NFC=not presented");
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI)) {
                // device has Wifi
                if (android.os.Build.VERSION.SDK_INT < 26) {
                    if (WifiApManager.canExploitWifiAP(context))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else {
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }
                else
                if (Build.VERSION.SDK_INT < 28) {
                    if (PPApplication.isRooted(fromUIThread)) {
                        // device is rooted

                        if (profile != null) {
                            // test if grant root is disabled
                            if (profile._deviceWiFiAP != 0) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                    // not needed to test all parameters
                                    return preferenceAllowed;
                                }
                            }
                        }
                        else
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                    // not needed to test all parameters
                                    return preferenceAllowed;
                                }
                            }
                        }

                        if (ActivateProfileHelper.wifiServiceExists(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
                            if (PPApplication.serviceBinaryExists(fromUIThread))
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        } else {
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        }
                    }
                    else
                    if (WifiApManager.canExploitWifiTethering(context))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
        {
            if (android.os.Build.VERSION.SDK_INT == 23) {
                if (PPApplication.isRooted(fromUIThread)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._vibrateWhenRinging != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    if (PPApplication.settingsBinaryExists(fromUIThread))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                {
                    if (PPApplication.isRooted(fromUIThread))
                    {
                        // device is rooted

                        if (profile != null) {
                            // test if grant root is disabled
                            if (profile.getDeviceBrightnessChange() && profile.getDeviceBrightnessAutomatic()) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                    // not needed to test all parameters
                                    return preferenceAllowed;
                                }
                            }
                        }
                        else
                        if (sharedPreferences != null) {
                            String value = sharedPreferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS));
                            if (Profile.getDeviceBrightnessChange(value) && Profile.getDeviceBrightnessAutomatic(value)) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                    // not needed to test all parameters
                                    return preferenceAllowed;
                                }
                            }
                        }

                        if (PPApplication.settingsBinaryExists(fromUIThread))
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            /*}
            else {
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }*/
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                if (PPApplication.isRooted(fromUIThread)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._devicePowerSaveMode != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    if (PPApplication.settingsBinaryExists(fromUIThread))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            /*}
            else {
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }*/
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
            {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    final int phoneType = telephonyManager.getPhoneType();
                    if ((phoneType == TelephonyManager.PHONE_TYPE_GSM) || (phoneType == TelephonyManager.PHONE_TYPE_CDMA)) {
                        if (PPApplication.isRooted(fromUIThread)) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if (profile._deviceNetworkType != 0) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                        // not needed to test all parameters
                                        return preferenceAllowed;
                                    }
                                }
                            }
                            else
                            if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                        // not needed to test all parameters
                                        return preferenceAllowed;
                                    }
                                }
                            }

                            if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                                if (PPApplication.serviceBinaryExists(fromUIThread))
                                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                                else
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                            } else {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                            }
                        } else
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                    } else {
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                    }
                }
                else {
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            int value = Settings.System.getInt(context.getContentResolver(), "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/, -10);
            if ((value != -10) && (android.os.Build.VERSION.SDK_INT >= 23)) {
                if (PPApplication.isRooted(fromUIThread)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._notificationLed != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    if (PPApplication.settingsBinaryExists(fromUIThread))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            if (value != -10)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else {
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            boolean secureKeyguard;
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                secureKeyguard = keyguardManager.isKeyguardSecure();
                if (secureKeyguard) {
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                    preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_secure_lock);
                } else
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI))
                // device has Wifi
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI))
            {
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if ((profile != null) || preferenceKey.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS))
        {
            int value = Settings.Global.getInt(context.getContentResolver(), "heads_up_notifications_enabled", -10);
            if ((value != -10) /*&& (android.os.Build.VERSION.SDK_INT >= 21)*/) {
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                if (PPApplication.isRooted(fromUIThread)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._headsUpNotifications != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }
                    else
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                                // not needed to test all parameters
                                return preferenceAllowed;
                            }
                        }
                    }

                    if (PPApplication.settingsBinaryExists(fromUIThread))
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            }
            //else
            //if (value != -10)
            //    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else {
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
            }
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            //if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE))
        {
            //if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
            {
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        if (preferenceKey.equals(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY))
        {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else {
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
            checked = true;
        }
        if (checked && (profile == null))
            return preferenceAllowed;

        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;

        return preferenceAllowed;
    }

    //--------------------------------------------------------------


    static long getActivatedProfileForDuration(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, 0);
    }

    static void setActivatedProfileForDuration(Context context, long profileId)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, profileId);
        editor.apply();
    }

    private static long getActivatedProfileEndDurationTime(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, 0);
    }

    static void setActivatedProfileEndDurationTime(Context context, long time)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, time);
        editor.apply();
    }

    static int getIconResource(String identifier) {
        int iconResource = R.drawable.ic_profile_default;
        try {
            //noinspection ConstantConditions
            iconResource = profileIconIdMap.get(identifier);
        } catch (Exception ignored) {}
        return iconResource;
    }

}
