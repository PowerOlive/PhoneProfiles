package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Profile {

    //private variables
    long _id;
    String _name;
    String _icon;
    boolean _checked;
    int _porder;
    int _duration;
    int _afterDurationDo;
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
    int _deviceAutosync;
    int _deviceAutoRotate;
    int _volumeSpeakerPhone;
    int _deviceNFC;
    int _deviceKeyguard;
    int _vibrationOnTouch;
    int _deviceWiFiAP;
    int _devicePowerSaveMode;
    boolean _askForDuration;
    int _deviceNetworkType;
    int _notificationLed;
    int _vibrateWhenRinging;
    int _deviceWallpaperFor;

    Bitmap _iconBitmap;
    Bitmap _preferencesIndicator;

    static final int AFTERDURATIONDO_NOTHING = 0;
    static final int AFTERDURATIONDO_UNDOPROFILE = 1;
    static final int AFTERDURATIONDO_BACKGROUNPROFILE = 2;

    static final int BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET = -99;


    static final String[] profileIconId = {
            "ic_profile_default",

            "ic_profile_home", "ic_profile_home_2",

            "ic_profile_outdoors_1", "ic_profile_outdoors_2", "ic_profile_outdoors_3", "ic_profile_outdoors_4",
            "ic_profile_outdoors_5", "ic_profile_outdoors_6", "ic_profile_outdoors_7",

            "ic_profile_meeting", "ic_profile_meeting_2", "ic_profile_meeting_3", "ic_profile_mute", "ic_profile_mute_2",
            "ic_profile_volume_1", "ic_profile_volume_2", "ic_profile_volume_3",

            "ic_profile_work_1", "ic_profile_work_2", "ic_profile_work_3", "ic_profile_work_4", "ic_profile_work_5",
            "ic_profile_work_6", "ic_profile_work_7", "ic_profile_work_8", "ic_profile_work_9", "ic_profile_work_10",
            "ic_profile_work_11", "ic_profile_work_12",

            "ic_profile_sleep", "ic_profile_sleep_2", "ic_profile_night",
            "ic_profile_call_1",

            "ic_profile_car_1", "ic_profile_car_2", "ic_profile_car_3", "ic_profile_car_4", "ic_profile_car_5",
            "ic_profile_car_6", "ic_profile_car_7", "ic_profile_car_8", "ic_profile_car_9",
            "ic_profile_airplane_1", "ic_profile_airplane_2", "ic_profile_airplane_3",

            "ic_profile_battery_1", "ic_profile_battery_2", "ic_profile_battery_3",

            "ic_profile_culture_1", "ic_profile_culture_2", "ic_profile_culture_3", "ic_profile_culture_4"
    };

    static final int[] profileIconColor = {
            0xff1c9cd7,

            0xff99cc00, 0xff99cc00,

            0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33,
            0xffffbc33, 0xffffbc33, 0xffffbc33,

            0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000,
            0xffcc0000, 0xffcc0000, 0xffcc0000,

            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff,

            0xff0099cc, 0xff0099cc, 0xff0099cc,
            0xff0099cc,

            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174,

            0xffdb3514, 0xffdb9714, 0xff2aa561,

            0xff38d043, 0xff38d043, 0xff38d043, 0xff38d043
    };

    // Empty constructorn
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
                   int deviceAutosync,
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
                   int deviceWallpaperFor)
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
        this._deviceAutosync = deviceAutosync;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWiFiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._askForDuration = askForDuration;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
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
                   int deviceAutosync,
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
                   int deviceWallpaperFor)
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
        this._deviceAutosync = deviceAutosync;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWiFiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._askForDuration = askForDuration;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
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
        this._deviceAutosync = profile._deviceAutosync;
        this._deviceAutoRotate = profile._deviceAutoRotate;
        this._deviceLocationServicePrefs = profile._deviceLocationServicePrefs;
        this._volumeSpeakerPhone = profile._volumeSpeakerPhone;
        this._deviceNFC = profile._deviceNFC;
        this._duration = profile._duration;
        this._afterDurationDo = profile._afterDurationDo;
        this._deviceKeyguard = profile._deviceKeyguard;
        this._vibrationOnTouch = profile._vibrationOnTouch;
        this._deviceWiFiAP = profile._deviceWiFiAP;
        this._devicePowerSaveMode = profile._devicePowerSaveMode;
        this._askForDuration = profile._askForDuration;
        this._deviceNetworkType = profile._deviceNetworkType;
        this._notificationLed = profile._notificationLed;
        this._vibrateWhenRinging = profile._vibrateWhenRinging;
        this._deviceWallpaperFor = profile._deviceWallpaperFor;

        this._iconBitmap = profile._iconBitmap;
        this._preferencesIndicator = profile._preferencesIndicator;
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

    //gettig where icon has custom color
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

    // geting icon custom color
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

    boolean getVolumeRingtoneDefaultProfile()
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

    boolean getVolumeNotificationDefaultProfile()
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

    boolean getVolumeMediaDefaultProfile()
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

    boolean getVolumeAlarmDefaultProfile()
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

    boolean getVolumeSystemDefaultProfile()
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

    boolean getVolumeVoiceDefaultProfile()
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

    int getDeviceBrightnessValue()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
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

    boolean getDeviceBrightnessDefaultProfile()
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
            catch (Resources.NotFoundException e) {
                // ignore
            }
        }
        return 0;
    }

    private static int getMaximumScreenBrightnessSetting ()
    {
        final Resources res = Resources.getSystem();
        final int id = res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");  // API17+
        if (id != 0)
        {
            try {
                return res.getInteger(id);
            }
            catch (Resources.NotFoundException e) {
                // ignore
            }
        }
        return 255;
    }

    static int convertPercentsToBrightnessManualValue(int perc, Context context)
    {
        int maximumValue = getMaximumScreenBrightnessSetting();
        int minimumValue = getMinimumScreenBrightnessSetting();

        if (maximumValue-minimumValue > 65535) {
            minimumValue = 0;
            maximumValue = 65535;
        }

        int value;

        if (perc == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            // brightness is not set, change it to default manual brightness value
            value = Settings.System.getInt(context.getContentResolver(),
                                            Settings.System.SCREEN_BRIGHTNESS, 128);
        else
            value = Math.round((float)(maximumValue - minimumValue) / 100 * perc) + minimumValue;

        return value;
    }

    int getDeviceBrightnessManualValue(Context context)
    {
        int perc = getDeviceBrightnessValue();
        return convertPercentsToBrightnessManualValue(perc, context);
    }

    static float convertPercentsToBrightnessAdaptiveValue(int perc, Context context)
    {
        float value;

        if (perc == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            // brightness is not set, change it to default adaptive brightness value
            value = Settings.System.getFloat(context.getContentResolver(),
                                ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, 0f);
        else
            value = (perc - 50) / 50f;

        return value;
    }

    float getDeviceBrightnessAdaptiveValue(Context context)
    {
        int perc = getDeviceBrightnessValue();
        return convertPercentsToBrightnessAdaptiveValue(perc, context);
    }

    static long convertBrightnessToPercents(int value,
                                int maxValue, int minValue)
    {
        long perc;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            perc = value; // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else
            perc = Math.round((float)(value-minValue) / (maxValue - minValue) * 100.0);

        return perc;
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

        long perc = convertBrightnessToPercents(value, maxValue, minValue, context);

        //value|noChange|automatic|defaultProfile
        String[] splits = _deviceBrightness.split("\\|");
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */
    /*
    public void setDeviceBrightnessAdaptiveValue(float value)
    {
        long perc;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            perc = Math.round(value); // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else
            perc = Math.round(value * 50 + 50);

        //value|noChange|automatic|defaultProfile
        String[] splits = _deviceBrightness.split("\\|");
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */
    // getting wallpaper identifikator
    String getDeviceWallpaperIdentifier()
    {
        String value;
        try {
            String[] splits = _deviceWallpaper.split("\\|");
            value = splits[0];
        } catch (Exception e) {
            value = "-";
        }
        return value;
    }

    //----------------------------------

    void generateIconBitmap(Context context, boolean monochrome, int monochromeValue)
    {
        if (!getIsIconResourceID())
        {
            releaseIconBitmap();

            Resources resources = context.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            _iconBitmap = BitmapManipulator.resampleBitmap(getIconIdentifier(), width, height, context);

            if (_iconBitmap == null)
            {
                // no icon found, set default icon
                _icon = "ic_profile_default|1|0|0";
                if (monochrome)
                {
                    int iconResource = context.getResources().getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                    _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                    // getIsIconResourceID must return false
                    //_icon = getIconIdentifier() + "|0";
                }
            }
            else
            if (monochrome)
                _iconBitmap = BitmapManipulator.grayscaleBitmap(_iconBitmap);
        }
        else
        if (monochrome)
        {
            int iconResource = context.getResources().getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
            _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
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
            int iconResource = context.getResources().getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
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

        _preferencesIndicator = ProfilePreferencesIndicator.paint(this, context);

        if (monochrome)
            _preferencesIndicator = BitmapManipulator.monochromeBitmap(_preferencesIndicator, monochromeValue);
    }

    void releaseIconBitmap()
    {
        if (_iconBitmap != null)
        {
            _iconBitmap.recycle();
            _iconBitmap = null;
        }
    }

    void releasePreferencesIndicator()
    {
        if (_preferencesIndicator != null)
        {
            _preferencesIndicator.recycle();
            _preferencesIndicator = null;
        }
    }

    @SuppressLint("DefaultLocale")
    public String getProfileNameWithDuration(boolean multyline, Context context) {
        String profileName = _name;
        if ((_duration > 0) && (_afterDurationDo != Profile.AFTERDURATIONDO_NOTHING)) {
            boolean showEndTime = false;
            if (_checked) {
                long endDurationTime = GlobalData.getActivatedProfileEndDurationTime(context);
                if (endDurationTime > 0) {
                    if (multyline)
                        profileName = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ")\n" + profileName;
                    else
                        profileName = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ") " + profileName;
                    showEndTime = true;
                }
            }
            if (!showEndTime) {
                //profileName = "[" + _duration + "] " + profileName;
                final int hours = _duration / 3600;
                final int minutes = (_duration % 3600) / 60;
                final int seconds = _duration % 60;
                if (multyline)
                    profileName = "[" + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "]\n" + profileName;
                else
                    profileName = "[" + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "] " + profileName;
            }
        }
        return profileName;
    }

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

}
