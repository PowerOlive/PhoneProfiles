package sk.henrichg.phoneprofiles;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class WallpaperViewPreference extends Preference {

    private String imageIdentifier;
    private Bitmap bitmap;

    private final Context prefContext;

    static final int RESULT_LOAD_IMAGE = 1970;

    public WallpaperViewPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        imageIdentifier = "-";

        prefContext = context;

        //preferenceTitle = getTitle();

        setWidgetLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView
        //setLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        //imageTitle = view.findViewById(R.id.imageview_pref_label);
        //imageTitle.setText(preferenceTitle);

        ImageView imageView = view.findViewById(R.id.imageview_pref_imageview);

        if (imageView != null)
        {
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(R.drawable.ic_empty);
        }
    }

    @Override
    protected void onClick()
    {
        if (Permissions.grantWallpaperPermissions(prefContext, this))
            startGallery();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            imageIdentifier = getPersistedString(imageIdentifier);
            getBitmap();
        }
        else {
            // set state
            imageIdentifier = (String) defaultValue;
            getBitmap();
            persistString(imageIdentifier);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.imageIdentifier = imageIdentifier;
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
        imageIdentifier = myState.imageIdentifier;
        notifyChanged();
    }

    private void getBitmap() {
        if (!imageIdentifier.startsWith("-")) {
            Resources resources = prefContext.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            bitmap = BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, false, prefContext);
        }
    }

    void setImageIdentifier(String newImageIdentifier)
    {
        //String newValue = newImageIdentifier;

        //if (!callChangeListener(newValue)) {
        if (!callChangeListener(newImageIdentifier)) {
            return;
        }

        imageIdentifier = newImageIdentifier;
        getBitmap();

        persistString(newImageIdentifier);

        notifyChanged();
    }

    void startGallery()
    {
        Intent intent;
        try {
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }else{*/
                intent = new Intent(Intent.ACTION_GET_CONTENT);
            //}
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");

            ProfilePreferencesFragment.setChangedWallpaperViewPreference(this);
            ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } catch (Exception ignored) {}
        /*} catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");

                ProfilePreferencesFragment.setChangedWallpaperViewPreference(this);
                ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } catch (Exception ignored) {}
        }*/
    }


    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifier;

        SavedState(Parcel source)
        {
            super(source);

            // restore image identifier
            imageIdentifier = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(imageIdentifier);
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

//---------------------------------------------------------------------------------------------

    static Uri getImageContentUri(Context context, String imageFile) {
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { imageFile }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = context.getApplicationContext().getContentResolver();
                //noinspection WrongConstant
                resolver.takePersistableUriPermission(uri, takeFlags);
            }
            return uri;*/
        } else {
            if (cursor != null)
                cursor.close();
            File file = new File(imageFile);
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, imageFile);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    ContentResolver resolver = context.getApplicationContext().getContentResolver();
                    //noinspection WrongConstant
                    resolver.takePersistableUriPermission(uri, takeFlags);
                }
                return uri;*/
            } else {
                return null;
            }
        }
    }

}
