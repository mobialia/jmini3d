package jmini3d.android.demo;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.HashMap;

import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.android.Activity3d;
import jmini3d.android.input.TouchController;
import jmini3d.demo.CubeScene;
import jmini3d.demo.CubesScene;
import jmini3d.demo.DemoSceneController;
import jmini3d.demo.DiscoBallScene;
import jmini3d.demo.TeapotScene;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class DemoActivity extends Activity3d {
	public static final String TAG = "DemoActivity";

	TouchController touchController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DemoSceneController sceneController = new DemoSceneController();
		glSurfaceView3d.setSceneController(sceneController);
		glSurfaceView3d.getRenderer3d().setLogFps(true);
		setTouchListener(sceneController);
	}

	public void onCreateSetContentView() {
		setContentView(R.layout.main);
		LinearLayout root = ((LinearLayout) findViewById(R.id.root));
		root.addView(glSurfaceView3d);
	}

	public void setTouchListener(TouchListener listener) {
		if (touchController == null) {
			touchController = new TouchController(glSurfaceView3d);
		}
		touchController.setListener(listener);
	}

}