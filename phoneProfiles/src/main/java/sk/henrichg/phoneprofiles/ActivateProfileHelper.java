package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.DEVICE_POLICY_SERVICE;

public class ActivateProfileHelper {

    private DataWrapper dataWrapper;

    private Context context;
    private NotificationManager notificationManager;

    static boolean lockRefresh = false;

    static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    // Setting.Global "zen_mode"
    static final int ZENMODE_ALL = 0;
    static final int ZENMODE_PRIORITY = 1;
    static final int ZENMODE_NONE = 2;
    static final int ZENMODE_ALARMS = 3;
    @SuppressWarnings("WeakerAccess")
    static final int ZENMODE_SILENT = 99;

    static final String EXTRA_LINKUNLINK_VOLUMES = "link_unlink_volumes";
    static final String EXTRA_FOR_PROFILE_ACTIVATION = "for_profile_activation";

    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    private static final String PREF_SCREEN_UNLOCKED = "screen_unlocked";
    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT = "activated_profile_screen_timeout";
    static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";

    public ActivateProfileHelper()
    {

    }

    public void initialize(DataWrapper dataWrapper, Context c)
    {
        this.dataWrapper = dataWrapper;
        initializeNoNotificationManager(c);
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void initializeNoNotificationManager(Context c)
    {
        context = c;
    }

    void deinitialize()
    {
        dataWrapper = null;
        context = null;
        notificationManager = null;
    }

    @SuppressWarnings("deprecation")
    private void doExecuteForRadios(Profile profile)
    {
        //try { Thread.sleep(300); } catch (InterruptedException e) { }
        //SystemClock.sleep(300);
        PPApplication.sleep(300);

        // nahodenie network type
        if (profile._deviceNetworkType >= 100) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, context) == PPApplication.PREFERENCE_ALLOWED)
            {
                setPreferredNetworkType(context, profile._deviceNetworkType - 100);
                //try { Thread.sleep(200); } catch (InterruptedException e) { }
                //SystemClock.sleep(200);
                PPApplication.sleep(200);
            }
        }

        // nahodenie mobilnych dat
        if (profile._deviceMobileData != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == PPApplication.PREFERENCE_ALLOWED) {
                boolean _isMobileData = isMobileData(context);
                boolean _setMobileData = false;
                switch (profile._deviceMobileData) {
                    case 1:
                        if (!_isMobileData) {
                            _isMobileData = true;
                            _setMobileData = true;
                        }
                        break;
                    case 2:
                        if (_isMobileData) {
                            _isMobileData = false;
                            _setMobileData = true;
                        }
                        break;
                    case 3:
                        _isMobileData = !_isMobileData;
                        _setMobileData = true;
                        break;
                }
                if (_setMobileData) {
                    setMobileData(context, _isMobileData);
                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    PPApplication.sleep(200);
                }
            }
        }

        // nahodenie WiFi AP
        boolean canChangeWifi = true;
        if (profile._deviceWiFiAP != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, context) == PPApplication.PREFERENCE_ALLOWED) {
                WifiApManager wifiApManager = null;
                try {
                    wifiApManager = new WifiApManager(context);
                } catch (NoSuchMethodException ignored) {
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
                            canChangeWifi = !isWifiAPEnabled;
                            break;
                    }
                    if (setWifiAPState) {
                        wifiApManager.setWifiApState(isWifiAPEnabled);
                        //try { Thread.sleep(200); } catch (InterruptedException e) { }
                        //SystemClock.sleep(200);
                        PPApplication.sleep(200);
                    }
                }
            }
        }

        if (canChangeWifi) {
            // nahodenie WiFi
            if (profile._deviceWiFi != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, context) == PPApplication.PREFERENCE_ALLOWED) {
                    boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                    if ((!isWifiAPEnabled) || (profile._deviceWiFi == 4)) { // only when wifi AP is not enabled, change wifi
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        int wifiState = wifiManager.getWifiState();
                        boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                        boolean setWifiState = false;
                        switch (profile._deviceWiFi) {
                            case 1:
                            case 4:
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
                            case 5:
                                isWifiEnabled = !isWifiEnabled;
                                setWifiState = true;
                                break;
                        }
                        if (setWifiState) {
                            try {
                                wifiManager.setWifiEnabled(isWifiEnabled);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.doExecuteForRadios", e.toString());
                            }
                            //try { Thread.sleep(200); } catch (InterruptedException e) { }
                            //SystemClock.sleep(200);
                            PPApplication.sleep(200);
                        }
                    }
                }
            }

            // connect to SSID
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, context) == PPApplication.PREFERENCE_ALLOWED) {
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiManager.getWifiState();
                    if  (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                        // check if wifi is connected
                        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
                        boolean wifiConnected = (activeNetwork != null) &&
                                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                                activeNetwork.isConnected();
                        WifiInfo wifiInfo = null;
                        if (wifiConnected)
                            wifiInfo = wifiManager.getConnectionInfo();

                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        if (list != null) {
                            for (WifiConfiguration i : list) {
                                if (i.SSID != null && i.SSID.equals(profile._deviceConnectToSSID)) {
                                    if (wifiConnected) {
                                        if (!wifiInfo.getSSID().equals(i.SSID)) {
                                            // conected to another SSID
                                            wifiManager.disconnect();
                                            wifiManager.enableNetwork(i.networkId, true);
                                            wifiManager.reconnect();
                                        }
                                    } else
                                        wifiManager.enableNetwork(i.networkId, true);
                                    break;
                                }
                            }
                        }
                    }
                }
                //else {
                //    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //    int wifiState = wifiManager.getWifiState();
                //    if  (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                //        wifiManager.disconnect();
                //        wifiManager.reconnect();
                //    }
                //}
                PhoneProfilesService.connectToSSID = profile._deviceConnectToSSID;
            }
        }

        // nahodenie bluetooth
        if (profile._deviceBluetooth != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, context) == PPApplication.PREFERENCE_ALLOWED) {
                BluetoothAdapter bluetoothAdapter;
                if (android.os.Build.VERSION.SDK_INT < 18)
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                else {
                    BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
                    bluetoothAdapter = bluetoothManager.getAdapter();
                }
                if (bluetoothAdapter != null) {
                    boolean isBluetoothEnabled = bluetoothAdapter.isEnabled();
                    boolean setBluetoothState = false;
                    switch (profile._deviceBluetooth) {
                        case 1:
                            if (!isBluetoothEnabled) {
                                isBluetoothEnabled = true;
                                setBluetoothState = true;
                            }
                            break;
                        case 2:
                            if (isBluetoothEnabled) {
                                isBluetoothEnabled = false;
                                setBluetoothState = true;
                            }
                            break;
                        case 3:
                            isBluetoothEnabled = !isBluetoothEnabled;
                            setBluetoothState = true;
                            break;
                    }
                    if (setBluetoothState) {
                        if (isBluetoothEnabled)
                            bluetoothAdapter.enable();
                        else
                            bluetoothAdapter.disable();
                    }
                }
            }
        }

        // nahodenie GPS
        if (profile._deviceGPS != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, context) == PPApplication.PREFERENCE_ALLOWED) {
                boolean isEnabled;
                if (android.os.Build.VERSION.SDK_INT < 19)
                    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                }

                switch (profile._deviceGPS) {
                    case 1:
                        setGPS(context, true);
                        break;
                    case 2:
                        setGPS(context, false);
                        break;
                    case 3:
                        if (!isEnabled) {
                            setGPS(context, true);
                        } else {
                            setGPS(context, false);
                        }
                        break;
                }
            }
        }

        // nahodenie NFC
        if (profile._deviceNFC != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, context) == PPApplication.PREFERENCE_ALLOWED) {
                //Log.e("ActivateProfileHelper.doExecuteForRadios", "allowed");
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                if (nfcAdapter != null) {
                    switch (profile._deviceNFC) {
                        case 1:
                            setNFC(context, true);
                            break;
                        case 2:
                            setNFC(context, false);
                            break;
                        case 3:
                            if (!nfcAdapter.isEnabled()) {
                                setNFC(context, true);
                            } else if (nfcAdapter.isEnabled()) {
                                setNFC(context, false);
                            }
                            break;
                    }
                }
            }
            //else
            //    Log.e("ActivateProfileHelper.doExecuteForRadios", "not allowed");
        }
    }

    void executeForRadios(Profile profile)
    {
        boolean _isAirplaneMode = false;
        boolean _setAirplaneMode = false;
        if (profile._deviceAirplaneMode != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                _isAirplaneMode = isAirplaneMode(context);
                switch (profile._deviceAirplaneMode) {
                    case 1:
                        if (!_isAirplaneMode) {
                            _isAirplaneMode = true;
                            _setAirplaneMode = true;
                        }
                        break;
                    case 2:
                        if (_isAirplaneMode) {
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
        }

        if (_setAirplaneMode /*&& _isAirplaneMode*/) {
            // switch ON airplane mode, set it before executeForRadios
            setAirplaneMode(context, _isAirplaneMode);

            PPApplication.sleep(2000);
        }

        doExecuteForRadios(profile);

        /*if (_setAirplaneMode && (!_isAirplaneMode)) {
            // 200 miliseconds is in doExecuteForRadios
            PPApplication.sleep(1800);

            // switch OFF airplane mode, set if after executeForRadios
            setAirplaneMode(context, _isAirplaneMode);
        }*/

    }

    private static boolean isAudibleRinging(int ringerMode, int zenMode) {
        return (!((ringerMode == 3) || (ringerMode == 4) ||
                ((ringerMode == 5) && ((zenMode == 3) || (zenMode == 4) || (zenMode == 5) || (zenMode == 6)))
        ));
    }

    private boolean isVibrateRingerMode(int ringerMode) {
        return (ringerMode == 3);

    }

    /*
    private void correctVolume0(AudioManager audioManager, int linkUnlink) {
        int ringerMode, zenMode;
        if (linkUnlink == PhoneCallService.LINKMODE_NONE) {
            ringerMode = PPApplication.getRingerMode(context);
            zenMode = PPApplication.getZenMode(context);
        }
        else {
            ringerMode = PPApplication.getRingerMode(context);
            zenMode = PPApplication.getZenMode(context);
            //ringerMode = RingerModeChangeReceiver.getRingerMode(context, audioManager);
        }
        if ((ringerMode == 1) || (ringerMode == 2) || (ringerMode == 4) ||
            ((ringerMode == 5) && ((zenMode == 1) || (zenMode == 2)))) {
            // any "nonVIBRATE" ringer mode is selected
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                //Log.e("ActivateProfileHelper","correctVolume0 set ring volume=1");
                // actual system ringer mode = vibrate
                // volume changed it to vibrate
                //RingerModeChangeReceiver.internalChange = true;
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, 1);
            }
        }
    }
    */

    static boolean getMergedRingNotificationVolumes(Context context) {
        ApplicationPreferences.getSharedPreferences(context);
        if (ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context) > 0)
            return ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context) == 1;
        else
            return ApplicationPreferences.preferences.getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true);
    }

    // test if ring and notification volumes are merged
    static void setMergedRingNotificationVolumes(Context context, boolean force) {
        ApplicationPreferences.getSharedPreferences(context);

        PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        setMergedRingNotificationVolumes(context, force, editor);
        editor.apply();
    }

    static void setMergedRingNotificationVolumes(Context context, boolean force, SharedPreferences.Editor editor) {
        ApplicationPreferences.getSharedPreferences(context);

        PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

        if (!ApplicationPreferences.preferences.contains(PREF_MERGED_RING_NOTIFICATION_VOLUMES) || force) {
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
                    PPApplication.sleep(1000);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_RING) == newNotificationVolume)
                        merged = true;
                    else
                        merged = false;
                } else
                    merged = false;
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, 0);
                audioManager.setRingerMode(ringerMode);

                PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "merged="+merged);

                editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
            } catch (Exception ignored) {}
        }
    }

    @SuppressLint("NewApi")
    void setVolumes(Profile profile, AudioManager audioManager, int linkUnlink, boolean forProfileActivation)
    {
        if (profile.getVolumeRingtoneChange()) {
            if (forProfileActivation)
                setRingerVolume(context, profile.getVolumeRingtoneValue());
        }
        if (profile.getVolumeNotificationChange()) {
            if (forProfileActivation)
                setNotificationVolume(context, profile.getVolumeNotificationValue());
        }

        int ringerMode = getRingerMode(context);
        int zenMode = getZenMode(context);

        // for ringer mode VIBRATE or SILENT (and not for link/unlink volumes) or
        // for interruption types NONE and ONLY_ALARMS
        // not set system, ringer, npotification volume
        // (Android 6 - priority mode = ONLY_ALARMS)
        if (isAudibleRinging(ringerMode, zenMode)) {

            //if (Permissions.checkAccessNotificationPolicy(context)) {

                if (forProfileActivation) {
                    if (profile.getVolumeSystemChange()) {
                        try {
                        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, profile.getVolumeSystemValue(), 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
                        //correctVolume0(/*profile, */audioManager, linkUnlink);
                        } catch (Exception ignored) { }
                    }
                }

                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int callState = telephony.getCallState();

                boolean volumesSet = false;
                if (getMergedRingNotificationVolumes(context) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
                    //if (doUnlink) {
                    //if (linkUnlink == PhoneCallService.LINKMODE_UNLINK) {
                    if (callState == TelephonyManager.CALL_STATE_RINGING) {
                        // for separating ringing and notification
                        // in ringing state ringer volumes must by set
                        // and notification volumes must not by set
                        int volume = getRingerVolume(context);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                                //correctVolume0(/*profile, */audioManager, linkUnlink);
                            } catch (Exception ignored) { }
                        }
                        volumesSet = true;
                    } else if (linkUnlink == PhoneCallService.LINKMODE_LINK) {
                        // for separating ringing and notification
                        // in not ringing state ringer and notification volume must by change
                        //Log.e("ActivateProfileHelper","setVolumes get audio mode="+audioManager.getMode());
                        int volume = getRingerVolume(context);
                        if (volume != -999) {
                            //Log.e("ActivateProfileHelper","setVolumes set ring volume="+volume);
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                            } catch (Exception ignored) { }
                        }
                        volume = getNotificationVolume(context);
                        if (volume != -999) {
                            //Log.e("ActivateProfileHelper","setVolumes set notification volume="+volume);
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                            } catch (Exception ignored) { }
                        }
                        //correctVolume0(/*profile, */audioManager, linkUnlink);
                        volumesSet = true;
                    } else {
                        int volume = getRingerVolume(context);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                                //correctVolume0(/*profile, */audioManager, linkUnlink);
                            } catch (Exception ignored) { }
                        }
                        volume = getNotificationVolume(context);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                //correctVolume0(/*profile, */audioManager, linkUnlink);
                            } catch (Exception ignored) { }
                        }
                        volumesSet = true;
                    }
                    //}
                }
                if (!volumesSet) {
                    // reverted order for disabled unlink
                    int volume;
                    if (!getMergedRingNotificationVolumes(context)) {
                        volume = getNotificationVolume(context);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                //correctVolume0(/*profile, */audioManager, linkUnlink);
                            } catch (Exception ignored) { }
                        }
                    }
                    volume = getRingerVolume(context);
                    if (volume != -999) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                            //correctVolume0(/*profile, */audioManager, linkUnlink);
                        } catch (Exception ignored) { }
                    }
                }
            //}
        }

        if (forProfileActivation) {
            if (profile.getVolumeMediaChange()) {
                // Fatal Exception: java.lang.SecurityException: Only SystemUI can disable the safe media volume:
                // Neither user 10118 nor current process has android.permission.STATUS_BAR_SERVICE.
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, profile.getVolumeMediaValue());
                } catch (SecurityException e) {
                    if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                        try {
                            Settings.Global.putInt(context.getContentResolver(), "audio_safe_volume_state", 2);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
                        }
                        catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
            if (profile.getVolumeAlarmChange()) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, profile.getVolumeAlarmValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, profile.getVolumeAlarmValue());
                } catch (Exception ignored) {}
            }
            if (profile.getVolumeVoiceChange()) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, profile.getVolumeVoiceValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception ignored) {}
            }
        }

    }

    private void setZenMode(int zenMode, AudioManager audioManager, int ringerMode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            int _zenMode = getSystemZenMode(context, -1);
            PPApplication.logE("ActivateProfileHelper.setZenMode", "_zenMode=" + _zenMode);
            int _ringerMode = audioManager.getRingerMode();
            PPApplication.logE("ActivateProfileHelper.setZenMode", "_ringerMode=" + _ringerMode);

            if ((zenMode != ZENMODE_SILENT) && canChangeZenMode(context, false)) {
                audioManager.setRingerMode(ringerMode);
                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);

                if ((zenMode != _zenMode) || (zenMode == ZENMODE_PRIORITY)) {
                    PPNotificationListenerService.requestInterruptionFilter(context, zenMode);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(context, zenMode);
                /* if (PPApplication.isRootGranted(false) && (PPApplication.settingsBinaryExists()))
                {
                    String command1 = "settings put global zen_mode " + mode;
                    //if (PPApplication.isSELinuxEnforcing())
                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setZenMode", e.getMessage());
                    }
                }*/
                }
            } else {
                try {
                    switch (zenMode) {
                        case ZENMODE_SILENT:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                            //SystemClock.sleep(1000);
                            PPApplication.sleep(1000);
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            break;
                        default:
                            audioManager.setRingerMode(ringerMode);
                    }
                } catch (Exception ignored) {
                    // may be produced this exception:
                    //
                    // java.lang.SecurityException: Not allowed to change Do Not Disturb state
                    //
                    // when changed is ringer mode in activated Do not disturb
                }
            }
        }
        else
            audioManager.setRingerMode(ringerMode);
    }

    private void setVibrateWhenRinging(Profile profile, int value) {
        int lValue = value;
        if (profile != null) {
            switch (profile._vibrateWhenRinging) {
                case 1:
                    lValue = 1;
                    break;
                case 2:
                    lValue = 0;
                    break;
            }
        }

        if (lValue != -1) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, context)
                    == PPApplication.PREFERENCE_ALLOWED) {
                if (Permissions.checkProfileVibrateWhenRinging(context, profile)) {
                    if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                        Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", lValue);
                    else {
                        try {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, lValue);
                        } catch (Exception ee) {
                            Log.e("ActivateProfileHelper.setVibrateWhenRinging", ee.toString());
                            if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                                synchronized (PPApplication.startRootCommandMutex) {
                                    String command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        //RootTools.closeAllShells();
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        commandWait(command);
                                    } catch (Exception e) {
                                        Log.e("ActivateProfileHelper.setVibrateWhenRinging", "Error on run su: " + e.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void setTones(Profile profile) {
        if (Permissions.checkProfileRingtones(context, profile)) {
            if (profile._soundRingtoneChange == 1) {
                if (!profile._soundRingtone.isEmpty()) {
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, profile._soundRingtone);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(profile._soundRingtone));
                    }
                    catch (Exception ignored){ }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, null);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, null);
                    }
                    catch (Exception ignored){ }
                }
            }
            if (profile._soundNotificationChange == 1) {
                if (!profile._soundNotification.isEmpty()) {
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, profile._soundNotification);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(profile._soundNotification));
                    }
                    catch (Exception ignored){ }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, null);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, null);
                    }
                    catch (Exception ignored){ }
                }
            }
            if (profile._soundAlarmChange == 1) {
                if (!profile._soundAlarm.isEmpty()) {
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, profile._soundAlarm);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, Uri.parse(profile._soundAlarm));
                    }
                    catch (Exception ignored){ }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, null);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, null);
                    }
                    catch (Exception ignored){ }
                }
            }
        }
    }

    private void setNotificationLed(int value) {
        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, context)
                == PPApplication.PREFERENCE_ALLOWED) {
            if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putInt(context.getContentResolver(), "notification_light_pulse", value);
            else {
                if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                    synchronized (PPApplication.startRootCommandMutex) {
                        String command1 = "settings put system " + "notification_light_pulse" + " " + value;
                        //if (PPApplication.isSELinuxEnforcing())
                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                        Command command = new Command(0, false, command1); //, command2);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setNotificationLed", "Error on run su: " + e.toString());
                        }
                    }
                }
            }
        }
    }

    void changeRingerModeForVolumeEqual0(Profile profile) {
        if (profile.getVolumeRingtoneChange()) {
            //int ringerMode = PPApplication.getRingerMode(context);
            //int zenMode = PPApplication.getZenMode(context);

            //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "ringerMode=" + ringerMode);
            //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "zenMode=" + zenMode);

            if (profile.getVolumeRingtoneValue() == 0) {
                profile.setVolumeRingtoneValue(1);

                // for profile ringer/zen mode = "only vibrate" do not change ringer mode to Silent
                if (!isVibrateRingerMode(profile._volumeRingerMode)) {
                    // for ringer mode VIBRATE or SILENT or
                    // for interruption types NONE and ONLY_ALARMS
                    // not change ringer mode
                    // (Android 6 - priority mode = ONLY_ALARMS)
                    if (isAudibleRinging(profile._volumeRingerMode, profile._volumeZenMode)) {
                        // change ringer mode to Silent
                        PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "changed to silent");
                        profile._volumeRingerMode = 4;
                    }
                }
            }
        }
    }

    void changeNotificationVolumeForVolumeEqual0(Profile profile) {
        if (profile.getVolumeNotificationChange() && getMergedRingNotificationVolumes(context)) {
            if (profile.getVolumeNotificationValue() == 0) {
                PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "changed notification value to 1");
                profile.setVolumeNotificationValue(1);
            }
        }
    }

    static boolean canChangeZenMode(Context context, boolean notCheckAccess) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
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
    static int getSystemZenMode(Context context, int defaultValue) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
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

    @SuppressWarnings("deprecation")
    static boolean vibrationIsOn(AudioManager audioManager, boolean testRingerMode) {
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

    @SuppressWarnings("deprecation")
    void setRingerMode(Profile profile, AudioManager audioManager, boolean firstCall, boolean forProfileActivation)
    {
        // linkUnlink == LINKMODE_NONE: not do link and unlink volumes for phone call - called from ActivateProfileHelper.execute()
        // linkUnlink != LINKMODE_NONE: do link and unlink volumes for phone call - called from PhoneCallService

        int ringerMode;
        int zenMode;

        if (forProfileActivation) {
            if (profile._volumeRingerMode != 0) {
                setRingerMode(context, profile._volumeRingerMode);
                if ((profile._volumeRingerMode == 5) && (profile._volumeZenMode != 0))
                    setZenMode(context, profile._volumeZenMode);
            }
        }

        if (firstCall)
            return;

        ringerMode = getRingerMode(context);
        zenMode = getZenMode(context);

        if (forProfileActivation) {
            switch (ringerMode) {
                case 1:  // Ring
                    setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(null, 0);
                    break;
                case 2:  // Ring & Vibrate
                    setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(null, 1);
                    break;
                case 3:  // Vibrate
                    setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(null, 1);
                    break;
                case 4:  // Silent
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        //setZenMode(ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_SILENT);
                        setZenMode(ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    }
                    else {
                        setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_SILENT);
                        try {
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception ignored) {
                        }
                        try {
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception ignored) {
                        }
                    }
                    setVibrateWhenRinging(null, 0);
                    break;
                case 5: // Zen mode
                    switch (zenMode) {
                        case 1:
                            setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                            setVibrateWhenRinging(profile, -1);
                            break;
                        case 2:
                            setZenMode(ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_NORMAL);
                            setVibrateWhenRinging(profile, -1);
                            break;
                        case 3:
                            // must be AudioManager.RINGER_MODE_SILENT, because, ZENMODE_NONE set it to silent
                            // without this, duplicate set this zen mode not working
                            setZenMode(ZENMODE_NONE, audioManager, AudioManager.RINGER_MODE_SILENT);
                            break;
                        case 4:
                            setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(null, 1);
                            break;
                        case 5:
                            setZenMode(ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(null, 1);
                            break;
                        case 6:
                            // must be AudioManager.RINGER_MODE_SILENT, because, ZENMODE_ALARMS set it to silent
                            // without this, duplicate set this zen mode not working
                            setZenMode(ZENMODE_ALARMS, audioManager, AudioManager.RINGER_MODE_SILENT);
                            break;
                    }
                    break;
            }
        }
    }

    void executeForWallpaper(Profile profile) {
        if (profile._deviceWallpaperChange == 1)
        {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (android.os.Build.VERSION.SDK_INT >= 17)
                display.getRealMetrics(displayMetrics);
            else
                display.getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //noinspection SuspiciousNameCombination
                height = displayMetrics.widthPixels;
                //noinspection SuspiciousNameCombination
                width = displayMetrics.heightPixels;
            }
            // for lock screen no double width
            if ((android.os.Build.VERSION.SDK_INT < 24) || (profile._deviceWallpaperFor != 2))
                width = width << 1; // best wallpaper width is twice screen width

            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmapUri(profile._deviceWallpaper, width, height, context);
            if (decodedSampleBitmap != null)
            {
                // set wallpaper
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                try {
                    if (android.os.Build.VERSION.SDK_INT >= 24) {
                        int flags = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;
                        Rect visibleCropHint = null;
                        if (profile._deviceWallpaperFor == 1)
                            flags = WallpaperManager.FLAG_SYSTEM;
                        if (profile._deviceWallpaperFor == 2) {
                            flags = WallpaperManager.FLAG_LOCK;
                            int left = 0;
                            int right = decodedSampleBitmap.getWidth();
                            if (decodedSampleBitmap.getWidth() > width) {
                                left = (decodedSampleBitmap.getWidth() / 2) - (width / 2);
                                right = (decodedSampleBitmap.getWidth() / 2) + (width / 2);
                            }
                            visibleCropHint = new Rect(left, 0, right, decodedSampleBitmap.getHeight());
                        }
                        //noinspection WrongConstant
                        wallpaperManager.setBitmap(decodedSampleBitmap, visibleCropHint, true, flags);
                    }
                    else
                        wallpaperManager.setBitmap(decodedSampleBitmap);
                } catch (IOException e) {
                    Log.e("ActivateProfileHelper.executeForWallpaper", "Cannot set wallpaper. Image="+profile._deviceWallpaper);
                }
            }
        }
    }

    void executeForRunApplications(Profile profile) {
        if (profile._deviceRunApplicationChange == 1)
        {
            String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
            Intent intent;
            PackageManager packageManager = context.getPackageManager();

            for (String split : splits) {
                if (!ApplicationsCache.isShortcut(split)) {
                    intent = packageManager.getLaunchIntentForPackage(ApplicationsCache.getPackageName(split));
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(intent);
                        } catch (Exception ignore) {
                        }
                        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                        //SystemClock.sleep(1000);
                        PPApplication.sleep(1000);
                    }
                } else {
                    long shortcutId = ApplicationsCache.getShortcutId(split);
                    if (shortcutId > 0) {
                        Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(shortcutId);
                        if (shortcut != null) {
                            try {
                                intent = Intent.parseUri(shortcut._intent, 0);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    context.startActivity(intent);
                                } catch (Exception ignore) {
                                }
                                //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                //SystemClock.sleep(1000);
                                PPApplication.sleep(1000);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }
    }

    void executeRootForAdaptiveBrightness(Profile profile) {
        if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
            synchronized (PPApplication.startRootCommandMutex) {
                String command1 = "settings put system " + ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " +
                        Float.toString(profile.getDeviceBrightnessAdaptiveValue(context));
                //if (PPApplication.isSELinuxEnforcing())
                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                Command command = new Command(0, false, command1); //, command2);
                try {
                    //RootTools.closeAllShells();
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.execute", "Error on run su: " + e.toString());
                }
            }
        }
    }

    public void execute(Profile _profile, boolean _interactive)
    {
        // rozdelit zvonenie a notifikacie - zial je to oznacene ako @Hide :-(
        //Settings.System.putInt(context.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        final Profile profile = Profile.getMappedProfile(_profile, context);

        // nahodenie volume a ringer modu
        // run service for execute volumes
        Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
        volumeServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        volumeServiceIntent.putExtra(EXTRA_LINKUNLINK_VOLUMES, PhoneCallService.LINKMODE_NONE);
        volumeServiceIntent.putExtra(EXTRA_FOR_PROFILE_ACTIVATION, true);
        //WakefulIntentService.sendWakefulWork(context, radioServiceIntent);
        context.startService(volumeServiceIntent);
        /*AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        // nahodenie ringer modu - aby sa mohli nastavit hlasitosti
        setRingerMode(profile, audioManager);
        setVolumes(profile, audioManager);
        // nahodenie ringer modu - hlasitosti zmenia silent/vibrate
        setRingerMode(profile, audioManager);*/

        // set vibration on touch
        if (Permissions.checkProfileVibrationOnTouch(context, profile)) {
            switch (profile._vibrationOnTouch) {
                case 1:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                    break;
                case 2:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                    break;
            }
        }

        // nahodenie tonov
        // moved to ExecuteVolumeProfilePrefsService
        //setTones(profile);

        // nahodenie radio preferences
        // run service for execute radios
        Intent radioServiceIntent = new Intent(context, ExecuteRadioProfilePrefsService.class);
        radioServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
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
        if (Permissions.checkProfileScreenTimeout(context, profile)) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            if (pm.isScreenOn()) {
                //Log.d("ActivateProfileHelper.execute","screen on");
                if (PPApplication.screenTimeoutHandler != null) {
                    final Context _context = context;
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            setScreenTimeout(profile._deviceScreenTimeout, _context);
                        }
                    });
                }// else
                //    setScreenTimeout(profile._deviceScreenTimeout);
            }
            else {
                //Log.d("ActivateProfileHelper.execute","screen off");
                setActivatedProfileScreenTimeout(context, profile._deviceScreenTimeout);
            }
        }
        //else
        //    PPApplication.setActivatedProfileScreenTimeout(context, 0);

        // zapnutie/vypnutie lockscreenu
        boolean setLockscreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                // enable lockscreen
                setLockscreenDisabled(context, false);
                setLockscreen = true;
                break;
            case 2:
                // disable lockscreen
                setLockscreenDisabled(context, true);
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
            //noinspection deprecation
            isScreenOn = pm.isScreenOn();
            //}
            //PPApplication.logE("$$$ ActivateProfileHelper.execute","isScreenOn="+isScreenOn);
            boolean keyguardShowing;
            KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 16)
                keyguardShowing = kgMgr.isKeyguardLocked();
            else
                keyguardShowing = kgMgr.inKeyguardRestrictedInputMode();
            //PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguardShowing="+keyguardShowing);

            if (isScreenOn && !keyguardShowing) {
                Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
                context.startService(keyguardService);
            }
        }

        // nahodenie podsvietenia
        if (Permissions.checkProfileScreenBrightness(context, profile)) {
            if (profile.getDeviceBrightnessChange()) {
                if (profile.getDeviceBrightnessAutomatic()) {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            profile.getDeviceBrightnessManualValue(context));
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, context)
                            == PPApplication.PREFERENCE_ALLOWED) {
                        if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                            Settings.System.putFloat(context.getContentResolver(),
                                    ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                                    profile.getDeviceBrightnessAdaptiveValue(context));
                        else {
                            try {
                                Settings.System.putFloat(context.getContentResolver(),
                                        ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                                        profile.getDeviceBrightnessAdaptiveValue(context));
                            } catch (Exception ee) {
                                Intent rootServiceIntent = new Intent(context, ExecuteRootProfilePrefsService.class);
                                rootServiceIntent.setAction(ExecuteRootProfilePrefsService.ACTION_ADAPTIVE_BRIGHTNESS);
                                rootServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                                context.startService(rootServiceIntent);
                            }
                        }
                    }
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                } else {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            profile.getDeviceBrightnessManualValue(context));
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                }

                if (PPApplication.brightnessHandler != null) {
                    final Context __context = context;
                    PPApplication.brightnessHandler.post(new Runnable() {
                        public void run() {
                            createBrightnessView(profile, __context);
                        }
                    });
                }// else
                //    createBrightnessView(profile, context);
            }
        }

        // nahodenie rotate
        if (Permissions.checkProfileAutoRotation(context, profile)) {
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
        }

        // set notification led
        if (profile._notificationLed != 0) {
            //if (Permissions.checkProfileNotificationLed(context, profile)) { not needed for Android 6+, because root is required
            switch (profile._notificationLed) {
                case 1:
                    setNotificationLed(1);
                    break;
                case 2:
                    setNotificationLed(0);
                    break;
            }
            //}
        }

        // nahodenie pozadia
        if (Permissions.checkProfileWallpaper(context, profile)) {
            if (profile._deviceWallpaperChange == 1) {
                Intent wallpaperServiceIntent = new Intent(context, ExecuteWallpaperProfilePrefsService.class);
                wallpaperServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                context.startService(wallpaperServiceIntent);
            }
        }

        // set power save mode
        Intent rootServiceIntent = new Intent(context, ExecuteRootProfilePrefsService.class);
        rootServiceIntent.setAction(ExecuteRootProfilePrefsService.ACTION_POWER_SAVE_MODE);
        rootServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        context.startService(rootServiceIntent);

        if (Permissions.checkProfileLockDevice(context, profile)) {
            if (profile._lockDevice != 0) {
                rootServiceIntent = new Intent(context, ExecuteRootProfilePrefsService.class);
                rootServiceIntent.setAction(ExecuteRootProfilePrefsService.ACTION_LOCK_DEVICE);
                rootServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                context.startService(rootServiceIntent);
            }
        }

        if (_interactive)
        {
            // preferences, ktore vyzaduju interakciu uzivatela

            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, context) == PPApplication.PREFERENCE_ALLOWED)
            {
                if (profile._deviceMobileDataPrefs == 1)
                {
                    /*try {
                        final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                        intent.setComponent(componentName);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        try {
                            final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }*/
                    try {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
                        context.startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            }

            //if (PPApplication.hardwareCheck(Profile.PREF_PROFILE_DEVICE_GPS, context))
            //{  No check only GPS
                if (profile._deviceLocationServicePrefs == 1)
                {
                    try {
                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            //}

            if (profile._deviceRunApplicationChange == 1)
            {
                Intent runApplicationsServiceIntent = new Intent(context, ExecuteRunApplicationsProfilePrefsService.class);
                runApplicationsServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                context.startService(runApplicationsServiceIntent);
            }

        }

    }

    void setScreenTimeout(int screenTimeout, Context context) {
        DisableScreenTimeoutInternalChangeReceiver.internalChange = true;
        //Log.d("ActivateProfileHelper.setScreenTimeout", "current="+Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0));
        switch (screenTimeout) {
            case 1:
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                break;
            case 2:
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                break;
            case 3:
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                break;
            case 4:
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                break;
            case 5:
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                break;
            case 6:
                //2147483647 = Integer.MAX_VALUE
                //18000000   = 5 hours
                //86400000   = 24 hounrs
                //43200000   = 12 hours
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000); //18000000);
                break;
            case 7:
                screenTimeoutUnlock(context);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000);
                break;
            case 8:
                screenTimeoutUnlock(context);
                //if (android.os.Build.VERSION.SDK_INT < 19)  // not working in Sony
                //    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
                //else
                screenTimeoutLock(context);
                break;
        }
        setActivatedProfileScreenTimeout(context, 0);
        DisableScreenTimeoutInternalChangeReceiver.setAlarm(context);
    }

    private static void screenTimeoutLock(Context context)
    {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        screenTimeoutUnlock(context);

        int type;
        if (android.os.Build.VERSION.SDK_INT < 25)
            type = WindowManager.LayoutParams.TYPE_TOAST;
        else
            type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE /*| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE*/ | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
        /*if (android.os.Build.VERSION.SDK_INT < 17)
            params.gravity = Gravity.RIGHT | Gravity.TOP;
        else
            params.gravity = Gravity.END | Gravity.TOP;*/
        GlobalGUIRoutines.keepScreenOnView = new BrightnessView(context);
        try {
            windowManager.addView(GlobalGUIRoutines.keepScreenOnView, params);
        } catch (Exception e) {
            GlobalGUIRoutines.keepScreenOnView = null;
            //e.printStackTrace();
        }
    }

    static void screenTimeoutUnlock(Context context)
    {
        if (GlobalGUIRoutines.keepScreenOnView != null)
        {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            try {
                windowManager.removeView(GlobalGUIRoutines.keepScreenOnView);
            } catch (Exception ignore) {
            }
            GlobalGUIRoutines.keepScreenOnView = null;
        }

        PPApplication.logE("@@@ screenTimeoutLock.unlock", "xxx");
    }

    @SuppressLint("RtlHardcoded")
    private void createBrightnessView(Profile profile, Context context)
    {
        //if (context != null)
        //{
            RemoveBrightnessViewBroadcastReceiver.setAlarm(context);

            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            if (GlobalGUIRoutines.brightneesView != null)
            {
                windowManager.removeView(GlobalGUIRoutines.brightneesView);
                GlobalGUIRoutines.brightneesView = null;
            }
            int type;
            if (android.os.Build.VERSION.SDK_INT < 25)
                type = WindowManager.LayoutParams.TYPE_TOAST;
            else
                type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE /*| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE*/,
                        PixelFormat.TRANSLUCENT
                    );
            /*if (android.os.Build.VERSION.SDK_INT < 17)
                params.gravity = Gravity.RIGHT | Gravity.TOP;
            else
                params.gravity = Gravity.END | Gravity.TOP;*/
            if (profile.getDeviceBrightnessAutomatic())
                params.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else
                params.screenBrightness = profile.getDeviceBrightnessManualValue(context) / (float) 255;
            GlobalGUIRoutines.brightneesView = new BrightnessView(context);
            try {
                windowManager.addView(GlobalGUIRoutines.brightneesView, params);
            } catch (Exception e) {
                GlobalGUIRoutines.brightneesView = null;
                //e.printStackTrace();
            }
        //}
    }

    static void removeBrightnessView(Context context) {
        if (GlobalGUIRoutines.brightneesView != null)
        {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            try {
                windowManager.removeView(GlobalGUIRoutines.brightneesView);
            } catch (Exception ignore) {
            }
            GlobalGUIRoutines.brightneesView = null;
        }
    }

    @SuppressLint("NewApi")
    public void showNotification(Profile profile)
    {
        if (lockRefresh)
            // no refres notification
            return;

        if (ApplicationPreferences.notificationStatusBar(context))
        {
            // close showed notification
            //notificationManager.cancel(PPApplication.NOTIFICATION_ID);
            // vytvorenie intentu na aktivitu, ktora sa otvori na kliknutie na notifikaciu
            Intent intent = new Intent(context, ActivateProfileActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // nastavime, ze aktivita sa spusti z notifikacnej listy
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // vytvorenie samotnej notifikacie
            Notification.Builder notificationBuilder;
            RemoteViews contentView;
            if (ApplicationPreferences.notificationTheme(context).equals("1"))
                contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer_dark);
            else
            if (ApplicationPreferences.notificationTheme(context).equals("2"))
                contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer_light);
            else
                contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer);

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile.getProfileNameWithDuration(false, context);
                iconBitmap = profile._iconBitmap;
                preferencesIndicator = profile._preferencesIndicator;
            }
            else
            {
                isIconResourceID = true;
                iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
                profileName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new Notification.Builder(context)
                    .setContentIntent(pIntent);

            if (android.os.Build.VERSION.SDK_INT >= 16) {
                if (ApplicationPreferences.notificationShowInStatusBar(context)) {
                    KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    if ((ApplicationPreferences.notificationHideInLockscreen(context) && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        notificationBuilder.setPriority(Notification.PRIORITY_MIN);
                    else
                        notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                }
                else
                    notificationBuilder.setPriority(Notification.PRIORITY_MIN);
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
                    if (ApplicationPreferences.notificationStatusBarStyle(context).equals("0")) {
                        // colorful icon

                        // FC in Note 4, 6.0.1 :-/
                        String manufacturer = PPApplication.getROMManufacturer();
                        boolean isNote4 = (manufacturer != null) && (manufacturer.compareTo("samsung") == 0) &&
                                /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                                 Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                                ) &&*/
                                (android.os.Build.VERSION.SDK_INT == 23);
                        //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                        if ((android.os.Build.VERSION.SDK_INT >= 23) && (!isNote4)) {
                            notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                        }
                        else {
                            iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", context.getPackageName());
                            if (iconSmallResource == 0)
                                iconSmallResource = R.drawable.ic_profile_default;
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }
                    }
                    else {
                        // native icon
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                }
                else {
                    if (ApplicationPreferences.notificationStatusBarStyle(context).equals("0")) {
                        // colorful icon
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    } else {
                        // native icon
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    }
                }
            }
            else
            {
                // FC in Note 4, 6.0.1 :-/
                String manufacturer = PPApplication.getROMManufacturer();
                boolean isNote4 = (manufacturer != null) && (manufacturer.compareTo("samsung") == 0) &&
                        /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                         Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                        ) &&*/
                        (android.os.Build.VERSION.SDK_INT == 23);
                //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                if ((Build.VERSION.SDK_INT >= 23) && (!isNote4) && (iconBitmap != null)) {
                    notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                }
                else {
                    int iconSmallResource;
                    if (ApplicationPreferences.notificationStatusBarStyle(context).equals("0"))
                        iconSmallResource = R.drawable.ic_profile_default;
                    else
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                    notificationBuilder.setSmallIcon(iconSmallResource);
                }

                if (iconBitmap != null)
                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                else
                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
            }

            // workaround for LG G4, Android 6.0
            if (android.os.Build.VERSION.SDK_INT < 24)
                contentView.setInt(R.id.notification_activated_app_root, "setVisibility", View.GONE);

            if (ApplicationPreferences.notificationTextColor(context).equals("1")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if (android.os.Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.BLACK);
            }
            else
            if (ApplicationPreferences.notificationTextColor(context).equals("2")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if (android.os.Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.WHITE);
            }
            contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            //contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator,
            //		ProfilePreferencesIndicator.paint(profile, context));
            if ((preferencesIndicator != null) && (ApplicationPreferences.notificationPrefIndicator(context)))
                contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            //noinspection deprecation
            notificationBuilder.setContent(contentView);

            Notification notification = notificationBuilder.build();


            if (ApplicationPreferences.notificationStatusBarPermanent(context))
                {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                }
                else
                {
                    setAlarmForNotificationCancel();
                }

                notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, notification);
        }
        else
        {
            notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }
    }

    void removeNotification()
    {
        removeAlarmForRecreateNotification();
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
    }

    private void setAlarmForNotificationCancel()
    {
        if (ApplicationPreferences.notificationStatusBarCancel(context).isEmpty() || ApplicationPreferences.notificationStatusBarCancel(context).equals("0"))
            return;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + Integer.valueOf(ApplicationPreferences.notificationStatusBarCancel(context)) * 1000;

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    void setAlarmForRecreateNotification()
    {
        Intent intent = new Intent(context, RecreateNotificationBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + 500;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void removeAlarmForRecreateNotification() {
        Intent intent = new Intent(context, RecreateNotificationBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

    }

    void updateWidget()
    {
        if (lockRefresh)
            // no refres widgets
            return;

        // icon widget
        try {
            Intent intent = new Intent(context, IconWidgetProvider.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        } catch (Exception ignored) {}

        // one row widget
        try {
            Intent intent4 = new Intent(context, OneRowWidgetProvider.class);
            intent4.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids4[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
            intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4);
            context.sendBroadcast(intent4);
        } catch (Exception ignored) {}

        // list widget
        try {
            Intent intent2 = new Intent(context, ProfileListWidgetProvider.class);
            intent2.setAction(ProfileListWidgetProvider.INTENT_REFRESH_LISTWIDGET);
            int ids2[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
            context.sendBroadcast(intent2);
        } catch (Exception ignored) {}

        // dashclock extension
        /*Intent intent3 = new Intent();
        intent3.setAction(DashClockBroadcastReceiver.INTENT_REFRESH_DASHCLOCK);
        context.sendBroadcast(intent3);*/
        Intent intent3 = new Intent("DashClockBroadcastReceiver");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        // activities
        /*Intent intent5 = new Intent();
        intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
        context.sendBroadcast(intent5);*/
        Intent intent5 = new Intent("RefreshGUIBroadcastReceiver");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);

        // Samsung edge panel
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            try {
                Intent intent2 = new Intent(context, SamsungEdgeProvider.class);
                intent2.setAction(SamsungEdgeProvider.INTENT_REFRESH_EDGEPANEL);
                context.sendBroadcast(intent2);
            } catch (Exception ignored) {
            }
        }
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
            setAirplaneMode_SDK17(/*context, */mode);
        else
            setAirplaneMode_SDK8(context, mode);
    }

    /*
    private boolean isMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            return Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        else {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
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
        }
    }
    */
    private boolean isMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT < 21)
        {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                return (Boolean)getMobileDataEnabledMethod.invoke(connectivityManager);
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        if (android.os.Build.VERSION.SDK_INT < 22)
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;
            Object ITelephonyStub;
            Class<?> ITelephonyClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                getDataEnabledMethod = ITelephonyClass.getDeclaredMethod("getDataEnabled");

                getDataEnabledMethod.setAccessible(true);

                return (Boolean)getDataEnabledMethod.invoke(ITelephonyStub);

            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                getDataEnabledMethod.setAccessible(true);

                return (Boolean)getDataEnabledMethod.invoke(telephonyManager);

            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }

    }

    static boolean canSetMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 22)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                getDataEnabledMethod.setAccessible(true);
                return true;
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                return true;
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void setMobileData(Context context, boolean enable)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            if (Permissions.hasPermission(context, Manifest.permission.MODIFY_PHONE_STATE)) {
                if (android.os.Build.VERSION.SDK_INT == 21)
                {
                    Method dataConnSwitchMethod;
                    Class<?> telephonyManagerClass;
                    Object ITelephonyStub;
                    Class<?> ITelephonyClass;

                    TelephonyManager telephonyManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);

                    try {
                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                        getITelephonyMethod.setAccessible(true);
                        ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                        ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
                        dataConnSwitchMethod = ITelephonyClass.getDeclaredMethod("setDataEnabled", Boolean.TYPE);

                        dataConnSwitchMethod.setAccessible(true);
                        dataConnSwitchMethod.invoke(ITelephonyStub, enable);

                    } catch (Exception ignored) {
                    }
                }
                else
                {
                    Method setDataEnabledMethod;
                    Class<?> telephonyManagerClass;

                    TelephonyManager telephonyManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);

                    try {
                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        setDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                        setDataEnabledMethod.setAccessible(true);

                        setDataEnabledMethod.invoke(telephonyManager, enable);

                    } catch (Exception ignored) {
                    }
                }
            }
            else
            if (PPApplication.isRooted()/*PPApplication.isRootGranted()*/)
            {
                synchronized (PPApplication.startRootCommandMutex) {
                    String command1 = "svc data " + (enable ? "enable" : "disable");
                    Command command = new Command(0, false, command1);
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                    }
                }
                /*
                int state = 0;
                try {
                    // Get the current state of the mobile network.
                    state = enable ? 1 : 0;
                    // Get the value of the "TRANSACTION_setDataEnabled" field.
                    String transactionCode = PPApplication.getTransactionCode(context, "TRANSACTION_setDataEnabled");
                    //Log.e("ActivateProfileHelper.setMobileData", "transactionCode="+transactionCode);
                    // Android 5.1+ (API 22) and later.
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        //Log.e("ActivateProfileHelper.setMobileData", "dual SIM?");
                        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                        // Loop through the subscription list i.e. SIM list.
                        for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                            if (transactionCode != null && transactionCode.length() > 0) {
                                // Get the active subscription ID for a given SIM card.
                                int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                                //Log.e("ActivateProfileHelper.setMobileData", "subscriptionId="+subscriptionId);
                                String command1 = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    commandWait(command);
                                    //RootTools.closeAllShells();
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                                }
                            }
                        }
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                        //Log.e("ActivateProfileHelper.setMobileData", "NO dual SIM?");
                        // Android 5.0 (API 21) only.
                        if (transactionCode != null && transactionCode.length() > 0) {
                            String command1 = "service call phone " + transactionCode + " i32 " + state;
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                                //RootTools.closeAllShells();
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                */
            }
        }
        else {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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

            } catch (Exception ignored) {
            }

            if (!OK) {
                try {
                    Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);

                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(connectivityManager, enable);

                } catch (Exception ignored) {
                }
            }
        }
    }

    /*
    private int getPreferredNetworkType(Context context) {
        if (PPApplication.isRooted())
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                String transactionCode = PPApplication.getTransactionCode(context, "TRANSACTION_getPreferredNetworkType");
                if (transactionCode != null && transactionCode.length() > 0) {
                    String command1 = "service call phone " + transactionCode + " i32";
                    Command command = new Command(0, false, command1) {
                        @Override
                        public void commandOutput(int id, String line) {
                            super.commandOutput(id, line);
                            String splits[] = line.split(" ");
                            try {
                                networkType = Integer.parseInt(splits[2]);
                            } catch (Exception e) {
                                networkType = -1;
                            }
                        }

                        @Override
                        public void commandTerminated(int id, String reason) {
                            super.commandTerminated(id, reason);
                        }

                        @Override
                        public void commandCompleted(int id, int exitcode) {
                            super.commandCompleted(id, exitcode);
                        }
                    };
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                    }
                }

            } catch(Exception ignored) {
            }
        }
        else
            networkType = -1;
        return networkType;
    }
    */

    private static String getTransactionCode(Context context, String fieldName) throws Exception {
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

            if (preference.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                getTransactionCode(context, "TRANSACTION_setDataEnabled");
            }
            else
            if (preference.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    private void setPreferredNetworkType(Context context, int networkType)
    {
        if (PPApplication.isRooted() && PPApplication.serviceBinaryExists())
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                String transactionCode = getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
                // Android 6?
                if (Build.VERSION.SDK_INT >= 23) {
                    SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                    // Loop through the subscription list i.e. SIM list.
                    List<SubscriptionInfo> subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                    if (subscriptionList != null) {
                        for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                            if (transactionCode.length() > 0) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if (subscriptionInfo != null) {
                                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                                    synchronized (PPApplication.startRootCommandMutex) {
                                        String command1 = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + networkType;
                                        Command command = new Command(0, false, command1);
                                        try {
                                            //RootTools.closeAllShells();
                                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                            commandWait(command);
                                        } catch (Exception e) {
                                            Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else  {
                    if (transactionCode.length() > 0) {
                        synchronized (PPApplication.startRootCommandMutex) {
                            String command1 = "service call phone " + transactionCode + " i32 " + networkType;
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                            }
                        }
                    }
                }
            } catch(Exception ignored) {
            }
        }
    }

    private void setNFC(Context context, boolean enable)
    {
        //Log.e("ActivateProfileHelper.setNFC", "xxx");
        /*if (Permissions.checkNFC(context)) {
            Log.e("ActivateProfileHelper.setNFC", "permission granted!!");
            CmdNfc.run(enable);
        }
        else */
        if (PPApplication.isRooted()/*PPApplication.isRootGranted()*/) {
            synchronized (PPApplication.startRootCommandMutex) {
                String command1 = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", context, enable);
                //Log.e("ActivateProfileHelper.setNFC", "command1="+command1);
                if (command1 != null) {
                    Command command = new Command(0, false, command1);
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setNFC", "Error on run su");
                    }
                }
            }
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
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void setGPS(Context context, boolean enable)
    {
        boolean isEnabled;
        if (android.os.Build.VERSION.SDK_INT < 19)
            isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
        else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }


        PPApplication.logE("ActivateProfileHelper.setGPS", "isEnabled="+isEnabled);

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled)  && enable)
        {
            if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                String newSet;
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (provider.equals(""))
                        newSet = LocationManager.GPS_PROVIDER;
                    else
                        newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);
                }
                else
                    newSet = "+gps";
                Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
            }
            else
            if ((android.os.Build.VERSION.SDK_INT >= 16) && PPApplication.isRooted() && PPApplication.settingsBinaryExists())
            {
                // zariadenie je rootnute
                PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;
                //String command2;

                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                    String newSet;
                    if (provider.isEmpty())
                        newSet = LocationManager.GPS_PROVIDER;
                    else
                        newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);

                    synchronized (PPApplication.startRootCommandMutex) {
                        command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                        //if (PPApplication.isSELinuxEnforcing())
                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);

                        //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state true";
                        Command command = new Command(0, false, command1); //, command2);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                        }
                    }
                }
                else {
                    synchronized (PPApplication.startRootCommandMutex) {
                        command1 = "settings put secure location_providers_allowed +gps";
                        Command command = new Command(0, false, command1);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                        }
                    }
                }
            }
            else
            if (canExploitGPS(context))
            {
                PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            //else
            //{
                /*PPApplication.logE("ActivateProfileHelper.setGPS", "old method");

                try {
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
            //}
        }
        else
            //if(provider.contains(LocationManager.GPS_PROVIDER) && (!enable))
            if (isEnabled && (!enable))
            {
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    String newSet = "";
                    if (android.os.Build.VERSION.SDK_INT < 23) {
                        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                        String[] list = provider.split(",");
                        int j = 0;
                        for (int i = 0; i < list.length; i++) {
                            if (!list[i].equals(LocationManager.GPS_PROVIDER)) {
                                if (j > 0)
                                    newSet += ",";
                                newSet += list[i];
                                j++;
                            }
                        }
                    }
                    else
                        newSet = "-gps";
                    Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
                }
                else
                if ((android.os.Build.VERSION.SDK_INT >= 16) && PPApplication.isRooted() && PPApplication.settingsBinaryExists())
                {
                    // zariadenie je rootnute
                    PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                    String command1;
                    //String command2;

                    if (android.os.Build.VERSION.SDK_INT < 23) {
                        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                        String[] list = provider.split(",");

                        String newSet = "";
                        int j = 0;
                        for (String aList : list) {

                            if (!aList.equals(LocationManager.GPS_PROVIDER)) {
                                if (j > 0)
                                    newSet += ",";
                                newSet += aList;
                                j++;
                            }
                        }

                        synchronized (PPApplication.startRootCommandMutex) {
                            command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state false";
                            Command command = new Command(0, false, command1);//, command2);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                            }
                        }
                    }
                    else {
                        synchronized (PPApplication.startRootCommandMutex) {
                            command1 = "settings put secure location_providers_allowed -gps";
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                            }
                        }
                    }
                }
                else
                if (canExploitGPS(context))
                {
                    PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

                    final Intent poke = new Intent();
                    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                    poke.setData(Uri.parse("3"));
                    context.sendBroadcast(poke);
                }
                //else
                //{
                    //PPApplication.logE("ActivateProfileHelper.setGPS", "old method");

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
                //}
            }
    }

    private void setAirplaneMode_SDK17(/*Context context, */boolean mode)
    {
        if (PPApplication.isRooted() && PPApplication.settingsBinaryExists())
        {
            // zariadenie je rootnute
            synchronized (PPApplication.startRootCommandMutex) {
                String command1;
                String command2;
                if (mode) {
                    command1 = "settings put global airplane_mode_on 1";
                    command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
                } else {
                    command1 = "settings put global airplane_mode_on 0";
                    command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
                }
                //if (PPApplication.isSELinuxEnforcing())
                //{
                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                //	command2 = PPApplication.getSELinuxEnforceCommand(command2, Shell.ShellContext.SYSTEM_APP);
                //}
                Command command = new Command(0, false, command1, command2);
                try {
                    //RootTools.closeAllShells();
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                } catch (Exception e) {
                    Log.e("AirPlaneMode_SDK17.setAirplaneMode", "Error on run su");
                }
            }
        }
        //else
        //{
            //Log.e("ActivateProfileHelper.setAirplaneMode_SDK17","root NOT granted");
            // for normal apps it is only possible to open the system settings dialog
        /*	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent); */
        //}
    }

    @SuppressWarnings("deprecation")
    private void setAirplaneMode_SDK8(Context context, boolean mode)
    {
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, mode ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", mode);
        context.sendBroadcast(intent);
    }

    void setPowerSaveMode(Profile profile) {
        if (profile._devicePowerSaveMode != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean _isPowerSaveMode = false;
                if (Build.VERSION.SDK_INT >= 21)
                    _isPowerSaveMode = powerManager.isPowerSaveMode();
                boolean _setPowerSaveMode = false;
                switch (profile._devicePowerSaveMode) {
                    case 1:
                        if (!_isPowerSaveMode) {
                            _isPowerSaveMode = true;
                            _setPowerSaveMode = true;
                        }
                        break;
                    case 2:
                        if (_isPowerSaveMode) {
                            _isPowerSaveMode = false;
                            _setPowerSaveMode = true;
                        }
                        break;
                    case 3:
                        _isPowerSaveMode = !_isPowerSaveMode;
                        _setPowerSaveMode = true;
                        break;
                }
                if (_setPowerSaveMode) {
                    if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                        synchronized (PPApplication.startRootCommandMutex) {
                            String command1 = "settings put global low_power " + ((_isPowerSaveMode) ? 1 : 0);
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setPowerSaveMode", "Error on run su: " + e.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    void lockDevice(Profile profile) {
        if (PPApplication.startedOnBoot)
            // not lock device after boot
            return;

        switch (profile._lockDevice) {
            case 3:
                DevicePolicyManager manager = (DevicePolicyManager)context.getSystemService(DEVICE_POLICY_SERVICE);
                final ComponentName component = new ComponentName(context, PPDeviceAdminReceiver.class);
                if (manager.isAdminActive(component))
                    manager.lockNow();
                break;
            case 2:
                /*if (PPApplication.isRooted()) {
                    //String command1 = "input keyevent 26";
                    Command command = new Command(0, false, command1);
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.lockDevice", "Error on run su: " + e.toString());
                    }
                }*/
                if (PPApplication.isRooted())
                {
                    synchronized (PPApplication.startRootCommandMutex) {
                        String command1 = PPApplication.getJavaCommandFile(CmdGoToSleep.class, "power", context, 0);
                        if (command1 != null) {
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.lockDevice", "Error on run su");
                            }
                        }
                    }
                }
                break;
            case 1:
                Intent intent = new Intent(context, LockDeviceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(intent);
                break;
        }
    }

    private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("ActivateProfileHelper", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }


    static boolean getLockscreenDisabled(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_LOCKSCREEN_DISABLED, false);
    }

    static void setLockscreenDisabled(Context context, boolean disabled)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_LOCKSCREEN_DISABLED, disabled);
        editor.apply();
    }

    /*
    private static boolean getScreenUnlocked(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SCREEN_UNLOCKED, true);
    }

    static void setScreenUnlocked(Context context, boolean unlocked)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SCREEN_UNLOCKED, unlocked);
        editor.apply();
    }
    */

    private static int getRingerVolume(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_RINGER_VOLUME, -999);
    }

    static void setRingerVolume(Context context, int volume)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_RINGER_VOLUME, volume);
        editor.apply();
    }

    private static int getNotificationVolume(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_NOTIFICATION_VOLUME, -999);
    }

    static void setNotificationVolume(Context context, int volume)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_NOTIFICATION_VOLUME, volume);
        editor.apply();
    }

    private static int getRingerMode(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_RINGER_MODE, 0);
    }

    static void setRingerMode(Context context, int mode)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_RINGER_MODE, mode);
        editor.apply();
    }

    private static int getZenMode(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_ZEN_MODE, 0);
    }

    static void setZenMode(Context context, int mode)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_ZEN_MODE, mode);
        editor.apply();
    }

    static int getActivatedProfileScreenTimeout(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, 0);
    }

    static void setActivatedProfileScreenTimeout(Context context, int timeout)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, timeout);
        editor.apply();
    }

}
