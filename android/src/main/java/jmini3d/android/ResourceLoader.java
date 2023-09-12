package jmini3d.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ResourceLoader {
	static final String TAG = ResourceLoader.class.getName();

	Context context;

	HashMap<String, Bitmap> customBitmaps = new HashMap<>();

	public ResourceLoader(Context ctx) {
		this.context = ctx;
	}

	public Bitmap getImage(String image) {
		if (customBitmaps.containsKey(image)) {
			return customBitmaps.get(image);
		}

		if (image.lastIndexOf(".") > 0) {
			image = image.substring(0, image.lastIndexOf("."));
		}

		String uri = "drawable/" + image;
		int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

		if (imageResource == 0) {
			Log.e(TAG, "Image not found in resources: " + image);
			return null;
		}

		try {
			return makeBitmapFromInputStream(context.getResources().openRawResource(imageResource));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Bitmap makeBitmapFromInputStream(InputStream is) {
		try (is) {
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			// Ignore.
		}
		return null;
	}

	public void freeBitmap(String name, Bitmap bitmap) {
		if (name != null && customBitmaps.containsKey(name)) {
//			customBitmaps.remove(name);
		} else {
			bitmap.recycle();
		}
	}

	public void addCustomBitmap(String name, Bitmap bitmap) {
		customBitmaps.put(name, bitmap);
	}

	public Context getContext() {
		return context;
	}

	public String loadRawString(String file) {
		try {
			if (file.lastIndexOf(".") > 0) {
				file = file.substring(0, file.lastIndexOf("."));
			}
			InputStream inputStream = context.getResources().openRawResource(context.getResources().getIdentifier(file, "raw", context.getPackageName()));
			return inputStream2String(inputStream);
		} catch (Exception e) {
			return null;
		}
	}

	private String inputStream2String(InputStream inputStream) {
		try {
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			return new String(b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}