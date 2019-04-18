package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.DialogPreference;

public class ConnectToSSIDDialogPreferenceX extends DialogPreference {

    ConnectToSSIDDialogPreferenceFragmentX fragment;

    private final Context context;

    String value = "";
    final int disableSharedProfile;

    List<WifiSSIDData> ssidList;

    public ConnectToSSIDDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ConnectToSSIDDialogPreference);

        disableSharedProfile = typedArray.getInteger(
                R.styleable.ConnectToSSIDDialogPreference_cDisableSharedProfile, 0);

        this.context = context;

        //setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();

        ssidList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        try {
            value = getPersistedString((String) defaultValue);
        } catch  (Exception e) {
            value = (String) defaultValue;
        }
        setSummaryCTSDP();
    }

    void setSummaryCTSDP()
    {
        String prefSummary = context.getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any);
        if (!value.isEmpty() && value.equals(Profile.CONNECTTOSSID_SHAREDPROFILE))
            prefSummary = context.getString(R.string.array_pref_default_profile);
        else
        if (!value.isEmpty() && !value.equals(Profile.CONNECTTOSSID_JUSTANY))
            prefSummary = value;
        setSummary(prefSummary);
    }

    void persistValue() {
        persistString(value);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ConnectToSSIDDialogPreferenceX.SavedState myState = new ConnectToSSIDDialogPreferenceX.SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(ConnectToSSIDDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCTSDP();
            return;
        }

        // restore instance state
        ConnectToSSIDDialogPreferenceX.SavedState myState = (ConnectToSSIDDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;

        setSummaryCTSDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<ConnectToSSIDDialogPreferenceX.SavedState> CREATOR =
                new Creator<ConnectToSSIDDialogPreferenceX.SavedState>() {
                    public ConnectToSSIDDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ConnectToSSIDDialogPreferenceX.SavedState(in);
                    }
                    public ConnectToSSIDDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ConnectToSSIDDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
