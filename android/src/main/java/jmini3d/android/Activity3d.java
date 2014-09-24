package jmini3d.android;

import android.app.Activity;
import android.os.Bundle;

import jmini3d.Scene;
import jmini3d.SceneController;

public class Activity3d extends Activity implements SceneController {
	public static final String TAG = "RendererActivity";

	public GlSurfaceView3d glSurfaceView3d;
	public boolean traslucent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		glSurfaceView3d = new GlSurfaceView3d(this, this, true, traslucent);

		onCreateSetContentView();
	}

	protected void onCreateSetContentView() {
		setContentView(glSurfaceView3d);
	}

	@Override
	protected void onResume() {
		super.onResume();
		glSurfaceView3d.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		glSurfaceView3d.onPause();
	}

	public Scene getScene(int width, int height) {
		return null;
	}
}