package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission;

public class Permissions {

    public static final int PERMISSION_VOLUME_PREFERENCES = 1;
    public static final int PERMISSION_VIBRATION_ON_TOUCH = 2;
    public static final int PERMISSION_RINGTONES = 3;
    public static final int PERMISSION_SCREEN_TIMEOUT = 4;
    public static final int PERMISSION_SCREEN_BRIGHTNESS = 5;
    public static final int PERMISSION_AUTOROTATION = 6;
    public static final int PERMISSION_WALLPAPER = 7;
    public static final int PERMISSION_RADIO_PREFERENCES = 8;
    public static final int PERMISSION_PHONE_BROADCAST = 9;
    public static final int PERMISSION_CUSTOM_PROFILE_ICON = 10;
    public static final int PERMISSION_INSTALL_TONE = 11;
    public static final int PERMISSION_EXPORT = 12;
    public static final int PERMISSION_IMPORT = 13;
    public static final int PERMISSION_NOTIFICATION_LED = 15;
    public static final int PERMISSION_VIBRATE_WHEN_RINGING = 16;
    public static final int PERMISSION_ACCESS_NOTIFICATION_POLICY = 17;

    public static final int GRANT_TYPE_PROFILE = 1;
    public static final int GRANT_TYPE_INSTALL_TONE = 2;
    public static final int GRANT_TYPE_WALLPAPER = 3;
    public static final int GRANT_TYPE_CUSTOM_PROFILE_ICON = 4;
    public static final int GRANT_TYPE_EXPORT = 5;
    public static final int GRANT_TYPE_IMPORT = 6;
    public static final int GRANT_TYPE_BRIGHTNESS_DIALOG = 8;

    public static final String EXTRA_GRANT_TYPE = "grant_type";
    public static final String EXTRA_PERMISSION_TYPES = "permission_types";
    public static final String EXTRA_ONLY_NOTIFICATION = "only_notification";
    public static final String EXTRA_FOR_GUI = "for_gui";
    public static final String EXTRA_MONOCHROME = "monochrome";
    public static final String EXTRA_MONOCHROME_VALUE = "monochrome_value";
    public static final String EXTRA_INTERACTIVE = "interactive";
    public static final String EXTRA_APPLICATION_DATA_PATH = "application_data_path";

    public static Activity profileActivationActivity = null;
    public static ImageViewPreference imageViewPreference = null;
    public static ProfileIconPreference profileIconPreference = null;
    public static EditorProfilesActivity editorActivity = null;
    public static BrightnessDialogPreference brightnessDialogPreference = null;

    public static class PermissionType implements Parcelable {
        public int preference;
        public String permission;

        PermissionType (int preference, String permission) {
            this.preference = preference;
            this.permission = permission;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.preference);
            dest.writeString(this.permission);
        }

        protected PermissionType(Parcel in) {
            this.preference = in.readInt();
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

    public static List<PermissionType> recheckPermissions(Context context, List<PermissionType> _permissions) {
        List<PermissionType>  permissions = new ArrayList<PermissionType>();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            for (PermissionType _permission : _permissions) {
                if (_permission.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    if (!Settings.System.canWrite(context))
                        permissions.add(new PermissionType(_permission.preference, _permission.permission));
                } else if (_permission.permission.equals(permission.ACCESS_NOTIFICATION_POLICY)) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (!mNotificationManager.isNotificationPolicyAccessGranted())
                        permissions.add(new PermissionType(_permission.preference, _permission.permission));
                } else {
                    if (ContextCompat.checkSelfPermission(context, _permission.permission) != PackageManager.PERMISSION_GRANTED)
                        permissions.add(new PermissionType(_permission.preference, _permission.permission));
                }
            }
        }
        return permissions;
    }

    public static List<PermissionType> checkProfilePermissions(Context context, Profile profile) {
        List<PermissionType>  permissions = new ArrayList<PermissionType>();
        //Log.e("Permissions", "checkProfilePermissions - profile.icon="+profile._icon);
        if (profile == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!checkProfileVolumePreferences(context, profile)) permissions.add(new PermissionType(PERMISSION_VOLUME_PREFERENCES, permission.WRITE_SETTINGS));
            if (!checkProfileVibrateWhenRinging(context, profile)) permissions.add(new PermissionType(PERMISSION_VIBRATE_WHEN_RINGING, permission.WRITE_SETTINGS));
            if (!checkProfileVibrationOnTouch(context, profile)) permissions.add(new PermissionType(PERMISSION_VIBRATION_ON_TOUCH, permission.WRITE_SETTINGS));
            if (!checkProfileRingtones(context, profile)) permissions.add(new PermissionType(PERMISSION_RINGTONES, permission.WRITE_SETTINGS));
            if (!checkProfileScreenTimeout(context, profile)) permissions.add(new PermissionType(PERMISSION_SCREEN_TIMEOUT, permission.WRITE_SETTINGS));
            if (!checkProfileScreenBrightness(context, profile)) permissions.add(new PermissionType(PERMISSION_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));
            if (!checkProfileAutoRotation(context, profile)) permissions.add(new PermissionType(PERMISSION_AUTOROTATION, permission.WRITE_SETTINGS));
            if (!checkProfileNotificationLed(context, profile)) permissions.add(new PermissionType(PERMISSION_NOTIFICATION_LED, permission.WRITE_SETTINGS));
            if (!checkProfileWallpaper(context, profile)) permissions.add(new PermissionType(PERMISSION_WALLPAPER, permission.READ_EXTERNAL_STORAGE));
            if (!checkProfileRadioPreferences(context, profile)) {
                permissions.add(new PermissionType(PERMISSION_RADIO_PREFERENCES, permission.WRITE_SETTINGS));
                permissions.add(new PermissionType(PERMISSION_RADIO_PREFERENCES, permission.READ_PHONE_STATE));
            }
            if (!checkProfilePhoneBroadcast(context, profile)) {
                permissions.add(new PermissionType(PERMISSION_PHONE_BROADCAST, permission.READ_PHONE_STATE));
                permissions.add(new PermissionType(PERMISSION_PHONE_BROADCAST, permission.PROCESS_OUTGOING_CALLS));
            }
            if (!checkCustomProfileIcon(context, profile)) permissions.add(new PermissionType(PERMISSION_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));
            if (!checkProfileAccessNotificationPolicy(context, profile)) permissions.add(new PermissionType(PERMISSION_ACCESS_NOTIFICATION_POLICY, permission.ACCESS_NOTIFICATION_POLICY));
            return permissions;
        }
        else
            return permissions;
    }

    public static boolean checkProfileVolumePreferences(Context context, Profile profile) {
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

    public static boolean checkInstallTone(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkProfileVibrationOnTouch(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._vibrationOnTouch != 0) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileVibrateWhenRinging(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._vibrateWhenRinging != 0) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileNotificationLed(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._notificationLed != 0) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileRingtones(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile._soundRingtoneChange != 0) ||
                (profile._soundNotificationChange != 0) ||
                (profile._soundAlarmChange != 0)) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileScreenTimeout(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._deviceScreenTimeout != 0) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkScreenBrightness(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = Settings.System.canWrite(context);
            if (granted)
                GlobalData.setShowRequestWriteSettingsPermission(context, true);
            return granted;
        }
        else
            return true;
    }

    public static boolean checkProfileScreenBrightness(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile.getDeviceBrightnessChange()) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileAutoRotation(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._deviceAutoRotate != 0) {
                boolean granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileWallpaper(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._deviceWallpaperChange != 0) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkCustomProfileIcon(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            Profile _profile = dataWrapper.getDatabaseHandler().getProfile(profile._id);
            if (_profile == null) return true;
            if (!_profile.getIsIconResourceID()) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkGallery(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkImport(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkExport(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkProfileRadioPreferences(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = true;
            if ((profile._deviceWiFiAP != 0)) {
                granted = Settings.System.canWrite(context);
                if (granted)
                    GlobalData.setShowRequestWriteSettingsPermission(context, true);
            }
            if ((profile._deviceMobileData != 0) || (profile._deviceNetworkType != 0))
                granted = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            return granted;
        }
        else
            return true;
    }

    public static boolean checkProfilePhoneBroadcast(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._volumeSpeakerPhone != 0) {
                boolean granted = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
                granted = granted && (ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileAccessNotificationPolicy(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            if ((profile._volumeRingerMode != 0) ||
                    profile.getVolumeRingtoneChange() ||
                    profile.getVolumeNotificationChange() ||
                    profile.getVolumeSystemChange()) {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                return mNotificationManager.isNotificationPolicyAccessGranted();
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkAccessNotificationPolicy(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return mNotificationManager.isNotificationPolicyAccessGranted();
        }
        else
            return true;
    }

    public static boolean grantProfilePermissions(Context context, Profile profile, boolean onlyNotification,
                                                  boolean forGUI, boolean monochrome, int monochromeValue,
                                                  int startupSource, boolean interactive, Activity activity) {
        List<PermissionType> permissions = checkProfilePermissions(context, profile);
        if (permissions.size() > 0) {
            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PROFILE);
            intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
            if (onlyNotification)
                GlobalData.addMergedPermissions(context, permissions);
            else
                intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
            intent.putExtra(EXTRA_FOR_GUI, forGUI);
            intent.putExtra(EXTRA_MONOCHROME, monochrome);
            intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
            intent.putExtra(GlobalData.EXTRA_STARTUP_SOURCE, startupSource);
            intent.putExtra(EXTRA_INTERACTIVE, interactive);
            if (!onlyNotification)
                profileActivationActivity = activity;
            else
                profileActivationActivity = null;
            context.startActivity(intent);
        }
        return permissions.size() == 0;
    }

    public static boolean grantInstallTonePermissions(Context context, boolean onlyNotification) {
        boolean granted = checkInstallTone(context);
        if (!granted) {
            List<PermissionType>  permissions = new ArrayList<PermissionType>();
            permissions.add(new PermissionType(PERMISSION_INSTALL_TONE, permission.WRITE_EXTERNAL_STORAGE));

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_INSTALL_TONE);
            intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
            context.startActivity(intent);
        }
        return granted;
    }

    public static boolean grantWallpaperPermissions(Context context, ImageViewPreference preference) {
        boolean granted = checkGallery(context);
        if (!granted) {
            List<PermissionType>  permissions = new ArrayList<PermissionType>();
            permissions.add(new PermissionType(PERMISSION_WALLPAPER, permission.READ_EXTERNAL_STORAGE));

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WALLPAPER);
            intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
            imageViewPreference = preference;
            context.startActivity(intent);
        }
        return granted;
    }

    public static boolean grantCustomProfileIconPermissions(Context context, ProfileIconPreference preference) {
        boolean granted = checkGallery(context);
        if (!granted) {
            List<PermissionType>  permissions = new ArrayList<PermissionType>();
            permissions.add(new PermissionType(PERMISSION_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CUSTOM_PROFILE_ICON);
            intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
            profileIconPreference = preference;
            context.startActivity(intent);
        }
        return granted;
    }

    public static boolean grantBrightnessDialogPermissions(Context context, BrightnessDialogPreference preference) {
        boolean granted = checkScreenBrightness(context);
        if (!granted) {
            List<PermissionType>  permissions = new ArrayList<PermissionType>();
            permissions.add(new PermissionType(PERMISSION_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_BRIGHTNESS_DIALOG);
            intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
            brightnessDialogPreference = preference;
            context.startActivity(intent);
        }
        return granted;
    }

    public static boolean grantExportPermissions(Context context, EditorProfilesActivity editor) {
        boolean granted = checkExport(context);
        if (!granted) {
            List<PermissionType>  permissions = new ArrayList<PermissionType>();
            permissions.add(new PermissionType(PERMISSION_EXPORT, permission.WRITE_EXTERNAL_STORAGE));

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EXPORT);
            intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
            editorActivity = editor;
            context.startActivity(intent);
        }
        return granted;
    }

    public static boolean grantImportPermissions(Context context, EditorProfilesActivity editor, String applicationDataPath) {
        boolean granted = checkImport(context);
        if (!granted) {
            List<PermissionType>  permissions = new ArrayList<PermissionType>();
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
        }
        return granted;
    }

    public static void removeProfileNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalData.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
    }

    public static void removeInstallToneNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalData.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
    }

    public static void removeNotifications(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalData.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
        notificationManager.cancel(GlobalData.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
    }

    public static  void releaseReferences() {
        profileActivationActivity = null;
        imageViewPreference = null;
        profileIconPreference = null;
        editorActivity = null;
    }


}
