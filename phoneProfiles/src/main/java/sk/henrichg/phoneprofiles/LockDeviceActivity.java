package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class LockDeviceActivity extends AppCompatActivity {

    private View view = null;
    private boolean displayed = false;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        if (PhoneProfilesService.getInstance() != null) {
            PhoneProfilesService.getInstance().lockDeviceActivity = this;

            /*
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);

            setContentView(R.layout.activity_lock_device);

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness =0.005f;
            getWindow().setAttributes(lp);
            */

            PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
            ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);


            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.flags = 1808;
            if (android.os.Build.VERSION.SDK_INT < 26)
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            else
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.gravity = Gravity.TOP;
            params.width = -1;
            params.height = -1;
            params.format = -1;
            params.screenBrightness = 0f;

            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //noinspection ConstantConditions
            view = layoutInflater.inflate(R.layout.activity_lock_device, null);
            view.setSystemUiVisibility(5894);
            view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    view.setSystemUiVisibility(5894);
                }
            });

            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null)
                windowManager.addView(view, params);

            LockDeviceActivityFinishBroadcastReceiver.setAlarm(getApplicationContext());

            displayed = true;
        }
        else
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (displayed && (PhoneProfilesService.getInstance() != null)) {
            if (view != null) {
                try {
                    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null)
                        windowManager.removeViewImmediate(view);
                } catch (Exception ignored) {
                }
            }

            final Context appContext = getApplicationContext();

            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);
            PhoneProfilesService.getInstance().lockDeviceActivity = null;

            boolean canWriteSettings = true;
            if (android.os.Build.VERSION.SDK_INT >= 23)
                canWriteSettings = Settings.System.canWrite(appContext);
            if (canWriteSettings)
                Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock);

            // change screen timeout
            final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
            final int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
            PPApplication.logE("LockDeviceActivity.onDestroy", "screenTimeout="+screenTimeout);
            if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                PPApplication.logE("LockDeviceActivity.onDestroy", "permission ok");
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            PPApplication.logE("LockDeviceActivity.onDestroy", "call ActivateProfileHelper.setScreenTimeout");
                            ActivateProfileHelper.setScreenTimeout(screenTimeout, appContext);
                        }
                    });
                }/* else {
                    dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);
                }*/
            }
            dataWrapper.invalidateDataWrapper();
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
        displayed = false;
    }

}
