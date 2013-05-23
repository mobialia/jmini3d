package jmini3d.android;

import java.util.HashMap;

import jmini3d.Scene;
import jmini3d.SceneController;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;
import android.app.Activity;
import android.os.Bundle;

public class RendererActivity extends Activity implements SceneController, TouchListener {
	public static final String TAG = "RendererActivity";

	public Scene scene;
	public Renderer renderer;
	public boolean traslucent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		scene = new Scene(this);
		renderer = new Renderer(this, scene, new ResourceLoader(this), traslucent);

		renderer.setTouchListener(this);

		onCreateSetContentView();
	}

	protected void onCreateSetContentView() {
		setContentView(renderer.getView());
	}

	@Override
	protected void onResume() {
		super.onResume();
		renderer.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		renderer.onPause();
	}

	public void initScene() {
	}

	public boolean updateScene() {
		return false;
	}

	@Override
	public boolean onTouch(HashMap<Integer, TouchPointer> arg0) {
		return false;
	}
}