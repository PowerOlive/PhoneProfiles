package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.redmadrobot.inputmask.MaskedTextChangedListener;

import java.util.Arrays;

class FastAccessDurationDialog implements SeekBar.OnSeekBarChangeListener{

    private int mMin, mMax;
    private Profile mProfile;
    private int mAfterDo;

    private DataWrapper mDataWrapper;
    private int mStartupSource;
    private Activity mActivity;
    private boolean mInteractive;
    private String[] afterDoValues;

    //Context mContext;

    MaterialDialog mDialog;
    private EditText mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;

    //private int mColor = 0;

    FastAccessDurationDialog(Activity activity, Profile profile, DataWrapper dataWrapper, int startupSource, boolean interactive) {

        mMax = 86400;
        mMin = 0;
        mAfterDo = -1;

        mActivity = activity;
        //mContext = activity.getBaseContext();
        mProfile = profile;
        mDataWrapper = dataWrapper;
        mStartupSource = startupSource;
        mInteractive = interactive;

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
            */


        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title(mActivity.getString(R.string.profile_preferences_duration))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .customView(R.layout.activity_fast_access_duration_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        int hours = mSeekBarHours.getProgress();
                        int minutes = mSeekBarMinutes.getProgress();
                        int seconds = mSeekBarSeconds.getProgress();

                        int iValue = (hours * 3600 + minutes * 60 + seconds);
                        if (iValue < mMin) iValue = mMin;
                        if (iValue > mMax) iValue = mMax;

                        mProfile._duration = iValue;
                        if (mAfterDo != -1)
                            mProfile._afterDurationDo = mAfterDo;
                        mDataWrapper.getDatabaseHandler().updateProfile(mProfile);
                        mDataWrapper._activateProfile(mProfile, mStartupSource, mInteractive, mActivity);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDataWrapper.finishActivity(mStartupSource, false, mActivity);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mDataWrapper.finishActivity(mStartupSource, false, mActivity);
                    }
                });

        mDialog = mBuilder.build();

        View layout = mDialog.getCustomView();

        TextView mTextViewRange = (TextView) layout.findViewById(R.id.duration_pref_dlg_range);
        mValue = (EditText) layout.findViewById(R.id.duration_pref_dlg_value);
        mSeekBarHours = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_seconds);

        //mSeekBarHours.setRotation(180);
        //mSeekBarMinutes.setRotation(180);
        //mSeekBarSeconds.setRotation(180);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = mMax / 3600;
        minutes = (mMax % 3600) / 60;
        seconds = mMax % 60;
        final String sMax = GlobalGUIRoutines.getDurationString(mMax);
        mSeekBarHours.setMax(hours);
        if (hours == 0)
            mSeekBarMinutes.setMax(minutes);
        else
            mSeekBarMinutes.setMax(59);
        if ((hours == 0) && (minutes == 0))
            mSeekBarSeconds.setMax(seconds);
        else
            mSeekBarSeconds.setMax(59);
        final String sMin = GlobalGUIRoutines.getDurationString(mMin);
        int iValue = mProfile._duration;
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

        final MaskedTextChangedListener listener = new MaskedTextChangedListener(
                "[00]{:}[00]{:}[00]",
                true,
                mValue,
                null,
                new MaskedTextChangedListener.ValueListener() {
                    @Override
                    public void onTextChanged(boolean maskFilled, @NonNull final String extractedValue) {
                        Log.d(FastAccessDurationDialog.class.getSimpleName(), extractedValue);
                        Log.d(FastAccessDurationDialog.class.getSimpleName(), String.valueOf(maskFilled));

                        int hours = 0;
                        int minutes = 0;
                        int seconds = 0;
                        String[] splits = extractedValue.split(":");
                        try {
                            hours = Integer.parseInt(splits[0].replaceFirst("\\s+$", ""));
                        } catch (Exception ignore) {
                        }
                        try {
                            minutes = Integer.parseInt(splits[1].replaceFirst("\\s+$", ""));
                        } catch (Exception ignore) {
                        }
                        try {
                            seconds = Integer.parseInt(splits[2].replaceFirst("\\s+$", ""));
                        } catch (Exception ignore) {
                        }

                        int iValue = (hours * 3600 + minutes * 60 + seconds);

                        boolean badText = false;
                        if (iValue < mMin) {
                            iValue = mMin;
                            badText = true;
                        }
                        if (iValue > mMax) {
                            iValue = mMax;
                            badText = true;
                        }

                        if (mDialog != null) {
                            MDButton button = mDialog.getActionButton(DialogAction.POSITIVE);
                            button.setEnabled(!badText);
                        }

                        hours = iValue / 3600;
                        minutes = (iValue % 3600) / 60;
                        seconds = iValue % 60;

                        mSeekBarHours.setProgress(hours);
                        mSeekBarMinutes.setProgress(minutes);
                        mSeekBarSeconds.setProgress(seconds);
                    }
                }
        );
        mValue.addTextChangedListener(listener);
        mValue.setOnFocusChangeListener(listener);
        mValue.setHint(listener.placeholder());

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        Spinner afterDoSpinner = (Spinner) layout.findViewById(R.id.fast_access_duration_dlg_after_do_spinner);
        afterDoValues = mActivity.getResources().getStringArray(R.array.afterProfileDurationDoValues);
        afterDoSpinner.setSelection(Arrays.asList(afterDoValues).indexOf(String.valueOf(mProfile._afterDurationDo)));
        afterDoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAfterDo = Integer.valueOf(afterDoValues[position]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();
            int seconds = mSeekBarSeconds.getProgress();

            int iValue = (hours * 3600 + minutes * 60 + seconds);
            if (iValue < mMin) iValue = mMin;
            if (iValue > mMax) iValue = mMax;

            mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void show() {
        mDialog.show();
    }

}
