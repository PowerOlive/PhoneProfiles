package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission;

public class Permissions {

    //private static final int PERMISSION_VOLUME_PREFERENCES = 1;
    private static final int PERMISSION_VIBRATION_ON_TOUCH = 2;
    private static final int PERMISSION_RINGTONES = 3;
    private static final int PERMISSION_SCREEN_TIMEOUT = 4;
    private static final int PERMISSION_SCREEN_BRIGHTNESS = 5;
    private static final int PERMISSION_AUTOROTATION = 6;
    private static final int PERMISSION_WALLPAPER = 7;
    private static final int PERMISSION_RADIO_PREFERENCES = 8;
    private static final int PERMISSION_PHONE_BROADCAST = 9;
    private static final int PERMISSION_CUSTOM_PROFILE_ICON = 10;
    private static final int PERMISSION_INSTALL_TONE = 11;
    private static final int PERMISSION_EXPORT = 12;
    private static final int PERMISSION_IMPORT = 13;
    private static final int PERMISSION_NOTIFICATION_LED = 15;
    private static final int PERMISSION_VIBRATE_WHEN_RINGING = 16;
    private static final int PERMISSION_ACCESS_NOTIFICATION_POLICY = 17;
    private static final int PERMISSION_LOCK_DEVICE = 18;
    private static final int PERMISSION_RINGTONE_PREFERENCE = 19;
    private static final int PERMISSION_PLAY_RINGTONE_NOTIFICATION = 20;

    static final int GRANT_TYPE_PROFILE = 1;
    static final int GRANT_TYPE_INSTALL_TONE = 2;
    static final int GRANT_TYPE_WALLPAPER = 3;
    static final int GRANT_TYPE_CUSTOM_PROFILE_ICON = 4;
    static final int GRANT_TYPE_EXPORT = 5;
    static final int GRANT_TYPE_IMPORT = 6;
    static final int GRANT_TYPE_BRIGHTNESS_DIALOG = 8;
    static final int GRANT_TYPE_RINGTONE_PREFERENCE = 9;
    private static final int GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION = 10;

    static final String EXTRA_GRANT_TYPE = "grant_type";
    static final String EXTRA_PERMISSION_TYPES = "permission_types";
    static final String EXTRA_ONLY_NOTIFICATION = "only_notification";
    static final String EXTRA_FOR_GUI = "for_gui";
    static final String EXTRA_MONOCHROME = "monochrome";
    static final String EXTRA_MONOCHROME_VALUE = "monochrome_value";
    static final String EXTRA_INTERACTIVE = "interactive";
    static final String EXTRA_APPLICATION_DATA_PATH = "application_data_path";
    static final String EXTRA_ACTIVATE_PROFILE = "activate_profile";
    private static final String EXTRA_GRANT_ALSO_CONTACTS = "grant_also_contacts";

    static Activity profileActivationActivity = null;
    static ImageViewPreference imageViewPreference = null;
    static ProfileIconPreference profileIconPreference = null;
    static EditorProfilesActivity editorActivity = null;
    static BrightnessDialogPreference brightnessDialogPreference = null;
    static RingtonePreference ringtonePreference = null;

    private static final String PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION = "show_request_write_settings_permission";
    private static final String PREF_MERGED_PERRMISSIONS = "merged_permissions";
    private static final String PREF_MERGED_PERRMISSIONS_COUNT = "merged_permissions_count";
    private static final String PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION = "show_request_access_notification_policy_permission";
    private static final String PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION = "show_request_draw_overlays_permission";

    static class PermissionType implements Parcelable {
        final int type;
        final String permission;

        PermissionType (int type, String permission) {
            this.type = type;
            this.permission = permission;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.type);
            dest.writeString(this.permission);
        }

        PermissionType(Parcel in) {
            this.type = in.readInt();
            this.permission = in.readString();
        }

        public static final Parcelable.Creator<PermissionType> CREATOR = new Parcelable.Creator<PermissionType>() {
            public PermissionType createFromParcel(Parcel source) {
                return new PermissionType(source);
            }

            public PermissionType[] newArray(int size) {
                return new PermissionType[size];
            }
        };
    }

    static boolean hasPermission(Context context, String permission) {
        return context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    /*
    static boolean isSystemApp(Context context) {
        return (context.getApplicationInfo().flags
                & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
    }
    */

    static List<PermissionType> recheckPermissions(Context context, List<PermissionType> _permissions) {
        List<PermissionType>  permissions = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            for (PermissionType _permission : _permissions) {
                if (_permission.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    if (!Settings.System.canWrite(context))
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                } else if (_permission.permission.equals(permission.ACCESS_NOTIFICATION_POLICY)) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null) {
                        if (!mNotificationManager.isNotificationPolicyAccessGranted())
                            permissions.add(new PermissionType(_permission.type, _permission.permission));
                    }
                    else
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                } else if (_permission.permission.equals(permission.SYSTEM_ALERT_WINDOW)) {
                    if (!Settings.canDrawOverlays(context))
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                } else {
                    if (ContextCompat.checkSelfPermission(context, _permission.permission) != PackageManager.PERMISSION_GRANTED)
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                }
            }
        }
        return permissions;
    }

    private static List<PermissionType> checkProfilePermissions(Context context, Profile profile) {
        List<PermissionType>  permissions = new ArrayList<>();
        //Log.e("Permissions", "checkProfilePermissions - profile.icon="+profile._icon);
        if (profile == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //if (!checkProfileVolumePreferences(context, profile)) permissions.add(new PermissionType(PERMISSION_VOLUME_PREFERENCES, permission.WRITE_SETTINGS));
            checkProfileVibrateWhenRinging(context, profile, permissions);
            checkProfileVibrationOnTouch(context, profile, permissions);
            checkProfileRingTones(context, profile, permissions);
            checkProfileScreenTimeout(context, profile, permissions);
            checkProfileScreenBrightness(context, profile, permissions);
            checkProfileAutoRotation(context, profile, permissions);
            checkProfileNotificationLed(context, profile, permissions);
            checkProfileWallpaper(context, profile, permissions);
            checkProfileRadioPreferences(context, profile, permissions);
            checkProfilePhoneBroadcast(context, profile, permissions);
            checkCustomProfileIcon(context, profile, permissions);
            checkProfileAccessNotificationPolicy(context, profile, permissions);
            checkProfileLockDevice(context, profile, permissions);
            return permissions;
        }
        else
            return permissions;
    }

    /*
    static boolean checkProfileVolumePreferences(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0); -- NOT WORKING
            if ((profile._volumeRingerMode != 0) ||
                 profile.getVolumeRingtoneChange() ||
                 profile.getVolumeNotificationChange() ||
                 profile.getVolumeMediaChange() ||
                 profile.getVolumeAlarmChange() ||
                 profile.getVolumeSystemChange() ||
                 profile.getVolumeVoiceChange()) {
                //return Settings.System.canWrite(context);
                return true;
            }
            else
                return true;
        }
        else
            return true;
    }
    */

    static boolean checkInstallTone(Context context, List<PermissionType>  permissions) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean granted = ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if ((permissions != null) && (!granted))
                    permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_INSTALL_TONE, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                return granted;
            } else
                return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkProfileVibrationOnTouch(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._vibrationOnTouch != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    else if (permissions != null)
                        permissions.add(new PermissionType(PERMISSION_VIBRATION_ON_TOUCH, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileVibrateWhenRinging(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._vibrateWhenRinging != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    else if (permissions != null)
                        permissions.add(new PermissionType(PERMISSION_VIBRATE_WHEN_RINGING, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    private static void checkProfileNotificationLed(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return/* true*/;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._notificationLed != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_NOTIFICATION_LED, permission.WRITE_SETTINGS));
                    //return granted;
                }// else
                //    return/* true*/;
            } catch (Exception e) {
                //return/* false*/;
            }
        }
        //else
        //    return/* true*/;
    }

    static boolean checkProfileRingTones(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if ((profile._soundRingtoneChange != 0) ||
                        (profile._soundNotificationChange != 0) ||
                        (profile._soundAlarmChange != 0)) {
                    boolean grantedSystemSettings = Settings.System.canWrite(context);
                    boolean grantedStorage = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if (grantedSystemSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (permissions != null) {
                        if (!grantedSystemSettings)
                            permissions.add(new PermissionType(PERMISSION_RINGTONES, permission.WRITE_SETTINGS));
                        if (!grantedStorage)
                            permissions.add(new PermissionType(PERMISSION_RINGTONES, permission.READ_EXTERNAL_STORAGE));
                    }
                    return grantedSystemSettings && grantedStorage;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkScreenTimeout(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);
                return grantedWriteSettings && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileScreenTimeout(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._deviceScreenTimeout != 0) {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_SCREEN_TIMEOUT, permission.WRITE_SETTINGS));
                        if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_SCREEN_TIMEOUT, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkScreenBrightness(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);
                return grantedWriteSettings && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileScreenBrightness(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile.getDeviceBrightnessChange()) {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));
                        if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_SCREEN_TIMEOUT, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileAutoRotation(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._deviceAutoRotate != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_AUTOROTATION, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileWallpaper(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (profile._deviceWallpaperChange != 0) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_WALLPAPER, permission.READ_EXTERNAL_STORAGE));
                    return granted;
                } else
                    return true;
            } else {
                if (profile._deviceWallpaperChange != 0) {
                    return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
                } else
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkCustomProfileIcon(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                Profile _profile = dataWrapper.getDatabaseHandler().getProfile(profile._id);
                if (_profile == null) return true;
                if (!_profile.getIsIconResourceID()) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));
                    return granted;
                } else
                    return true;
            } else {
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                Profile _profile = dataWrapper.getDatabaseHandler().getProfile(profile._id);
                if (_profile == null) return true;
                if (!_profile.getIsIconResourceID()) {
                    return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
                } else
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkGallery(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkImport(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkExport(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkRingtonePreference(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkProfileRadioPreferences(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean grantedWriteSettings = true;
                if ((profile._deviceWiFiAP != 0)) {
                    grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                }
                boolean grantedReadPhoneState = true;
                if ((profile._deviceMobileData != 0) || (profile._deviceNetworkType != 0))
                    grantedReadPhoneState = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
                //if (profile._deviceNFC != 0)
                //    granted = checkNFC(context);
                if (permissions != null) {
                    if (!grantedWriteSettings)
                        permissions.add(new PermissionType(PERMISSION_RADIO_PREFERENCES, permission.WRITE_SETTINGS));
                    if (!grantedReadPhoneState)
                        permissions.add(new PermissionType(PERMISSION_RADIO_PREFERENCES, permission.READ_PHONE_STATE));
                    //permissions.add(new PermissionType(PERMISSION_PROFILE_RADIO_PREFERENCES, permission.MODIFY_PHONE_STATE));
                }
                return grantedWriteSettings && grantedReadPhoneState;
            } else {
                if ((profile._deviceMobileData != 0) || (profile._deviceNetworkType != 0))
                    return hasPermission(context, Manifest.permission.READ_PHONE_STATE);
                else
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkProfilePhoneBroadcast(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (profile._volumeSpeakerPhone != 0) {
                    boolean grantedReadPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedOutgoingCall = ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedReadPhoneState)
                            permissions.add(new PermissionType(PERMISSION_PHONE_BROADCAST, permission.READ_PHONE_STATE));
                        if (!grantedOutgoingCall)
                            permissions.add(new PermissionType(PERMISSION_PHONE_BROADCAST, permission.PROCESS_OUTGOING_CALLS));
                    }
                    return grantedOutgoingCall && grantedReadPhoneState;
                } else
                    return true;
            } else
                return hasPermission(context, Manifest.permission.READ_PHONE_STATE) &&
                        hasPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkProfileAccessNotificationPolicy(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    if ((profile._volumeRingerMode != 0) ||
                            profile.getVolumeRingtoneChange() ||
                            profile.getVolumeNotificationChange() ||
                            profile.getVolumeSystemChange()) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        boolean granted = false;
                        if (mNotificationManager != null)
                            granted = mNotificationManager.isNotificationPolicyAccessGranted();
                        if (granted)
                            setShowRequestAccessNotificationPolicyPermission(context, true);
                        if ((permissions != null) && (!granted))
                            permissions.add(new PermissionType(PERMISSION_ACCESS_NOTIFICATION_POLICY, permission.ACCESS_NOTIFICATION_POLICY));
                        return granted;
                    } else
                        return true;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkAccessNotificationPolicy(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    boolean granted = false;
                    if (mNotificationManager != null)
                        granted = mNotificationManager.isNotificationPolicyAccessGranted();
                    if (granted)
                        setShowRequestAccessNotificationPolicyPermission(context, true);
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkLockDevice(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);
                return grantedWriteSettings && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileLockDevice(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._lockDevice == 1) {
                    // only for lockDevice = Screen off
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_LOCK_DEVICE, permission.WRITE_SETTINGS));
                        if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_LOCK_DEVICE, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    private static boolean checkPlayRingtoneNotification(Context context, boolean alsoContacts, List<PermissionType>  permissions) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean grantedReadExternalStorage = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                boolean grantedContacts = true;
                if (alsoContacts)
                    grantedContacts = ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                if (permissions != null) {
                    if (!grantedReadExternalStorage)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION, Manifest.permission.READ_EXTERNAL_STORAGE));
                    if (!grantedContacts)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION, Manifest.permission.READ_CONTACTS));
                }
                return grantedReadExternalStorage && grantedContacts;
            } else {
                boolean granted = hasPermission(context, permission.READ_EXTERNAL_STORAGE);
                if (alsoContacts)
                    granted = granted && hasPermission(context, permission.READ_CONTACTS);
                return granted;
            }
        } catch (Exception e) {
            return false;
        }
    }

    static boolean grantProfilePermissions(Context context, Profile profile, boolean onlyNotification,
                                                  boolean forGUI, boolean monochrome, int monochromeValue,
                                                  int startupSource, boolean interactive, Activity activity,
                                                  boolean activateProfile) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = checkProfilePermissions(context, profile);
            if (permissions.size() > 0) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PROFILE);
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    if (onlyNotification)
                        addMergedPermissions(context, permissions);
                    else
                        intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
                    intent.putExtra(EXTRA_FOR_GUI, forGUI);
                    intent.putExtra(EXTRA_MONOCHROME, monochrome);
                    intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                    intent.putExtra(EXTRA_INTERACTIVE, interactive);
                    intent.putExtra(EXTRA_ACTIVATE_PROFILE, activateProfile);
                    if (!onlyNotification)
                        profileActivationActivity = activity;
                    else
                        profileActivationActivity = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return permissions.size() == 0;
        }
        else
            return true;
    }

    static boolean grantInstallTonePermissions(Context context, boolean onlyNotification) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkInstallTone(context, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_INSTALL_TONE);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantWallpaperPermissions(Context context, ImageViewPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkGallery(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_WALLPAPER, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WALLPAPER);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    imageViewPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantCustomProfileIconPermissions(Context context, ProfileIconPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkGallery(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CUSTOM_PROFILE_ICON);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    profileIconPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantBrightnessDialogPermissions(Context context, BrightnessDialogPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkScreenBrightness(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_BRIGHTNESS_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    brightnessDialogPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantExportPermissions(Context context, EditorProfilesActivity editor) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkExport(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EXPORT, permission.WRITE_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EXPORT);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    editorActivity = editor;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantImportPermissions(Context context, EditorProfilesActivity editor, String applicationDataPath) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkImport(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_IMPORT, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_IMPORT);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_APPLICATION_DATA_PATH, applicationDataPath);
                    editorActivity = editor;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantRingtonePreferenceDialogPermissions(Context context, RingtonePreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkRingtonePreference(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_RINGTONE_PREFERENCE, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_RINGTONE_PREFERENCE);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    ringtonePreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static void grantPlayRingtoneNotificationPermissions(Context context/*, boolean onlyNotification, boolean alsoContacts*/) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkPlayRingtoneNotification(context, true/*alsoContacts*/, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, true/*onlyNotification*/);
                    intent.putExtra(EXTRA_GRANT_ALSO_CONTACTS, true/*alsoContacts*/);
                    context.startActivity(intent);
                } catch (Exception e) {
                    //return false;
                }
            }
            //return granted;
        }
        //else
        //    return true;
    }

    static void removeProfileNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
    }

    static void removeInstallToneNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
    }

    static void removeNotifications(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
            notificationManager.cancel(PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
        }
    }

    static  void releaseReferences() {
        profileActivationActivity = null;
        imageViewPreference = null;
        profileIconPreference = null;
        editorActivity = null;
        brightnessDialogPreference = null;
        ringtonePreference = null;
    }


    static boolean getShowRequestWriteSettingsPermission(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, true);
    }

    static void setShowRequestWriteSettingsPermission(Context context, boolean value)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, value);
        editor.apply();
    }

    static boolean getShowRequestAccessNotificationPolicyPermission(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, true);
    }

    static void setShowRequestAccessNotificationPolicyPermission(Context context, boolean value)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, value);
        editor.apply();
    }

    static boolean getShowRequestDrawOverlaysPermission(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, true);
    }

    static void setShowRequestDrawOverlaysPermission(Context context, boolean value)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, value);
        editor.apply();
    }

    static List<Permissions.PermissionType> getMergedPermissions(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);

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

    private static void addMergedPermissions(Context context, List<Permissions.PermissionType> permissions)
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
                savedPermissions.add(new Permissions.PermissionType(permission.type, permission.permission));
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(PREF_MERGED_PERRMISSIONS_COUNT, savedPermissions.size());

        Gson gson = new Gson();

        for (int i = 0; i < savedPermissions.size(); i++)
        {
            String json = gson.toJson(savedPermissions.get(i));
            editor.putString(PREF_MERGED_PERRMISSIONS+i, json);
        }

        editor.apply();
    }

    static void clearMergedPermissions(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}
