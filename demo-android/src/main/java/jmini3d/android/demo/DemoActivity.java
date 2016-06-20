package jmini3d.android.demo;

import android.os.Bundle;
import android.widget.LinearLayout;

import jmini3d.android.Activity3d;
import jmini3d.android.input.InputController;
import jmini3d.demo.DemoScreenController;

public class DemoActivity extends Activity3d {
	InputController inputController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DemoScreenController screenController = new DemoScreenController();
		glSurfaceView3d.setScreenController(screenController);
		glSurfaceView3d.setLogFps(true);
		inputController = new InputController(glSurfaceView3d);
		inputController.setTouchListener(screenController);
		inputController.setKeyListener(screenController);
	}

	public void onCreateSetContentView() {
		setContentView(R.layout.main);
		LinearLayout root = ((LinearLayout) findViewById(R.id.root));
		root.addView(glSurfaceView3d);
	}

}