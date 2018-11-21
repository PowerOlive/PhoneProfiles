package sk.henrichg.phoneprofiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class ShortcutCreatorListAdapter extends BaseAdapter {

    private ShortcutCreatorListFragment fragment;
    private DataWrapper activityDataWrapper;

    ShortcutCreatorListAdapter(ShortcutCreatorListFragment f, DataWrapper dataWrapper)
    {
        fragment = f;
        activityDataWrapper = dataWrapper;
    }

    public void release()
    {
        fragment = null;
        activityDataWrapper = null;
    }

    public int getCount() {
        fragment.textViewNoData.setVisibility(
                ((activityDataWrapper.profileListFilled &&
                  (activityDataWrapper.profileList.size() > 0))
                ) ? View.GONE : View.VISIBLE);

        return activityDataWrapper.profileList.size();
    }

    public Object getItem(int position) {
        return activityDataWrapper.profileList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView profileIcon;
        TextView profileName;
        ImageView profileIndicator;
        //int position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
                vi = inflater.inflate(R.layout.shortcut_list_item, parent, false);
            else
                vi = inflater.inflate(R.layout.shortcut_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.profileName = vi.findViewById(R.id.shortcut_list_item_profile_name);
            holder.profileIcon = vi.findViewById(R.id.shortcut_list_item_profile_icon);
            if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
                holder.profileIndicator = vi.findViewById(R.id.shortcut_list_profile_pref_indicator);
            vi.setTag(holder);        
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        Profile profile = activityDataWrapper.profileList.get(position);

        String profileName = profile.getProfileNameWithDuration("", false, fragment.getActivity());
        holder.profileName.setText(profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                //        vi.getContext().getPackageName());
                int res = Profile.getIconResource(profile.getIconIdentifier());
                holder.profileIcon.setImageResource(res);
            }
        }
        else
        {
            holder.profileIcon.setImageBitmap(profile._iconBitmap);
        }
        
        if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
        {
            if (profile._preferencesIndicator != null) {
                //profilePrefIndicatorImageView.setImageBitmap(null);
                //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
            }
            else
                holder.profileIndicator.setImageResource(R.drawable.ic_empty);
        }
        
        return vi;
    }

    /*
    public void setList(List<Profile> pl) {
        profileList = pl;
        notifyDataSetChanged();
    }
    */

}
