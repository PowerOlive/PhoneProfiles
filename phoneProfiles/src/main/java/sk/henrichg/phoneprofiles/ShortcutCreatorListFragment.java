package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;

public class ShortcutCreatorListFragment extends Fragment {

    private DataWrapper activityDataWrapper;
    private ShortcutCreatorListAdapter profileListAdapter;
    private ListView listView;
    TextView textViewNoData;
    private LinearLayout progressBar;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    public ShortcutCreatorListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        //noinspection ConstantConditions
        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.shortcut_creator_list, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view/*, savedInstanceState*/);
    }

    private void doOnViewCreated(View view/*, Bundle savedInstanceState*/)
    {
        listView = view.findViewById(R.id.shortcut_profiles_list);
        textViewNoData = view.findViewById(R.id.shortcut_profiles_list_empty);
        progressBar = view.findViewById(R.id.shortcut_profiles_list_linla_progress);

        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                createShortcut(position);

            }

        });

        if (!activityDataWrapper.profileListFilled)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
        }
        else
        {
            listView.setAdapter(profileListAdapter);
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<ShortcutCreatorListFragment> fragmentWeakRef;
        private final DataWrapper dataWrapper;

        final boolean applicationActivatorPrefIndicator;

        private class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                if (GlobalGUIRoutines.collator != null)
                    return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
                else
                    return 0;
            }
        }

        private LoadProfileListAsyncTask (ShortcutCreatorListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            //noinspection ConstantConditions
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false);

            applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator(this.dataWrapper.context);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                fragment.textViewNoData.setVisibility(View.GONE);
                fragment.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
            Collections.sort(dataWrapper.profileList, new ProfileComparator());
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {
                fragment.progressBar.setVisibility(View.GONE);

                // get local profileList
                dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
                // set copy local profile list into activity profilesDataWrapper
                fragment.activityDataWrapper.copyProfileList(dataWrapper);

                fragment.profileListAdapter = new ShortcutCreatorListAdapter(fragment, fragment.activityDataWrapper);
                fragment.listView.setAdapter(fragment.profileListAdapter);
            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    @Override
    public void onDestroy()
    {
        if (isAsyncTaskPendingOrRunning()) {
            this.asyncTaskContext.get().cancel(true);
        }

        if (listView != null)
            listView.setAdapter(null);
        if (profileListAdapter != null)
            profileListAdapter.release();

        if (activityDataWrapper != null)
            activityDataWrapper.invalidateDataWrapper();
        activityDataWrapper = null;

        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private void createShortcut(final int position)
    {
        new AsyncTask<Void, Integer, Void>() {
            Profile profile;
            boolean isIconResourceID;
            String iconIdentifier;
            Bitmap profileBitmap;
            Bitmap shortcutOverlayBitmap;
            Bitmap profileShortcutBitmap;
            String profileName;
            String longLabel;
            boolean useCustomColor;
            Context context;
            Intent shortcutIntent;
            ShortcutInfoCompat.Builder shortcutBuilder;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                profile = activityDataWrapper.profileList.get(position);
                //noinspection ConstantConditions
                context = getActivity().getApplicationContext();

                if (profile != null) {
                    isIconResourceID = profile.getIsIconResourceID();
                    iconIdentifier = profile.getIconIdentifier();
                    profileName = profile._name;
                    longLabel = getString(R.string.shortcut_activate_profile) + profileName;
                    useCustomColor = profile.getUseCustomColorForIcon();
                    String id = "profile_" + profile._id;

                    if (profileName.isEmpty())
                        profileName = " ";

                    shortcutIntent = new Intent(context, BackgroundActivateProfileActivity.class);
                    shortcutIntent.setAction(Intent.ACTION_MAIN);
                    shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                    //noinspection ConstantConditions
                    shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);

                    /*
                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profileName);
                    */

                    shortcutBuilder = new ShortcutInfoCompat.Builder(context, id);
                    shortcutBuilder.setIntent(shortcutIntent);
                    shortcutBuilder.setShortLabel(profileName);
                    shortcutBuilder.setLongLabel(longLabel);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (profile != null) {
                    if (isIconResourceID) {
                        //noinspection ConstantConditions
                        if (profile._iconBitmap != null)
                            profileBitmap = profile._iconBitmap;
                        else {
                            //int iconResource = getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                            int iconResource = Profile.getIconResource(iconIdentifier);
                            profileBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
                        }
                        if (Build.VERSION.SDK_INT < 26)
                            shortcutOverlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_shortcut_overlay);
                    } else {
                        Resources resources = getResources();
                        int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                        int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                        //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
                        profileBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, true, false, context);
                        if (profileBitmap == null) {
                            int iconResource = R.drawable.ic_profile_default;
                            profileBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
                        }
                        if (Build.VERSION.SDK_INT < 26)
                            shortcutOverlayBitmap = BitmapManipulator.resampleResource(resources, R.drawable.ic_shortcut_overlay, width, height);
                    }

                    if (ApplicationPreferences.applicationWidgetIconColor(activityDataWrapper.context).equals("1")) {
                        if (isIconResourceID || useCustomColor) {
                            // icon is from resource or colored by custom color
                            int monochromeValue = 0xFF;
                            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(activityDataWrapper.context);
                            if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
                            if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
                            if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
                            if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
                            //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;
                            profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, context*/);
                        } else {
                            float monochromeValue = 255f;
                            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(context);
                            if (applicationWidgetIconLightness.equals("0")) monochromeValue = -255f;
                            if (applicationWidgetIconLightness.equals("25")) monochromeValue = -128f;
                            if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0f;
                            if (applicationWidgetIconLightness.equals("75")) monochromeValue = 128f;
                            //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 255f;
                            profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
                            profileBitmap = BitmapManipulator.setBitmapBrightness(profileBitmap, monochromeValue);
                        }
                    }

                    if (Build.VERSION.SDK_INT < 26)
                        profileShortcutBitmap = combineImages(profileBitmap, shortcutOverlayBitmap);
                    else
                        profileShortcutBitmap = profileBitmap;

                    //intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, profileShortcutBitmap);
                    shortcutBuilder.setIcon(IconCompat.createWithBitmap(profileShortcutBitmap));
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (profile != null) {
                    //intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                    //context.sendBroadcast(intent);

                    ShortcutInfoCompat shortcutInfo = shortcutBuilder.build();
                    Intent intent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcutInfo);

                    //noinspection ConstantConditions
                    getActivity().setResult(Activity.RESULT_OK, intent);
                }

                //noinspection ConstantConditions
                getActivity().finish();
            }

        }.execute();
    }

    private Bitmap combineImages(Bitmap bitmap1, Bitmap bitmap2)
    {
        Bitmap combined;

        int width;
        int height;

        width = bitmap2.getWidth();
        height = bitmap2.getHeight();

        combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(combined);
        canvas.drawBitmap(bitmap1, 0f, 0f, null);
        if (ApplicationPreferences.applicationShortcutEmblem(activityDataWrapper.context))
            canvas.drawBitmap(bitmap2, 0f, 0f, null);

        return combined;
    }

}
