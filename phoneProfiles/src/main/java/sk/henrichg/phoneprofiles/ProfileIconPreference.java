package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class ProfileIconPreference extends DialogPreference {

    private String imageIdentifier;
    private boolean isImageResourceID;
    private boolean useCustomColor;
    private int customColor;

    private MaterialDialog mDialog;
    private ProfileIconColorChooserDialog mColorDialog;

    private ImageView imageView;
    private ProfileIconPreferenceAdapter adapter;
    private ImageView dialogIcon;
    private Button colorChooserButton;

    private Context prefContext;

    static int RESULT_LOAD_IMAGE = 1971;

    private static final String PREF_SHOW_HELP = "profile_icon_pref_show_help";

    public ProfileIconPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        /*
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ProfileIconPreference);

        // resource, resource_file, file
        imageSource = typedArray.getString(
            R.styleable.ProfileIconPreference_iconSource);
        */


        imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
        isImageResourceID = true;
        useCustomColor = false;
        customColor = 0;

        prefContext = context;

        setWidgetLayoutResource(R.layout.profileicon_preference); // resource na layout custom preference - TextView-ImageView

        //typedArray.recycle();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        imageView = (ImageView)view.findViewById(R.id.profileicon_pref_imageview); // resource na Textview v custom preference layoute
        updateIcon(false);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                        //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.imageview_resource_file_pref_dialog_gallery_btn)
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_profileicon_pref_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist()) {
                            setImageIdentifierAndType("", true, true);
                        }
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        // zavolat galeriu na vyzdvihnutie image
                        if (Permissions.grantCustomProfileIconPermissions(prefContext, ProfileIconPreference.this)) {
                            startGallery();
                            mDialog.dismiss();
                        }
                    }
                });

        getValuePIDP();

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        GridView gridView = (GridView)layout.findViewById(R.id.profileicon_pref_dlg_gridview);
        adapter = new ProfileIconPreferenceAdapter(prefContext, imageIdentifier, isImageResourceID, useCustomColor, customColor);
        gridView.setAdapter(adapter);
        gridView.setSelection(ProfileIconPreferenceAdapter.getImageResourcePosition(imageIdentifier));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setImageIdentifierAndType(Profile.profileIconId[position], true, false);
                adapter.imageIdentifierAndTypeChanged(imageIdentifier, isImageResourceID);
                updateIcon(true);
                if (isImageResourceID)
                    colorChooserButton.setVisibility(View.VISIBLE);
                else
                    colorChooserButton.setVisibility(View.GONE);
            }
        });

        dialogIcon = (ImageView)layout.findViewById(R.id.profileicon_pref_dlg_icon);
        updateIcon(true);

        colorChooserButton = (Button)layout.findViewById(R.id.profileicon_pref_dlg_change_color);
        colorChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomColorChooser();
            }
        });
        if (isImageResourceID)
            colorChooserButton.setVisibility(View.VISIBLE);
        else
            colorChooserButton.setVisibility(View.GONE);

        final TextView helpText = (TextView)layout.findViewById(R.id.profileicon_pref_dlg_helpText);

        final ImageView helpIcon = (ImageView)layout.findViewById(R.id.profileicon_pref_dlg_helpIcon);
        ApplicationPreferences.getSharedPreferences(prefContext);
        if (ApplicationPreferences.preferences.getBoolean(PREF_SHOW_HELP, true)) {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
            helpText.setVisibility(View.VISIBLE);
        }
        else {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help);
            helpText.setVisibility(View.GONE);
        }
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationPreferences.getSharedPreferences(prefContext);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE) {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help);
                    visibility = View.GONE;
                    editor.putBoolean(PREF_SHOW_HELP, false);
                }
                else {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
                    visibility = View.VISIBLE;
                    editor.putBoolean(PREF_SHOW_HELP, true);
                }
                helpText.setVisibility(visibility);
                editor.apply();
            }
        });

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mColorDialog != null && mColorDialog.mDialog != null && mColorDialog.mDialog.isShowing())
            mColorDialog.mDialog.dismiss();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);  // ikona bude vratena ako retazec
    }

    private void getValuePIDP() {
        String value = getPersistedString(imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor);
        String[] splits = value.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
        }
        try {
            isImageResourceID = splits[1].equals("1");
        } catch (Exception e) {
            isImageResourceID = true;
        }
        try {
            useCustomColor = splits[2].equals("1");
        } catch (Exception e) {
            useCustomColor = false;
        }
        try {
            customColor = Integer.valueOf(splits[3]);
        } catch (Exception e) {
            customColor = ProfileIconPreferenceAdapter.getIconColor(imageIdentifier);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValuePIDP();
        }
        else {
            // set state
            String value = (String) defaultValue;
            String[] splits = value.split("\\|");
            try {
                imageIdentifier = splits[0];
            } catch (Exception e) {
                imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
            }
            try {
                isImageResourceID = splits[1].equals("1");
            } catch (Exception e) {
                isImageResourceID = true;
            }
            try {
                useCustomColor = splits[2].equals("1");
            } catch (Exception e) {
                useCustomColor = false;
            }
            try {
                customColor = Integer.valueOf(splits[3]);
            } catch (Exception e) {
                customColor = ProfileIconPreferenceAdapter.getIconColor(imageIdentifier);
            }
            persistString(value);
        }
    }

    /*
    @Override
    protected Parcelable onSaveInstanceState()
    {
        // ulozime instance state - napriklad kvoli zmene orientacie

        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // netreba ukladat, je ulozene persistentne
            return superState;
        }

        // ulozenie istance state
        final SavedState myState = new SavedState(superState);
        myState.imageIdentifierAndType = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0");
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        String value = (String) myState.imageIdentifierAndType;
        String[] splits = value.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = PPApplication.PROFILE_ICON_DEFAULT;
        }
        try {
            isImageResourceID = splits[1].equals("1");
        } catch (Exception e) {
            isImageResourceID = true;
        }
        notifyChanged();
    }
    */

    /*
    public String getImageIdentifier()
    {
        return imageIdentifier;
    }

    public boolean getIsImageResourceID()
    {
        return isImageResourceID;
    }
    */

    void setImageIdentifierAndType(String newImageIdentifier, boolean newIsImageResourceID, boolean saveToPreference)
    {
        String newValue = newImageIdentifier+"|"+((newIsImageResourceID) ? "1" : "0");

        if (!saveToPreference) {
            if (!imageIdentifier.equals(newImageIdentifier)) {
                useCustomColor = false;
                customColor = 0;
            }
            String[] splits = newValue.split("\\|");
            try {
                imageIdentifier = splits[0];
            } catch (Exception e) {
                imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
            }
            try {
                isImageResourceID = splits[1].equals("1");
            } catch (Exception e) {
                isImageResourceID = true;
            }
        }

        if (saveToPreference) {
            if (!newIsImageResourceID) {
                imageIdentifier = newImageIdentifier;
                isImageResourceID = false;
                useCustomColor = false;
                customColor = 0;
            }
            newValue = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor;
            if (callChangeListener(newValue)) {
                persistString(newValue);
                // Data sa zmenili,notifikujeme
                notifyChanged();
            }
        }

    }

    void setCustomColor(boolean newUseCustomColor, int newCustomColor) {
        useCustomColor = newUseCustomColor;
        customColor = newCustomColor;
        adapter.setCustomColor(useCustomColor, customColor);
        updateIcon(true);
    }

    void startGallery()
    {
        //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // hm, neda sa ziskat aktivita z preference, tak vyuzivam static metodu
        ProfilePreferencesFragment.setChangedProfileIconPreference(this);
        ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    private void showCustomColorChooser() {
        mColorDialog = new ProfileIconColorChooserDialog(prefContext, this, useCustomColor, customColor,
                ProfileIconPreferenceAdapter.getIconColor(imageIdentifier));
        mColorDialog.show();
    }

    private void updateIcon(boolean inDialog) {
        ImageView imageView;
        if (inDialog)
            imageView = this.dialogIcon;
        else
            imageView = this.imageView;

        if (imageView != null)
        {
            if (isImageResourceID)
            {
                // je to resource id
                int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());

                if (useCustomColor) {
                    Bitmap bitmap = BitmapFactory.decodeResource(prefContext.getResources(), res);
                    bitmap = BitmapManipulator.recolorBitmap(bitmap, customColor);
                    imageView.setImageBitmap(bitmap);
                }
                else
                    imageView.setImageResource(res); // resource na ikonu
            }
            else
            {
                // je to file
                Resources resources = prefContext.getResources();
                int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                Bitmap bitmap = BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, prefContext);

                if (bitmap != null)
                    imageView.setImageBitmap(bitmap);
                else
                    imageView.setImageResource(R.drawable.ic_profile_default);
            }
        }
    }

    /*
    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifierAndType;

        public SavedState(Parcel source)
        {
            super(source);

            // restore image identifier and type
            imageIdentifierAndType = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(imageIdentifierAndType);
        }

        public SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
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
    */

}
