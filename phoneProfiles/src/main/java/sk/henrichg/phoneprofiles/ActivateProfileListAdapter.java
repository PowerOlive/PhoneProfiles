package sk.henrichg.phoneprofiles;

import android.app.Fragment;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ActivateProfileListAdapter extends BaseAdapter
{

    private List<Profile> profileList;
    private Fragment fragment;
    private DataWrapper dataWrapper;

    public ActivateProfileListAdapter(Fragment f, List<Profile> pl, DataWrapper dataWrapper)
    {
        fragment = f;
        profileList = pl;
        this.dataWrapper = dataWrapper;
    }

    public void release()
    {
        fragment = null;
        profileList = null;
    }

    public int getCount()
    {
        return profileList.size();
    }

    public Object getItem(int position)
    {
        return profileList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

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

    public void addItem(Profile profile)
    {
        int maxPOrder = 0;
        int pOrder;
        for (Profile p : profileList)
        {
            pOrder = p._porder;
            if (pOrder > maxPOrder) maxPOrder = pOrder;
        }
        profile._porder = maxPOrder+1;
        profileList.add(profile);
        notifyDataSetChanged();
    }

    public void updateItem(Profile profile)
    {
        notifyDataSetChanged();
    }

    public void deleteItem(Profile profile)
    {
        profileList.remove(profile);
        notifyDataSetChanged();
    }

    public void changeItemOrder(int from, int to)
    {
        Profile profile = profileList.get(from);
        profileList.remove(from);
        profileList.add(to, profile);
        for (int i = 0; i < profileList.size(); i++)
        {
            profileList.get(i)._porder = i+1;
        }
        notifyDataSetChanged();
    }

    public Profile getActivatedProfile()
    {
        for (Profile p : profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Profile profile : profileList) {
                dataWrapper.refreshProfileIcon(profile, false, 0);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
          ViewGroup listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          ImageView durationButton;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (!GlobalData.applicationActivatorGridLayout)
            {
                if (GlobalData.applicationActivatorPrefIndicator)
                    vi = inflater.inflate(R.layout.activate_profile_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.activate_profile_list_item_no_indicator, parent, false);
                holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
                if (GlobalData.applicationActivatorPrefIndicator)
                    holder.profileIndicator = (ImageView)vi.findViewById(R.id.act_prof_list_profile_pref_indicator);
                holder.durationButton = (ImageView)vi.findViewById(R.id.act_prof_list_item_duration);
            }
            else
            {
                vi = inflater.inflate(R.layout.activate_profile_grid_item, parent, false);
                holder.listItemRoot = (LinearLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
                holder.durationButton = (ImageView)vi.findViewById(R.id.act_prof_list_item_duration);
            }
            vi.setTag(holder);        
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        final Profile profile = profileList.get(position);

        if (profile._checked && (!GlobalData.applicationActivatorHeader))
        {
            if (GlobalData.applicationTheme.equals("material"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            else
            if (GlobalData.applicationTheme.equals("dark"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
            else
            if (GlobalData.applicationTheme.equals("dlight"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            holder.profileName.setTypeface(null, Typeface.BOLD);
        }
        else
        {
            if (GlobalData.applicationTheme.equals("material"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card);
            else
            if (GlobalData.applicationTheme.equals("dark"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card_dark);
            else
            if (GlobalData.applicationTheme.equals("dlight"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card);
            holder.profileName.setTypeface(null, Typeface.NORMAL);
        }

        String profileName = profile.getProfileNameWithDuration();
        holder.profileName.setText(profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        vi.getContext().getPackageName());
                holder.profileIcon.setImageResource(res); // resource na ikonu
            }
        }
        else
        {
            holder.profileIcon.setImageBitmap(profile._iconBitmap);
        }

        if ((GlobalData.applicationActivatorPrefIndicator) && (!GlobalData.applicationActivatorGridLayout))
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }

        if (profile._showDurationButton) {
            holder.durationButton.setVisibility(View.VISIBLE);
            if (GlobalData.applicationActivatorGridLayout) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.profileIcon.getLayoutParams();
                params.gravity = Gravity.LEFT | Gravity.START;
                holder.profileIcon.setLayoutParams(params);
            }
            holder.durationButton.setTag(position);
            holder.durationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
                    final Profile profile = (Profile) getItem(position);
                    FastAccessDurationDialog dialog = new FastAccessDurationDialog(fragment.getActivity(), profile, dataWrapper, GlobalData.STARTUP_SOURCE_ACTIVATOR);
                    dialog.show();
                }
            });
        }
        else {
            holder.durationButton.setVisibility(View.GONE);
            if (GlobalData.applicationActivatorGridLayout) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.profileIcon.getLayoutParams();
                params.gravity = Gravity.CENTER_HORIZONTAL;
                holder.profileIcon.setLayoutParams(params);
            }
        }

        return vi;
    }

}
