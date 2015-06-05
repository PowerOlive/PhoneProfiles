package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import java.io.File;

public class BitmapManipulator {
	
	public static Bitmap resampleBitmap(String bitmapFile, int width, int height)
	{
		File f = new File(bitmapFile);
		if (f.exists())
		{
			// first decode with inJustDecodeDpunds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(bitmapFile, options);
			// calaculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, width, height);
			// decode bitmap with inSampleSize
			options.inJustDecodeBounds = false;
			Bitmap decodedSampleBitmap = BitmapFactory.decodeFile(bitmapFile, options);
			
			return decodedSampleBitmap;
		}
		else
			return null;
	}

	public static Bitmap recolorBitmap(Bitmap bitmap, int color, Context context)
	{
		if (bitmap == null)
			return null;

		Bitmap colorBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(),
				bitmap.getConfig());
		//Config.ARGB_8888);

		Canvas canvas = new Canvas(colorBitmap);
		Paint paint = new Paint();
		Matrix matrix = new Matrix();

		ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		paint.setColorFilter(filter);
		canvas.drawBitmap(bitmap, matrix, paint);

		return colorBitmap;
	}

	public static Bitmap monochromeBitmap(Bitmap bitmap, int value, Context context) {
		int color = Color.argb(0xFF, value, value, value);
		return recolorBitmap(bitmap, color, context);
	}

	public static Bitmap grayscaleBitmap(Bitmap bitmap)
	{
		if (bitmap == null)
			return null;

		Bitmap monochromeBitmap = Bitmap.createBitmap(bitmap.getWidth(),
													bitmap.getHeight(),
													bitmap.getConfig());
    	Canvas canvas = new Canvas(monochromeBitmap);
    	Paint paint = new Paint();
    	ColorMatrix colorMatrix = new ColorMatrix();
    	colorMatrix.setSaturation(0.0f);
    	paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    	Matrix matrix = new Matrix();
    	canvas.drawBitmap(bitmap, matrix, paint);

    	return monochromeBitmap;
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth)
		{
			// calculate ratios of height and width to requested height an width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			
			// choose the smalest ratio as InSamleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width
			inSampleSize = (heightRatio < widthRatio) ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	
}
