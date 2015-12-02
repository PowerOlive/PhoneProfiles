package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.ListView;

public class PhoneProfilesPreferencesFragment extends PreferenceFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private static Activity preferencesActivity = null;
    String extraScrollTo;

    static final String PREF_APPLICATION_PERMISSIONS = "prf_pref_applicationPermissions";
    static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "prf_pref_writeSystemSettingsPermissions";
    static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        preferencesActivity = getActivity();
        //context = getActivity().getBaseContext();

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(GlobalData.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();

        addPreferencesFromResource(R.xml.phone_profiles_preferences);

        preferences.registerOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT >= 23) {
            Preference preference = prefMng.findPreference(PREF_APPLICATION_PERMISSIONS);
            preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:sk.henrichg.phoneprofiles"));
                    startActivityForResult(intent, RESULT_APPLICATION_PERMISSIONS);
                    return false;
                }
            });
            preference = prefMng.findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                    return false;
                }
            });
        }
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("prf_pref_permissionsCategory");
            preferenceScreen.removePreference(preferenceCategory);
        }


        extraScrollTo = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "");
    }

    private void setTitleStyle(Preference preference, boolean bold, boolean underline)
    {
        CharSequence title = preference.getTitle();
        Spannable sbt = new SpannableString(title);
        Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        if (bold || underline)
        {
            if (bold)
                sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (underline)
                sbt.setSpan(new UnderlineSpan(), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preference.setTitle(sbt);
        }
        else
        {
            preference.setTitle(sbt);
        }
    }

    private void setSummary(String key)
    {
        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && preference instanceof TwoStatePreference)) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(GlobalData.PREF_APPLICATION_BACKGROUND_PROFILE))
        {
            String sProfileId = stringValue;
            long lProfileId;
            try {
                lProfileId = Long.parseLong(sProfileId);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreference profilePreference = (ProfilePreference)preference;
            profilePreference.setSummary(lProfileId);
        }
        else
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            // **** Heno changes ** support for "%" in list items
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            if (summary != null)
            {
                String sSummary = summary.toString();
                sSummary = sSummary.replace("%", "%%");
                preference.setSummary(sSummary);
            }
            else
                preference.setSummary(summary);

            if (key.equals(GlobalData.PREF_APPLICATION_LANGUAGE))
                setTitleStyle(preference, true, false);
        }
        /*else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            if (TextUtils.isEmpty(stringValue)) {
                // Empty values correspond to 'silent' (no ringtone).
                preference.setSummary(R.string.ringtone_silent);
            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                if (ringtone == null) {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(null);
                } else {
                    // Set the summary to reflect the new ringtone display
                    // name.
                    String name = ringtone
                            .getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }

        }*/
        else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            //preference.setSummary(preference.toString());
            preference.setSummary(stringValue);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummary(key);
    }

    private void updateSharedPreference()
    {
        setSummary(GlobalData.PREF_APPLICATION_START_ON_BOOT);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATE);
        setSummary(GlobalData.PREF_APPLICATION_ALERT);
        setSummary(GlobalData.PREF_APPLICATION_CLOSE);
        setSummary(GlobalData.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(GlobalData.PREF_APPLICATION_LANGUAGE);
        setSummary(GlobalData.PREF_APPLICATION_THEME);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_HEADER);
        setSummary(GlobalData.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(GlobalData.PREF_NOTIFICATION_TOAST);
        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            setSummary(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
                if (preference != null) {
                    preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (preference != null) {
                preference.setEnabled(false);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
                editor.commit();
            }
        }

        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        //setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_CANCEL);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            // for Android 5.0, color notification icon is not supported
            Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_STATUS_BAR_STYLE);
            if (preference != null)
            {
                PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotifications");
                preferenceCategory.removePreference(preference);
            }
        }
        else
            setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_STYLE);

        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(GlobalData.PREF_APPLICATION_BACKGROUND_PROFILE);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        updateSharedPreference();

        PreferenceCategory scrollCategory = (PreferenceCategory) findPreference(extraScrollTo);
        if (scrollCategory != null) {
            // scroll to category
            for (int i = 0; i <  getPreferenceScreen().getRootAdapter().getCount(); i++){
                Object o = getPreferenceScreen().getRootAdapter().getItem(i);
                if (o instanceof PreferenceCategory ){
                    if (o.equals(scrollCategory)){
                        ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
                        if (listView != null)
                            listView.setSelection(i);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this); 
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
                (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS)) {

            Context context = preferencesActivity.getApplicationContext();
            DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

            ActivateProfileHelper activateProfileHelper = dataWrapper.getActivateProfileHelper();
            activateProfileHelper.initialize(null, context);

            Profile activatedProfile = dataWrapper.getActivatedProfile();
            dataWrapper.refreshProfileIcon(activatedProfile, false, 0);
            activateProfileHelper.showNotification(activatedProfile);
            activateProfileHelper.updateWidget();

            /*Intent intent5 = new Intent();
            intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
            intent5.putExtra(RefreshGUIBroadcastReceiver.EXTRA_REFRESH_ICONS, true);
            context.sendBroadcast(intent5);*/

            preferencesActivity.finishAffinity();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        doOnActivityResult(requestCode, resultCode, data);
    }

    static public Activity getPreferencesActivity()
    {
        return preferencesActivity;
    }

}
