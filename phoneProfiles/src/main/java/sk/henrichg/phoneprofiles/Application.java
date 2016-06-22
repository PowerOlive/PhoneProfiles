package sk.henrichg.phoneprofiles;

import android.graphics.drawable.Drawable;

public class Application {
    public boolean shortcut = false;
    public String appLabel = "";
    public String packageName = "";
    public String activityName = "";
    public long shortcutId = 0;
    public Drawable icon;
    public boolean checked = false;

    public Application() {
    }

    public String toString() {
        return appLabel;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}