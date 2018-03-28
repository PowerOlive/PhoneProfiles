package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
class SamsungEdgeFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private Context context;
    //private int appWidgetId;
    private List<Profile> profileList = new ArrayList<>();

    SamsungEdgeFactory(Context ctxt, @SuppressWarnings("unused") Intent intent) {
        context = ctxt;
        /*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID); */
    }
  
    private void createProfilesDataWrapper()
    {
        int monochromeValue = 0xFF;
        String applicationWidgetListIconLightness = ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
        if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
        if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
        if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
        if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
        if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

        if (dataWrapper == null)
        {
            dataWrapper = new DataWrapper(context, ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1"),
                                                        monochromeValue);
        }
        else
        {
            dataWrapper.setParameters(ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1"),
                                                        monochromeValue);
        }
    }

    public void onCreate() {
    }
  
    public void onDestroy() {
        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    public int getCount() {
        if (profileList != null)
            return(profileList.size());
        else
            return 0;
    }

    private Profile getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
            return profileList.get(position);
    }

    public RemoteViews getViewAt(int position) {

        RemoteViews row;
        /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
            row=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_item);
        else*/
            row=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_item);

        Profile profile = getItem(position);

        if (profile != null) {

            if (profile.getIsIconResourceID()) {
                if (profile._iconBitmap != null)
                    row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
                else {
                    row.setImageViewResource(R.id.widget_profile_list_item_profile_icon,
                            context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.getPackageName()));
                }
            } else {
                row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
            }
            int red = 0xFF;
            int green;
            int blue;
            String applicationWidgetListLightnessT = ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red;
            blue = red;
            if (!ApplicationPreferences.applicationSamsungEdgeHeader(context)) {
                if (profile._checked) {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_SP, 17);

                    //if (PPApplication.applicationWidgetListIconColor.equals("1"))
                    row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                    //else
                    //	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.parseColor("#33b5e5"));
                } else {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_SP, 15);

                    //if (PPApplication.applicationWidgetListIconColor.equals("1"))
                    row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xCC, red, green, blue));
                    //else
                    //	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                }
            } else {
                row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
            }
            if ((!ApplicationPreferences.applicationSamsungEdgeHeader(context)) && (profile._checked)) {
                // hm, interesting, how to set bold style for RemoteView text ;-)
                //String profileName = profile.getProfileNameWithDuration(ApplicationPreferences.applicationSamsungEdgeGridLayout(context), context);
                String profileName = profile.getProfileNameWithDuration(true, context);
                Spannable sb = new SpannableString(profileName);
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, sb);
            } else {
                //String profileName = profile.getProfileNameWithDuration(ApplicationPreferences.applicationSamsungEdgeGridLayout(context), context);
                String profileName = profile.getProfileNameWithDuration(true, context);
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, profileName);
            }
            /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context)) {
                if (ApplicationPreferences.applicationSamsungEdgePrefIndicator(context)) {
                    if (profile._preferencesIndicator != null)
                        row.setImageViewBitmap(R.id.widget_profile_list_profile_pref_indicator, profile._preferencesIndicator);
                    else
                        row.setImageViewResource(R.id.widget_profile_list_header_profile_pref_indicator, R.drawable.ic_empty);
                } else
                    row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
            }*/

            Intent i = new Intent();
            Bundle extras = new Bundle();

            extras.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
            extras.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            i.putExtras(extras);
            row.setOnClickFillInIntent(R.id.widget_profile_list_item, i);

        }

        return(row);
    }

    public RemoteViews getLoadingView() {
        return(null);
    }
  
    public int getViewTypeCount() {
        return(1);
    }

    public long getItemId(int position) {
        return(position);
    }

    public boolean hasStableIds() {
        return(true);
    }

    public void onDataSetChanged() {
        createProfilesDataWrapper();

        List<Profile> newProfileList = dataWrapper.getNewProfileList(true, false);

        dataWrapper.clearProfileList();
        dataWrapper.setProfileList(newProfileList);
        profileList = newProfileList;
    }
}