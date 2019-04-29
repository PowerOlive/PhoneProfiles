package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class EditorProfileListViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener, View.OnLongClickListener

{

    final DragHandle dragHandle;
    //private RelativeLayout listItemRoot;
    private final ImageView profileIcon;
    private final TextView profileName;
    private ImageView profileIndicator;
    private final AppCompatImageButton profileItemEditMenu;

    private Profile profile;
    private final EditorProfileListFragment editorFragment;

    private final Context context;

    EditorProfileListViewHolder(View itemView, EditorProfileListFragment editorFragment, Context context) {
        super(itemView);

        this.context = context;
        this.editorFragment = editorFragment;

        dragHandle = itemView.findViewById(R.id.main_list_drag_handle);
        profileName = itemView.findViewById(R.id.main_list_item_profile_name);
        profileIcon = itemView.findViewById(R.id.main_list_item_profile_icon);
        profileItemEditMenu = itemView.findViewById(R.id.main_list_item_edit_menu);
        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
            profileIndicator = itemView.findViewById(R.id.main_list_profile_pref_indicator);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    void bindProfile(Profile profile) {

        this.profile = profile;

        boolean isPermissionGranted = Permissions.checkProfilePermissions(context, profile).size() == 0;

        if (profile._checked && (!ApplicationPreferences.applicationEditorHeader(context)))
        {
            profileName.setTypeface(null, Typeface.BOLD);
            profileName.setTextSize(16);
            profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(context));
        }
        else
        if ((!isPermissionGranted) ||
                (Profile.isProfilePreferenceAllowed("-", profile, null, true, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
            profileName.setTypeface(null, Typeface.NORMAL);
            profileName.setTextSize(15);
            profileName.setTextColor(Color.RED);
        }
        else
        {
            profileName.setTypeface(null, Typeface.NORMAL);
            profileName.setTextSize(15);
            profileName.setTextColor(GlobalGUIRoutines.getThemeTextColor(context));
        }

        Spannable _profileName = profile.getProfileNameWithDuration("",true, context);
        profileName.setText(_profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                //int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                //        context.getPackageName());
                int res = Profile.getIconResource(profile.getIconIdentifier());
                profileIcon.setImageResource(res);
            }
        }
        else
        {
            profileIcon.setImageBitmap(profile._iconBitmap);
        }

        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            if (profileIndicator != null)
                profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }

        profileItemEditMenu.setTag(profile);
        profileItemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                editorFragment.showEditMenu(profileItemEditMenu);
            }
        });

    }

    @Override
    public void onClick(View v) {
        editorFragment.startProfilePreferencesActivity(profile, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        editorFragment.activateProfile(profile);
        return true;
    }

}
