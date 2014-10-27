package jmini3d.android;

import android.app.Activity;
import android.os.Bundle;

public class Activity3d extends Activity {
	public GlSurfaceView3d glSurfaceView3d;
	public boolean traslucent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		glSurfaceView3d = new GlSurfaceView3d(this, traslucent);

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

}