package sk.henrichg.phoneprofiles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RingtonePreference extends DialogPreference {

    String ringtone;

    private String ringtoneType;
    private boolean showSilent;
    private boolean showDefault;

    private Context prefContext;
    private MaterialDialog mDialog;
    private ListView listView;

    private RingtonePreferenceAdapter listAdapter;

    public RingtonePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference);

        ringtoneType = typedArray.getString(R.styleable.RingtonePreference_ringtoneType);
        showSilent = typedArray.getBoolean(R.styleable.RingtonePreference_showSilent, false);
        showDefault = typedArray.getBoolean(R.styleable.RingtonePreference_showDefault, false);

        if (ringtoneType.equals("ringtone"))
            ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
        else
        if (ringtoneType.equals("notification"))
            ringtone = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
        else
        if (ringtoneType.equals("alarm"))
            ringtone = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();

        prefContext = context;

        typedArray.recycle();

    }

    protected void showDialog(Bundle state) {
        PPApplication.logE("RingtonePreference.showDialog", "xx");

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .customView(R.layout.activity_ringtone_pref_dialog, false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist())
                        {
                            // set summary
                            _setSummary(ringtone);

                            // zapis do preferences
                            persistString(ringtone);

                            // Data sa zmenili,notifikujeme
                            notifyChanged();

                            mDialog.dismiss();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mDialog.dismiss();
                    }
                });

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                RingtonePreference.this.onShow(dialog);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        listView = (ListView)layout.findViewById(R.id.ringtone_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                RingtonePreferenceAdapter.ViewHolder viewHolder = (RingtonePreferenceAdapter.ViewHolder) item.getTag();
                setRingtone((String)listAdapter.getItem(position), viewHolder.radioBtn);
                viewHolder.radioBtn.setChecked(true);
            }
        });

        Map<String, String> toneList = new LinkedHashMap<>();

        RingtoneManager manager = new RingtoneManager(prefContext);
        if (ringtoneType.equals("ringtone")) {
            manager.setType(RingtoneManager.TYPE_RINGTONE);
            if (showDefault) {
                Uri uri = Settings.System.DEFAULT_RINGTONE_URI;
                Ringtone _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                String ringtoneName;
                try {
                    ringtoneName = _ringtone.getTitle(prefContext);
                } catch (SecurityException e) {
                    ringtoneName = prefContext.getString(R.string.ringtone_preference_default_ringtone);
                }
                toneList.put(Settings.System.DEFAULT_RINGTONE_URI.toString(), ringtoneName);
            }
        }
        else
        if (ringtoneType.equals("notification")) {
            manager.setType(RingtoneManager.TYPE_NOTIFICATION);
            if (showDefault) {
                Uri uri = Settings.System.DEFAULT_NOTIFICATION_URI;
                Ringtone _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                String ringtoneName;
                try {
                    ringtoneName = _ringtone.getTitle(prefContext);
                } catch (SecurityException e) {
                    ringtoneName = prefContext.getString(R.string.ringtone_preference_default_notification);
                }
                toneList.put(Settings.System.DEFAULT_NOTIFICATION_URI.toString(), ringtoneName);
            }
        }
        else
        if (ringtoneType.equals("alarm")) {
            manager.setType(RingtoneManager.TYPE_ALARM);
            if (showDefault) {
                Uri uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                Ringtone _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                String ringtoneName;
                try {
                    ringtoneName = _ringtone.getTitle(prefContext);
                } catch (SecurityException e) {
                    ringtoneName = prefContext.getString(R.string.ringtone_preference_default_alarm);
                }
                toneList.put(Settings.System.DEFAULT_ALARM_ALERT_URI.toString(), ringtoneName);
            }
        }

        if (showSilent)
            toneList.put("", prefContext.getString(R.string.ringtone_preference_none));

        Cursor cursor = manager.getCursor();

        /*
        profile._soundRingtone=content://settings/system/ringtone
        profile._soundNotification=content://settings/system/notification_sound
        profile._soundAlarm=content://settings/system/alarm_alert
        */

        while (cursor.moveToNext()) {
            String _uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String _title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String _id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            toneList.put(_uri + "/" + _id, _title);
        }

        listAdapter = new RingtonePreferenceAdapter(this, prefContext, toneList);
        listView.setAdapter(listAdapter);

        listAdapter.checkedRadioButton = null;
        String value;
        try {
            value = getPersistedString(ringtone);
        } catch  (Exception e) {
            value = ringtone;
        }
        ringtone = value;
        PPApplication.logE("RingtonePreference.onShow", "ringtone="+ringtone);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private void onShow(DialogInterface dialog) {
        List<String> uris = new ArrayList(listAdapter.toneList.keySet());
        final int position = uris.indexOf(ringtone);
        listView.setSelection(position);

        _setSummary(ringtone);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            String value;
            try {
                value = getPersistedString(ringtone);
            } catch  (Exception e) {
                value = ringtone;
            }
            ringtone = value;
        }
        else {
            // set state
            String value = (String) defaultValue;
            ringtone = value;
            persistString(value);
        }
        _setSummary(ringtone);
    }

    private void _setSummary(String ringtone)
    {
        String ringtoneName;

        if (ringtone.isEmpty())
            ringtoneName = prefContext.getString(R.string.ringtone_preference_none);
        else {
            Uri uri = Uri.parse(ringtone);
            Ringtone _ringtone = RingtoneManager.getRingtone(prefContext, uri);
            try {
                ringtoneName = _ringtone.getTitle(prefContext);
            } catch (SecurityException e) {
                ringtoneName = prefContext.getString(R.string.ringtone_preference_not_set);
            }
        }

        PPApplication.logE("RingtonePreference._setSummary", "ringtoneName="+ringtoneName);

        setSummary(ringtoneName);
    }

    void setRingtone(String newRingtone, RadioButton newCheckedRadioButton)
    {
        ringtone = newRingtone;

        if (listAdapter.checkedRadioButton != null)
            listAdapter.checkedRadioButton.setChecked(false);
        listAdapter.checkedRadioButton = newCheckedRadioButton;

        // set summary
        //_setSummary(ringtone);
    }

}
