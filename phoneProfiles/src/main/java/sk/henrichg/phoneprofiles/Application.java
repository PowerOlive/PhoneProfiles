package sk.henrichg.phoneprofiles;

import android.graphics.drawable.Drawable;

public class Application {
    boolean shortcut = false;
    String appLabel = "";
    String packageName = "";
    String activityName = "";
    long shortcutId = 0;
    Drawable icon;
    boolean checked = false;

    public Application() {
    }

    public String toString() {
        return appLabel;
    }

    void toggleChecked() {
        checked = !checked;
    }
}