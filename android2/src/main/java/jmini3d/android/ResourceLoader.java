package jmini3d.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ResourceLoader {

	Context context;

	HashMap<String, Bitmap> customBitmaps = new HashMap<String, Bitmap>();

	public ResourceLoader(Context ctx) {
		this.context = ctx;
	}

	public Bitmap getImage(String image) {
		if (customBitmaps.containsKey(image)) {
			return customBitmaps.get(image);
		}

		String uri = "drawable/" + image;
		int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

		return makeBitmapFromResourceId(imageResource);
	}

	public Bitmap makeBitmapFromResourceId(int id) {
		InputStream is = context.getResources().openRawResource(id);

		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Ignore.
			}
		}
		return bitmap;
	}

	public void freeBitmap(String name, Bitmap bitmap) {
		bitmap.recycle();
		if (customBitmaps.containsKey(name)) {
			customBitmaps.remove(name);
		}
	}

	public void addCustomBitmap(String name, Bitmap bitmap) {
		customBitmaps.put(name, bitmap);
	}

	public Context getContext() {
		return context;
	}

	public String loadRawResource(int res) {
		try {
			final Resources resources = context.getResources();
			InputStream inputStream = resources.openRawResource(res);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder strBuild = new StringBuilder();
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					strBuild.append(line);
				}
			} finally {
				reader.close();
			}
			return strBuild.toString();
		} catch (Exception e) {
			return null;
		}
	}
}