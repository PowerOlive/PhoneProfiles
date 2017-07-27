package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
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

    private Context _context;
    private MaterialDialog mDialog;
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox defaultProfileChBox = null;

    private AudioManager audioManager = null;
    private MediaPlayer mediaPlayer = null;

    // Custom xml attributes.
    private String volumeType = null;
    private int noChange = 0;
    private int defaultProfile = 0;
    private int disableDefaultProfile = 0;

    private int maximumValue = 7;
    private int minimumValue = 0;
    private int maximumMediaValue = 15;
    private int defaultValueRing = 0;
    private int defaultValueNotification = 0;
    private int defaultValueMusic = 0;
    private int defaultValueAlarm = 0;
    private int defaultValueSystem = 0;
    private int defaultValueVoice = 0;
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

        // zistime maximalnu hodnotu z audio managera
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
        maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // zistime default hodnotu z audio managera
        defaultValueRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        defaultValueNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        defaultValueMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        defaultValueAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        defaultValueSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        defaultValueVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

        mediaPlayer = MediaPlayer.create(context, R.raw.volume_change_notif);
        if (mediaPlayer != null)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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
                .customView(R.layout.activity_volume_pref_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist()) {
                            persistString(Integer.toString(value + minimumValue)
                                    + "|" + Integer.toString(noChange)
                                    + "|" + Integer.toString(defaultProfile));
                            setSummaryVDP();
                        }
                    }
                });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

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

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

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
        if (mediaPlayer != null) {
            int volume;
            if (volumeType.equalsIgnoreCase("MEDIA"))
                volume = value;
            else {
                float percentage = (float) value / maximumValue * 100.0f;
                volume = Math.round(maximumMediaValue / 100.0f * percentage);
            }

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            try {
                mediaPlayer.start();
            } catch (Exception ignore) { }
        }
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
        super.onDismiss(dialog);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultValueMusic, 0);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.release();
        }
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
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
        int value = 0;
        String volumeType = null;
        int noChange = 0;
        int defaultProfile = 0;
        int disableDefaultProfile = 0;

        SavedState(Parcel source)
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

        SavedState(Parcelable superState)
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
