package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;

public class PhoneProfilesPreferencesFragment extends PreferenceFragment 
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private PreferenceManager prefMng;
	private SharedPreferences preferences;
	private static Activity preferencesActivity = null;
	
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
            DataWrapper dataWrapper = new DataWrapper(preferencesActivity.getApplicationContext(), false, false, 0);
            Profile profile = dataWrapper.getProfileById(lProfileId);
            if (profile != null)
            {
                prefMng.findPreference(key).setSummary(profile._name);
            }
            else
            {
                if (lProfileId == GlobalData.PROFILE_NO_ACTIVATE)
                    prefMng.findPreference(key).setSummary(preferencesActivity.getBaseContext().getResources().getString(R.string.profile_preference_profile_end_no_activate));
                else
                    prefMng.findPreference(key).setSummary(preferencesActivity.getBaseContext().getResources().getString(R.string.profile_preference_profile_not_set));
            }
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
        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_CANCEL);

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
