package sk.henrichg.phoneprofiles;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public abstract class PreferenceActivity extends AppCompatPreferenceActivity
{
    Toolbar toolbar;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void setPreferenceFragment(sk.henrichg.phoneprofiles.PreferenceFragment preferenceFragment) {

        //First check if it's already loaded (configuration change) so we don't overlap fragments
        //noinspection deprecation
        if(getFragmentManager()
                .findFragmentByTag("sk.henrichg.phoneprofiles.MainFragment") != null){
            return;
        }

        @SuppressWarnings("deprecation")
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fragmentTransaction.replace(R.id.content, preferenceFragment,
                    "sk.henrichg.phoneprofiles.MainFragment");
        //}else{
        //    fragmentTransaction.replace(android.R.id.content, preferenceFragment,
        //            "sk.henrichg.phoneprofiles.MainFragment");
        //}
        fragmentTransaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mp_activity_settings);

        if (Build.VERSION.SDK_INT >= 21) {
            View toolbarShadow = findViewById(R.id.mp_activity_settings_toolbar_shadow);
            if (toolbarShadow != null)
                toolbarShadow.setVisibility(View.GONE);
        }

        toolbar = findViewById(R.id.mp_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GlobalGUIRoutines.lockScreenOrientation(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //noinspection deprecation
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    //noinspection deprecation
                    getFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    PreferenceFragment getFragment() {
        int id;
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            id = R.id.content;
        //} else {
        //    id = android.R.id.content;
        //}

        //noinspection deprecation
        return (PreferenceFragment) getFragmentManager().findFragmentById(id);
    }

}
