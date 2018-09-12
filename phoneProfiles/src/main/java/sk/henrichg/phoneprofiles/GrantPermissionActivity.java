package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GrantPermissionActivity extends AppCompatActivity {

    private int grantType;
    private List<Permissions.PermissionType> permissions;
    //private boolean onlyNotification;
    private boolean forceGrant;
    //private boolean mergedNotification;
    //private boolean forGUI;
    //private boolean monochrome;
    //private int monochromeValue;
    private int startupSource;
    private boolean interactive;
    private String applicationDataPath;
    private boolean activateProfile;
    private boolean fromNotification;

    private Profile profile;
    private DataWrapper dataWrapper;

    private boolean started = false;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 9090;
    private static final int PERMISSIONS_REQUEST_CODE = 9091;
    private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE = 9092;
    private static final int DRAW_OVERLAYS_REQUEST_CODE = 9093;

    private static final String NOTIFICATION_DELETED_ACTION = "sk.henrichg.phoneprofiles.PERMISSIONS_NOTIFICATION_DELETED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        grantType = intent.getIntExtra(Permissions.EXTRA_GRANT_TYPE, 0);
        //onlyNotification = intent.getBooleanExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
        forceGrant = intent.getBooleanExtra(Permissions.EXTRA_FORCE_GRANT, false);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        /*mergedNotification = false;
        if (permissions == null) {
            permissions = Permissions.getMergedPermissions(getApplicationContext());
            mergedNotification = true;
        }*/

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        //forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        //monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        //monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
        interactive = intent.getBooleanExtra(Permissions.EXTRA_INTERACTIVE, true);
        applicationDataPath = intent.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH);
        activateProfile = intent.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, true) && (profile_id != Profile.SHARED_PROFILE_ID);

        fromNotification = intent.getBooleanExtra(Permissions.EXTRA_FROM_NOTIFICATION, false);

        dataWrapper = new DataWrapper(getApplicationContext(), /*forGUI,*/ false, 0);
        if (profile_id != Profile.SHARED_PROFILE_ID)
            profile = dataWrapper.getProfileById(profile_id, false, false);
        else
            profile = Profile.getSharedProfile(getApplicationContext());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (started) return;
        started = true;

        if ((grantType == Permissions.GRANT_TYPE_PROFILE) && (profile == null)) {
            finish();
            return;
        }

        final Context context = getApplicationContext();

        if (fromNotification) {
            // called from notification - recheck permissions
            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                boolean granted = Permissions.checkInstallTone(context, permissions);
                if (granted) {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else
            if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                boolean granted = Permissions.checkPlayRingtoneNotification(context, permissions);
                if (granted) {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else {
                // get permissions from shared preferences and recheck it
                /*permissions = Permissions.recheckPermissions(context, Permissions.getMergedPermissions(context));
                mergedNotification = true;*/
                permissions = Permissions.recheckPermissions(context, permissions);
                if (permissions.size() == 0) {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
        }

        boolean showRequestWriteSettings = false;
        boolean showRequestAccessNotificationPolicy = false;
        boolean showRequestDrawOverlays = false;
        boolean showRequestReadExternalStorage = false;
        boolean showRequestReadPhoneState = false;
        boolean showRequestProcessOutgoingCalls = false;
        boolean showRequestWriteExternalStorage = false;
        boolean showRequestAccessCoarseLocation = false;
        boolean showRequestAccessFineLocation = false;

        String[] whyPermissionString = new String[9];

        for (Permissions.PermissionType permissionType : permissions) {
            if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                showRequestWriteSettings = Permissions.getShowRequestWriteSettingsPermission(context) || forceGrant;
                whyPermissionString[0] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                showRequestAccessNotificationPolicy = Permissions.getShowRequestAccessNotificationPolicyPermission(context) || forceGrant;
                whyPermissionString[1] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                showRequestDrawOverlays = Permissions.getShowRequestDrawOverlaysPermission(context) || forceGrant;
                whyPermissionString[2] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || forceGrant;
                whyPermissionString[3] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) || forceGrant;
                whyPermissionString[4] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.PROCESS_OUTGOING_CALLS)) {
                showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS) || forceGrant;
                whyPermissionString[5] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRequestWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || forceGrant;
                whyPermissionString[6] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showRequestAccessCoarseLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || forceGrant;
                whyPermissionString[7] = getWhyPermissionString(permissionType.type);
            }
            if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRequestAccessFineLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || forceGrant;
                whyPermissionString[8] = getWhyPermissionString(permissionType.type);
            }
        }

        if (showRequestWriteSettings ||
                showRequestReadExternalStorage ||
                showRequestReadPhoneState ||
                showRequestProcessOutgoingCalls ||
                showRequestWriteExternalStorage ||
                showRequestAccessNotificationPolicy ||
                showRequestAccessCoarseLocation ||
                showRequestAccessFineLocation ||
                showRequestDrawOverlays) {

            /*if (onlyNotification) {
                showNotification(context);
            }
            else {*/
                String showRequestString;

                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                    showRequestString = context.getString(R.string.permissions_for_install_tone_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                    showRequestString = context.getString(R.string.permissions_for_wallpaper_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                    showRequestString = context.getString(R.string.permissions_for_custom_profile_icon_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                    showRequestString = context.getString(R.string.permissions_for_export_app_data_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                    showRequestString = context.getString(R.string.permissions_for_import_app_data_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                    showRequestString = context.getString(R.string.permissions_for_brightness_dialog_text1) + "<br><br>";
                else {
                    /*if (mergedNotification) {
                        showRequestString = context.getString(R.string.permissions_for_profile_text1m) + " ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    }
                    else {*/
                        showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
                        if (profile != null)
                            showRequestString = showRequestString + "\"" + profile._name + "\" ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    //}
                }

                if (showRequestWriteSettings) {
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                    if (whyPermissionString[0] != null)
                        showRequestString = showRequestString + whyPermissionString[0];
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadExternalStorage || showRequestWriteExternalStorage) {
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                    if (whyPermissionString[3] != null)
                        showRequestString = showRequestString + whyPermissionString[3];
                    if (whyPermissionString[6] != null)
                        showRequestString = showRequestString + whyPermissionString[6];
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadPhoneState || showRequestProcessOutgoingCalls) {
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_phone) + "</b>";
                    if (whyPermissionString[4] != null)
                        showRequestString = showRequestString + whyPermissionString[4];
                    if (whyPermissionString[5] != null)
                        showRequestString = showRequestString + whyPermissionString[5];
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestAccessCoarseLocation || showRequestAccessFineLocation) {
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_location) + "</b>";
                    if (whyPermissionString[7] != null)
                        showRequestString = showRequestString + whyPermissionString[7];
                    if (whyPermissionString[8] != null)
                        showRequestString = showRequestString + whyPermissionString[8];
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestAccessNotificationPolicy) {
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_access_notification_policy) + "</b>";
                    if (whyPermissionString[1] != null)
                        showRequestString = showRequestString + whyPermissionString[1];
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestDrawOverlays) {
                    if (!PPApplication.romIsMIUI)
                        showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_draw_overlays) + "</b>";
                    else
                        showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_draw_overlays_miui) + "</b>";
                    if (whyPermissionString[2] != null)
                        showRequestString = showRequestString + whyPermissionString[2];
                    showRequestString = showRequestString + "<br>";
                }

                showRequestString = showRequestString + "<br>";

                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_install_tone_text2);
                else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_wallpaper_text2);
                else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_custom_profile_icon_text2);
                else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_export_app_data_text2);
                else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_import_app_data_text2);
                else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_brightness_dialog_text2);
                else
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

                // set theme and language for dialog alert ;-)
                // not working on Android 2.3.x
                GlobalGUIRoutines.setTheme(this, true, true);
                GlobalGUIRoutines.setLanguage(this.getBaseContext());

                final boolean _showRequestWriteSettings = showRequestWriteSettings;
                final boolean _showRequestAccessNotificationPolicy = showRequestAccessNotificationPolicy;
                final boolean _showRequestDrawOverlays = showRequestDrawOverlays;

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(GlobalGUIRoutines.fromHtml(showRequestString));
                dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int iteration = 4;
                        if (_showRequestWriteSettings)
                            iteration = 1;
                        else if (_showRequestAccessNotificationPolicy)
                            iteration = 2;
                        else if (_showRequestDrawOverlays)
                            iteration = 3;
                        requestPermissions(iteration);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                        //Permissions.releaseReferences();
                        /*if (mergedNotification)
                            Permissions.clearMergedPermissions(context);*/
                    }
                });
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
            //}
        }
        else {
            /*if (onlyNotification)
                showNotification(context);
            else*/
                requestPermissions(4);
        }
    }

    private String getWhyPermissionString(int permissionType) {
        String s = "";
        switch (permissionType) {
            //case Permissions.PERMISSION_PROFILE_VOLUME_PREFERENCES:
            //    break;
            case Permissions.PERMISSION_PROFILE_VIBRATION_ON_TOUCH:
                s = "enable/disable vibration on touch";
                break;
            case Permissions.PERMISSION_PROFILE_RINGTONES:
                s = "change ringtone, notification, alarm sound";
                break;
            case Permissions.PERMISSION_PROFILE_SCREEN_TIMEOUT:
                s = "change screen timeout";
                break;
            case Permissions.PERMISSION_PROFILE_SCREEN_BRIGHTNESS:
                s = "change screen brightness";
                break;
            case Permissions.PERMISSION_PROFILE_AUTOROTATION:
                s = "change screen rotation";
                break;
            case Permissions.PERMISSION_PROFILE_WALLPAPER:
                s = "change wallpaper";
                break;
            case Permissions.PERMISSION_PROFILE_RADIO_PREFERENCES:
                s = "enable/disable mobile data, enable/disable mobile network type, connect to SSID";
                break;
            case Permissions.PERMISSION_PROFILE_SPEAKER_PHONE_BROADCAST:
                s = "enable/disable speakerphone during call";
                break;
            case Permissions.PERMISSION_PROFILE_CUSTOM_PROFILE_ICON:
                s = "use custom profile icon";
                break;
            case Permissions.PERMISSION_INSTALL_TONE:
                s = "write tone to storage";
                break;
            case Permissions.PERMISSION_EXPORT:
                s = "write backup data to storage";
                break;
            case Permissions.PERMISSION_IMPORT:
                s = "read backup data from storage";
                break;
            case Permissions.PERMISSION_PROFILE_NOTIFICATION_LED:
                s = "enable/disable notification LED";
                break;
            case Permissions.PERMISSION_PROFILE_VIBRATE_WHEN_RINGING:
                s = "enable/disable vibrate when ringing";
                break;
            case Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION:
                s = "play ringing and notification sounds";
                break;
            case Permissions.PERMISSION_PROFILE_ACCESS_NOTIFICATION_POLICY:
                s = "change sound profile, change ringing, notification, system volume";
                break;
            case Permissions.PERMISSION_PROFILE_LOCK_DEVICE:
                s = "lock device";
                break;
            case Permissions.PERMISSION_RINGTONE_PREFERENCE:
                s = "read ringtones from storage";
                break;
            case Permissions.PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING:
                s = "enable/disable DTMF tone when dialing";
                break;
            case Permissions.PERMISSION_PROFILE_SOUND_ON_TOUCH:
                s = "enable/disable sound on touch";
                break;
            case Permissions.PERMISSION_BRIGHTNESS_PREFERENCE:
                s = "change brightness during configuration";
                break;
            case Permissions.PERMISSION_WALLPAPER_PREFERENCE:
                s = "read wallpapers from storage";
                break;
            case Permissions.PERMISSION_CUSTOM_PROFILE_ICON_PREFERENCE:
                s = "read custom icons from storage";
                break;
        }
        if (s.isEmpty())
            return s;
        else
            return "<br>" + "&nbsp;&nbsp;&nbsp;- for " + s;
    }

    static void showNotification(int grantType, List<Permissions.PermissionType> permissions,
                                 @SuppressWarnings("SameParameterValue") boolean forceGrant,
                                 int startupSource, boolean interactive,
                                 Profile profile, boolean activateProfile, Context context) {
        int notificationID;
        NotificationCompat.Builder mBuilder;

        PPApplication.createGrantPermissionNotificationChannel(context);

        Intent intent = new Intent(context, GrantPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
        if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
            String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
            String nText = context.getString(R.string.permissions_for_install_tone_big_text_notification);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = context.getString(R.string.app_name);
                nText = context.getString(R.string.permissions_for_install_tone_text_notification)+": "+
                        context.getString(R.string.permissions_for_install_tone_big_text_notification);
            }
            mBuilder =   new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                    .setColor(ContextCompat.getColor(context, R.color.primary))
                    .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                    .setContentTitle(nTitle) // title for notification
                    .setContentText(nText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                    .setAutoCancel(true); // clear notification after click
            notificationID = PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID;
        }
        else
        if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
            String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
            String nText = context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = context.getString(R.string.app_name);
                nText = context.getString(R.string.permissions_for_install_tone_text_notification) + ": " +
                        context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
            }
            mBuilder =   new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                    .setColor(ContextCompat.getColor(context, R.color.primary))
                    .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                    .setContentTitle(nTitle) // title for notification
                    .setContentText(nText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                    .setAutoCancel(true); // clear notification after click
            notificationID = PPApplication.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID;
        }
        else {
            String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
            String nText = "";
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = context.getString(R.string.app_name);
                nText = context.getString(R.string.permissions_for_profile_text_notification)+": ";
            }
            /*if (mergedNotification) {
                nText = nText + context.getString(R.string.permissions_for_profile_text1m) + " " +
                        context.getString(R.string.permissions_for_profile_big_text_notification);
            }
            else {*/
                nText = nText + context.getString(R.string.permissions_for_profile_text1) + " ";
                if (profile != null)
                    nText = nText + "\"" + profile._name + "\" ";
                nText = nText + context.getString(R.string.permissions_for_profile_big_text_notification);
            //}
            mBuilder =   new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                    .setColor(ContextCompat.getColor(context, R.color.primary))
                    .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                    .setContentTitle(nTitle) // title for notification
                    .setContentText(nText) // message for notification
                    .setAutoCancel(true); // clear notification after click
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

            Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, 0);
            mBuilder.setDeleteIntent(deletePendingIntent);

            //intent.putExtra(Permissions.EXTRA_FOR_GUI, forGUI);
            //intent.putExtra(Permissions.EXTRA_MONOCHROME, monochrome);
            //intent.putExtra(Permissions.EXTRA_MONOCHROME_VALUE, monochromeValue);

            if (profile != null) {
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                notificationID = 9999 + (int)profile._id;
            }
            else
                notificationID = PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID;
        }
        //permissions.clear();
        intent.putExtra(Permissions.EXTRA_GRANT_TYPE, grantType);
        intent.putParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES, (ArrayList<Permissions.PermissionType>) permissions);
        //intent.putExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
        intent.putExtra(Permissions.EXTRA_FROM_NOTIFICATION, true);
        intent.putExtra(Permissions.EXTRA_FORCE_GRANT, forceGrant);
        intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
        intent.putExtra(Permissions.EXTRA_INTERACTIVE, interactive);
        intent.putExtra(Permissions.EXTRA_ACTIVATE_PROFILE, activateProfile);

        PendingIntent pi = PendingIntent.getActivity(context, grantType, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(notificationID, mBuilder.build());

        //finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.

                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    finishGrant();
                } else {
                    //if (!onlyNotification) {
                        Context context = getApplicationContext();
                        Toast msg = Toast.makeText(context,
                                context.getResources().getString(R.string.app_name) + ": " +
                                        context.getResources().getString(R.string.toast_permissions_not_granted),
                                Toast.LENGTH_SHORT);
                        msg.show();
                    //}
                    finish();
                    //Permissions.releaseReferences();
                    /*if (mergedNotification)
                        Permissions.clearMergedPermissions(getApplicationContext());*/
                }
                //return;
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context context = getApplicationContext();
        if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {
            if (!Settings.System.canWrite(context)) {
                if (!forceGrant) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    dialogBuilder.setMessage(R.string.permissions_write_settings_not_allowed_confirm);
                    dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestWriteSettingsPermission(context, false);
                            requestPermissions(2);
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestWriteSettingsPermission(context, true);
                            requestPermissions(2);
                        }
                    });
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            requestPermissions(2);
                        }
                    });
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
                else
                    requestPermissions(2);
            }
            else {
                Permissions.setShowRequestWriteSettingsPermission(context, true);
                requestPermissions(2);
            }
        }
        if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                    if (!forceGrant) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                        dialogBuilder.setTitle(R.string.permissions_alert_title);
                        dialogBuilder.setMessage(R.string.permissions_access_notification_policy_not_allowed_confirm);
                        dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Permissions.setShowRequestAccessNotificationPolicyPermission(context, false);
                                requestPermissions(3);
                            }
                        });
                        dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                                requestPermissions(3);
                            }
                        });
                        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                requestPermissions(3);
                            }
                        });
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
                    else
                        requestPermissions(3);
                } else {
                    Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    requestPermissions(3);
                }
            }
            else
                requestPermissions(3);
        }
        if (requestCode == DRAW_OVERLAYS_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(context)) {
                if (!forceGrant) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    if (!PPApplication.romIsMIUI)
                        dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm);
                    else
                        dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm_miui);
                    dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestDrawOverlaysPermission(context, false);
                            requestPermissions(4);
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                            requestPermissions(4);
                        }
                    });
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            requestPermissions(4);
                        }
                    });
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
                else
                    requestPermissions(4);
            }
            else {
                Permissions.setShowRequestDrawOverlaysPermission(context, true);
                requestPermissions(4);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(int iteration) {

        if (iteration == 1) {
            boolean writeSettingsFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, getApplicationContext())) {
                            writeSettingsFound = true;
                            final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                            break;
                        }
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", getPackageName());
                            startActivityForResult(localIntent, WRITE_SETTINGS_REQUEST_CODE);
                            writeSettingsFound = true;
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getPackageName());
                                startActivityForResult(localIntent, WRITE_SETTINGS_REQUEST_CODE);
                                writeSettingsFound = true;
                            } catch (Exception e1) {
                                writeSettingsFound = false;
                            }
                        }
                    }*/
                }
            }
            if (!writeSettingsFound)
                requestPermissions(2);
        }
        else
        if (iteration == 2) {
            boolean accessNotificationPolicyFound = false;
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            for (Permissions.PermissionType permissionType : permissions) {
                if (no60 && permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getApplicationContext())) {
                    accessNotificationPolicyFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivityForResult(intent, ACCESS_NOTIFICATION_POLICY_REQUEST_CODE);
                    break;
                }
            }
            if (!accessNotificationPolicyFound)
                requestPermissions(3);
        }
        else
        if (iteration == 3) {
            boolean drawOverlaysFound = false;
            //boolean api25 = android.os.Build.VERSION.SDK_INT >= 25;
            for (Permissions.PermissionType permissionType : permissions) {
                if (/*api25 &&*/ permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getApplicationContext())) {
                            drawOverlaysFound = true;
                            final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            startActivityForResult(intent, DRAW_OVERLAYS_REQUEST_CODE);
                            break;
                        }
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", getPackageName());
                            startActivityForResult(localIntent, DRAW_OVERLAYS_REQUEST_CODE);
                            drawOverlaysFound = true;
                            break;
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getPackageName());
                                startActivityForResult(localIntent, DRAW_OVERLAYS_REQUEST_CODE);
                                drawOverlaysFound = true;
                                break;
                            } catch (Exception e1) {
                                drawOverlaysFound = false;
                            }
                        }
                    }*/
                }
            }
            if (!drawOverlaysFound)
                requestPermissions(4);
        }
        else {
            List<String> permList = new ArrayList<>();
            for (Permissions.PermissionType permissionType : permissions) {
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                    (!permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) &&
                    (!permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) &&
                    (!permList.contains(permissionType.permission))) {
                    permList.add(permissionType.permission);
                }
            }

            if (permList.size() > 0) {

                String[] permArray = new String[permList.size()];
                for (int i = 0; i < permList.size(); i++) permArray[i] = permList.get(i);

                ActivityCompat.requestPermissions(this, permArray, PERMISSIONS_REQUEST_CODE);
            }
            else
                finishGrant();
        }
    }

    private void finishGrant() {
        Context context = getApplicationContext();

        /*
        if (forGUI && (profile != null))
        {
            // regenerate profile icon
            dataWrapper.refreshProfileIcon(profile, monochrome, monochromeValue);
        }
        */

        if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
            //finishAffinity();
            finish();
            Permissions.removeInstallToneNotification(context);
            TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, context, true);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WALLPAPER) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.wallpaperViewPreference != null)
                Permissions.wallpaperViewPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.profileIconPreference != null)
                Permissions.profileIconPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.editorActivity != null)
                Permissions.editorActivity.doExportData();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_IMPORT) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Permissions.EXTRA_APPLICATION_DATA_PATH, applicationDataPath);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
            /*if (Permissions.editorActivity != null)
                Permissions.editorActivity.doImportData(applicationDataPath);*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.brightnessDialogPreference != null)
                Permissions.brightnessDialogPreference.enableViews();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.ringtonePreference != null)
                Permissions.ringtonePreference.refreshListView();*/
        }
        else {
            /*Intent returnIntent = new Intent();
            returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            returnIntent.putExtra(Permissions.EXTRA_ACTIVATE_PROFILE, activateProfile);
            setResult(Activity.RESULT_OK,returnIntent);*/

            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(context);
            if (activateProfile)
                dataWrapper._activateProfile(profile, startupSource, interactive,null);
        }

        if (permissions != null) {
            permissions = Permissions.recheckPermissions(context, permissions);
            if (permissions.size() != 0) {
                Toast msg = Toast.makeText(context,
                        context.getResources().getString(R.string.toast_permissions_not_granted),
                        Toast.LENGTH_SHORT);
                msg.show();
            }
        }

        //Permissions.releaseReferences();
        /*if (mergedNotification)
            Permissions.clearMergedPermissions(context);*/

        //if (grantType != Permissions.GRANT_TYPE_PROFILE) {
            PPApplication.showProfileNotification(dataWrapper.context);
            ActivateProfileHelper.updateGUI(dataWrapper.context, true);
        //}
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
