package jmini3d.android.demo;

import android.os.Bundle;
import android.widget.LinearLayout;

import jmini3d.android.Activity3d;
import jmini3d.android.input.InputController;
import jmini3d.demo.DemoSceneController;

public class DemoActivity extends Activity3d {
	InputController inputController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DemoSceneController sceneController = new DemoSceneController();
		glSurfaceView3d.setSceneController(sceneController);
		glSurfaceView3d.getRenderer3d().setLogFps(true);
		inputController = new InputController(glSurfaceView3d);
		inputController.setTouchListener(sceneController);
	}

	public void onCreateSetContentView() {
		setContentView(R.layout.main);
		LinearLayout root = ((LinearLayout) findViewById(R.id.root));
		root.addView(glSurfaceView3d);
	}

}