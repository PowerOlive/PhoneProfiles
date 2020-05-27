package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
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
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.content.Context.POWER_SERVICE;

class ActivateProfileHelper {

    //private DataWrapper dataWrapper;

    static final boolean lockRefresh = false;

    static boolean disableScreenTimeoutInternalChange = false;

    // bluetooth calls volume stream
    static final int STREAM_BLUETOOTH_SCO = 6;

    static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    // Setting.Global "zen_mode"
    static final int ZENMODE_ALL = 0;
    static final int ZENMODE_PRIORITY = 1;
    static final int ZENMODE_NONE = 2;
    static final int ZENMODE_ALARMS = 3;
    @SuppressWarnings("WeakerAccess")
    static final int ZENMODE_SILENT = 99;

    //static final String EXTRA_LINKUNLINK_VOLUMES = "link_unlink_volumes";
    //static final String EXTRA_FOR_PROFILE_ACTIVATION = "for_profile_activation";

    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    //private static final String PREF_SCREEN_UNLOCKED = "screen_unlocked";
    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT = "activated_profile_screen_timeout";
    static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";

    private static void doExecuteForRadios(Context context, Profile profile)
    {
        //try { Thread.sleep(300); } catch (InterruptedException e) { }
        //SystemClock.sleep(300);
        PPApplication.sleep(300);

        // setup network type
        // in array.xml, networkTypeGSMValues are 100+ values
        if (profile._deviceNetworkType >= 100) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                // in array.xml, networkTypeGSMValues are 100+ values
                setPreferredNetworkType(context, profile._deviceNetworkType - 100);
                //try { Thread.sleep(200); } catch (InterruptedException e) { }
                //SystemClock.sleep(200);
                PPApplication.sleep(200);
            }
        }

        // setup mobile data
        if (profile._deviceMobileData != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
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

        // setup WiFi AP
        boolean canChangeWifi = true;
        if (profile._deviceWiFiAP != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (Build.VERSION.SDK_INT < 28) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(context);
                    } catch (Exception ignored) {
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
                            setWifiAP(context, wifiApManager, isWifiAPEnabled);
                            //try { Thread.sleep(200); } catch (InterruptedException e) { }
                            //SystemClock.sleep(200);
                            PPApplication.sleep(1000);
                        }
                    }
                }
                else {
                    boolean setWifiAPState = false;
                    boolean isWifiAPEnabled = CmdWifiAP.isEnabled();
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
                        CmdWifiAP.setWifiAP(isWifiAPEnabled);
                        //try { Thread.sleep(200); } catch (InterruptedException e) { }
                        //SystemClock.sleep(200);
                        PPApplication.sleep(1000);
                    }
                }
            }
        }

        if (canChangeWifi) {
            // setup WiFi
            if (profile._deviceWiFi != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean isWifiAPEnabled;
                    if (Build.VERSION.SDK_INT < 28)
                        isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                    else
                        isWifiAPEnabled = CmdWifiAP.isEnabled();
                    if ((!isWifiAPEnabled) || (profile._deviceWiFi == 4)) { // only when wifi AP is not enabled, change wifi
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
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
                                    Log.e("ActivateProfileHelper.doExecuteForRadios", Log.getStackTraceString(e));
                                }
                                //try { Thread.sleep(200); } catch (InterruptedException e) { }
                                //SystemClock.sleep(200);
                                PPApplication.sleep(200);
                            }
                        }
                    }
                }
            }

            // connect to SSID
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    if (Permissions.checkLocation(context)) {
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            int wifiState = wifiManager.getWifiState();
                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                // check if wifi is connected
                                ConnectivityManager connManager = null;
                                try {
                                    connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                                } catch (Exception ignored) {
                                    // java.lang.NullPointerException: missing IConnectivityManager
                                    // Dual SIM?? Bug in Android ???
                                }
                                if (connManager != null) {
                                    boolean wifiConnected = false;
                                    /*if (Build.VERSION.SDK_INT < 28) {
                                        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
                                        wifiConnected = (activeNetwork != null) &&
                                                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                                                activeNetwork.isConnected();
                                    }
                                    else */{
                                        Network[] activeNetworks=connManager.getAllNetworks();
                                        for(Network network : activeNetworks){
                                            try {
                                                //NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                                                //if ((networkInfo != null) && networkInfo.isConnected()) {
                                                    NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                                    if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                        wifiConnected = PPWifiNetworkCallback.connected;
                                                        break;
                                                    }
                                                //}
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                    WifiInfo wifiInfo = null;
                                    if (wifiConnected)
                                        wifiInfo = wifiManager.getConnectionInfo();

                                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                    if (list != null) {
                                        for (WifiConfiguration i : list) {
                                            if (i.SSID != null && i.SSID.equals(profile._deviceConnectToSSID)) {
                                                if (wifiConnected) {
                                                    if (!wifiInfo.getSSID().equals(i.SSID)) {
                                                        // connected to another SSID
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
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().connectToSSID = profile._deviceConnectToSSID;
            }
        }

        // setup bluetooth
        if (profile._deviceBluetooth != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                BluetoothAdapter bluetoothAdapter;
                //if (android.os.Build.VERSION.SDK_INT < 18)
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                //else {
                    /*BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
                    if (bluetoothManager != null)
                        bluetoothAdapter = bluetoothManager.getAdapter();
                    else
                        bluetoothAdapter = null;*/
                //}
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
                        try {
                            if (isBluetoothEnabled)
                                bluetoothAdapter.enable();
                            else
                                bluetoothAdapter.disable();
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.doExecuteForRadio", Log.getStackTraceString(e));
                        }
                    }
                }
            }
        }

        // setup GPS
        if (profile._deviceGPS != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isEnabled = false;
                boolean ok = true;
                /*if (android.os.Build.VERSION.SDK_INT < 19)
                    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {*/
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null)
                        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    else
                        ok = false;
                //}
                if (ok) {
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
        }

        // setup NFC
        if (profile._deviceNFC != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, null, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
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
                            } else {
                                setNFC(context, false);
                            }
                            break;
                    }
                }
            }
        }
    }

    private static void executeForRadios(Context context, final Profile profile)
    {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadRadios();
        final Handler handler = new Handler(PPApplication.handlerThreadRadios.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.executeForRadios");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    boolean _isAirplaneMode = false;
                    boolean _setAirplaneMode = false;
                    if (profile._deviceAirplaneMode != 0) {
                        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            _isAirplaneMode = isAirplaneMode(appContext);
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
                        setAirplaneMode(appContext, _isAirplaneMode);

                        PPApplication.sleep(2000);
                    }

                    doExecuteForRadios(appContext, profile);

                /*if (_setAirplaneMode && (!_isAirplaneMode)) {
                    // 200 milliseconds is in doExecuteForRadios
                    PPApplication.sleep(1800);

                    // switch OFF airplane mode, set if after executeForRadios
                    setAirplaneMode(context, _isAirplaneMode);
                }*/

                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    private static boolean isAudibleRinging(int ringerMode, int zenMode/*, boolean onlyVibrateSilent*/) {
        /*if (onlyVibrateSilent)
            return (!((ringerMode == Profile.RINGERMODE_VIBRATE) || (ringerMode == Profile.RINGERMODE_SILENT) ||
                    ((ringerMode == Profile.RINGERMODE_ZENMODE) &&
                            ((zenMode == Profile.ZENMODE_ALL_AND_VIBRATE) || (zenMode == Profile.ZENMODE_PRIORITY_AND_VIBRATE)))
            ));
        else*/
            return (!((ringerMode == Profile.RINGERMODE_VIBRATE) || (ringerMode == Profile.RINGERMODE_SILENT) ||
                    ((ringerMode == Profile.RINGERMODE_ZENMODE) &&
                            ((zenMode == Profile.ZENMODE_NONE) || (zenMode == Profile.ZENMODE_ALL_AND_VIBRATE) ||
                             (zenMode == Profile.ZENMODE_PRIORITY_AND_VIBRATE) || (zenMode == Profile.ZENMODE_ALARMS)))
            ));
    }

    private static boolean isVibrateRingerMode(int ringerMode) {
        return (ringerMode == Profile.RINGERMODE_VIBRATE);
    }

    static boolean isAudibleSystemRingerMode(AudioManager audioManager, Context context) {
        /*int ringerMode = audioManager.getRingerMode();
        PPApplication.logE("ActivateProfileHelper.isAudibleSystemRingerMode", "ringerMode="+ringerMode);
        if (ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                int zenMode = getSystemZenMode(context, -1);
                return (zenMode == ActivateProfileHelper.ZENMODE_PRIORITY);
            }
            else
                return false;
        }
        else
            return true;*/

        int systemRingerMode = audioManager.getRingerMode();
        int systemZenMode = getSystemZenMode(context/*, -1*/);

        /*return (audibleRingerMode) ||
                ((systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY) &&
                        (systemRingerMode != AudioManager.RINGER_MODE_VIBRATE));*/

        boolean audibleRingerMode;
        // In AOSP, when systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY, systemRingerMode == AudioManager.RINGER_MODE_SILENT :-/
        if (systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY) {
            audibleRingerMode = (systemRingerMode == AudioManager.RINGER_MODE_NORMAL) ||
                                (systemRingerMode == AudioManager.RINGER_MODE_SILENT);
        }
        else
            audibleRingerMode = systemRingerMode == AudioManager.RINGER_MODE_NORMAL;
        boolean audibleZenMode = (systemZenMode == -1) ||
                                 (systemZenMode == ActivateProfileHelper.ZENMODE_ALL) ||
                                 (systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY);
        return audibleRingerMode && audibleZenMode;
    }

    /*
    private void correctVolume0(AudioManager audioManager, int linkUnlink) {
        int ringerMode, zenMode;
        if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_NONE) {
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
    static void setMergedRingNotificationVolumes(Context context,
                                                 @SuppressWarnings("SameParameterValue") boolean force) {
        ApplicationPreferences.getSharedPreferences(context);

        //PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        setMergedRingNotificationVolumes(context, force, editor);
        editor.apply();
    }

    static void setMergedRingNotificationVolumes(Context context, boolean force, SharedPreferences.Editor editor) {
        ApplicationPreferences.getSharedPreferences(context);

        //PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

        if (!ApplicationPreferences.preferences.contains(PREF_MERGED_RING_NOTIFICATION_VOLUMES) || force) {
            try {
                boolean merged;
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
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
                        PPApplication.sleep(2000);
                        merged = audioManager.getStreamVolume(AudioManager.STREAM_RING) == newNotificationVolume;
                    } else
                        merged = false;
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, 0);
                    audioManager.setRingerMode(ringerMode);

                    //PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "merged=" + merged);

                    editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
                }
            } catch (Exception ignored) {}
        }
    }

    @SuppressLint("NewApi")
    private static void setVolumes(Context context, Profile profile, AudioManager audioManager, int linkUnlink, boolean forProfileActivation)
    {
        if (profile.getVolumeRingtoneChange()) {
            if (forProfileActivation) {
                RingerModeChangeReceiver.notUnlinkVolumes = false;
                setRingerVolume(context, profile.getVolumeRingtoneValue());
            }
        }
        if (profile.getVolumeNotificationChange()) {
            if (forProfileActivation) {
                RingerModeChangeReceiver.notUnlinkVolumes = false;
                setNotificationVolume(context, profile.getVolumeNotificationValue());
            }
        }

        int ringerMode = getRingerMode(context);
        //int zenMode = getZenMode(context);

        if (forProfileActivation) {
            if (Build.VERSION.SDK_INT >= 26) {
                if (profile.getVolumeAccessibilityChange()) {
                    try {
                        audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY /* 10 */, profile.getVolumeAccessibilityValue(), 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.STREAM_ACCESSIBILITY, profile.getVolumeAccessibilityValue());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        //if (isAudibleRinging(ringerMode, zenMode))
        if (isAudibleSystemRingerMode(audioManager, context) || (ringerMode == 0)) {
            // test only system ringer mode

            //if (Permissions.checkAccessNotificationPolicy(context)) {

                if (forProfileActivation) {
                    if (profile.getVolumeDTMFChange()) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_DTMF /* 8 */, profile.getVolumeDTMFValue(), 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_DTMF, profile.getVolumeDTMFValue());
                        } catch (Exception ignored) {}
                    }
                    if (profile.getVolumeSystemChange()) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, profile.getVolumeSystemValue(), 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
                            //correctVolume0(/*profile, */audioManager, linkUnlink);
                        } catch (Exception ignored) { }
                    }
                }

                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                boolean volumesSet = false;
                if ((telephony != null) && getMergedRingNotificationVolumes(context) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
                    int callState = telephony.getCallState();
                    //if (doUnlink) {
                    //if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_UNLINK) {
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
                    } else if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_LINK) {
                        // for separating ringing and notification
                        // in not ringing state ringer and notification volume must by change
                        int volume = getRingerVolume(context);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                            } catch (Exception ignored) { }
                        }
                        volume = getNotificationVolume(context);
                        if (volume != -999) {
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
                setMediaVolume(context, audioManager, profile.getVolumeMediaValue());
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
            if (profile.getVolumeBluetoothSCOChange()) {
                try {
                    audioManager.setStreamVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO, profile.getVolumeBluetoothSCOValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception ignored) {}
            }
        }

    }

    static void setMediaVolume(Context context, AudioManager audioManager, int value) {
        // Fatal Exception: java.lang.SecurityException: Only SystemUI can disable the safe media volume:
        // Neither user 10118 nor current process has android.permission.STATUS_BAR_SERVICE.
        try {
            //PPApplication.logE("ActivateProfileHelper.setMediaVolume", "set media volume");
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, profile.getVolumeMediaValue());
        } catch (SecurityException e) {
            //PPApplication.logE("ActivateProfileHelper.setMediaVolume", Log.getStackTraceString(e));
            // adb shell pm grant sk.henrichg.phoneprofiles android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                try {
                    //PPApplication.logE("ActivateProfileHelper.setMediaVolume", "disable safe volume without root");
                    Settings.Global.putInt(context.getContentResolver(), "audio_safe_volume_state", 2);
                    //PPApplication.logE("ActivateProfileHelper.setMediaVolume", "set media volume");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
                }
                catch (Exception e2) {
                    //PPApplication.logE("ActivateProfileHelper.setVolumes", Log.getStackTraceString(e2));
                }
            }
            else {
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                        (PPApplication.isRooted(false))) {
                    synchronized (PPApplication.rootMutex) {
                        String command1 = "settings put global audio_safe_volume_state 2";
                        Command command = new Command(0, false, command1);
                        try {
                            //PPApplication.logE("ActivateProfileHelper.setMediaVolume", "disable safe volume with root");
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            PPApplication.commandWait(command);
                            //PPApplication.logE("ActivateProfileHelper.setMediaVolume", "set media volume");
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
                                /*} catch (RootDeniedException ee) {
                                    PPApplication.rootMutex.rootGranted = false;
                                    Log.e("ActivateProfileHelper.setVolumes", Log.getStackTraceString(ee));*/
                        } catch (Exception ee) {
                            Log.e("ActivateProfileHelper.setMediaVolume", Log.getStackTraceString(ee));
                        }
                    }
                }
            }
        } catch (Exception e3) {
            //PPApplication.logE("ActivateProfileHelper.setMediaVolume", Log.getStackTraceString(e3));
        }
    }

    private static void setZenMode(Context context, int zenMode, AudioManager audioManager, int ringerMode)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            int systemZenMode = getSystemZenMode(context/*, -1*/);
            //PPApplication.logE("ActivateProfileHelper.setZenMode", "systemZenMode=" + systemZenMode);
            //int systemRingerMode = audioManager.getRingerMode();
            //PPApplication.logE("ActivateProfileHelper.setZenMode", "systemRingerMode=" + systemRingerMode);

            if ((zenMode != ZENMODE_SILENT) && canChangeZenMode(context, false)) {
                try {
                    if (ringerMode != -1) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        audioManager.setRingerMode(ringerMode);
                        //try { Thread.sleep(500); } catch (InterruptedException e) { }
                        //SystemClock.sleep(500);
                        PPApplication.sleep(500);
                    }

                    if ((zenMode != systemZenMode) || (zenMode == ZENMODE_PRIORITY)) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        PPNotificationListenerService.requestInterruptionFilter(context, zenMode);
                        InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(context, zenMode);
                    }
                } catch (Exception ignored) {}
            } else {
                try {
                    if (zenMode == ZENMODE_SILENT) {//audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                        //SystemClock.sleep(1000);
                        //PPApplication.sleep(1000);
                        //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        if ((systemZenMode != ZENMODE_ALL) && canChangeZenMode(context, false)) {
                            //PPApplication.logE("ActivateProfileHelper.setZenMode", "change zen mode");
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            PPNotificationListenerService.requestInterruptionFilter(context, ZENMODE_ALL);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(context, ZENMODE_ALL);
                            PPApplication.sleep(500);
                        }
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    } else {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        audioManager.setRingerMode(ringerMode);
                    }
                } catch (Exception ignored) {
                    // may be produced this exception:
                    //
                    // java.lang.SecurityException: Not allowed to change Do Not Disturb state
                    //
                    // when changed is ringer mode in activated Do not disturb
                    // GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context) returns false.
                }
            }
        //}
        //else {
        //    RingerModeChangeReceiver.notUnlinkVolumes = false;
        //    audioManager.setRingerMode(ringerMode);
        //}
    }

    private static void setVibrateWhenRinging(Context context, Profile profile, int value) {
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
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, null, false, context).allowed
                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //Log.e("ActivateProfileHelper.setVibrateWhenRinging", "1");
                if (Permissions.checkVibrateWhenRinging(context)) {
                    //Log.e("ActivateProfileHelper.setVibrateWhenRinging", "2");
                    if (android.os.Build.VERSION.SDK_INT < 23) {   // Not working in Android M (exception)
                        //Log.e("ActivateProfileHelper.setVibrateWhenRinging", "3");
                        Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", lValue);
                    }
                    else {
                        try {
                            //Log.e("ActivateProfileHelper.setVibrateWhenRinging", "4");
                            Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, lValue);
                        } catch (Exception ee) {
                            Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(ee));
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                //Log.e("ActivateProfileHelper.setVibrateWhenRinging", "5");
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command);
                                    /*} catch (RootDeniedException e) {
                                        PPApplication.rootMutex.rootGranted = false;
                                        Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(e));*/
                                    } catch (Exception e) {
                                        Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(e));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setTones(Context context, Profile profile) {
        if (Permissions.checkProfileRingTones(context, profile, null)) {
            if (profile._soundRingtoneChange == 1) {
                if (!profile._soundRingtone.isEmpty()) {
                    try {
                        String[] splits = profile._soundRingtone.split("\\|");
                        //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, splits[0]);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(splits[0]));
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
                        String[] splits = profile._soundNotification.split("\\|");
                        //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, splits[0]);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(splits[0]));
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
                        String[] splits = profile._soundAlarm.split("\\|");
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, splits[0]);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, Uri.parse(splits[0]));
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

    static void executeForVolumes(Context context, final Profile profile, final int linkUnlinkVolumes, final boolean forProfileActivation) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadVolumes();
        final Handler handler = new Handler(PPApplication.handlerThreadVolumes.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.executeForVolumes");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    // link, unlink volumes during activation of profile
                    int linkUnlink = PhoneCallBroadcastReceiver.LINKMODE_NONE;
                    if (ActivateProfileHelper.getMergedRingNotificationVolumes(appContext) &&
                            ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(appContext)) {
                        if (Permissions.checkPhone(appContext))
                            linkUnlink = linkUnlinkVolumes;
                    }

                    if (profile != null) {
                        setTones(appContext, profile);

                        if (/*Permissions.checkProfileVolumePreferences(context, profile) &&*/
                                Permissions.checkProfileAccessNotificationPolicy(appContext, profile, null)) {

                            final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

                            changeRingerModeForVolumeEqual0(profile, audioManager);
                            changeNotificationVolumeForVolumeEqual0(appContext, profile);

                            RingerModeChangeReceiver.internalChange = true;

                            setRingerMode(appContext, profile, audioManager, true, forProfileActivation);
                            // !!! DO NOT CALL setVolumes before setRingerMode(..., firsCall:false).
                            //     Ringer mode must be changed before call of setVolumes() because is checked in setVolumes().
                            setRingerMode(appContext, profile, audioManager, false, forProfileActivation);
                            PPApplication.sleep(500);
                            setVolumes(appContext, profile, audioManager, linkUnlink, forProfileActivation);
                            if (getSystemZenMode(appContext/*, -1*/) == ActivateProfileHelper.ZENMODE_PRIORITY) {
                                //PPApplication.sleep(500);
                                setRingerMode(appContext, profile, audioManager, false, /*linkUnlink,*/ forProfileActivation);
                            }

                            //try { Thread.sleep(500); } catch (InterruptedException e) { }
                            //SystemClock.sleep(500);
                            PPApplication.sleep(500);

                            PPApplication.startHandlerThreadInternalChangeToFalse();
                            final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //PPApplication.logE("ActivateProfileHelper.executeForVolumes", "disable ringer mode change internal change");
                                    RingerModeChangeReceiver.internalChange = false;
                                }
                            }, 3000);

                        }

                        setTones(appContext, profile);
                    }
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    private static void setNotificationLed(Context context, final int value) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadNotificationLed();
        final Handler handler = new Handler(PPApplication.handlerThreadNotificationLed.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.setHeadsUpNotifications");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, null, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                            Settings.System.putInt(appContext.getContentResolver(), "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/, value);
                        else {
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) &&
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/ + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command);
                                /*} catch (RootDeniedException e) {
                                    PPApplication.rootMutex.rootGranted = false;
                                    Log.e("ActivateProfileHelper.setNotificationLed", Log.getStackTraceString(e));*/
                                    } catch (Exception e) {
                                        Log.e("ActivateProfileHelper.setNotificationLed", Log.getStackTraceString(e));
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    private static void setHeadsUpNotifications(Context context, final int value) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadHeadsUpNotifications();
        final Handler handler = new Handler(PPApplication.handlerThreadHeadsUpNotifications.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.setHeadsUpNotifications");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, null, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        //if (android.os.Build.VERSION.SDK_INT >= 21) {
                            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                                Settings.Global.putInt(appContext.getContentResolver(), "heads_up_notifications_enabled", value);
                            } else {
                                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) &&
                                        (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                    synchronized (PPApplication.rootMutex) {
                                        String command1 = "settings put global " + "heads_up_notifications_enabled" + " " + value;
                                        //if (PPApplication.isSELinuxEnforcing())
                                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                        Command command = new Command(0, false, command1); //, command2);
                                        try {
                                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                            PPApplication.commandWait(command);
                                    /*} catch (RootDeniedException e) {
                                        PPApplication.rootMutex.rootGranted = false;
                                        Log.e("ActivateProfileHelper.setHeadsUpNotifications", Log.getStackTraceString(e));*/
                                        } catch (Exception e) {
                                            Log.e("ActivateProfileHelper.setHeadsUpNotifications", Log.getStackTraceString(e));
                                        }
                                    }
                                }
                            }
                        //}
                    }
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    private static void changeRingerModeForVolumeEqual0(Profile profile, AudioManager audioManager) {
        //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "volumeRingtoneChange=" + profile.getVolumeRingtoneChange());
        //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "volumeRingtoneValue=" + profile.getVolumeRingtoneValue());

        profile._ringerModeForZenMode = AudioManager.RINGER_MODE_NORMAL;

        if (profile.getVolumeRingtoneChange()) {

            if (profile.getVolumeRingtoneValue() == 0) {
                profile.setVolumeRingtoneValue(1);
                profile._ringerModeForZenMode = AudioManager.RINGER_MODE_SILENT;

                // for profile ringer/zen mode = "only vibrate" do not change ringer mode to Silent
                if (!isVibrateRingerMode(profile._volumeRingerMode/*, profile._volumeZenMode*/)) {
                    if (isAudibleRinging(profile._volumeRingerMode, profile._volumeZenMode/*, false*/)) {
                        // change ringer mode to Silent
                        //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "changed to silent");
                        profile._volumeRingerMode = Profile.RINGERMODE_SILENT;
                    }
                }
            } else {
                if ((profile._volumeRingerMode == 0) && (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)) {
                    // change ringer mode to Ringing
                    //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "changed to ringing");
                    profile._volumeRingerMode = Profile.RINGERMODE_RING;
                }
            }
        }
    }

    private static void changeNotificationVolumeForVolumeEqual0(Context context, Profile profile) {
        if (profile.getVolumeNotificationChange() && getMergedRingNotificationVolumes(context)) {
            if (profile.getVolumeNotificationValue() == 0) {
                //PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "changed notification value to 1");
                profile.setVolumeNotificationValue(1);
            }
        }
    }

    static boolean canChangeZenMode(Context context, boolean notCheckAccess) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                return notCheckAccess || Permissions.checkAccessNotificationPolicy(context);
            }
            else
                return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        }
        else
        //if (android.os.Build.VERSION.SDK_INT >= 21)
            return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        //return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static int getSystemZenMode(Context context/*, int defaultValue*/) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                    switch (interruptionFilter) {
                        case NotificationManager.INTERRUPTION_FILTER_ALL:
                        case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                            return ActivateProfileHelper.ZENMODE_ALL;
                        case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                            return ActivateProfileHelper.ZENMODE_PRIORITY;
                        case NotificationManager.INTERRUPTION_FILTER_NONE:
                            return ActivateProfileHelper.ZENMODE_NONE;
                        case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                            return ActivateProfileHelper.ZENMODE_ALARMS;
                    }
                }
            }
            else {
                int interruptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
                switch (interruptionFilter) {
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
        else {
        //if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) {
            int interruptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
            switch (interruptionFilter) {
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
        return -1;//defaultValue;
    }

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

        //PPApplication.logE("PPApplication.vibrationIsOn", "ringerMode="+ringerMode);
        //PPApplication.logE("PPApplication.vibrationIsOn", "vibrateType="+vibrateType);
        //PPApplication.logE("PPApplication.vibrationIsOn", "vibrateWhenRinging="+vibrateWhenRinging);

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT);// ||
        //(vibrateWhenRinging == 1);
    }

    private static void setRingerMode(Context context, Profile profile, AudioManager audioManager, boolean firstCall, boolean forProfileActivation)
    {
        // linkUnlink == LINKMODE_NONE: not do link and unlink volumes for phone call - called from ActivateProfileHelper.execute()
        // linkUnlink != LINKMODE_NONE: do link and unlink volumes for phone call - called from PhoneCallBroadcastReceiver

        int ringerMode;
        int zenMode;

        if (forProfileActivation) {
            if (profile._volumeRingerMode != 0) {
                setRingerMode(context, profile._volumeRingerMode);
                if ((profile._volumeRingerMode == Profile.RINGERMODE_ZENMODE) && (profile._volumeZenMode != 0))
                    setZenMode(context, profile._volumeZenMode);
            }
        }

        if (firstCall)
            return;

        ringerMode = getRingerMode(context);
        zenMode = getZenMode(context);

        if (forProfileActivation) {
            switch (ringerMode) {
                case Profile.RINGERMODE_RING:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=RING");
                    setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(context, null, 0);
                    break;
                case Profile.RINGERMODE_RING_AND_VIBRATE:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=RING & VIBRATE");
                    setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(context, null, 1);
                    break;
                case Profile.RINGERMODE_VIBRATE:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=VIBRATE");
                    setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(context, null, 1);
                    break;
                case Profile.RINGERMODE_SILENT:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=SILENT");
                    //if (android.os.Build.VERSION.SDK_INT >= 21) {
                        /*int systemRingerMode = audioManager.getRingerMode();
                        //int systemZenMode = getSystemZenMode(context);
                        PPApplication.logE("ActivateProfileHelper.setRingerMode", "systemRingerMode="+systemRingerMode);
                        //PPApplication.logE("ActivateProfileHelper.setRingerMode", "systemZenMode="+systemZenMode);
                        if (systemRingerMode != AudioManager.RINGER_MODE_SILENT) {
                            //setZenMode(ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_SILENT);
                            setZenMode(context, ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_NORMAL);
                        }*/
                        setZenMode(context, ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_SILENT);
                    /*}
                    else {
                        setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_SILENT);
                        try {
                            //noinspection deprecation
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception ignored) {
                        }
                        try {
                            //noinspection deprecation
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception ignored) {
                        }
                    }*/
                    setVibrateWhenRinging(context, null, 0);
                    break;
                case Profile.RINGERMODE_ZENMODE:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=ZEN MODE");
                    switch (zenMode) {
                        case Profile.ZENMODE_ALL:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALL");
                            setZenMode(context, ZENMODE_ALL, audioManager, /*AudioManager.RINGER_MODE_NORMAL*/profile._ringerModeForZenMode);
                            setVibrateWhenRinging(context, profile, -1);
                            break;
                        case Profile.ZENMODE_PRIORITY:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=PRIORITY");
                            setZenMode(context, ZENMODE_PRIORITY, audioManager, /*AudioManager.RINGER_MODE_NORMAL*/profile._ringerModeForZenMode);
                            setVibrateWhenRinging(context, profile, -1);
                            break;
                        case Profile.ZENMODE_NONE:
                            //int systemRingerMode = audioManager.getRingerMode();
                            //int systemZenMode = getSystemZenMode(context);
                            //if ((systemZenMode != ActivateProfileHelper.ZENMODE_NONE) ||
                            //        (systemRingerMode != profile._ringerModeForZenMode)) {
                                //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=NONE");
                                // must be set to ALL and after to NONE
                                // without this, duplicate set this zen mode not working
                                // change only zen mode, not ringer mode
                                setZenMode(context, ZENMODE_ALL, audioManager, -1/*AudioManager.RINGER_MODE_NORMAL*/);
                                //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                //SystemClock.sleep(1000);
                                PPApplication.sleep(1000);
                                setZenMode(context, ZENMODE_NONE, audioManager, /*AudioManager.RINGER_MODE_NORMAL*/profile._ringerModeForZenMode);
                            //}
                            //else
                            //    PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=NONE -> already set");
                            break;
                        case Profile.ZENMODE_ALL_AND_VIBRATE:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALL & VIBRATE");
                            setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(context, null, 1);
                            break;
                        case Profile.ZENMODE_PRIORITY_AND_VIBRATE:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=PRIORITY & VIBRATE");
                            setZenMode(context, ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(context, null, 1);
                            break;
                        case Profile.ZENMODE_ALARMS:
                            //systemRingerMode = audioManager.getRingerMode();
                            //systemZenMode = getSystemZenMode(context);
                            //if ((systemZenMode != ActivateProfileHelper.ZENMODE_ALARMS) ||
                            //        (systemRingerMode != profile._ringerModeForZenMode)) {
                                //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALARMS");
                                // must be set to ALL and after to ALARMS
                                // without this, duplicate set this zen mode not working
                                // change only zen mode, not ringer mode
                                setZenMode(context, ZENMODE_ALL, audioManager, -1/*AudioManager.RINGER_MODE_NORMAL*/);
                                //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                //SystemClock.sleep(1000);
                                PPApplication.sleep(1000);
                                setZenMode(context, ZENMODE_ALARMS, audioManager, /*AudioManager.RINGER_MODE_NORMAL*/profile._ringerModeForZenMode);
                            //}
                            //else
                            //    PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALARMS -> already set");
                            break;
                    }
                    break;
            }
        }
    }

    private static void executeForWallpaper(Context context, final Profile profile) {
        if (profile._deviceWallpaperChange == 1)
        {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadWallpaper();
            final Handler handler = new Handler(PPApplication.handlerThreadWallpaper.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.executeForWallpaper");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        WindowManager wm = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                        if (wm != null) {
                            Display display = wm.getDefaultDisplay();
                            //if (android.os.Build.VERSION.SDK_INT >= 17)
                            display.getRealMetrics(displayMetrics);
                        /*else
                            display.getMetrics(displayMetrics);*/
                            int height = displayMetrics.heightPixels;
                            int width = displayMetrics.widthPixels;
                            if (appContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                //noinspection SuspiciousNameCombination
                                height = displayMetrics.widthPixels;
                                //noinspection SuspiciousNameCombination
                                width = displayMetrics.heightPixels;
                            }
                            // for lock screen no double width
                            if ((android.os.Build.VERSION.SDK_INT < 24) || (profile._deviceWallpaperFor != 2))
                                width = width << 1; // best wallpaper width is twice screen width

                            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmapUri(profile._deviceWallpaper, width, height, false, true, appContext);
                            if (decodedSampleBitmap != null) {
                                // set wallpaper
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(appContext);
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
                                    } else
                                        wallpaperManager.setBitmap(decodedSampleBitmap);
                                } catch (IOException e) {
                                    Log.e("ActivateProfileHelper.executeForWallpaper", Log.getStackTraceString(e));
                                }
                            }
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
    }

    private static void executeForRunApplications(Context context, final Profile profile) {
        if (profile._deviceRunApplicationChange == 1)
        {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadRunApplication();
            final Handler handler = new Handler(PPApplication.handlerThreadRunApplication.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (PPApplication.blockProfileEventActions)
                        // not start application after boot
                        return;

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.executeForRunApplications");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
                        Intent intent;
                        PackageManager packageManager = appContext.getPackageManager();

                        for (String split : splits) {
                            int startApplicationDelay = Application.getStartApplicationDelay(split);
                            if (Application.getStartApplicationDelay(split) > 0) {
                                RunApplicationWithDelayBroadcastReceiver.setDelayAlarm(appContext, startApplicationDelay, split);
                            } else {
                                if (Application.isShortcut(split)) {
                                    long shortcutId = Application.getShortcutId(split);
                                    if (shortcutId > 0) {
                                        //Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(shortcutId);
                                        Shortcut shortcut = DatabaseHandler.getInstance(appContext).getShortcut(shortcutId);
                                        if (shortcut != null) {
                                            try {
                                                intent = Intent.parseUri(shortcut._intent, 0);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                try {
                                                    appContext.startActivity(intent);
                                                    //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                                    //SystemClock.sleep(1000);
                                                    PPApplication.sleep(1000);
                                                } catch (Exception ignore) {
                                                }
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                } else if (Application.isIntent(split)) {
                                    long intentId = Application.getIntentId(split);
                                    if (intentId > 0) {
                                        PPIntent ppIntent = DatabaseHandler.getInstance(appContext).getIntent(intentId);
                                        if (ppIntent != null) {
                                            intent = ApplicationEditorIntentActivityX.createIntent(ppIntent);
                                            if (intent != null) {
                                                if (ppIntent._intentType == 0) {
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    try {
                                                        appContext.startActivity(intent);
                                                        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                                        //SystemClock.sleep(1000);
                                                        PPApplication.sleep(1000);
                                                    } catch (Exception ignored) {
                                                    }
                                                } else {
                                                    try {
                                                        appContext.sendBroadcast(intent);
                                                        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                                        //SystemClock.sleep(1000);
                                                        PPApplication.sleep(1000);
                                                    } catch (Exception ignored) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    intent = packageManager.getLaunchIntentForPackage(Application.getPackageName(split));
                                    if (intent != null) {
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        try {
                                            appContext.startActivity(intent);
                                            //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                            //SystemClock.sleep(1000);
                                            PPApplication.sleep(1000);
                                        } catch (Exception ignore) {
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
        }
    }

    private static void executeForForceStopApplications(final Profile profile, Context context) {
        //PPApplication.logE("ActivateProfilesHelper.executeForForceStopApplications", "xxx");

        if (PPApplication.blockProfileEventActions)
            // not force stop applications after boot
            return;

        if (profile._lockDevice != 0)
            // not force stop if profile has lock device enabled
            return;

        String applications = profile._deviceForceStopApplicationPackageName;
        if (!(applications.isEmpty() || (applications.equals("-")))) {
            Intent intent = new Intent(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_START);
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            intent.putExtra(PPApplication.EXTRA_APPLICATIONS, applications);
            context.sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
    }

    static void executeForInteractivePreferences(final Profile profile, final Context context) {
        if (profile == null)
            return;

        if (profile._deviceRunApplicationChange == 1)
        {
            executeForRunApplications(context, profile);
        }

        //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, null, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            if (profile._deviceMobileDataPrefs == 1)
            {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    boolean ok = true;
                    try {
                        Intent intent = new Intent(Intent.ACTION_MAIN, null);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        ok = false;
                    }
                    if (!ok) {
                        ok = true;
                        try {
                            final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                            intent.setComponent(componentName);
                            context.startActivity(intent);
                        } catch (Exception e) {
                            ok = false;
                        }
                    }
                    if (!ok) {
                        try {
                            final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (Exception ignored) {
                        }
                    }
                }
                else {
                    boolean ok = false;
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                    if (GlobalGUIRoutines.activityIntentExists(intent, context))
                        ok = true;
                    if (!ok) {
                        intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.Settings"));
                        if (GlobalGUIRoutines.activityIntentExists(intent, context))
                            ok = true;
                    }
                    if (!ok) {
                        intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        if (GlobalGUIRoutines.activityIntentExists(intent, context))
                            ok = true;
                    }
                    if (ok) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String title = context.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                        String text = context.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                                context.getString(R.string.profile_preferences_deviceMobileDataPrefs);
                        showNotificationForInteractiveParameters(context, title, text, intent, PPApplication.PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID);
                    }
                }
            }
        }

        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, null, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            if (profile._deviceNetworkTypePrefs == 1)
            {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    try {
                        final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
                else {
                    Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String title = context.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                    String text = context.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                            context.getString(R.string.profile_preferences_deviceNetworkTypePrefs);
                    showNotificationForInteractiveParameters(context, title, text, intent, PPApplication.PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID);
                }
            }
        }

        //if (PPApplication.hardwareCheck(Profile.PREF_PROFILE_DEVICE_GPS, context))
        //{  No check only GPS
        if (profile._deviceLocationServicePrefs == 1)
        {
            if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                try {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception ignored) {
                }
            }
            else {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                if (GlobalGUIRoutines.activityIntentExists(intent, context)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String title = context.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                    String text = context.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                            context.getString(R.string.profile_preferences_deviceLocationServicePrefs);
                    showNotificationForInteractiveParameters(context, title, text, intent, PPApplication.PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID);
                }
            }
        }
        //}
        if (profile._deviceWiFiAPPrefs == 1) {
            if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
                    context.startActivity(intent);
                } catch (Exception ignored) {
                }
            }
            else {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
                if (GlobalGUIRoutines.activityIntentExists(intent, context)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String title = context.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                    String text = context.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                            context.getString(R.string.profile_preferences_deviceWiFiAPPrefs);
                    showNotificationForInteractiveParameters(context, title, text, intent, PPApplication.PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID);
                }
            }
        }
    }

    private static void executeRootForAdaptiveBrightness(Context context, final Profile profile) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadAdaptiveBrightness();
        final Handler handler = new Handler(PPApplication.handlerThreadAdaptiveBrightness.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.executeRootForAdaptiveBrightness");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) &&
                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " +
                                    profile.getDeviceBrightnessAdaptiveValue(appContext);
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command);
                        /*} catch (RootDeniedException e) {
                            PPApplication.rootMutex.rootGranted = false;
                            Log.e("ActivateProfileHelper.executeRootForAdaptiveBrightness", Log.getStackTraceString(e));*/
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.executeRootForAdaptiveBrightness", Log.getStackTraceString(e));
                            }
                        }
                    }
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    public static void execute(final Context context, Profile _profile/*, boolean _interactive*/)
    {
        // separate ringing and notification - is marked with @Hide :-(
        //Settings.System.putInt(context.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        final Profile profile = _profile; //Profile.getMappedProfile(_profile, context);

        // setup volume and ringer mode
        ActivateProfileHelper.executeForVolumes(context, profile, PhoneCallBroadcastReceiver.LINKMODE_NONE, true);

        // set vibration on touch
        if (Permissions.checkProfileVibrationOnTouch(context, profile, null)) {
            switch (profile._vibrationOnTouch) {
                case 1:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                    break;
                case 2:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                    break;
            }
        }
        // set dtmf tone when dialing
        if (Permissions.checkProfileDtmfToneWhenDialing(context, profile, null)) {
            switch (profile._dtmfToneWhenDialing) {
                case 1:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1);
                    break;
                case 2:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 0);
                    break;
            }
        }
        // set sound on touch
        if (Permissions.checkProfileSoundOnTouch(context, profile, null)) {
            switch (profile._soundOnTouch) {
                case 1:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                    break;
                case 2:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                    break;
            }
        }

        // setup tones
        // moved to executeForVolumes
        //setTones(profile);

        // setup radio preferences
        ActivateProfileHelper.executeForRadios(context, profile);

        // setup auto-sync
        try {
            boolean _isAutoSync = ContentResolver.getMasterSyncAutomatically();
            boolean _setAutoSync = false;
            switch (profile._deviceAutoSync) {
                case 1:
                    if (!_isAutoSync) {
                        _isAutoSync = true;
                        _setAutoSync = true;
                    }
                    break;
                case 2:
                    if (_isAutoSync) {
                        _isAutoSync = false;
                        _setAutoSync = true;
                    }
                    break;
                case 3:
                    _isAutoSync = !_isAutoSync;
                    _setAutoSync = true;
                    break;
            }
            if (_setAutoSync)
                ContentResolver.setMasterSyncAutomatically(_isAutoSync);
        } catch (Exception ignored) {} // fixed DeadObjectException

        // screen timeout
        if (Permissions.checkProfileScreenTimeout(context, profile, null)) {
            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (PPApplication.isScreenOn) {
                //PPApplication.logE("ActivateProfileHelper.execute","screen on");
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            //PPApplication.logE("ActivateProfileHelper.execute","call setScreenTimeout");
                            setScreenTimeout(profile._deviceScreenTimeout, context);
                        }
                    });
                }// else
                //    setScreenTimeout(profile._deviceScreenTimeout);
            }
            else {
                //PPApplication.logE("ActivateProfileHelper.execute","screen off");
                setActivatedProfileScreenTimeout(context, profile._deviceScreenTimeout);
            }
        }
        //else
        //    PPApplication.setActivatedProfileScreenTimeout(context, 0);

        // on/off lock screen
        boolean setLockScreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                // enable lockscreen
                setLockScreenDisabled(context, false);
                setLockScreen = true;
                break;
            case 2:
                // disable lockscreen
                setLockScreenDisabled(context, true);
                setLockScreen = true;
                break;
        }
        if (setLockScreen) {
            //boolean isScreenOn;
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                //isScreenOn = PPApplication.isScreenOn(pm);
                //}
                //PPApplication.logE("$$$ ActivateProfileHelper.execute","isScreenOn="+isScreenOn);
                boolean keyguardShowing;
                KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null) {
                    keyguardShowing = kgMgr.isKeyguardLocked();
                    //PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguardShowing="+keyguardShowing);

                    if (PPApplication.isScreenOn && !keyguardShowing) {
                        try {
                            // start PhoneProfilesService
                            //PPApplication.firstStartServiceStarted = false;
                            /*Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                            PPApplication.startPPService(context, serviceIntent);*/
                            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            commandIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                            PPApplication.runCommand(context, commandIntent);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        // setup brightness
        if (Permissions.checkProfileScreenBrightness(context, profile, null)) {
            if (profile.getDeviceBrightnessChange()) {
                try {
                    if (profile.getDeviceBrightnessAutomatic()) {
                        Settings.System.putInt(context.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        if (profile.getDeviceBrightnessChangeLevel()) {
                            Settings.System.putInt(context.getContentResolver(),
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    profile.getDeviceBrightnessManualValue(context));
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, null, true, context).allowed
                                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
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
                                        ActivateProfileHelper.executeRootForAdaptiveBrightness(context, profile);
                                    }
                                }
                            }
                        }
                    } else {
                        Settings.System.putInt(context.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        if (profile.getDeviceBrightnessChangeLevel()) {
                            Settings.System.putInt(context.getContentResolver(),
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    profile.getDeviceBrightnessManualValue(context));
                        }
                    }

                    if (PPApplication.brightnessHandler != null) {
                        PPApplication.brightnessHandler.post(new Runnable() {
                            public void run() {
                                createBrightnessView(profile, context);
                            }
                        });
                    }// else
                    //    createBrightnessView(profile, context);
                } catch (Exception ignored) {}
            }
        }

        // setup rotate
        if (Permissions.checkProfileAutoRotation(context, profile, null)) {
            switch (profile._deviceAutoRotate) {
                case 1:
                    // set autorotate on
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    //Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    break;
                case 6:
                    // set autorotate off
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    //Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
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
                    setNotificationLed(context, 1);
                    break;
                case 2:
                    setNotificationLed(context, 0);
                    break;
            }
            //}
        }

        // setup wallpaper
        if (Permissions.checkProfileWallpaper(context, profile, null)) {
            if (profile._deviceWallpaperChange == 1) {
                ActivateProfileHelper.executeForWallpaper(context, profile);
            }
        }

        //Intent rootServiceIntent;

        // set power save mode
        ActivateProfileHelper.setPowerSaveMode(context, profile);

        if (Permissions.checkProfileLockDevice(context, profile, null)) {
            if (profile._lockDevice != 0) {
                boolean keyguardLocked;
                KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null) {
                    keyguardLocked = kgMgr.isKeyguardLocked();
                    //PPApplication.logE("---$$$ ActivateProfileHelper.execute", "keyguardLocked=" + keyguardLocked);
                    if (!keyguardLocked) {
                        ActivateProfileHelper.lockDevice(context, profile);
                    }
                }
            }
        }

        // set heads-up notifications
        if (profile._headsUpNotifications != 0) {
            switch (profile._headsUpNotifications) {
                case 1:
                    setHeadsUpNotifications(context, 1);
                    break;
                case 2:
                    setHeadsUpNotifications(context, 0);
                    break;
            }
        }

        /*
        // set screen night mode
        if (profile._screenNightMode != 0) {
            setScreenNightMode(context, profile._screenNightMode);
        }
        */

        // close all applications
        if (profile._deviceCloseAllApplications == 1) {
            //if (!PPApplication.startedOnBoot) {
            if (!PPApplication.blockProfileEventActions) {
                try {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startMain);
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.execute", Log.getStackTraceString(e));
                }
            }
        }


        if (profile._deviceForceStopApplicationChange == 1) {
            boolean enabled;
            if (Build.VERSION.SDK_INT >= 29)
                enabled = PPPExtenderBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_5_1_2);
            else
                enabled = PPPExtenderBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_3_0);
            if (enabled) {
                // executeForInteractivePreferences() is called from broadcast receiver PPPExtenderBroadcastReceiver
                ActivateProfileHelper.executeForForceStopApplications(profile, context);
            }
        }
        else
            executeForInteractivePreferences(profile, context);

    }

    private static void showNotificationForInteractiveParameters(Context context, String title, String text, Intent intent, int notificationId) {
        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = context.getString(R.string.pp_app_name);
            nText = title+": "+text;
        }
        PPApplication.createInformationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.INFORMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(notificationId, mBuilder.build());
    }

    static void setScreenTimeout(int screenTimeout, Context context) {
        disableScreenTimeoutInternalChange = true;
        //Log.d("ActivateProfileHelper.setScreenTimeout", "current="+Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0));
        //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "current="+Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0));
        switch (screenTimeout) {
            case 1:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 15000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                break;
            case 2:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 30000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                break;
            case 3:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 60000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                break;
            case 4:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 120000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                break;
            case 5:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 600000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                break;
            case 6:
                //2147483647 = Integer.MAX_VALUE
                //18000000   = 5 hours
                //86400000   = 24 hours
                //43200000   = 12 hours
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null)) {
                    //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "max value - lock activity displayed");
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 86400000;
                }
                else {
                    //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "max value - lock activity not displayed");
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000); //18000000);
                }
                break;
            case 7:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 300000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000);
                break;
            case 8:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null)) {
                    //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "permanent on - lock activity displayed");
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 86400000;
                }
                else {
                    //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "permanent on - lock activity not displayed");
                    createScreenTimeoutAlwaysOnView(context);
                }
                break;
            case 9:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 1800000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1800000);
                break;
        }
        setActivatedProfileScreenTimeout(context, 0);
        PPApplication.startHandlerThreadInternalChangeToFalse();
        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "disable screen timeout internal change");
                disableScreenTimeoutInternalChange = false;
            }
        }, 3000);
    }

    private static void createScreenTimeoutAlwaysOnView(Context context)
    {
        //PPApplication.logE("ActivateProfileHelper.createScreenTimeoutAlwaysOnView", "xxx");
        removeScreenTimeoutAlwaysOnView(context);

        if (PhoneProfilesService.getInstance() != null) {
            final Context appContext = context.getApplicationContext();

            // Put 24 hour screen timeout. Required for SettingsContentObserver.OnChange to call removeScreenTimeoutAlwaysOnView
            // when user change system setting.
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000);

            WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {

                //PPApplication.logE("ActivateProfileHelper.createScreenTimeoutAlwaysOnView", "windowManager != null");

                int type;
                if (android.os.Build.VERSION.SDK_INT < 25)
                    type = WindowManager.LayoutParams.TYPE_TOAST;
                else if (android.os.Build.VERSION.SDK_INT < 26)
                    type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
                else
                    type = LayoutParams.TYPE_APPLICATION_OVERLAY;
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE /*| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE*/ | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT
                );
                //PPApplication.logE("ActivateProfileHelper.createScreenTimeoutAlwaysOnView", "params configured");
                /*if (android.os.Build.VERSION.SDK_INT < 17)
                    params.gravity = Gravity.RIGHT | Gravity.TOP;
                else
                    params.gravity = Gravity.END | Gravity.TOP;*/
                PhoneProfilesService.getInstance().keepScreenOnView = new BrightnessView(appContext);
                //PPApplication.logE("ActivateProfileHelper.createScreenTimeoutAlwaysOnView", "new BrightnessView() called");
                try {
                    //PPApplication.logE("ActivateProfileHelper.createScreenTimeoutAlwaysOnView", "call addView");
                    windowManager.addView(PhoneProfilesService.getInstance().keepScreenOnView, params);
                } catch (Exception e) {
                    //PPApplication.logE("ActivateProfileHelper.createScreenTimeoutAlwaysOnView", Log.getStackTraceString(e));
                    PhoneProfilesService.getInstance().keepScreenOnView = null;
                }
            }
        }
    }

    static void removeScreenTimeoutAlwaysOnView(Context context)
    {
        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().keepScreenOnView != null) {
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(PhoneProfilesService.getInstance().keepScreenOnView);
                    } catch (Exception ignore) {
                    }
                    PhoneProfilesService.getInstance().keepScreenOnView = null;
                }
            }
        }
        //PPApplication.logE("@@@ createScreenTimeoutAlwaysOnView.unlock", "xxx");
    }

    @SuppressLint("RtlHardcoded")
    private static void createBrightnessView(Profile profile, Context context)
    {
        if (PhoneProfilesService.getInstance() != null) {
            final Context appContext = context.getApplicationContext();

            WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                if (PhoneProfilesService.getInstance().brightnessView != null) {
                    try {
                        windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                    } catch (Exception ignored) {
                    }
                    PhoneProfilesService.getInstance().brightnessView = null;
                }
                int type;
                if (android.os.Build.VERSION.SDK_INT < 25)
                    type = WindowManager.LayoutParams.TYPE_TOAST;
                else if (android.os.Build.VERSION.SDK_INT < 26)
                    type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
                else
                    type = LayoutParams.TYPE_APPLICATION_OVERLAY;
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
                if (profile.getDeviceBrightnessAutomatic() || (!profile.getDeviceBrightnessChangeLevel()))
                    params.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                else
                    params.screenBrightness = profile.getDeviceBrightnessManualValue(appContext) / (float) 255;
                PhoneProfilesService.getInstance().brightnessView = new BrightnessView(appContext);
                try {
                    windowManager.addView(PhoneProfilesService.getInstance().brightnessView, params);
                } catch (Exception e) {
                    PhoneProfilesService.getInstance().brightnessView = null;
                }

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //PPApplication.logE("ActivateProfileHelper.createBrightnessView", "remove brightness view");

                        WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                        if (windowManager != null) {
                            if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().brightnessView != null)) {
                                try {
                                    windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                                } catch (Exception ignored) {
                                }
                                PhoneProfilesService.getInstance().brightnessView = null;
                            }
                        }
                    }
                }, 5000);
            }
        }
    }

    static void removeBrightnessView(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().brightnessView != null) {
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                    } catch (Exception ignore) {
                    }
                    PhoneProfilesService.getInstance().brightnessView = null;
                }
            }
        }
    }

    static void updateGUI(Context context, boolean alsoEditor)
    {
        if (lockRefresh || EditorProfilesActivity.doImport)
            // no refresh widgets
            return;

        // icon widget
        try {
            /*Intent intent = new Intent(context, IconWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);*/
            int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
            IconWidgetProvider myWidget = new IconWidgetProvider();
            myWidget.onUpdate(context, AppWidgetManager.getInstance(context),ids);
        } catch (Exception ignored) {}

        // one row widget
        try {
            /*Intent intent4 = new Intent(context, OneRowWidgetProvider.class);
            intent4.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int ids4[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
            intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4);
            context.sendBroadcast(intent4);*/
            int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
            OneRowWidgetProvider myWidget = new OneRowWidgetProvider();
            myWidget.onUpdate(context, AppWidgetManager.getInstance(context),ids);
        } catch (Exception ignored) {}

        // list widget
        try {
            /*Intent intent2 = new Intent(context, ProfileListWidgetProvider.class);
            intent2.setAction(ProfileListWidgetProvider.INTENT_REFRESH_LISTWIDGET);
            int ids2[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
            context.sendBroadcast(intent2);*/
            //int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));
            ProfileListWidgetProvider myWidget = new ProfileListWidgetProvider();
            myWidget.updateWidgets(context);
        } catch (Exception ignored) {}

        // Samsung edge panel
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            try {
                /*Intent intent2 = new Intent(context, SamsungEdgeProvider.class);
                intent2.setAction(SamsungEdgeProvider.INTENT_REFRESH_EDGEPANEL);
                context.sendBroadcast(intent2);*/
                SamsungEdgeProvider myWidget = new SamsungEdgeProvider();
                myWidget.updateWidgets(context);
            } catch (Exception ignored) {
            }
        }

        // dash clock extension
        Intent intent3 = new Intent(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        // activities
        Intent intent5 = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
        intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);
    }



    @SuppressLint("NewApi")
    private static boolean isAirplaneMode(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 17)
            return Settings.Global.getInt(context.getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;
        /*else
            //noinspection deprecation
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;*/
    }

    private static void setAirplaneMode(Context context, boolean mode)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 17)
            setAirplaneMode_SDK17(context, mode);
        /*else
            setAirplaneMode_SDK8(context, mode);*/
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
                return false;
            } catch (NoSuchMethodException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            } catch (IllegalAccessException e) {
                return false;
            } catch (InvocationTargetException e) {
                return false;
            }
        }
    }
    */
    private static boolean isMobileData(Context context)
    {
        /*if (android.os.Build.VERSION.SDK_INT < 21)
        {
            ConnectivityManager connectivityManager = null;
            try {
                connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception ignored) {
                // java.lang.NullPointerException: missing IConnectivityManager
                // Dual SIM?? Bug in Android ???
            }
            if (connectivityManager != null) {
                try {
                    final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                    final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else*/
        if (android.os.Build.VERSION.SDK_INT < 22)
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;
            Object ITelephonyStub;
            Class<?> ITelephonyClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    getITelephonyMethod.setAccessible(true);
                    ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);

                    if (ITelephonyStub != null) {
                        ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                        getDataEnabledMethod = ITelephonyClass.getDeclaredMethod("getDataEnabled");

                        getDataEnabledMethod.setAccessible(true);

                        //noinspection ConstantConditions
                        return (Boolean) getDataEnabledMethod.invoke(ITelephonyStub);
                    }
                    else
                        return false;

                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
        if (android.os.Build.VERSION.SDK_INT < 28)
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                    getDataEnabledMethod.setAccessible(true);

                    //noinspection ConstantConditions
                    return (Boolean) getDataEnabledMethod.invoke(telephonyManager);

                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return CmdMobileData.isEnabled(context);
    }

    static boolean canSetMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 28)
            return true;
        else
        if (android.os.Build.VERSION.SDK_INT >= 22)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                    getDataEnabledMethod.setAccessible(true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    getITelephonyMethod.setAccessible(true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        /*else
        {
            ConnectivityManager connectivityManager = null;
            try {
                connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception ignored) {
                // java.lang.NullPointerException: missing IConnectivityManager
                // Dual SIM?? Bug in Android ???
            }
            if (connectivityManager != null) {
                try {
                    final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                    final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }*/
    }

    private static void setMobileData(Context context, boolean enable)
    {
        // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.MODIFY_PHONE_STATE
        // not working :-/
        /*if (Permissions.hasPermission(context, Manifest.permission.MODIFY_PHONE_STATE)) {
            if (android.os.Build.VERSION.SDK_INT == 21)
            {
                Method dataConnSwitchMethod;
                Class<?> telephonyManagerClass;
                Object ITelephonyStub;
                Class<?> ITelephonyClass;

                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
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
            }
            else
            {
                Method setDataEnabledMethod;
                Class<?> telephonyManagerClass;

                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    try {
                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        setDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                        setDataEnabledMethod.setAccessible(true);

                        setDataEnabledMethod.invoke(telephonyManager, enable);

                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else*/
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted(false)))
        {
/*            synchronized (PPApplication.rootMutex) {
                String command1 = "svc data " + (enable ? "enable" : "disable");
                PPApplication.logE("ActivateProfileHelper.setMobileData", "command=" + command1);
                Command command = new Command(0, false, command1);// {
//                @Override
//                public void commandOutput(int id, String line) {
//                    super.commandOutput(id, line);
//                    PPApplication.logE("ActivateProfileHelper.setMobileData","shell output="+line);
//                }
//
//                @Override
//                public void commandTerminated(int id, String reason) {
//                    super.commandTerminated(id, reason);
//                    PPApplication.logE("ActivateProfileHelper.setMobileData","terminated="+reason);
//                }
//
//                @Override
//                public void commandCompleted(int id, int exitCode) {
//                    super.commandCompleted(id, exitCode);
//                    PPApplication.logE("ActivateProfileHelper.setMobileData","completed="+exitCode);
//                }
//                };
                try {
                    RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                    PPApplication.commandWait(command);
                    PPApplication.logE("ActivateProfileHelper.setMobileData", "after wait");
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                }
            }
*/
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            Object serviceManager = PPApplication.getServiceManager("phone");
            int transactionCode = -1;
            if (serviceManager != null) {
                if (Build.VERSION.SDK_INT >= 28)
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setUserDataEnabled");
                else
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setDataEnabled");
            }

            int state = enable ? 1 : 0;

            if (transactionCode != -1) {
                // Android 6?
                if (Build.VERSION.SDK_INT >= 23) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager)context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                            //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionList="+subscriptionList);
                        } catch (SecurityException ignored) {
                        }
                        if (subscriptionList != null) {
                            //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionList.size()="+subscriptionList.size());
                            for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionInfo="+subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                                    //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionId="+subscriptionId);
                                    synchronized (PPApplication.rootMutex) {
                                        String command1 = PPApplication.getServiceCommand("phone", transactionCode, subscriptionId, state);
                                        //PPApplication.logE("ActivateProfileHelper.setMobileData", "command1="+command1);
                                        if (command1 != null) {
                                            Command command = new Command(0, false, command1);
                                            try {
                                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                PPApplication.commandWait(command);
                                            } catch (Exception e) {
                                                //PPApplication.logE("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    synchronized (PPApplication.rootMutex) {
                        String command1 = PPApplication.getServiceCommand("phone", transactionCode, state);
                        if (command1 != null) {
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                            }
                        }
                    }
                }
            }
        }
    }

    /*
    private int getPreferredNetworkType(Context context) {
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted()))
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
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (RootDeniedException e) {
                        PPApplication.rootMutex.rootGranted = false;
                        Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
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

    static boolean telephonyServiceExists(/*Context context, */
            @SuppressWarnings("SameParameterValue") String preference) {
        try {
            Object serviceManager = PPApplication.getServiceManager("phone");
            if (serviceManager != null) {
                int transactionCode = -1;
                if (preference.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                    if (Build.VERSION.SDK_INT >= 28)
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setUserDataEnabled");
                    else
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setDataEnabled");
                }
                else
                if (preference.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE))
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setPreferredNetworkType");
                return transactionCode != -1;
            }
            return false;
        } catch(Exception e) {
            return false;
        }
    }

    private static void setPreferredNetworkType(Context context, int networkType)
    {
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted(false) && PPApplication.serviceBinaryExists(false)))
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                Object serviceManager = PPApplication.getServiceManager("phone");
                int transactionCode = -1;
                if (serviceManager != null) {
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setPreferredNetworkType");
                }

                if (transactionCode != -1) {
                    // Android 6?
                    if (Build.VERSION.SDK_INT >= 23) {
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager)context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //SubscriptionManager.from(context);
                        if (mSubscriptionManager != null) {
                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                            } catch (SecurityException ignored) {
                            }
                            if (subscriptionList != null) {
                                for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                    if (subscriptionInfo != null) {
                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                                        synchronized (PPApplication.rootMutex) {
                                            String command1 = PPApplication.getServiceCommand("phone", transactionCode, subscriptionId, networkType);
                                            if (command1 != null) {
                                                Command command = new Command(0, false, command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    PPApplication.commandWait(command);
                                            /*} catch (RootDeniedException e) {
                                                PPApplication.rootMutex.rootGranted = false;
                                                Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));*/
                                                } catch (Exception e) {
                                                    Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = PPApplication.getServiceCommand("phone", transactionCode, networkType);
                            if (command1 != null) {
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command);
                                /*} catch (RootDeniedException e) {
                                    PPApplication.rootMutex.rootGranted = false;
                                    Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));*/
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                                }
                            }
                        }
                    }
                }
            } catch(Exception ignored) {
            }
        }
    }

    static boolean wifiServiceExists(/*Context context, */
            @SuppressWarnings("SameParameterValue") String preference) {
        try {
            Object serviceManager = PPApplication.getServiceManager("wifi");
            if (serviceManager != null) {
                int transactionCode = -1;
                if (preference.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setWifiApEnabled");
                return transactionCode != -1;
            }
            return false;
        } catch(Exception e) {
            return false;
        }
    }

    private static void setWifiAP(Context context, WifiApManager wifiApManager, boolean enable) {
        //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-enable="+enable);

        if (Build.VERSION.SDK_INT < 26) {
            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-API < 26");
            wifiApManager.setWifiApState(enable);
        }
        else
        if (Build.VERSION.SDK_INT < 28) {
            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-API >= 26");
            if (WifiApManager.canExploitWifiTethering(context)) {
                if (enable)
                    wifiApManager.startTethering();
                else
                    wifiApManager.stopTethering();
            }
            else
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                    (PPApplication.isRooted(false) && PPApplication.serviceBinaryExists(false))) {
                //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-rooted");
                try {
                    Object serviceManager = PPApplication.getServiceManager("wifi");
                    int transactionCode = -1;
                    if (serviceManager != null) {
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setWifiApEnabled");
                    }
                    //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-serviceManager=" + serviceManager);
                    //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-transactionCode=" + transactionCode);

                    if (transactionCode != -1) {
                        if (enable) {
                            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-wifiManager=" + wifiManager);
                            if (wifiManager != null) {
                                int wifiState = wifiManager.getWifiState();
                                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                                //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-isWifiEnabled=" + isWifiEnabled);
                                if (isWifiEnabled) {
                                    //PPApplication.logE("#### setWifiEnabled", "from ActivateProfileHelper.setWifiAP");
                                    wifiManager.setWifiEnabled(false);
                                    PPApplication.sleep(1000);
                                }
                            }
                        }
                        synchronized (PPApplication.rootMutex) {
                            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-start root command");
                            String command1 = PPApplication.getServiceCommand("wifi", transactionCode, 0, (enable) ? 1 : 0);
                            if (command1 != null) {
                                //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-command1=" + command1);
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command);
                                    //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-root command end");
                            /*} catch (RootDeniedException e) {
                                PPApplication.rootMutex.rootGranted = false;
                                Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));*/
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));
                                    //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-root command error");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));
                    //PPApplication.logE("$$$ WifiAP", Log.getStackTraceString(e));
                }
            }
        }
        else {
            if (enable)
                wifiApManager.startTethering();
            else
                wifiApManager.stopTethering();
        }
    }

    private static void setNFC(Context context, boolean enable)
    {
        if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            CmdNfc.setNFC(enable);
        }
        else
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted(false))) {
            synchronized (PPApplication.rootMutex) {
                String command1 = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", context, enable);
                if (command1 != null) {
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                        PPApplication.commandWait(command);
                    /*} catch (RootDeniedException e) {
                        PPApplication.rootMutex.rootGranted = false;
                        Log.e("ActivateProfileHelper.setNFC", Log.getStackTraceString(e));*/
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setNFC", Log.getStackTraceString(e));
                    }
                }
            }
        }
    }

    static boolean canExploitGPS(Context context)
    {
        // test exploiting power manager widget
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pacInfo = packageManager.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);

            if(pacInfo != null){
                for(ActivityInfo actInfo : pacInfo.receivers){
                    //test if receiver is exported. if so, we can toggle GPS.
                    if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false; //package not found
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static void setGPS(Context context, boolean enable)
    {
        boolean isEnabled = false;
        boolean ok = true;
        /*if (android.os.Build.VERSION.SDK_INT < 19)
            isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
        else {*/
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null)
                isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            else
                ok = false;
        //}
        if (!ok)
            return;

        //PPApplication.logE("ActivateProfileHelper.setGPS", "isEnabled="+isEnabled);

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled)  && enable)
        {
            // adb shell pm grant sk.henrichg.phoneprofiles android.permission.WRITE_SECURE_SETTINGS
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
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
            {
                // device is rooted
                //PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;
                //String command2;

                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                    String newSet;
                    if (provider.isEmpty())
                        newSet = LocationManager.GPS_PROVIDER;
                    else
                        newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);

                    synchronized (PPApplication.rootMutex) {
                        command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                        //if (PPApplication.isSELinuxEnforcing())
                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);

                        //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state true";
                        Command command = new Command(0, false, command1); //, command2);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            PPApplication.commandWait(command);
                        /*} catch (RootDeniedException e) {
                            PPApplication.rootMutex.rootGranted = false;
                            Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));*/
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                        }
                    }
                }
                else {
                    synchronized (PPApplication.rootMutex) {
                        command1 = "settings put secure location_providers_allowed +gps";
                        Command command = new Command(0, false, command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            PPApplication.commandWait(command);
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                        }
                    }
                }
            }
            else
            if (canExploitGPS(context))
            {
                //PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

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
                } catch (SecurityException ignored) {
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
                // adb shell pm grant sk.henrichg.phoneprofiles android.permission.WRITE_SECURE_SETTINGS
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    String newSet = "";
                    if (android.os.Build.VERSION.SDK_INT < 23) {
                        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                        String[] list = provider.split(",");
                        int j = 0;
                        for (String aList : list) {
                            if (!aList.equals(LocationManager.GPS_PROVIDER)) {
                                if (j > 0)
                                    //noinspection StringConcatenationInLoop
                                    newSet += ",";
                                //noinspection StringConcatenationInLoop
                                newSet += aList;
                                j++;
                            }
                        }
                    }
                    else
                        newSet = "-gps";
                    Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
                }
                else
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                        (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
                {
                    // device is rooted
                    //PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

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
                                    //noinspection StringConcatenationInLoop
                                    newSet += ",";
                                //noinspection StringConcatenationInLoop
                                newSet += aList;
                                j++;
                            }
                        }

                        synchronized (PPApplication.rootMutex) {
                            command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state false";
                            Command command = new Command(0, false, command1);//, command2);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command);
                            /*} catch (RootDeniedException e) {
                                PPApplication.rootMutex.rootGranted = false;
                                Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));*/
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                            }
                        }
                    }
                    else {
                        synchronized (PPApplication.rootMutex) {
                            command1 = "settings put secure location_providers_allowed -gps";
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                            }
                        }
                    }
                }
                else
                if (canExploitGPS(context))
                {
                    //PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

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
                } catch (SecurityException ignored) {
                }*/

                    // for normal apps it is only possible to open the system settings dialog
            /*	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); */
                //}
            }
    }

    private static void setAirplaneMode_SDK17(Context context, boolean mode)
    {
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
        {
            // device is rooted
            synchronized (PPApplication.rootMutex) {
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
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    PPApplication.commandWait(command);
                /*} catch (RootDeniedException e) {
                    PPApplication.rootMutex.rootGranted = false;
                    Log.e("ActivateProfileHelper.setAirplaneMode_SDK17", Log.getStackTraceString(e));*/
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setAirplaneMode_SDK17", Log.getStackTraceString(e));
                }
            }
        }
        //else
        //{
            // for normal apps it is only possible to open the system settings dialog
        /*	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent); */
        //}
    }

    /*
    private void setAirplaneMode_SDK8(Context context, boolean mode)
    {
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, mode ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", mode);
        context.sendBroadcast(intent);
    }
    */

    private static void setPowerSaveMode(Context context, final Profile profile) {
        if (profile._devicePowerSaveMode != 0) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadPowerSaveMode();
            final Handler handler = new Handler(PPApplication.handlerThreadPowerSaveMode.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.setPowerSaveMode");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if (powerManager != null) {
                                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                    boolean _isPowerSaveMode;
                                    //if (Build.VERSION.SDK_INT >= 21)
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
                                        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                                            //if (android.os.Build.VERSION.SDK_INT >= 21)
                                                Settings.Global.putInt(appContext.getContentResolver(), "low_power", ((_isPowerSaveMode) ? 1 : 0));
                                        } else if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) &&
                                                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                            synchronized (PPApplication.rootMutex) {
                                                String command1 = "settings put global low_power " + ((_isPowerSaveMode) ? 1 : 0);
                                                Command command = new Command(0, false, command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    PPApplication.commandWait(command);
                                            /*} catch (RootDeniedException e) {
                                                PPApplication.rootMutex.rootGranted = false;
                                                Log.e("ActivateProfileHelper.setPowerSaveMode", Log.getStackTraceString(e));*/
                                                } catch (Exception e) {
                                                    Log.e("ActivateProfileHelper.setPowerSaveMode", Log.getStackTraceString(e));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            });
        }
    }

    private static void lockDevice(Context context, final Profile profile) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadLockDevice();
        final Handler handler = new Handler(PPApplication.handlerThreadLockDevice.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (PPApplication.blockProfileEventActions)
                    // not lock device after boot
                    return;

                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper.lockDevice");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    switch (profile._lockDevice) {
                        case 1:
                            if (PhoneProfilesService.getInstance() != null) {
                                if (Permissions.checkLockDevice(appContext) && (PhoneProfilesService.getInstance().lockDeviceActivity == null)) {
                                    try {
                                        Intent intent = new Intent(appContext, LockDeviceActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        appContext.startActivity(intent);
                                    } catch (Exception ignore) {
                                    }
                                }
                            }
                            break;
                        case 2:
                            /*if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                                    (PPApplication.isRooted())) {
                                //String command1 = "input keyevent 26";
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    commandWait(command);
                                } catch (RootDeniedException e) {
                                    PPApplication.rootMutex.rootGranted = false;
                                    Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                }
                            }*/
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) &&
                                    (PPApplication.isRooted(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = PPApplication.getJavaCommandFile(CmdGoToSleep.class, "power", appContext, 0);
                                    if (command1 != null) {
                                        Command command = new Command(0, false, command1);
                                        try {
                                            RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                                            PPApplication.commandWait(command);
                                    /*} catch (RootDeniedException e) {
                                        PPApplication.rootMutex.rootGranted = false;
                                        Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));*/
                                        } catch (Exception e) {
                                            Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                        }
                                    }
                                }
                            }
                            break;
                        case 3:
                            Intent intent = new Intent(PPApplication.ACTION_LOCK_DEVICE);
                            appContext.sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                            break;
                    }
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    /*
    private static void setScreenNightMode(Context context, final int value) {
        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, context)
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            if (uiModeManager != null) {
                switch (value) {
                    case 1:
                        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                        break;
                    case 2:
                        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                        break;
                    case 3:
                        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
                        break;
                }
            }
        }
    }
    */

    static boolean getLockScreenDisabled(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_LOCKSCREEN_DISABLED, false);
    }

    static void setLockScreenDisabled(Context context, boolean disabled)
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
