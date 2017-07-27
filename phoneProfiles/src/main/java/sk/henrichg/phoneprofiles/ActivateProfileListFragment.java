package sk.henrichg.phoneprofiles;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivateProfileListFragment extends Fragment {

    private DataWrapper dataWrapper;
    private ActivateProfileHelper activateProfileHelper;
    private List<Profile> profileList = null;
    private ActivateProfileListAdapter profileListAdapter = null;
    private ListView listView = null;
    private GridView gridView = null;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;

    private int startupSource = 0;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    private static final String START_TARGET_HELPS_ARGUMENT = "start_target_helps";

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "activate_profile_list_fragment_start_target_helps";

    public static int PORDER_FOR_IGNORED_PROFILE = 1000000;

    public ActivateProfileListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);
        activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(dataWrapper, getActivity().getApplicationContext());

        Intent intent = getActivity().getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;

        if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context))
        {
            if (ApplicationPreferences.applicationActivatorPrefIndicator(dataWrapper.context) && ApplicationPreferences.applicationActivatorHeader(dataWrapper.context))
                rootView = inflater.inflate(R.layout.activate_profile_list, container, false);
            else
            if (ApplicationPreferences.applicationActivatorHeader(dataWrapper.context))
                rootView = inflater.inflate(R.layout.activate_profile_list_no_indicator, container, false);
            else
                rootView = inflater.inflate(R.layout.activate_profile_list_no_header, container, false);
        }
        else
        {
            if (ApplicationPreferences.applicationActivatorPrefIndicator(dataWrapper.context) && ApplicationPreferences.applicationActivatorHeader(dataWrapper.context))
                rootView = inflater.inflate(R.layout.activate_profile_grid, container, false);
            else
            if (ApplicationPreferences.applicationActivatorHeader(dataWrapper.context))
                rootView = inflater.inflate(R.layout.activate_profile_grid_no_indicator, container, false);
            else
                rootView = inflater.inflate(R.layout.activate_profile_grid_no_header, container, false);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view, savedInstanceState);

        boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
        if (startTargetHelps)
            showTargetHelps();
    }

    private void doOnViewCreated(View view, Bundle savedInstanceState)
    {
        activeProfileName = (TextView)view.findViewById(R.id.act_prof_activated_profile_name);
        activeProfileIcon = (ImageView)view.findViewById(R.id.act_prof_activated_profile_icon);
        if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context))
            listView = (ListView)view.findViewById(R.id.act_prof_profiles_list);
        else
            gridView = (GridView)view.findViewById(R.id.act_prof_profiles_grid);

        AbsListView absListView;
        if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context))
            absListView = listView;
        else
            absListView = gridView;

        //absListView.setLongClickable(false);

        absListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!ApplicationPreferences.applicationLongClickActivation(dataWrapper.context))
                    //activateProfileWithAlert(position);
                    activateProfile(position, PPApplication.STARTUP_SOURCE_ACTIVATOR);

            }

        });

        absListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (ApplicationPreferences.applicationLongClickActivation(dataWrapper.context))
                    //activateProfileWithAlert(position);
                    activateProfile(position, PPApplication.STARTUP_SOURCE_ACTIVATOR);

                return false;
            }

        });

        //absListView.setRemoveListener(onRemove);

        if (profileList == null)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
        }
        else
        {
            absListView.setAdapter(profileListAdapter);

            doOnStart();
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<ActivateProfileListFragment> fragmentWeakRef;
        private DataWrapper dataWrapper;

        private class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res = lhs._porder - rhs._porder;
                return res;
            }
        }

        private LoadProfileListAsyncTask (ActivateProfileListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Profile> profileList = dataWrapper.getProfileList();
            if (ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context)) {
                int modulo = profileList.size() % 3;
                if (modulo > 0) {
                    for (int i = 0; i < 3 - modulo; i++)
                        profileList.add(DataWrapper.getNonInitializedProfile(
                                dataWrapper.context.getResources().getString(R.string.profile_name_default),
                                Profile.PROFILE_ICON_DEFAULT, PORDER_FOR_IGNORED_PROFILE));
                }
            }
            Collections.sort(profileList, new ProfileComparator());
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            final ActivateProfileListFragment fragment = this.fragmentWeakRef.get();
            
            if ((fragment != null) && (fragment.isAdded())) {

                // get local profileList
                List<Profile> profileList = dataWrapper.getProfileList();
                // set copy local profile list into activity profilesDataWrapper
                fragment.dataWrapper.setProfileList(profileList, false);
                // set reference of profile list from profilesDataWrapper
                fragment.profileList = fragment.dataWrapper.getProfileList();

                if (fragment.profileList.size() == 0)
                {
                    // nie je ziaden profile, startneme Editor

                    fragment.doOnStart();

                    Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorProfilesActivity.class);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR_START);
                    fragment.getActivity().startActivity(intent);

                    fragment.getActivity().finish();

                    return;
                }

                fragment.profileListAdapter = new ActivateProfileListAdapter(fragment, fragment.profileList, fragment.dataWrapper);

                AbsListView absListView;
                if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context))
                    absListView = fragment.listView;
                else
                    absListView = fragment.gridView;
                absListView.setAdapter(fragment.profileListAdapter);

                fragment.doOnStart();

                final Handler handler = new Handler(fragment.getActivity().getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fragment.getActivity() != null)
                            ((ActivateProfileActivity)fragment.getActivity()).startTargetHelpsActivity();
                    }
                }, 1000);

            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    private void doOnStart()
    {
        if (!PPApplication.getApplicationStarted(getActivity().getApplicationContext(), false))
        {
            PPApplication.logE("ActivateProfileListFragment.doOnStart","application not started");

            // start service for first start
            //Intent firstStartServiceIntent = new Intent(getActivity().getApplicationContext(), FirstStartService.class);
            //getActivity().startService(firstStartServiceIntent);

            // start PhoneProfilesService
            getActivity().startService(new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class));
        }
        else
        {
            PPApplication.logE("ActivateProfileListFragment.doOnStart", "xxx");

            if (PhoneProfilesService.instance == null) {
                // start PhoneProfilesService
                getActivity().startService(new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class));
            }

            Profile profile = dataWrapper.getActivatedProfile();
            updateHeader(profile);
            setProfileSelection(profile, false);
            if (startupSource == 0)
            {
                // aktivita nebola spustena z notifikacie, ani z widgetu
                // pre profil, ktory je prave aktivny, treba aktualizovat notifikaciu a widgety
                activateProfileHelper.showNotification(profile);
                activateProfileHelper.updateWidget(true);
            }
        }
        endOnStart();

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onStart");

    }

    private void endOnStart()
    {
        // reset, aby sa to dalej chovalo ako normalne spustenie z lauchera
        startupSource = 0;

        //  aplikacia uz je 1. krat spustena - moved to FirstStartService
        //PPApplication.setApplicationStarted(getActivity().getApplicationContext(), true);
    }


    @Override
    public void onDestroy()
    {
        if (!isAsyncTaskPendingOrRunning())
        {
            AbsListView absListView;
            if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context))
                absListView = listView;
            else
                absListView = gridView;
            if (absListView != null)
                absListView.setAdapter(null);
            if (profileListAdapter != null)
                profileListAdapter.release();

            profileList = null;

            activateProfileHelper = null;
            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            dataWrapper = null;
        }

        super.onDestroy();
    }

    private void updateHeader(Profile profile)
    {
        if (!ApplicationPreferences.applicationActivatorHeader(dataWrapper.context))
            return;

        if (profile == null)
        {
            activeProfileName.setText(getResources().getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            activeProfileName.setText(profile.getProfileNameWithDuration(false, dataWrapper.context));
            if (profile.getIsIconResourceID())
            {
                if (profile.getUseCustomColorForIcon())
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().getPackageName());
                    activeProfileIcon.setImageResource(res); // resource na ikonu
                }
            }
            else
            {
                activeProfileIcon.setImageBitmap(profile._iconBitmap);
            }
        }

        if (ApplicationPreferences.applicationActivatorPrefIndicator(dataWrapper.context))
        {
            ImageView profilePrefIndicatorImageView = (ImageView)getActivity().findViewById(R.id.act_prof_activated_profile_pref_indicator);
            if (profilePrefIndicatorImageView != null)
            {
                if (profile == null)
                    profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
                else {
                    if (profile._preferencesIndicator != null)
                        profilePrefIndicatorImageView.setImageBitmap(profile._preferencesIndicator);
                    else
                        profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
                }
            }
        }
    }

    private void activateProfile(Profile profile, int startupSource)
    {
        if ((dataWrapper == null) || (profile == null))
            return;

        if (profile._porder != PORDER_FOR_IGNORED_PROFILE)
            dataWrapper.activateProfile(profile._id, startupSource, getActivity());
    }

    private void activateProfile(int position, int startupSource)
    {
        Profile profile = profileList.get(position);
        activateProfile(profile, startupSource);
    }

    private void setProfileSelection(Profile profile, boolean refreshIcons) {
        if (profileListAdapter != null)
        {
            int profilePos;

            if (profile != null)
                profilePos = profileListAdapter.getItemPosition(profile);
            else {
                if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context))
                    profilePos = listView.getCheckedItemPosition();
                else
                    profilePos = gridView.getCheckedItemPosition();
            }

            profileListAdapter.notifyDataSetChanged(refreshIcons);

            if ((!ApplicationPreferences.applicationActivatorHeader(dataWrapper.context)) && (profilePos != ListView.INVALID_POSITION))
            {
                // set profile visible in list
                if (!ApplicationPreferences.applicationActivatorGridLayout(dataWrapper.context)) {
                    listView.setItemChecked(profilePos, true);
                    int last = listView.getLastVisiblePosition();
                    int first = listView.getFirstVisiblePosition();
                    if ((profilePos <= first) || (profilePos >= last)) {
                        listView.setSelection(profilePos);
                        //listView.smoothScrollToPosition(profilePos);
                    }
                }
                else {
                    gridView.setItemChecked(profilePos, true);
                    int last = gridView.getLastVisiblePosition();
                    int first = gridView.getFirstVisiblePosition();
                    if ((profilePos <= first) || (profilePos >= last)) {
                        gridView.setSelection(profilePos);
                        //listView.smoothScrollToPosition(profilePos);
                    }
                }
            }
        }
    }

    public void refreshGUI(boolean refreshIcons)
    {
        synchronized (PPApplication.refreshActivatorListMutex) {
            if ((dataWrapper == null) || (profileListAdapter == null))
                return;

            Profile profileFromAdapter = profileListAdapter.getActivatedProfile();

            if (profileFromAdapter != null) {
                profileFromAdapter._checked = false;
                if (refreshIcons) {
                    dataWrapper.refreshProfileIcon(profileFromAdapter, false, 0);
                }
            }

            Profile profileFromDB = dataWrapper.getDatabaseHandler().getActivatedProfile();
            if (profileFromDB != null) {
                Profile profileFromDataWrapper = dataWrapper.getProfileById(profileFromDB._id);
                if (profileFromDataWrapper != null) {
                    profileFromDataWrapper._checked = true;
                    if (refreshIcons) {
                        dataWrapper.refreshProfileIcon(profileFromDataWrapper, false, 0);
                    }
                }
                updateHeader(profileFromDataWrapper);
                setProfileSelection(profileFromDataWrapper, refreshIcons);
            } else {
                updateHeader(null);
                setProfileSelection(null, refreshIcons);
            }

            profileListAdapter.notifyDataSetChanged(refreshIcons);
        }
    }

    void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (getActivity() == null)
            return;

        if (((ActivateProfileActivity)getActivity()).targetHelpsSequenceStarted)
            return;

        ApplicationPreferences.getSharedPreferences(getActivity());

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true)) {

            //Log.d("ActivateProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {

                //Log.d("ActivateProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                showAdapterTargetHelps();
            }
            else {
                //Log.d("ActivateProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAdapterTargetHelps();
                    }
                }, 500);
            }
        }
        else {
            final Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivatorTargetHelpsActivity.activity != null) {
                        //Log.d("ActivateProfileListFragment.showTargetHelps", "finish activity");
                        ActivatorTargetHelpsActivity.activity.finish();
                        ActivatorTargetHelpsActivity.activity = null;
                    }
                }
            }, 500);
        }
    }

    private void showAdapterTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (getActivity() == null)
            return;

        View itemView;
        if (!ApplicationPreferences.applicationActivatorGridLayout(getActivity())) {
            if (listView.getChildCount() > 1)
                itemView = listView.getChildAt(1);
            else
                itemView = listView.getChildAt(0);
        }
        else {
            if (gridView.getChildCount() > 1)
                itemView = gridView.getChildAt(1);
            else
                itemView = gridView.getChildAt(0);
        }
        //Log.d("ActivateProfileListFragment.showAdapterTargetHelps", "profileListAdapter="+profileListAdapter);
        //Log.d("ActivateProfileListFragment.showAdapterTargetHelps", "itemView="+itemView);
        if ((profileListAdapter != null) && (itemView != null))
            profileListAdapter.showTargetHelps(getActivity(), this,  itemView);
        else {
            final Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivatorTargetHelpsActivity.activity != null) {
                        //Log.d("ActivateProfileListFragment.showAdapterTargetHelps", "finish activity");
                        ActivatorTargetHelpsActivity.activity.finish();
                        ActivatorTargetHelpsActivity.activity = null;
                    }
                }
            }, 500);
        }
    }

}
