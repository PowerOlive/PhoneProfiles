package sk.henrichg.phoneprofiles;

import android.util.Pair;

import java.util.ArrayList;

class RootMutex {
    boolean rootChecked;
    boolean rooted;
    boolean settingsBinaryChecked;
    boolean settingsBinaryExists;
    //boolean isSELinuxEnforcingChecked;
    //boolean isSELinuxEnforcing;
    //String suVersion;
    //boolean suVersionChecked;
    boolean serviceBinaryChecked;
    boolean serviceBinaryExists;
    ArrayList<Pair> serviceList = null;
}
