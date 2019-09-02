package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class RingtonePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    RingtonePreferenceX preference;

    private RingtonePreferenceAdapterX listAdapter;
    private ListView listView;

    private Context prefContext;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (RingtonePreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_ringtone_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.ringtone_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                RingtonePreferenceAdapterX.ViewHolder viewHolder = (RingtonePreferenceAdapterX.ViewHolder) item.getTag();
                preference.setRingtone((String)listAdapter.getItem(position), false);
                viewHolder.radioBtn.setChecked(true);
                preference.playRingtone();
            }
        });

        listAdapter = new RingtonePreferenceAdapterX(this, prefContext, preference.toneList);
        listView.setAdapter(listAdapter);

        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext)) {
            Handler handler = new Handler(prefContext.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //preference.oldRingtoneUri = preference.ringtoneUri;
                    preference.refreshListView();
                }
            }, 200);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((preference.asyncTask != null) && !preference.asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            preference.asyncTask.cancel(true);
        }

        PPApplication.logE("RingtonePreferenceFragmentX.onDialogClosed", "ringtoneUri="+preference.ringtoneUri);
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                preference.stopPlayRingtone();
            }
        });

        preference.fragment = null;
    }

    void updateListView(boolean alsoSelection) {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();

            if (alsoSelection) {
                List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
                final int position = uris.indexOf(preference.ringtoneUri);
                listView.setSelection(position);
            }
        }
    }

    int getRingtonePosition() {
        List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
        return uris.indexOf(preference.ringtoneUri);
    }

}
