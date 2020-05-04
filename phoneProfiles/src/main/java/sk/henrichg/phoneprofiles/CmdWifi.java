package sk.henrichg.phoneprofiles;

import android.net.wifi.IWifiManager;
import android.os.ServiceManager;
import android.util.Log;

@SuppressWarnings("WeakerAccess")
public class CmdWifi {

    public static void main(String[] args) {
        if (!(run(Boolean.parseBoolean(args[0])))) {
            System.exit(1);
        }
    }

    private static boolean run(boolean enable) {
        return setWifi(enable);
    }

    private static boolean setWifi(boolean enable) {
        final String packageName = PPApplication.PACKAGE_NAME;
        try {
            //PPApplication.logE("CmdWifi.setWifi", "enable="+enable);
            IWifiManager wifiAdapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));  // service list | grep IWifiManager
            wifiAdapter.setWifiEnabled(packageName, enable);
            return true;
        } catch (Throwable e) {
            Log.e("CmdWifi.setWifi", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

    /*
    static boolean isEnabled() {
        try {
            boolean enabled;
            IWifiManager adapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));  // service list | grep IWifiManager
            int wifiState = adapter.getWifiEnabledState();
            enabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
            //PPApplication.logE("CmdWifi.isEnabled", "enabled="+enabled);
            return enabled;
        } catch (Throwable e) {
            Log.e("CmdWifi.isEnabled", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }
    */

}
