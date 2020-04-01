package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class ColorChooserPreferenceX extends DialogPreference {

    ColorChooserPreferenceFragmentX fragment;

    String value;

    private final Context context;

    final int[] mColors;

    public ColorChooserPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        final TypedArray ta = context.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
        mColors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            mColors[i] = ta.getColor(i, 0);
        }
        ta.recycle();

        setWidgetLayoutResource(R.layout.dialog_color_chooser_preference); // resource na layout custom preference - TextView-ImageView

        setPositiveButtonText(null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        FrameLayout layout = (FrameLayout)holder.findViewById(R.id.dialog_color_chooser_pref_color);

        int color = Integer.parseInt(value);

        Drawable selector = createSelector(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_pressed}
            };
            int[] colors = new int[]{
                    shiftColor(color),
                    color
            };
            ColorStateList rippleColors = new ColorStateList(states, colors);
            setBackgroundCompat(layout, new RippleDrawable(rippleColors, selector, null));
        } else {
            setBackgroundCompat(layout, selector);
        }

        Handler handler = new Handler(context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setSummary(R.string.empty_string);
            }
        }, 200);

    }

    void setBackgroundCompat(View view, Drawable d) {
        view.setBackground(d);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String) defaultValue);
    }

    void persistValue() {
        if (callChangeListener(value))
        {
            persistString(value);
        }
    }

    int shiftColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    Drawable createSelector(int color) {
        int position = -1;
        for (int i = 0; i < mColors.length; i++) {
            if (mColors[i] == color) {
                position = i;
                break;
            }
        }

        String applicationTheme = ApplicationPreferences.applicationTheme(context, true);

        GradientDrawable coloredCircle = new GradientDrawable();
        coloredCircle.setColor(color);
        coloredCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals("dark")) {
            if (position == 2) // dark gray color
                coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
        }
        else {
            if (position == 0) // white color
                coloredCircle.setStroke(2, Color.parseColor("#AEAEAE"));
        }
        GradientDrawable darkerCircle = new GradientDrawable();
        darkerCircle.setColor(shiftColor(color));
        darkerCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals("dark")) {
            if (position == 2) // dark gray color
                coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
        }
        else {
            if (position == 0) // white color
                darkerCircle.setStroke(2, Color.parseColor("#AEAEAE"));
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, coloredCircle);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ColorChooserPreferenceX.SavedState myState = new ColorChooserPreferenceX.SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(ColorChooserPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        ColorChooserPreferenceX.SavedState myState = (ColorChooserPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<ColorChooserPreferenceX.SavedState> CREATOR =
                new Creator<ColorChooserPreferenceX.SavedState>() {
                    public ColorChooserPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ColorChooserPreferenceX.SavedState(in);
                    }
                    public ColorChooserPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ColorChooserPreferenceX.SavedState[size];
                    }

                };

    }

}