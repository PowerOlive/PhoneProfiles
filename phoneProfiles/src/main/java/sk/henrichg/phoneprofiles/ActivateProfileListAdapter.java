package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

class ActivateProfileListAdapter extends BaseAdapter
{

    private ActivateProfileListFragment fragment;
    private final DataWrapper activityDataWrapper;

    //private boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "activate_profile_list_adapter_start_target_helps";

    ActivateProfileListAdapter(ActivateProfileListFragment f, /*List<Profile> pl, */DataWrapper dataWrapper)
    {
        fragment = f;
        this.activityDataWrapper = dataWrapper;
    }

    public void release()
    {
        fragment = null;
    }

    public int getCount()
    {
        fragment.textViewNoData.setVisibility(
                ((activityDataWrapper.profileListFilled &&
                  (activityDataWrapper.profileList.size() > 0))
                ) ? View.GONE : View.VISIBLE);

        return activityDataWrapper.profileList.size();
    }

    public Object getItem(int position)
    {
        return activityDataWrapper.profileList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    /*
    public int getItemId(Profile profile)
    {
        for (int i = 0; i < profileList.size(); i++)
        {
            if (profileList.get(i)._id == profile._id)
                return i;
        }
        return -1;
    }

    public void setList(List<Profile> pl)
    {
        profileList = pl;
        notifyDataSetChanged();
    }
    */

    int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (!activityDataWrapper.profileListFilled)
            return -1;

        int pos = -1;

        for (int i = 0; i < activityDataWrapper.profileList.size(); i++)
        {
            ++pos;
            if (activityDataWrapper.profileList.get(i)._id == profile._id)
                return pos;
        }
        return -1;
    }

    public Profile getActivatedProfile()
    {
        for (Profile p : activityDataWrapper.profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
    }

    void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Profile profile : activityDataWrapper.profileList) {
                activityDataWrapper.refreshProfileIcon(profile, true, ApplicationPreferences.applicationActivatorPrefIndicator(activityDataWrapper.context));
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
          //ViewGroup listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          //int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (!ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))
            {
                if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
                    vi = inflater.inflate(R.layout.activate_profile_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.activate_profile_list_item_no_indicator, parent, false);
                //holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = vi.findViewById(R.id.act_prof_list_item_profile_icon);
                if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
                    holder.profileIndicator = vi.findViewById(R.id.act_prof_list_profile_pref_indicator);
            }
            else
            {
                vi = inflater.inflate(R.layout.activate_profile_grid_item, parent, false);
                //holder.listItemRoot = (LinearLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = vi.findViewById(R.id.act_prof_list_item_profile_icon);
            }
            vi.setTag(holder);        
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        final Profile profile = activityDataWrapper.profileList.get(position);

        if ((ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity())) &&
                (profile._porder == ActivateProfileListFragment.PORDER_FOR_IGNORED_PROFILE)) {
            holder.profileName.setText(R.string.empty_string);
            holder.profileIcon.setImageResource(R.drawable.ic_empty);
        }
        else {
            if (profile._checked && (!ApplicationPreferences.applicationActivatorHeader(fragment.getActivity()))) {
                holder.profileName.setTypeface(/*Typeface.create("sans-serif-condensed", Typeface.BOLD)*/ null, Typeface.BOLD);
                if (ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))
                    holder.profileName.setTextSize(14);
                else
                    holder.profileName.setTextSize(16);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(fragment.getActivity()));
            } else {
                holder.profileName.setTypeface(/*Typeface.create("sans-serif-condensed", Typeface.NORMAL)*/ null, Typeface.NORMAL);
                if (ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))
                    holder.profileName.setTextSize(13);
                else
                    holder.profileName.setTextSize(15);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeTextColor(fragment.getActivity()));
            }

            String profileName = profile.getProfileNameWithDuration(ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()), activityDataWrapper.context);
            holder.profileName.setText(profileName);

            if (profile.getIsIconResourceID()) {
                if (profile._iconBitmap != null)
                    holder.profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                    //        vi.getContext().getPackageName());
                    int res = Profile.getIconResource(profile.getIconIdentifier());
                    holder.profileIcon.setImageResource(res);
                }
            } else {
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            }

            if (holder.profileIndicator != null) {
                if ((ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity())) && (!ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))) {
                    if (profile._preferencesIndicator != null) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
                    } else
                        holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                }
            }
        }

        return vi;
    }

    void showTargetHelps(final Activity activity, /*ActivateProfileListFragment fragment,*/ final View listItemView) {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        //if (fragment.targetHelpsSequenceStarted)
        //    return;

        ApplicationPreferences.getSharedPreferences(activity);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {

            //Log.d("ActivateProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.apply();

            Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
            int[] screenLocation = new int[2];
            //listItemView.getLocationOnScreen(screenLocation);
            listItemView.getLocationInWindow(screenLocation);
            profileItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);

            final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);

            /*int circleColor = R.color.tabTargetHelpCircleColor;
            if (ApplicationPreferences.applicationTheme(activity)).equals("dark"))
                circleColor = R.color.tabTargetHelpCircleColor_dark;*/
            int textColor = R.color.tabTargetHelpTextColor;
            if (ApplicationPreferences.applicationTheme(activity).equals("white"))
                textColor = R.color.tabTargetHelpTextColor_white;
            boolean tintTarget = !ApplicationPreferences.applicationTheme(activity).equals("white");

            sequence.targets(
                    TapTarget.forBounds(profileItemTarget, activity.getString(R.string.activator_activity_targetHelps_activateProfile_title), activity.getString(R.string.activator_activity_targetHelps_activateProfile_description))
                            .transparentTarget(true)
                            .textColor(textColor)
                            .tintTarget(tintTarget)
                            .drawShadow(true)
                            .id(1)
            );
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;
                    final Handler handler = new Handler(activity.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (ActivatorTargetHelpsActivity.activity != null) {
                                //Log.d("ActivateProfileListAdapter.showTargetHelps", "finish activity");
                                ActivatorTargetHelpsActivity.activity.finish();
                                ActivatorTargetHelpsActivity.activity = null;
                                //ActivatorTargetHelpsActivity.activatorActivity = null;
                            }
                        }
                    }, 500);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    //targetHelpsSequenceStarted = false;
                    final Handler handler = new Handler(activity.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (ActivatorTargetHelpsActivity.activity != null) {
                                //Log.d("ActivateProfileListAdapter.showTargetHelps", "finish activity");
                                ActivatorTargetHelpsActivity.activity.finish();
                                ActivatorTargetHelpsActivity.activity = null;
                                //ActivatorTargetHelpsActivity.activatorActivity = null;
                            }
                        }
                    }, 500);
                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);
            //targetHelpsSequenceStarted = true;
            sequence.start();
        }
        else {
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivatorTargetHelpsActivity.activity != null) {
                        //Log.d("ActivateProfileListAdapter.showTargetHelps", "finish activity");
                        ActivatorTargetHelpsActivity.activity.finish();
                        ActivatorTargetHelpsActivity.activity = null;
                        //ActivatorTargetHelpsActivity.activatorActivity = null;
                    }
                }
            }, 500);
        }
    }

}
