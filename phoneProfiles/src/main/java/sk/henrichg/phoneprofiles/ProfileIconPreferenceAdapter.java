package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class ProfileIconPreferenceAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private String imageIdentifier;
    private boolean isImageResourceID;
    private boolean useCustomColor;
    private int customColor;

    ProfileIconPreferenceAdapter(Context c, String imageIdentifier, boolean isImageResourceID, boolean useCustomColor, int customColor)
    {
        context = c;

        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.imageIdentifier = imageIdentifier;
        this.isImageResourceID = isImageResourceID;
        this.useCustomColor = useCustomColor;
        this.customColor = customColor;
    }

    public int getCount() {
        return Profile.profileIconId.length;
    }

    public Object getItem(int position) {
        return Profile.profileIconId[position];
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView icon;
        //int position;
    }

    @SuppressLint("PrivateResource")
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.profileicon_preference_gridview_item, parent, false);
            holder = new ViewHolder();
            holder.icon = vi.findViewById(R.id.profileicon_preference_gridview_item_icon);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        String iconResName = context.getResources().getResourceEntryName(Profile.profileIconId[position]);
        if (iconResName.equals(imageIdentifier) && isImageResourceID) {
            if (Build.VERSION.SDK_INT >= 21)
                holder.icon.setBackgroundColor(GlobalGUIRoutines.getThemeColorControlHighlight(context));
            else {
                if (ApplicationPreferences.applicationTheme(context).equals("dark"))
                    holder.icon.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_dark);
                else
                    holder.icon.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_light);
            }
        }
        else
            holder.icon.setBackgroundResource(0);

        int iconRes = Profile.profileIconId[position];
        if (iconResName.equals(imageIdentifier) && isImageResourceID && useCustomColor) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconRes);
            bitmap = BitmapManipulator.recolorBitmap(bitmap, customColor);
            holder.icon.setImageBitmap(bitmap);
        }
        else
            holder.icon.setImageResource(iconRes);

        return vi;
    }

    void imageIdentifierAndTypeChanged(String imageIdentifier, boolean isImageResourceID) {
        if (!this.imageIdentifier.equals(imageIdentifier)) {
            this.useCustomColor = false;
            this.customColor = 0;
        }
        this.imageIdentifier = imageIdentifier;
        this.isImageResourceID = isImageResourceID;
        notifyDataSetChanged();
    }

    static int getImageResourcePosition(String imageIdentifier, Context context) {
        for (int pos = 0; pos < Profile.profileIconId.length; pos++) {
            String resName = context.getResources().getResourceEntryName(Profile.profileIconId[pos]);
            if (resName.equals(imageIdentifier))
                return pos;
        }
        return 0;
    }

    void setCustomColor(boolean newUseCustomColor, int newCustomColor) {
        useCustomColor = newUseCustomColor;
        customColor = newCustomColor;
        notifyDataSetChanged();
    }

    static int getIconColor(String imageIdentifier, Context context) {
        return Profile.profileIconColor[getImageResourcePosition(imageIdentifier, context)];
    }

}
