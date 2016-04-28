package sk.henrichg.phoneprofiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class ApplicationsMultiSelectDialogPreference extends DialogPreference
{

    Context _context = null;
    String value = "";

    // Layout widgets.
    private ListView listView = null;
    private LinearLayout linlaProgress;
    private LinearLayout linlaListView;

    ImageView packageIcon;
    RelativeLayout packageIcons;
    ImageView packageIcon1;
    ImageView packageIcon2;
    ImageView packageIcon3;
    ImageView packageIcon4;

    private ApplicationsMultiselectPreferenceAdapter listAdapter;

    public ApplicationsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        packageIcon = (ImageView)view.findViewById(R.id.applications_pref_icon);
        packageIcons = (RelativeLayout)view.findViewById(R.id.applications_pref_icons);
        packageIcon1 = (ImageView)view.findViewById(R.id.applications_pref_icon1);
        packageIcon2 = (ImageView)view.findViewById(R.id.applications_pref_icon2);
        packageIcon3 = (ImageView)view.findViewById(R.id.applications_pref_icon3);
        packageIcon4 = (ImageView)view.findViewById(R.id.applications_pref_icon4);

        setIcons();
    }

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (shouldPersist())
                        {
                            // sem narvi stringy kontatkov oddelenych |
                            value = "";
                            List<Application> applicationList = EditorProfilesActivity.getApplicationsCache().getList();
                            if (applicationList != null)
                            {
                                for (Application application : applicationList)
                                {
                                    if (application.checked)
                                    {
                                        if (!value.isEmpty())
                                            value = value + "|";
                                        if (application.shortcut)
                                            value = value + "(s)" + application.packageName + "/" + application.activityName;
                                        else
                                            value = value + application.packageName;
                                    }
                                }
                            }
                            persistString(value);

                            setIcons();
                            setSummaryAMSDP();
                        }
                    }
                })
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_applications_multiselect_pref_dialog, null);
        onBindDialogView(layout);

        linlaProgress = (LinearLayout)layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        linlaListView = (LinearLayout)layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_listview);
        listView = (ListView)layout.findViewById(R.id.applications_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                Application application = (Application)listAdapter.getItem(position);
                application.toggleChecked();
                ApplicationViewHolder viewHolder = (ApplicationViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(application.checked);
            }
        });

        listAdapter = new ApplicationsMultiselectPreferenceAdapter(_context);

        mBuilder.customView(layout, false);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsMultiSelectDialogPreference.this.onShow(dialog);
            }
        });

        MaterialDialog mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    public void onShow(DialogInterface dialog) {

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                linlaListView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().getApplicationsList(_context);

                getValueAMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().clearCache(false);

                listView.setAdapter(listAdapter);
                linlaListView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);
            }

        }.execute();
    }

    public void onDismiss (DialogInterface dialog)
    {
        EditorProfilesActivity.getApplicationsCache().cancelCaching();

        if (!EditorProfilesActivity.getApplicationsCache().isCached())
            EditorProfilesActivity.getApplicationsCache().clearCache(false);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueAMSDP();
        }
        else {
            // set state
            // sem narvi default string kontaktov oddeleny |
            value = "";
            persistString("");
        }
        setSummaryAMSDP();
    }

    private void getValueAMSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);

        // change checked state by value
        List<Application> applicationList = EditorProfilesActivity.getApplicationsCache().getList();
        if (applicationList != null)
        {
            String[] splits = value.split("\\|");
            for (Application application : applicationList)
            {
                application.checked = false;
                for (int i = 0; i < splits.length; i++)
                {
                    String packageName = splits[i];
                    if (application.shortcut && (packageName.length() > 2)) {
                        String shortcut = packageName.substring(0, 3);
                        String[] splits2 = packageName.split("/");
                        if (shortcut.equals("(s)")) {
                            packageName = splits2[0].substring(3);
                            String activityName = splits2[1];
                            if (packageName.equals(application.packageName) &&
                                    activityName.equals(application.activityName))
                                application.checked = true;
                        }
                    }
                    else {
                        if (packageName.equals(application.packageName)) {
                            application.checked = true;
                        }
                    }
                }
            }
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < applicationList.size()) {
                Application application = applicationList.get(i);
                if (application.checked) {
                    applicationList.remove(i);
                    applicationList.add(ich, application);
                    ich++;
                }
                i++;
            }
        }
    }

    private void setSummaryAMSDP()
    {
        String prefVolumeDataSummary = _context.getString(R.string.applications_multiselect_summary_text_not_selected);
        if (!value.isEmpty() && !value.equals("-")) {
            String[] splits = value.split("\\|");
            prefVolumeDataSummary = _context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
            if (splits.length == 1) {
                PackageManager packageManager = _context.getPackageManager();
                if (!ApplicationsCache.isShortcut(splits[0])) {
                    ApplicationInfo app;
                    try {
                        app = packageManager.getApplicationInfo(splits[0], 0);
                        if (app != null)
                            prefVolumeDataSummary = packageManager.getApplicationLabel(app).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        //e.printStackTrace();
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null)
                        prefVolumeDataSummary = info.loadLabel(packageManager).toString();
                }
            }
        }
        setSummary(prefVolumeDataSummary);
    }

    private void setIcons() {
        PackageManager packageManager = _context.getPackageManager();
        ApplicationInfo app;

        String[] splits = value.split("\\|");

        if (splits.length == 1) {
            packageIcon.setVisibility(View.VISIBLE);
            packageIcons.setVisibility(View.GONE);

            if (!ApplicationsCache.isShortcut(splits[0])) {
                try {
                    app = packageManager.getApplicationInfo(splits[0], 0);
                    if (app != null) {
                        Drawable icon = packageManager.getApplicationIcon(app);
                        //CharSequence name = packageManager.getApplicationLabel(app);
                        packageIcon.setImageDrawable(icon);
                    } else {
                        packageIcon.setImageResource(R.drawable.ic_empty);
                    }
                }
                catch (PackageManager.NameNotFoundException e) {
                    //e.printStackTrace();
                    packageIcon.setImageResource(R.drawable.ic_empty);
                }
            }
            else {
                Intent intent = new Intent();
                intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                if (info != null)
                    packageIcon.setImageDrawable(info.loadIcon(packageManager));
                else
                    packageIcon.setImageResource(R.drawable.ic_empty);
            }
        }
        else {
            packageIcon.setVisibility(View.GONE);
            packageIcons.setVisibility(View.VISIBLE);
            packageIcon.setImageResource(R.drawable.ic_empty);

            ImageView packIcon = packageIcon1;
            for (int i = 0; i < 4; i++)
            {
                if (i == 0) packIcon = packageIcon1;
                if (i == 1) packIcon = packageIcon2;
                if (i == 2) packIcon = packageIcon3;
                if (i == 3) packIcon = packageIcon4;
                if (i < splits.length) {

                    if (!ApplicationsCache.isShortcut(splits[i])) {
                        try {
                            app = packageManager.getApplicationInfo(splits[i], 0);
                            if (app != null) {
                                Drawable icon = packageManager.getApplicationIcon(app);
                                //CharSequence name = packageManager.getApplicationLabel(app);
                                packIcon.setImageDrawable(icon);
                            } else {
                                packIcon.setImageResource(R.drawable.ic_empty);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            //e.printStackTrace();
                            packIcon.setImageResource(R.drawable.ic_empty);
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                        if (info != null) {
                            packIcon.setImageDrawable(info.loadIcon(packageManager));
                        } else {
                            packIcon.setImageResource(R.drawable.ic_empty);
                        }
                    }
                }
                else
                    packIcon.setImageResource(R.drawable.ic_empty);
            }
        }
    }
}
