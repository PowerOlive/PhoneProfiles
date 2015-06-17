package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FirstStartService extends IntentService {

	public FirstStartService()
	{
		super("FirstStartService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Context context = getApplicationContext();
		
		// grant root
		//if (GlobalData.isRooted(false))
		//{
			if (GlobalData.grantRoot(true))
			{
				GlobalData.settingsBinaryExists();
				//GlobalData.getSUVersion();
			}
		//}
		
		if (GlobalData.getApplicationStarted(context))
			return;
		
		//int startType = intent.getStringExtra(GlobalData.EXTRA_FIRST_START_TYPE);
		
		GlobalData.loadPreferences(context);
		GUIData.setLanguage(context);

        // remove phoneprofiles_silent.mp3
        //removeTone("phoneprofiles_silent.mp3", context);
        // install phoneprofiles_silent.ogg
        installTone(R.raw.phoneprofiles_silent, "PhoneProfiles Silent", context);

        GlobalData.setLockscreenDisabled(context, false);

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        GlobalData.setRingerVolume(context, audioManager.getStreamVolume(AudioManager.STREAM_RING));
        GlobalData.setNotificationVolume(context, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));

		// start ReceiverService
		context.startService(new Intent(context, ReceiversService.class));

        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        GlobalData.setActivatedProfileForDuration(context, 0);

		DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
		dataWrapper.getActivateProfileHelper().initialize(null, context);

        // create a handler to post messages to the main thread
        Handler toastHandler = new Handler(getMainLooper());
        dataWrapper.setToastHandler(toastHandler);
        Handler brightnessHandler = new Handler(getMainLooper());
        dataWrapper.getActivateProfileHelper().setBrightnessHandler(brightnessHandler);

		// zrusenie notifikacie
		dataWrapper.getActivateProfileHelper().removeNotification();

		// show notification about upgrade PPHelper
		//if (GlobalData.isRooted(false))
		//{
			if (!PhoneProfilesHelper.isPPHelperInstalled(context, PhoneProfilesHelper.PPHELPER_CURRENT_VERSION))
			{
				// proper PPHelper version is not installed
				if (PhoneProfilesHelper.PPHelperVersion != -1)
				{
					// PPHelper is installed, show notification 
					PhoneProfilesHelper.showPPHelperUpgradeNotification(context);							
				}
			}
		//}

		GlobalData.setApplicationStarted(context, true);
			
		dataWrapper.activateProfile(0, GlobalData.STARTUP_SOURCE_BOOT, null);
		dataWrapper.invalidateDataWrapper();

		// start PPHelper
		//PhoneProfilesHelper.startPPHelper(context);

        //  aplikacia uz je 1. krat spustena
        GlobalData.setApplicationStarted(context, true);

	}

    private boolean installTone(int resID, String title, Context context) {

        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return false;

        File path = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        // Make sure the directory exists
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
        String filename = context.getResources().getResourceEntryName(resID) + ".ogg";
        File outFile = new File(path, filename);

        boolean isError = false;

        if (!outFile.exists()) {

            // Write the file
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = context.getResources().openRawResource(resID);
                outputStream = new FileOutputStream(outFile);


                // Write in 1024-byte chunks
                byte[] buffer = new byte[1024];
                int bytesRead;
                // Keep writing until `inputStream.read()` returns -1, which means we reached the
                //  end of the stream
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

            } catch (Exception e) {
                Log.e("FirstStartService", "installTone: Error writing " + filename, e);
                isError = true;
            } finally {
                // Close the streams
                try {
                    if (inputStream != null)
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    // Means there was an error trying to close the streams, so do nothing
                }
            }
        }

        if (!isError) {

            String mimeType = "audio/ogg";

            // Set the file metadata
            String outAbsPath = outFile.getAbsolutePath();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DATA, outAbsPath);
            contentValues.put(MediaStore.MediaColumns.TITLE, title);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.MediaColumns.SIZE, outFile.length());
            contentValues.put(MediaStore.Audio.Media.IS_ALARM, true);
            contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            contentValues.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

            Cursor cursor = context.getContentResolver().query(contentUri,
                    new String[]{MediaStore.MediaColumns.DATA},
                    MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
            if (cursor != null) {
                if (!cursor.moveToFirst()) {

                    // not exists content

                    cursor.close();

                    //// If the ringtone already exists in the database, delete it first
                    //context.getContentResolver().delete(contentUri,
                    //        MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null);

                    // Add the metadata to the file in the database
                    Uri newUri = context.getContentResolver().insert(contentUri, contentValues);

                    if (newUri != null) {
                        // Tell the media scanner about the new ringtone
                        MediaScannerConnection.scanFile(
                                context,
                                new String[]{newUri.toString()},
                                new String[]{mimeType},
                                null
                        );

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }
                } else
                    cursor.close();
            }
        }


        return !isError;
    }

    /*
    private void removeTone(String voiceFile, Context context) {

        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;

        File path = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        String filename = voiceFile;
        File outFile = new File(path, filename);

        String outAbsPath = outFile.getAbsolutePath();
        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

        // If the ringtone already exists in the database, delete it first
        context.getContentResolver().delete(contentUri,
                MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null);

        // delete the file
        outFile.delete();
    }
    */

}
