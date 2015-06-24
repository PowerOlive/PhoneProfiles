package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

public class ActivateProfileHelper {

    private Activity activity;
    private Context context;
    private NotificationManager notificationManager;
    private Handler brightnessHandler;

    public static boolean lockRefresh = false;

    public static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    public ActivateProfileHelper()
    {

    }

    public void initialize(Activity a, Context c)
    {
        initializeNoNotificationManager(a, c);
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void initializeNoNotificationManager(Activity a, Context c)
    {
        activity = a;
        context = c;
    }

    public void setBrightnessHandler(Handler handler)
    {
        brightnessHandler = handler;
    }

    public void deinitialize()
    {
        activity = null;
        context = null;
        notificationManager = null;
    }

    @SuppressWarnings("deprecation")
    private void doExecuteForRadios(Profile profile)
    {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        // nahodenie mobilnych dat
        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == GlobalData.HARDWARE_CHECK_ALLOWED)
        {
            boolean _isMobileData = isMobileData(context);
            boolean _setMobileData = false;
            switch (profile._deviceMobileData) {
                case 1:
                    if (!_isMobileData)
                    {
                        _isMobileData = true;
                        _setMobileData = true;
                    }
                    break;
                case 2:
                    if (_isMobileData)
                    {
                        _isMobileData = false;
                        _setMobileData = true;
                    }
                    break;
                case 3:
                    _isMobileData = !_isMobileData;
                    _setMobileData = true;
                    break;
            }
            if (_setMobileData)
            {
                setMobileData(context, _isMobileData);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }

        // nahodenie WiFi AP
        boolean canChangeWifi = true;
        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, context) == GlobalData.HARDWARE_CHECK_ALLOWED)
        {
            WifiApManager wifiApManager = null;
            try {
                wifiApManager = new WifiApManager(context);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (wifiApManager != null) {
                boolean setWifiAPState = false;
                boolean isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                switch (profile._deviceWiFiAP) {
                    case 1:
                        if (!isWifiAPEnabled) {
                            isWifiAPEnabled = true;
                            setWifiAPState = true;
                            canChangeWifi = false;
                        }
                        break;
                    case 2:
                        if (isWifiAPEnabled) {
                            isWifiAPEnabled = false;
                            setWifiAPState = true;
                            canChangeWifi = true;
                        }
                        break;
                    case 3:
                        isWifiAPEnabled = !isWifiAPEnabled;
                        setWifiAPState = true;
                        canChangeWifi = false;
                        break;
                }
                if (setWifiAPState) {
                    wifiApManager.setWifiApState(isWifiAPEnabled);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        }

        if (canChangeWifi) {
            // nahodenie WiFi
            if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) == GlobalData.HARDWARE_CHECK_ALLOWED) {
                if (!WifiApManager.isWifiAPEnabled(context)) { // only when wifi AP is not enabled, change wifi
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiManager.getWifiState();
                    boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                    boolean setWifiState = false;
                    switch (profile._deviceWiFi) {
                        case 1:
                            if (!isWifiEnabled) {
                                isWifiEnabled = true;
                                setWifiState = true;
                            }
                            break;
                        case 2:
                            if (isWifiEnabled) {
                                isWifiEnabled = false;
                                setWifiState = true;
                            }
                            break;
                        case 3:
                            isWifiEnabled = !isWifiEnabled;
                            setWifiState = true;
                            break;
                    }
                    if (setWifiState) {
                        try {
                            wifiManager.setWifiEnabled(isWifiEnabled);
                        } catch (Exception e) {
                            // barla pre security exception INTERACT_ACROSS_USERS - chyba ROM
                            wifiManager.setWifiEnabled(isWifiEnabled);
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }

        // nahodenie bluetooth
        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) == GlobalData.HARDWARE_CHECK_ALLOWED)
        {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean isBluetoothEnabled = bluetoothAdapter.isEnabled();
            boolean setBluetoothState = false;
            switch (profile._deviceBluetooth) {
                case 1 :
                    if (!isBluetoothEnabled)
                    {
                        isBluetoothEnabled = true;
                        setBluetoothState = true;
                    }
                    break;
                case 2 :
                    if (isBluetoothEnabled)
                    {
                        isBluetoothEnabled = false;
                        setBluetoothState = true;
                    }
                    break;
                case 3 :
                    isBluetoothEnabled = ! isBluetoothEnabled;
                    setBluetoothState = true;
                    break;
            }
            if (setBluetoothState)
            {
                if (isBluetoothEnabled)
                    bluetoothAdapter.enable();
                else
                    bluetoothAdapter.disable();
            }
        }

        // nahodenie GPS
        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_GPS, context) == GlobalData.HARDWARE_CHECK_ALLOWED)
        {
            String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            switch (profile._deviceGPS) {
                case 1 :
                    setGPS(context, true);
                    break;
                case 2 :
                    setGPS(context, false);
                    break;
                case 3 :
                    if (!provider.contains("gps"))
                    {
                        setGPS(context, true);
                    }
                    else
                    if (provider.contains("gps"))
                    {
                        setGPS(context, false);
                    }
                    break;
            }
        }

        // nahodenie NFC - len v PPHelper
    }

    public void executeForRadios(Profile profile)
    {
        boolean _isAirplaneMode = false;
        boolean _setAirplaneMode = false;
        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == GlobalData.HARDWARE_CHECK_ALLOWED)
        {
            _isAirplaneMode = isAirplaneMode(context);
            switch (profile._deviceAirplaneMode) {
                case 1:
                    if (!_isAirplaneMode)
                    {
                        _isAirplaneMode = true;
                        _setAirplaneMode = true;
                    }
                    break;
                case 2:
                    if (_isAirplaneMode)
                    {
                        _isAirplaneMode = false;
                        _setAirplaneMode = true;
                    }
                    break;
                case 3:
                    _isAirplaneMode = !_isAirplaneMode;
                    _setAirplaneMode = true;
                    break;
            }
        }

        if (_setAirplaneMode && _isAirplaneMode)
            // switch ON airplane mode, set it before executeForRadios
            setAirplaneMode(context, _isAirplaneMode);

        doExecuteForRadios(profile);

        if (_setAirplaneMode && !(_isAirplaneMode))
            // switch OFF airplane mode, set if after executeForRadios
            setAirplaneMode(context, _isAirplaneMode);

    }

    private static final int ZENMODE_ALL = 0;
    private static final int ZENMODE_PRIORITY = 1;
    private static final int ZENMODE_NONE = 2;

    @SuppressLint("NewApi")
    public void setVolumes(Profile profile, AudioManager audioManager)
    {
        if (profile.getVolumeSystemChange())
        {
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, profile.getVolumeSystemValue(), 0);
            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
        }
        // when system volume changed, also set the ringer and notification volume
        if (profile.getVolumeRingtoneChange() || profile.getVolumeSystemChange()) {
            if (profile.getVolumeRingtoneChange())
                GlobalData.setRingerVolume(context, profile.getVolumeRingtoneValue());
            if (!GlobalData.applicationUnlinkRingerNotificationVolumes) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, GlobalData.getRingerVolume(context), 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
            }
        }
        if (profile.getVolumeNotificationChange() || profile.getVolumeSystemChange()) {
            if (profile.getVolumeNotificationChange())
                GlobalData.setNotificationVolume(context, profile.getVolumeNotificationValue());
            if (!GlobalData.applicationUnlinkRingerNotificationVolumes) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, GlobalData.getNotificationVolume(context), 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
            }
        }
        if (GlobalData.applicationUnlinkRingerNotificationVolumes) {
            boolean doUnlink = audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
            if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                    (Settings.Global.getInt(context.getContentResolver(), "zen_mode", ZENMODE_ALL) == ZENMODE_PRIORITY))
                doUnlink = true;

            if (doUnlink) {
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int callState = telephony.getCallState();
                if (callState == TelephonyManager.CALL_STATE_RINGING) {
                    // for separating ringing and notification
                    // in ringing state ringer volumes must by set
                    // and notification volumes must not by set
                    int volume = GlobalData.getRingerVolume(context);
                    if (volume != -999) {
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                    }
                } else {
                    // for separating ringing and notification
                    // in not ringing state ringer and notification volume must by change
                    int volume = GlobalData.getRingerVolume(context);
                    if (volume != -999) {
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                    }
                    volume = GlobalData.getNotificationVolume(context);
                    if (volume != -999) {
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                    }
                }
            }
        }
        if (profile.getVolumeMediaChange())
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, profile.getVolumeMediaValue());
        }
        if (profile.getVolumeAlarmChange())
        {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, profile.getVolumeAlarmValue(), 0);
            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, profile.getVolumeAlarmValue());
        }
        if (profile.getVolumeVoiceChange())
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, profile.getVolumeVoiceValue(), 0);
            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());

    }

    private void setZenMode(int mode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            if (GlobalData.grantRoot(false) && (GlobalData.settingsBinaryExists()))
            {
                String command1 = "settings put global zen_mode " + mode;
                //if (GlobalData.isSELinuxEnforcing())
                //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                Command command = new Command(0, false, command1);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                    //RootTools.closeAllShells();
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setZenMode", e.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void setRingerMode(Profile profile, AudioManager audioManager, boolean forSilent)
    {
        if (!forSilent) {
            if (profile._volumeRingerMode != 0)
                GlobalData.setRingerMode(context, profile._volumeRingerMode);
        }
        int ringerMode = GlobalData.getRingerMode(context);
        if (forSilent) {
            if (ringerMode != 4)
                return;
            else
                ringerMode = 1;
        }

        switch (ringerMode) {
        case 1:  // Ring
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", 0);
            //setZenMode(ZENMODE_ALL);
            break;
        case 2:  // Ring & Vibrate
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", 1);
            //setZenMode(ZENMODE_ALL);
            break;
        case 3:  // Vibrate
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", 1);
            //setZenMode(ZENMODE_ALL);
            break;
        case 4:  // Silent
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try
            {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", 0);
            //setZenMode(ZENMODE_PRIORITY);
            break;
        case 5: // Zen mode
            switch (profile._volumeZenMode) {
                case 1:
                    setZenMode(ZENMODE_ALL);
                    break;
                case 2:
                    setZenMode(ZENMODE_PRIORITY);
                    break;
                case 3:
                    setZenMode(ZENMODE_NONE);
                    break;
            }
            break;
        }
    }

    public void executeForWallpaper(Profile profile) {
        if (profile._deviceWallpaperChange == 1)
        {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            display.getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels << 1; // best wallpaper width is twice screen width
            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmap(profile.getDeviceWallpaperIdentifier(), width, height);
            if (decodedSampleBitmap != null)
            {
                // set wallpaper
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                try {
                    wallpaperManager.setBitmap(decodedSampleBitmap);
                } catch (IOException e) {
                    Log.e("ActivateProfileHelper.executeForWallpaper", "Cannot set wallpaper. Image="+profile.getDeviceWallpaperIdentifier());
                }
            }
        }
    }

    public void execute(Profile _profile, boolean _interactive)
    {
        // rozdelit zvonenie a notifikacie - zial je to oznacene ako @Hide :-(
        //Settings.System.putInt(context.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        final Profile profile = GlobalData.getMappedProfile(_profile, context);
        final boolean interactive = _interactive;

        // nahodenie volume a ringer modu
        // run service for execute volumes
        Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
        volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
        //WakefulIntentService.sendWakefulWork(context, radioServiceIntent);
        context.startService(volumeServiceIntent);
        /*AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        // nahodenie ringer modu - aby sa mohli nastavit hlasitosti
        setRingerMode(profile, audioManager);
        setVolumes(profile, audioManager);
        // nahodenie ringer modu - hlasitosti zmenia silent/vibrate
        setRingerMode(profile, audioManager);*/

        // set vibration on touch
        switch (profile._vibrationOnTouch) {
            case 1:
                Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                break;
            case 2:
                Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                break;
        }

        // nahodenie tonov
        if (profile._soundRingtoneChange == 1)
        {
            if (profile._soundRingtone.isEmpty())
                Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, null);
            else
                Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, profile._soundRingtone);
        }
        if (profile._soundNotificationChange == 1)
        {
            if (profile._soundNotification.isEmpty())
                Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, null);
            else
                Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, profile._soundNotification);
        }
        if (profile._soundAlarmChange == 1)
        {
            if (profile._soundAlarm.isEmpty())
                Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, null);
            else
                Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, profile._soundAlarm);
        }

        // nahodenie radio preferences
        // run service for execute radios
        Intent radioServiceIntent = new Intent(context, ExecuteRadioProfilePrefsService.class);
        radioServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
        context.startService(radioServiceIntent);

        // nahodenie auto-sync
        boolean _isAutosync = ContentResolver.getMasterSyncAutomatically();
        boolean _setAutosync = false;
        switch (profile._deviceAutosync) {
            case 1:
                if (!_isAutosync)
                {
                    _isAutosync = true;
                    _setAutosync = true;
                }
                break;
            case 2:
                if (_isAutosync)
                {
                    _isAutosync = false;
                    _setAutosync = true;
                }
                break;
            case 3:
                _isAutosync = !_isAutosync;
                _setAutosync = true;
                break;
        }
        if (_setAutosync)
            ContentResolver.setMasterSyncAutomatically(_isAutosync);

        // screen timeout
        switch (profile._deviceScreenTimeout) {
            case 1:
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                break;
            case 2:
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                break;
            case 3:
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                break;
            case 4:
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                break;
            case 5:
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                break;
            case 6:
                if (android.os.Build.VERSION.SDK_INT < 19)
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 18000000);
                break;
        }

        // zapnutie/vypnutie lockscreenu
        boolean setLockscreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                // enable lockscreen
                GlobalData.setLockscreenDisabled(context, false);
                setLockscreen = true;
                break;
            case 2:
                // disable lockscreen
                GlobalData.setLockscreenDisabled(context, true);
                setLockscreen = true;
                break;
        }
        if (setLockscreen) {
            boolean isScreenOn;
            //if (android.os.Build.VERSION.SDK_INT >= 20)
            //{
            //	Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            //	isScreenOn = display.getState() != Display.STATE_OFF;
            //}
            //else
            //{
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            //}
            //GlobalData.logE("$$$ ActivateProfileHelper.execute","isScreenOn="+isScreenOn);
            boolean keyguardShowing = false;
            KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 16)
                keyguardShowing = kgMgr.isKeyguardLocked();
            else
                keyguardShowing = kgMgr.inKeyguardRestrictedInputMode();
            //GlobalData.logE("$$$ ActivateProfileHelper.execute","keyguardShowing="+keyguardShowing);

            if (isScreenOn && !keyguardShowing) {
                Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
                context.startService(keyguardService);
            }
        }

        // nahodenie podsvietenia
        if (profile.getDeviceBrightnessChange())
        {
            if (profile.getDeviceBrightnessAutomatic())
            {
                Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        profile.getDeviceBrightnessManualValue(context));
                if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                    Settings.System.putFloat(context.getContentResolver(),
                            ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                            profile.getDeviceBrightnessAdaptiveValue(context));
            }
            else
            {
                Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            profile.getDeviceBrightnessManualValue(context));
            }

            if (brightnessHandler != null)
            {
                final Profile __profile = profile;
                final Context __context = context;
                brightnessHandler.post(new Runnable() {
                    public void run() {
                        createBrightnessView(__profile, __context);
                    }
                });
            }
            else
                createBrightnessView(profile, context);
        }

        // nahodenie rotate
        switch (profile._deviceAutoRotate) {
            case 1:
                // set autorotate on
                Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                break;
            case 2:
                // set autorotate off
                // degree 0
                Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                break;
            case 3:
                // set autorotate off
                // degree 90
                Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                break;
            case 4:
                // set autorotate off
                // degree 180
                Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                break;
            case 5:
                // set autorotate off
                // degree 270
                Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                break;
        }

        // nahodenie pozadia
        if (profile._deviceWallpaperChange == 1) {
            Intent wallpaperServiceIntent = new Intent(context, ExecuteWallpaperProfilePrefsService.class);
            wallpaperServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
            context.startService(wallpaperServiceIntent);
        }

        if (interactive)
        {
            // preferences, ktore vyzaduju interakciu uzivatela

            if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == GlobalData.HARDWARE_CHECK_ALLOWED)
            {
                if (profile._deviceMobileDataPrefs == 1)
                {
                    final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                        //intent.addCategory(Intent.ACTION_MAIN);
                        intent.setComponent(componentName);
                        activity.startActivity(intent);
                    }
                }
            }

            //if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_GPS, context))
            //{  No check only GPS
                if (profile._deviceLocationServicePrefs == 1)
                {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }
            //}

            if (profile._deviceRunApplicationChange == 1)
            {
                Intent intent;
                PackageManager packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage(profile._deviceRunApplicationPackageName);
                if (intent != null)
                {
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }
            }

        }

    }

    @SuppressLint("RtlHardcoded")
    private void createBrightnessView(Profile profile, Context context)
    {
        //if (context != null)
        //{
            RemoveBrightnessViewBroadcastReceiver.setAlarm(context);

            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            if (GUIData.brightneesView != null)
            {
                windowManager.removeView(GUIData.brightneesView);
                GUIData.brightneesView = null;
            }
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        WindowManager.LayoutParams.TYPE_TOAST,
                        //TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT
                    );
            params.gravity = Gravity.RIGHT | Gravity.TOP;
            if (profile.getDeviceBrightnessAutomatic())
                params.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else
                params.screenBrightness = profile.getDeviceBrightnessManualValue(context) / (float) 255;
            GUIData.brightneesView = new BrightnessView(context);
            windowManager.addView(GUIData.brightneesView, params);
        //}
    }

    public void showNotification(Profile profile)
    {
        if (lockRefresh)
            // no refres notification
            return;

        if (GlobalData.notificationStatusBar)
        {
                // close showed notification
                //notificationManager.cancel(GlobalData.NOTIFICATION_ID);
                // vytvorenie intentu na aktivitu, ktora sa otvori na kliknutie na notifikaciu
                Intent intent = new Intent(context, ActivateProfileActivity.class);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                // nastavime, ze aktivita sa spusti z notifikacnej listy
                intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_NOTIFICATION);
                PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                // vytvorenie samotnej notifikacie
                NotificationCompat.Builder notificationBuilder;
                RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer);

                boolean isIconResourceID;
                String iconIdentifier;
                String profileName;
                Bitmap iconBitmap;
                Bitmap preferencesIndicator;

                if (profile != null)
                {
                    isIconResourceID = profile.getIsIconResourceID();
                    iconIdentifier = profile.getIconIdentifier();
                    profileName = profile._name;
                    iconBitmap = profile._iconBitmap;
                    preferencesIndicator = profile._preferencesIndicator;
                }
                else
                {
                    isIconResourceID = true;
                    iconIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
                    profileName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                    iconBitmap = null;
                    preferencesIndicator = null;
                }

                notificationBuilder = new NotificationCompat.Builder(context)
                        .setContentIntent(pIntent);

                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    if (GlobalData.notificationShowInStatusBar)
                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    else
                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
                    //notificationBuilder.setPriority(Notification.PRIORITY_HIGH); // for heads-up in Android 5.0
                }
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                notificationBuilder.setTicker(profileName);

                if (isIconResourceID)
                {
                    int iconSmallResource;
                    if (iconBitmap != null) {
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                    }
                    else {
                        if (GlobalData.notificationStatusBarStyle.equals("0") && (android.os.Build.VERSION.SDK_INT < 21)) {
                            //notificationBuilder.setSmallIcon(0);
                            iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", context.getPackageName());
                            notificationBuilder.setSmallIcon(iconSmallResource);
                            //contentView.setImageViewResource(R.id.notification_activated_profile_icon, 0);
                            contentView.setImageViewResource(R.id.notification_activated_profile_icon, iconSmallResource);
                        } else {
                            //notificationBuilder.setSmallIcon(0);
                            //contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, null);
                            iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                            notificationBuilder.setSmallIcon(iconSmallResource);
                            int iconLargeResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResource);
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        }
                    }
                }
                else
                {
                    int iconSmallResource;
                    if (GlobalData.notificationStatusBarStyle.equals("0"))
                        iconSmallResource = R.drawable.ic_profile_default;
                    else
                        iconSmallResource = R.drawable.ic_profile_default_notify;

                    //notificationBuilder.setSmallIcon(0);
                    notificationBuilder.setSmallIcon(iconSmallResource);

                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                }


                Notification notification = notificationBuilder.build();

                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

                //contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator,
                //		ProfilePreferencesIndicator.paint(profile, context));
                if ((preferencesIndicator != null) && (GlobalData.notificationPrefIndicator))
                    contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
                else
                    contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

                notification.contentView = contentView;

                if (GlobalData.notificationStatusBarPermanent)
                {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                }
                else
                {
                    setAlarmForNotificationCancel();
                }

                notificationManager.notify(GlobalData.PROFILE_NOTIFICATION_ID, notification);
        }
        else
        {
            notificationManager.cancel(GlobalData.PROFILE_NOTIFICATION_ID);
        }
    }

    public void removeNotification()
    {
        notificationManager.cancel(GlobalData.PROFILE_NOTIFICATION_ID);
    }

    private void setAlarmForNotificationCancel()
    {
        if (GlobalData.notificationStatusBarCancel.isEmpty() || GlobalData.notificationStatusBarCancel.equals("0"))
            return;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + Integer.valueOf(GlobalData.notificationStatusBarCancel) * 1000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

    }

    public void updateWidget()
    {
        if (lockRefresh)
            // no refres widgets
            return;

        // icon widget
        Intent intent = new Intent(context, IconWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        // one row widget
        Intent intent4 = new Intent(context, OneRowWidgetProvider.class);
        intent4.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids4[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
        intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4);
        context.sendBroadcast(intent4);

        // list widget
        Intent intent2 = new Intent(context, ProfileListWidgetProvider.class);
        intent2.setAction(ProfileListWidgetProvider.INTENT_REFRESH_LISTWIDGET);
        int ids2[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
        context.sendBroadcast(intent2);

        // dashclock extension
        Intent intent3 = new Intent();
        intent3.setAction(DashClockBroadcastReceiver.INTENT_REFRESH_DASHCLOCK);
        context.sendBroadcast(intent3);

        // activities
        Intent intent5 = new Intent();
        intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
        context.sendBroadcast(intent5);
    }



    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private boolean isAirplaneMode(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            return Settings.Global.getInt(context.getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;
        else
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneMode(Context context, boolean mode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            setAirplaneMode_SDK17(context, mode);
        else
            setAirplaneMode_SDK8(context, mode);
    }

    private boolean isMobileData(Context context)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return (Boolean)getMobileDataEnabledMethod.invoke(connectivityManager);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

        /*
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
        {
            int netvorkType = networkInfo.getType(); // 0 = mobile, 1 = wifi
            //String netvorkTypeName = networkInfo.getTypeName(); // "mobile" or "WIFI"
            boolean connected = networkInfo.isConnected();  // true = active connection

            //if (netvorkType == 0)
            //{
                // connected into mobile data
                return connected;
            //}
            //else
            //{
                // conected into Wifi
            //	return false;
            //}
        }
        else
            return false;
        */
    }

    private void setMobileData(Context context, boolean enable)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean OK = false;
        try {
            final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Field iConnectivityManagerField = connectivityManagerClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
            final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);

            OK = true;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (!OK)
        {
            try {
                Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);

                setMobileDataEnabledMethod.setAccessible(true);
                setMobileDataEnabledMethod.invoke(connectivityManager, enable);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressWarnings("deprecation")
    private void setGPS(Context context, boolean enable)
    {
        boolean isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled)  && enable)
        {
            if (GlobalData.canExploitGPS(context))
            {
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            if ((android.os.Build.VERSION.SDK_INT >= 17) && GlobalData.grantRoot(false))
            {
                // zariadenie je rootnute
                String command1;
                //String command2;

                String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                String newSet;
                if (provider == "")
                    newSet = LocationManager.GPS_PROVIDER;
                else
                    newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);

                command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                //if (GlobalData.isSELinuxEnforcing())
                //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);

                //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state true";
                Command command = new Command(0, false, command1); //, command2);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                    //RootTools.closeAllShells();
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setGPS", "Error on run su");
                }
            }
            else
            {
                /*try {
                    Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                    intent.putExtra("enabled", enable);
                    context.sendBroadcast(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }*/

                // for normal apps it is only possible to open the system settings dialog
            /*	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); */
            }
        }
        else
        //if(provider.contains(LocationManager.GPS_PROVIDER) && (!enable))
        if (isEnabled && (!enable))
        {
            if (GlobalData.canExploitGPS(context))
            {
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            else
            if ((android.os.Build.VERSION.SDK_INT >= 17) && GlobalData.grantRoot(false))
            {
                // zariadenie je rootnute
                String command1;
                //String command2;

                String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                String[] list = provider.split(",");

                String newSet = "";
                int j = 0;
                for (int i = 0; i < list.length; i++)
                {

                    if  (!list[i].equals(LocationManager.GPS_PROVIDER))
                    {
                        if (j > 0)
                            newSet += ",";
                        newSet += list[i];
                        j++;
                    }
                }

                command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                //if (GlobalData.isSELinuxEnforcing())
                //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state false";
                Command command = new Command(0, false, command1);//, command2);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                    //RootTools.closeAllShells();
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setGPS", "Error on run su");
                }
            }
            else
            {
            /*	try {
                    Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                    intent.putExtra("enabled", enable);
                    context.sendBroadcast(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            */

                // for normal apps it is only possible to open the system settings dialog
            /*	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); */
            }
        }	    	
    }

    private void setAirplaneMode_SDK17(Context context, boolean mode)
    {
        if (GlobalData.grantRoot(false))
        {
            // zariadenie je rootnute

            String command1;
            String command2;
            if (mode)
            {
                command1 = "settings put global airplane_mode_on 1";
                command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
            }
            else
            {
                command1 = "settings put global airplane_mode_on 0";
                command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
            }
            //if (GlobalData.isSELinuxEnforcing())
            //{
            //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
            //	command2 = GlobalData.getSELinuxEnforceCommand(command2, Shell.ShellContext.SYSTEM_APP);
            //}
            Command command = new Command(0, false, command1, command2);
            try {
                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                commandWait(command);
                //RootTools.closeAllShells();
            } catch (Exception e) {
                Log.e("AirPlaneMode_SDK17.setAirplaneMode", "Error on run su");
            }
        }
        else
        {
            //Log.e("ActivateProfileHelper.setAirplaneMode_SDK17","root NOT granted");
            // for normal apps it is only possible to open the system settings dialog
        /*	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent); */
        }
    }

    @SuppressWarnings("deprecation")
    private void setAirplaneMode_SDK8(Context context, boolean mode)
    {
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, mode ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", mode);
        context.sendBroadcast(intent);
    }

    private void commandWait(Command cmd) throws Exception {
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
            Log.e("ActivateProfileHelper", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    public static int getMinimumScreenBrightnessSetting (Context context)
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

    public static int getMaximumScreenBrightnessSetting (Context context)
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

}
