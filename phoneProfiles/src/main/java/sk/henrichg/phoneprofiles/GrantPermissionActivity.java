package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GrantPermissionActivity extends Activity {

    private long profile_id;
    private List<Permissions.PermissionType> permissions;
    private boolean forGUI;
    private boolean monochrome;
    private int monochromeValue;

    private boolean installTonePreference = false;

    private Profile profile;
    private DataWrapper dataWrapper;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 909090;
    private static final int PERMISSIONS_REQUEST_CODE = 909091;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        Intent intent = getIntent();
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);

        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);

        dataWrapper = new DataWrapper(getApplicationContext(), forGUI, monochrome, monochromeValue);
        profile = dataWrapper.getProfileById(profile_id);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Context context = getApplicationContext();

        installTonePreference = false;

        boolean showRequestWriteSettings = false;
        boolean showRequestReadExternalStorage = false;
        boolean showRequestReadPhoneState = false;
        boolean showRequestProcessOutgoingCalls = false;
        boolean showRequestWriteExternalStorage = false;

        Log.e("GrantPermissionActivity", "onStart - permissions.size="+permissions.size());

        for (Permissions.PermissionType permissionType : permissions) {
            if (permissionType.preference == Permissions.PERMISSION_INSTALL_TONE)
                installTonePreference = true;

            if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS))
                showRequestWriteSettings = GlobalData.getShowRequestWriteSettingsPermission(context);
            if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE))
                showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE);
            if (permissionType.permission.equals(Manifest.permission.PROCESS_OUTGOING_CALLS))
                showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS);
            if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showRequestWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (showRequestWriteSettings ||
                showRequestReadExternalStorage ||
                showRequestReadPhoneState ||
                showRequestProcessOutgoingCalls ||
                showRequestWriteExternalStorage) {

            String showRequestString = "";

            if (installTonePreference) {
                showRequestString = context.getString(R.string.permissions_for_install_tone_text1) + "<br><br>";
            }
            else {
                showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
                if (profile != null)
                    showRequestString = showRequestString + "\"" + profile._name + "\" ";
                showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
            }

            if (showRequestWriteSettings) {
                Log.e("GrantPermissionActivity","onStart - showRequestWriteSettings");
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadExternalStorage) {
                Log.e("GrantPermissionActivity","onStart - showRequestReadExternalStorage");
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadPhoneState || showRequestProcessOutgoingCalls) {
                Log.e("GrantPermissionActivity","onStart - showRequestReadPhoneState");
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_phone) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestWriteExternalStorage) {
                Log.e("GrantPermissionActivity","onStart - showRequestWriteExternalStorage");
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                showRequestString = showRequestString + "<br>";
            }

            showRequestString = showRequestString + "<br>";

            if (installTonePreference)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_install_tone_text2);
            else
                showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GUIData.setTheme(this, true);
            GUIData.setLanguage(this.getBaseContext());

            final Activity _activity = this;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.permissions_alert_title);
            dialogBuilder.setMessage(Html.fromHtml(showRequestString));
            dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(true);
                }
            });
            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            dialogBuilder.show();

        }
        else {
            requestPermissions(true);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.

                boolean allGranted = true;
                for (int i=0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    finishGrant();
                } else {
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {
            requestPermissions(false);
        }
    }

    private void requestPermissions(boolean writeSettings) {

        if (writeSettings) {
            boolean writeSettingsFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    writeSettingsFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                    break;
                }
            }
            if (!writeSettingsFound)
                requestPermissions(false);
        }
        else {
            List<String> permList = new ArrayList<String>();
            for (Permissions.PermissionType permissionType : permissions) {
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                        (!permList.contains(permissionType.permission))) {
                    Log.e("GrantPermissionActivity", "requestPermissions - permission=" + permissionType.permission);
                    permList.add(permissionType.permission);
                }
            }

            Log.e("GrantPermissionActivity", "requestPermissions - permList.size=" + permList.size());
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
        finishAffinity();

        if (installTonePreference) {
            Permissions.removeInstallToneNotification(getApplicationContext());
            FirstStartService.installTone(FirstStartService.TONE_ID, FirstStartService.TONE_NAME, getApplicationContext(), true);
        }
        else {
            Permissions.removeProfileNotification(getApplicationContext());
            ActivateProfileHelper activateProfileHelper = new ActivateProfileHelper();
            activateProfileHelper.initialize(null, getApplicationContext());
            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if (activatedProfile._id == profile_id) {
                Profile profileFromDB = dataWrapper.getProfileById(profile_id);  // for regenerating icon bitmaps
                activateProfileHelper.showNotification(profileFromDB);
            }
            activateProfileHelper.updateWidget();
        }
    }
}
