package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class VolumeDialogPreference extends
        DialogPreference implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    // Layout widgets.

    Context _context;
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox defaultProfileChBox = null;

    private AudioManager audioManager = null;

    // Custom xml attributes.
    private String volumeType = null;
    private int noChange = 0;
    private int defaultProfile = 0;
    private int disableDefaultProfile = 0;

    private int maximumValue = 7;
    private int minimumValue = 0;
    private int defaultValueRing = 0;
    private int defaultValueNotification = 0;
    private int defaultValueMusic = 0;
    private int defaultValueAlarm = 0;
    private int defaultValueSystem = 0;
    private int defaultValueVoice = 0;
    private int defaultRingerMode = 0;
    private int defaultZenMode = 0;
    private int stepSize = 1;

    private String sValue = "0|1";
    private int value = 0;

    public VolumeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.VolumeDialogPreference);

        volumeType = typedArray.getString(
                R.styleable.VolumeDialogPreference_volumeType);
        noChange = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vNoChange, 1);
        defaultProfile = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vDefaultProfile, 0);
        disableDefaultProfile = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vDisableDefaultProfile, 0);

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        // zistima maximalnu hodnotu z audio managera
        if (volumeType.equalsIgnoreCase("RINGTONE"))
            maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        else
        if (volumeType.equalsIgnoreCase("NOTIFICATION"))
            maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        else
        if (volumeType.equalsIgnoreCase("MEDIA"))
            maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        else
        if (volumeType.equalsIgnoreCase("ALARM"))
            maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        else
        if (volumeType.equalsIgnoreCase("SYSTEM"))
            maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        else
        if (volumeType.equalsIgnoreCase("VOICE"))
            maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        // zistime default hodnotu z audio managera
        defaultValueRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        defaultValueNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        defaultValueMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        defaultValueAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        defaultValueSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        defaultValueVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        defaultRingerMode = audioManager.getRingerMode();
        if (android.os.Build.VERSION.SDK_INT >= 21)
            defaultZenMode = Settings.Global.getInt(context.getContentResolver(), "zen_mode", ActivateProfileHelper.ZENMODE_ALL);

        typedArray.recycle();
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                        //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (shouldPersist()) {
                            persistString(Integer.toString(value + minimumValue)
                                    + "|" + Integer.toString(noChange)
                                    + "|" + Integer.toString(defaultProfile));
                            setSummaryVDP();
                        }
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_volume_pref_dialog, null);
        onBindDialogView(layout);

        seekBar = (SeekBar)layout.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = (TextView)layout.findViewById(R.id.volumePrefDialogValueText);
        noChangeChBox = (CheckBox)layout.findViewById(R.id.volumePrefDialogNoChange);
        defaultProfileChBox = (CheckBox)layout.findViewById(R.id.volumePrefDialogDefaultProfile);

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setKeyProgressIncrement(stepSize);
        seekBar.setMax(maximumValue - minimumValue);

        getValueVDP();

        seekBar.setProgress(value);

        noChangeChBox.setOnCheckedChangeListener(this);
        noChangeChBox.setChecked((noChange == 1));

        defaultProfileChBox.setOnCheckedChangeListener(this);
        defaultProfileChBox.setChecked((defaultProfile == 1));
        defaultProfileChBox.setEnabled(disableDefaultProfile == 0);

        if (noChange == 1)
            defaultProfileChBox.setChecked(false);
        if (defaultProfile == 1)
            noChangeChBox.setChecked(false);

        valueText.setEnabled((noChange == 0) && (defaultProfile == 0));
        seekBar.setEnabled((noChange == 0) && (defaultProfile == 0));

        mBuilder.customView(layout, false);

        MaterialDialog mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    public void onProgressChanged(SeekBar seek, int newValue,
                                  boolean fromTouch) {
        // Round the value to the closest integer value.
        if (stepSize >= 1) {
            value = Math.round(newValue/stepSize)*stepSize;
        }
        else {
            value = newValue;
        }

        // Set the valueText text.
        valueText.setText(String.valueOf(value + minimumValue));

        callChangeListener(value);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == R.id.volumePrefDialogNoChange)
        {
            noChange = (isChecked)? 1 : 0;

            valueText.setEnabled((noChange == 0) && (defaultProfile == 0));
            seekBar.setEnabled((noChange == 0) && (defaultProfile == 0));
            if (isChecked)
                defaultProfileChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.volumePrefDialogDefaultProfile)
        {
            defaultProfile = (isChecked)? 1 : 0;

            valueText.setEnabled((noChange == 0) && (defaultProfile == 0));
            seekBar.setEnabled((noChange == 0) && (defaultProfile == 0));
            if (isChecked)
                noChangeChBox.setChecked(false);
        }

        callChangeListener(noChange);
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {

        RingerModeChangeReceiver.removeAlarm(_context);
        RingerModeChangeReceiver.internalChange = true;

        ActivateProfileHelper.setZenMode(_context, ActivateProfileHelper.ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);

        if (volumeType.equalsIgnoreCase("RINGTONE"))
            audioManager.setStreamVolume(AudioManager.STREAM_RING, value, AudioManager.FLAG_PLAY_SOUND);
        else
        if (volumeType.equalsIgnoreCase("NOTIFICATION"))
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, value, AudioManager.FLAG_PLAY_SOUND);
        else
        if (volumeType.equalsIgnoreCase("MEDIA"))
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_PLAY_SOUND);
        else
        if (volumeType.equalsIgnoreCase("ALARM"))
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, value, AudioManager.FLAG_PLAY_SOUND);
        else
        if (volumeType.equalsIgnoreCase("SYSTEM"))
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, value, AudioManager.FLAG_PLAY_SOUND);
        else
        if (volumeType.equalsIgnoreCase("VOICE"))
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, value, AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueVDP();
        }
        else {
            // set state
            value = 0;
            noChange = 1;
            defaultProfile = 0;
            persistString(Integer.toString(value + minimumValue)
                    + "|" + Integer.toString(noChange)
                    + "|" + Integer.toString(defaultProfile));
        }
        setSummaryVDP();
    }

    private void getValueVDP()
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString(sValue);

        String[] splits = sValue.split("\\|");
        try {
            value = Integer.parseInt(splits[0]);
            if (value == -1)
            {
                if (volumeType.equalsIgnoreCase("SYSTEM"))
                    value = defaultValueSystem;
                else
                if (volumeType.equalsIgnoreCase("RINGTONE"))
                    value = defaultValueRing;
                else
                if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                    value =  defaultValueNotification;
                else
                if (volumeType.equalsIgnoreCase("MEDIA"))
                    value =  defaultValueMusic;
                else
                if (volumeType.equalsIgnoreCase("ALARM"))
                    value =  defaultValueAlarm;
                else
                if (volumeType.equalsIgnoreCase("VOICE"))
                    value =  defaultValueVoice;
            }
        } catch (Exception e) {
            value = 0;
        }
        value = value - minimumValue;
        try {
            noChange = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            noChange = 1;
        }
        try {
            defaultProfile = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            defaultProfile = 0;
        }

        // You're never know...
        if (value < 0) {
            value = 0;
        }
    }

    private void setSummaryVDP()
    {
        String prefVolumeDataSummary;
        if (noChange == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_no_change);
        else
        if (defaultProfile == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_default_profile);
        else
            prefVolumeDataSummary = String.valueOf(value) + " / " + String.valueOf(maximumValue);
        setSummary(prefVolumeDataSummary);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        DataWrapper dataWrapper = new DataWrapper(_context, false, false, 0);
        Profile profile = dataWrapper.getActivatedProfile();
        if (profile != null) {
            Intent volumeServiceIntent = new Intent(_context, ExecuteVolumeProfilePrefsService.class);
            volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
            volumeServiceIntent.putExtra(GlobalData.EXTRA_LINKUNLINK_VOLUMES, PhoneCallService.LINKMODE_NONE);
            _context.startService(volumeServiceIntent);
        } else {

            RingerModeChangeReceiver.removeAlarm(_context);
            RingerModeChangeReceiver.internalChange = true;

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                // set ringer mode to Ring for proper change ringer mode to Priority
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }
            }

            /*
            Log.e("#### VolumeDialogPreference", "defaultValueSystem=" + defaultValueSystem);
            Log.e("#### VolumeDialogPreference", "defaultValueRing=" + defaultValueRing);
            Log.e("#### VolumeDialogPreference", "defaultValueNotification=" + defaultValueNotification);
            Log.e("#### VolumeDialogPreference", "defaultValueMusic=" + defaultValueMusic);
            Log.e("#### VolumeDialogPreference", "defaultValueAlarm=" + defaultValueAlarm);
            Log.e("#### VolumeDialogPreference", "defaultValueVoice=" + defaultValueVoice);
            */
            // set default volumes
            if (!((defaultZenMode == ActivateProfileHelper.ZENMODE_NONE) || (defaultZenMode == ActivateProfileHelper.ZENMODE_ALARMS))) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, defaultValueSystem, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, defaultValueRing, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, defaultValueNotification, 0);
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultValueMusic, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, defaultValueAlarm, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, defaultValueVoice, 0);

            ActivateProfileHelper.setZenMode(_context, defaultZenMode, audioManager, defaultRingerMode);
            //Log.e("#### VolumeDialogPreference", "defaultRingerMode=" + defaultRingerMode);
            // set ringer mode after volume because volumes change silent/vibrate
            audioManager.setRingerMode(defaultRingerMode);

            RingerModeChangeReceiver.setAlarmForDisableInternalChange(_context);

        }

    }

    public static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1)
            return Integer.parseInt(splits[1]) == 0;
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        // ulozime instance state - napriklad kvoli zmene orientacie

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // netreba ukladat, je ulozene persistentne
            return superState;
        }*/

        // ulozenie istance state
        final SavedState myState = new SavedState(superState);
        myState.value = value;
        myState.volumeType = volumeType;
        myState.noChange = noChange;
        myState.defaultProfile = defaultProfile;
        myState.disableDefaultProfile = disableDefaultProfile;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryVDP();
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        volumeType = myState.volumeType;
        noChange = myState.noChange;
        defaultProfile = myState.defaultProfile;
        disableDefaultProfile = myState.disableDefaultProfile;

        setSummaryVDP();
        notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        public int value = 0;
        public String volumeType = null;
        public int noChange = 0;
        public int defaultProfile = 0;
        public int disableDefaultProfile = 0;

        public SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readInt();
            volumeType = source.readString();
            noChange = source.readInt();
            defaultProfile = source.readInt();
            disableDefaultProfile = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeInt(value);
            dest.writeString(volumeType);
            dest.writeInt(noChange);
            dest.writeInt(defaultProfile);
            dest.writeInt(disableDefaultProfile);
        }

        public SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in)
                    {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size)
                    {
                        return new SavedState[size];
                    }

                };

    }

}
