package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;

class ApplicationsMultiSelectPreferenceAdapter extends BaseAdapter implements Scrollable
{
    private LayoutInflater inflater;
    private Context context;

    private boolean noShortcuts;

    ApplicationsMultiSelectPreferenceAdapter(Context context, int addShortcuts)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;

        noShortcuts = addShortcuts == 0;
    }

    public int getCount() {
        return EditorProfilesActivity.getApplicationsCache().getLength(noShortcuts);
    }

    public Object getItem(int position) {
        return EditorProfilesActivity.getApplicationsCache().getApplication(position, noShortcuts);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ApplicationsCache applicationsCache = EditorProfilesActivity.getApplicationsCache();

        // Application to display
        Application application = applicationsCache.getApplication(position, noShortcuts);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        ImageView imageViewIcon;
        TextView textViewAppName;
        CheckBox checkBox;
        TextView textViewAppType;

        // Create a new row view
        if (convertView == null)
        {
            if (noShortcuts)
                convertView = inflater.inflate(R.layout.applications_multiselect_preference_ns_list_item, parent, false);
            else
                convertView = inflater.inflate(R.layout.applications_multiselect_preference_list_item, parent, false);

            // Find the child views.
            imageViewIcon = convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_icon);
            textViewAppName = convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_name);
            checkBox = convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_checkbox);
            if (noShortcuts)
                textViewAppType = null;
            else
                textViewAppType = convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_type);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ApplicationViewHolder(imageViewIcon, textViewAppName, textViewAppType, checkBox));

            // If CheckBox is toggled, update the Application it is tagged with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Application application = (Application) cb.getTag();
                    application.checked = cb.isChecked();
                }
            });
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            ApplicationViewHolder viewHolder = (ApplicationViewHolder) convertView.getTag();
            imageViewIcon = viewHolder.imageViewIcon;
            textViewAppName = viewHolder.textViewAppName;
            checkBox = viewHolder.checkBox;
            textViewAppType = viewHolder.textViewAppType;
        }

        // Tag the CheckBox with the Application it is displaying, so that we
        // can
        // access the Application in onClick() when the CheckBox is toggled.
        checkBox.setTag(application);

        // Display Application data
        imageViewIcon.setImageBitmap(applicationsCache.getApplicationIcon(application, noShortcuts));
        textViewAppName.setText(application.appLabel);
        if (!noShortcuts) {
            if (application.shortcut)
                textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
            else
                textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));
        }

        checkBox.setChecked(application.checked);

        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
        Application application = (Application) getItem(childPosition);
        if (application.checked)
            return "*";
        else
            return application.appLabel.substring(0, 1);
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }
}
