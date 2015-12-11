package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Locale;

public class GUIData {

    public static BrightnessView brightneesView = null;
    public static BrightnessView keepScreenOnView = null;

    public static Collator collator = null;

    // import/export
    public static final String DB_FILEPATH = "/data/" + GlobalData.PACKAGE_NAME + "/databases";
    public static final String REMOTE_EXPORT_PATH = "/PhoneProfilesPlus";
    public static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    public static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";

    // this string is from material-preferences linrary (https://github.com/ferrannp/material-preferences)
    public static final String MAIN_PREFERENCE_FRAGMENT_TAG = "com.fnp.materialpreferences.MainFragment";


    public static void setLanguage(Context context)//, boolean restart)
    {
        //long nanoTimeStart = GlobalData.startMeasuringRunTime();

        // jazyk na aky zmenit
        String lang = GlobalData.applicationLanguage;

        Locale appLocale;

        if (!lang.equals("system"))
        {
            String[] langSplit = lang.split("-");
            if (langSplit.length == 1)
                appLocale = new Locale(lang);
            else
                appLocale = new Locale(langSplit[0], langSplit[1]);
        }
        else
        {
            appLocale = Resources.getSystem().getConfiguration().locale;
        }

        Locale.setDefault(appLocale);
        Configuration appConfig = new Configuration();
        appConfig.locale = appLocale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

        // collator for application locale sorting
        collator = getCollator();

        //languageChanged = restart;

        //GlobalData.getMeasuredRunTime(nanoTimeStart, "GUIData.setLanguage");

    }

    public static Collator getCollator()
    {
        // get application Locale
        String lang = GlobalData.applicationLanguage;
        Locale appLocale;
        if (!lang.equals("system"))
        {
            String[] langSplit = lang.split("-");
            if (langSplit.length == 1)
                appLocale = new Locale(lang);
            else
                appLocale = new Locale(langSplit[0], langSplit[1]);
        }
        else
        {
            appLocale = Resources.getSystem().getConfiguration().locale;
        }

        // get collator for application locale
        return Collator.getInstance(appLocale);
    }

    public static void setTheme(Activity activity, boolean forPopup, boolean withToolbar)
    {
        activity.setTheme(getTheme(forPopup, withToolbar));
    }

    public static int getTheme(boolean forPopup, boolean withToolbar) {
        if (GlobalData.applicationTheme.equals("material"))
        {
            if (forPopup)
            {
                //if (withToolbar)
                //    return R.style.PopupTheme_withToolbar_material;
                //else
                    return R.style.PopupTheme_material;
            }
            else
            {
                if (withToolbar)
                    return R.style.Theme_Phoneprofilestheme_withToolbar_material;
                else
                    return R.style.Theme_Phoneprofilestheme_material;
            }
        }
        else
        if (GlobalData.applicationTheme.equals("dark"))
        {
            if (forPopup)
            {
                //if (withToolbar)
                //    return R.style.PopupTheme_withToolbar_dark;
                //else
                    return R.style.PopupTheme_dark;
            }
            else
            {
                if (withToolbar)
                    return R.style.Theme_Phoneprofilestheme_withToolbar_dark;
                else
                    return R.style.Theme_Phoneprofilestheme_dark;
            }
        }
        else
        if (GlobalData.applicationTheme.equals("dlight"))
        {
            if (forPopup)
            {
                //if (withToolbar)
                //    return R.style.PopupTheme_withToolbar_dlight;
                //else
                    return R.style.PopupTheme_dlight;
            }
            else
            {
                if (withToolbar)
                    return R.style.Theme_Phoneprofilestheme_withToolbar_dlight;
                else
                    return R.style.Theme_Phoneprofilestheme_dlight;
            }
        }
        return 0;
    }

    public static int getDialogTheme(boolean forAlert) {
        if (GlobalData.applicationTheme.equals("material"))
        {
            if (forAlert)
                return R.style.AlertDialogStyle;
            else
                return R.style.DialogStyle;
        }
        else
        if (GlobalData.applicationTheme.equals("dark"))
        {
            if (forAlert)
                return R.style.AlertDialogStyleDark;
            else
                return R.style.DialogStyleDark;
        }
        else
        if (GlobalData.applicationTheme.equals("dlight"))
        {
            if (forAlert)
                return R.style.AlertDialogStyle;
            else
                return R.style.DialogStyle;
        }
        return 0;
    }

    public static void reloadActivity(Activity activity, boolean newIntent)
    {
        if (newIntent)
        {

            final Activity _activity = activity;
            new Handler().post(new Runnable() {

                @Override
                public void run()
                {
                    Intent intent = _activity.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    _activity.overridePendingTransition(0, 0);
                    _activity.finish();

                    _activity.overridePendingTransition(0, 0);
                    _activity.startActivity(intent);
                }
            });
        }
        else
            activity.recreate();
    }

    public static void registerOnActivityDestroyListener(Preference preference, PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

    public static void unregisterOnActivityDestroyListener(Preference preference, PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "unregisterOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

}
