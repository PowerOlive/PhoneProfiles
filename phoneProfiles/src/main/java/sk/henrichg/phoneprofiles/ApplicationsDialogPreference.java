package sk.henrichg.phoneprofiles;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsDialogPreference  extends DialogPreference
                                            implements OnStartDragItemListener {

    Context context;

    String value = "";

    List<Application> applicationsList = null;

    private MaterialDialog mDialog;
    private ApplicationEditorDialog mEditorDialog;

    private RecyclerView applicationsListView;
    private ItemTouchHelper itemTouchHelper;

    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    private ApplicationsDialogPreferenceAdapter listAdapter;

    private ImageView packageIcon;
    private RelativeLayout packageIcons;
    private ImageView packageIcon1;
    private ImageView packageIcon2;
    private ImageView packageIcon3;
    private ImageView packageIcon4;

    private DataWrapper dataWrapper;

    static final int RESULT_APPLICATIONS_EDITOR = 2100;

    public ApplicationsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        /*
        TypedArray applicationsType = context.obtainStyledAttributes(attrs,
                R.styleable.ApplicationsPreference, 0, 0);

        onlyEdit = applicationsType.getInt(R.styleable.ApplicationsPreference_onlyEdit, 0);

        applicationsType.recycle();
        */

        this.context = context;
        dataWrapper = new DataWrapper(context, false, false, 0);

        applicationsList = new ArrayList<>();

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

    @Override
    protected void showDialog(Bundle state) {

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_applications_pref_dialog, false);

        mBuilder.positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText());
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                if (shouldPersist())
                {
                    // sem narvi stringy aplikacii oddelenych |
                    value = "";
                    if (applicationsList != null)
                    {
                        for (Application application : applicationsList)
                        {
                            if (!value.isEmpty())
                                value = value + "|";
                            if (application.shortcut)
                                value = value + "(s)";
                            value = value + application.packageName + "/" + application.activityName;
                            if (application.shortcut && (application.shortcutId > 0))
                                value = value + "#" + application.shortcutId;
                        }
                    }

                    Log.d("----- ApplicationsDialogPreference.onPositive","value="+value);
                    persistString(value);

                    setIcons();
                    setSummaryAMSDP();
                }
                mDialog.dismiss();
            }
        });
        mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                mDialog.dismiss();
            }
        });

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsDialogPreference.this.onShow(dialog);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        AppCompatImageButton addButton = (AppCompatImageButton)layout.findViewById(R.id.applications_pref_dlg_add);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        applicationsListView = (RecyclerView) layout.findViewById(R.id.applications_pref_dlg_listview);
        applicationsListView.setLayoutManager(layoutManager);
        applicationsListView.setHasFixedSize(true);

        linlaProgress = (LinearLayout)layout.findViewById(R.id.applications_pref_dlg_linla_progress);
        rellaDialog = (RelativeLayout) layout.findViewById(R.id.applications_pref_dlg_rella_dialog);

        listAdapter = new ApplicationsDialogPreferenceAdapter(context, this, this);

        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(applicationsListView);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditor(null);
            }
        });

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

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
                rellaDialog.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().getApplicationsList(context);

                getValueAMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().clearCache(false);

                applicationsListView.setAdapter(listAdapter);
                rellaDialog.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);
            }

        }.execute();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        EditorProfilesActivity.getApplicationsCache().cancelCaching();
        if (!EditorProfilesActivity.getApplicationsCache().isCached())
            EditorProfilesActivity.getApplicationsCache().clearCache(false);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mEditorDialog != null && mEditorDialog.mDialog != null && mEditorDialog.mDialog.isShowing())
            mEditorDialog.mDialog.dismiss();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
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
            // sem narvi default string aplikacii oddeleny |
            value = "";
            persistString("");
        }
        setSummaryAMSDP();
    }

    private void getValueAMSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);
        //Log.d("ApplicationsDialogPreference.getValueAMSDP","value="+value);

        applicationsList.clear();

        List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
        if (cachedApplicationList != null)
        {
            String[] splits = value.split("\\|");
            for (String split : splits) {
                Application _application = null;
                for (Application application : cachedApplicationList) {
                    application.checked = false;

                    String packageName;
                    String activityName;
                    String shortcut;
                    String shortcutId = "";
                    String[] splits2 = split.split("/");
                    if (split.length() > 2) {
                        if (splits2.length == 2) {
                            shortcut = splits2[0].substring(0, 3);
                            packageName = splits2[0];
                            String[] splits3 = splits2[1].split("#");
                            activityName = splits3[0];
                            if (splits3.length == 2)
                                shortcutId = splits3[1];
                        } else {
                            shortcut = value.substring(0, 3);
                            packageName = value;
                            activityName = "";
                        }
                        if (shortcut.equals("(s)")) {
                            packageName = packageName.substring(3);
                        }
                        boolean shortcutPassed = shortcut.equals("(s)") == application.shortcut;
                        boolean packagePassed = packageName.equals(application.packageName);
                        boolean activityPassed = activityName.equals(application.activityName);

                        //Log.d("ApplicationsDialogPreference.getValueAMSDP","shortcut="+shortcut);
                        //Log.d("ApplicationsDialogPreference.getValueAMSDP","packageName="+packageName);
                        //Log.d("ApplicationsDialogPreference.getValueAMSDP","activityName="+activityName);

                        if (!activityName.isEmpty()) {
                            if (shortcutPassed && packagePassed && activityPassed) {
                                application.checked = true;
                                try {
                                    application.shortcutId = Long.parseLong(shortcutId);
                                } catch (Exception e) {
                                    application.shortcutId = 0;
                                }
                            }
                        } else {
                            if (!shortcut.equals("(s)")) {
                                if (packagePassed && (!application.shortcut))
                                    application.checked = true;
                            }
                        }
                        _application = application;
                        if (_application.checked)
                            break;
                    }
                }
                if ((_application != null) && _application.checked) {
                    Application newInfo = new Application();

                    newInfo.shortcut = _application.shortcut;
                    newInfo.appLabel = _application.appLabel;
                    newInfo.packageName = _application.packageName;
                    newInfo.activityName = _application.activityName;
                    newInfo.icon = _application.icon;
                    newInfo.shortcutId = _application.shortcutId;

                    //Log.d("ApplicationsDialogPreference.getValueAMSDP","app="+newInfo.appLabel);
                    applicationsList.add(newInfo);
                }
            }
        }
    }

    private void setSummaryAMSDP()
    {
        String prefSummary = context.getString(R.string.applications_multiselect_summary_text_not_selected);
        if (!value.isEmpty() && !value.equals("-")) {
            String[] splits = value.split("\\|");
            prefSummary = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
            if (splits.length == 1) {
                PackageManager packageManager = context.getPackageManager();
                if (!ApplicationsCache.isShortcut(splits[0])) {
                    if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
                        ApplicationInfo app;
                        try {
                            app = packageManager.getApplicationInfo(splits[0], 0);
                            if (app != null)
                                prefSummary = packageManager.getApplicationLabel(app).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            //e.printStackTrace();
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            prefSummary = info.loadLabel(packageManager).toString();
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null) {
                        long shortcutId = ApplicationsCache.getShortcutId(splits[0]);
                        if (shortcutId > 0) {
                            Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(shortcutId);
                            if (shortcut != null)
                                prefSummary = shortcut._name;
                        }
                        else
                            prefSummary = info.loadLabel(packageManager).toString();
                    }
                }
            }
        }
        setSummary(prefSummary);
    }

    private void setIcons() {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo app;

        String[] splits = value.split("\\|");

        if (!value.isEmpty() && !value.equals("-")) {
            if (splits.length == 1) {
                packageIcon.setVisibility(View.VISIBLE);
                packageIcon1.setImageResource(R.drawable.ic_empty);
                packageIcon2.setImageResource(R.drawable.ic_empty);
                packageIcon3.setImageResource(R.drawable.ic_empty);
                packageIcon4.setImageResource(R.drawable.ic_empty);
                packageIcons.setVisibility(View.GONE);

                if (!ApplicationsCache.isShortcut(splits[0])) {
                    if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
                        try {
                            app = packageManager.getApplicationInfo(splits[0], 0);
                            if (app != null) {
                                Drawable icon = packageManager.getApplicationIcon(app);
                                //CharSequence name = packageManager.getApplicationLabel(app);
                                packageIcon.setImageDrawable(icon);
                            } else {
                                packageIcon.setImageResource(R.drawable.ic_empty);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            //e.printStackTrace();
                            packageIcon.setImageResource(R.drawable.ic_empty);
                        }
                    } else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            packageIcon.setImageDrawable(info.loadIcon(packageManager));
                        else
                            packageIcon.setImageResource(R.drawable.ic_empty);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null)
                        packageIcon.setImageDrawable(info.loadIcon(packageManager));
                    else
                        packageIcon.setImageResource(R.drawable.ic_empty);
                }
            } else {
                packageIcons.setVisibility(View.VISIBLE);
                packageIcon.setVisibility(View.GONE);
                packageIcon.setImageResource(R.drawable.ic_empty);

                ImageView packIcon = packageIcon1;
                for (int i = 0; i < 4; i++) {
                    if (i == 0) packIcon = packageIcon1;
                    if (i == 1) packIcon = packageIcon2;
                    if (i == 2) packIcon = packageIcon3;
                    if (i == 3) packIcon = packageIcon4;
                    if (i < splits.length) {
                        Log.d("----- ApplicationsDialogPreference.setIcons", "splits[i]=" + splits[i]);
                        if (!ApplicationsCache.isShortcut(splits[i])) {
                            Log.d("----- ApplicationsDialogPreference.setIcons", "not shortcut");
                            if (ApplicationsCache.getActivityName(splits[i]).isEmpty()) {
                                Log.d("----- ApplicationsDialogPreference.setIcons", "activity name is empty");
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
                            } else {
                                Log.d("----- ApplicationsDialogPreference.setIcons", "activity name is not empty");
                                Intent intent = new Intent();
                                intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                                if (info != null) {
                                    packIcon.setImageDrawable(info.loadIcon(packageManager));
                                } else {
                                    packIcon.setImageResource(R.drawable.ic_empty);
                                }
                            }
                        } else {
                            Log.d("----- ApplicationsDialogPreference.setIcons", "shortcut");
                            Intent intent = new Intent();
                            intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                            if (info != null) {
                                packIcon.setImageDrawable(info.loadIcon(packageManager));
                            } else {
                                packIcon.setImageResource(R.drawable.ic_empty);
                            }
                        }
                    } else
                        packIcon.setImageResource(R.drawable.ic_empty);
                }
            }
        }
        else {
            packageIcon.setVisibility(View.VISIBLE);
            packageIcons.setVisibility(View.GONE);
            packageIcon.setImageResource(R.drawable.ic_empty);
        }
    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup;
        if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
        else
            popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.applications_pref_dlg_item_edit, popup.getMenu());

        final Application application = (Application) view.getTag();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.applications_pref_dlg_item_menu_edit:
                        startEditor(application);
                        return true;
                    case R.id.applications_pref_dlg_item_menu_delete:
                        deleteApplication(application);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }

    void startEditor(Application application) {
        mEditorDialog = new ApplicationEditorDialog(context, this, application);
        mEditorDialog.show();
    }

    private void deleteApplication(Application application) {

        if (application.shortcutId > 0)
            dataWrapper.getDatabaseHandler().deleteShortcut(application.shortcutId);

        applicationsList.remove(application);
        listAdapter.notifyDataSetChanged();
    }

    void updateApplication(Application application, int positionInEditor) {
        List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
        if (cachedApplicationList != null) {
            int _position = applicationsList.indexOf(application);
            Application cachedApplication = cachedApplicationList.get(positionInEditor);
            Application editedApplication = application;
            if (editedApplication == null) {
                //Log.d("ApplicationsDialogPreference.updateApplication", "add");
                editedApplication = new Application();
                applicationsList.add(editedApplication);
                _position = applicationsList.size()-1;
            }
            editedApplication.shortcut = cachedApplication.shortcut;
            editedApplication.appLabel = cachedApplication.appLabel;
            editedApplication.packageName = cachedApplication.packageName;
            editedApplication.activityName = cachedApplication.activityName;
            editedApplication.icon = cachedApplication.icon;
            if (!editedApplication.shortcut)
                editedApplication.shortcutId = 0;

            listAdapter.notifyDataSetChanged();

            if (editedApplication.shortcut &&
                (editedApplication.packageName != null)) {
                Intent intent = new Intent(context, LaunchShortcutActivity.class);
                intent.putExtra(LaunchShortcutActivity.EXTRA_PACKAGE_NAME, editedApplication.packageName);
                intent.putExtra(LaunchShortcutActivity.EXTRA_ACTIVITY_NAME, editedApplication.activityName);
                intent.putExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, _position);

                ProfilePreferencesFragment.setApplicationsDialogPreference(this);
                ((Activity)context).startActivityForResult(intent, RESULT_APPLICATIONS_EDITOR);
            }
        }
    }

    void updateShortcut(Intent shortcutIntent, String shortcutName, int position) {
        /* Storing Intent to SQLite ;-)
        You can simply store the intent in a String way:

        String intentDescription = intent.toUri(0);
        //Save the intent string into your database

        Later you can restore the Intent:

        String intentDescription = cursor.getString(intentIndex);
        Intent intent = Intent.parseUri(intentDescription, 0);
        */

        String intentDescription = shortcutIntent.toUri(0);

        Application application = applicationsList.get(position);
        Shortcut shortcut = new Shortcut();
        shortcut._intent = intentDescription;
        shortcut._name = shortcutName;
        if (application.shortcutId > 0) {
            dataWrapper.getDatabaseHandler().deleteShortcut(application.shortcutId);
        }
        dataWrapper.getDatabaseHandler().addShortcut(shortcut);
        application.shortcutId = shortcut._id;

        listAdapter.notifyDataSetChanged();
    }
}
