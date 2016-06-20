package jmini3d.demo;

import java.util.HashMap;

import jmini3d.Renderer3d;
import jmini3d.ScreenController;
import jmini3d.Vector3;
import jmini3d.input.KeyListener;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class DemoScreenController implements ScreenController, TouchListener, KeyListener {
	float cameraAngle;
	long initialTime;

	int sceneIndex = 0;
	ParentScene scenes[] = {new TeapotScene(), new CubeScene(), new EnvMapCubeScene(), new CubesScene(), new NormalMapScene(), new ChildObjectsScene(), new RubikSceneFlickering(), new RubikSceneNoFlickering()};
	ArrowsHudScene hudScene;
	int cameraModes[] = {0, 0, 0, 0, 1, 2, 2, 2};

	public DemoScreenController() {
		initialTime = System.currentTimeMillis();
		hudScene = new ArrowsHudScene();
	}

	@Override
	public boolean onNewFrame(boolean forceRedraw) {
		hudScene.setTitle(scenes[sceneIndex].title);

		// Rotate camera...
		cameraAngle = 0.0005f * (System.currentTimeMillis() - initialTime);

		float d = 5;
		Vector3 target = scenes[sceneIndex].getCamera().getTarget();

		switch (cameraModes[sceneIndex]) {
			case 0:
				scenes[sceneIndex].getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
						(float) (target.y - d * Math.sin(cameraAngle)), //
						target.z + (float) (d * Math.sin(cameraAngle)));
				break;
			case 1:
				scenes[sceneIndex].getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
						(float) (target.y - d * Math.sin(cameraAngle)), d / 2);
				break;
			case 2:
				scenes[sceneIndex].getCamera().setPosition(target.x - d, target.y, target.z + d / 4);
				break;
		}
		scenes[sceneIndex].update();
		return true; // Render all the frames
	}

	@Override
	public void render(Renderer3d renderer3d) {
		renderer3d.render(scenes[sceneIndex]);
		renderer3d.render(hudScene);
	}

	private void nextScene() {
		if (sceneIndex >= scenes.length - 1) {
			sceneIndex = 0;
		} else {
			sceneIndex++;
		}
	}

	private void previousScene() {
		if (sceneIndex <= 0) {
			sceneIndex = scenes.length - 1;
		} else {
			sceneIndex--;
		}
	}

	@Override
	public boolean onTouch(HashMap<Integer, TouchPointer> pointers) {
		for (Integer key : pointers.keySet()) {
			TouchPointer pointer = pointers.get(key);
			if (pointer.status == TouchPointer.TOUCH_DOWN) {
				if (pointer.x > scenes[sceneIndex].getCamera().getWidth() / 2) {
					nextScene();
				} else {
					previousScene();
				}
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int key) {
		switch (key) {
			case KeyListener.KEY_RIGHT:
				nextScene();
				return true;
			case KeyListener.KEY_LEFT:
				previousScene();
				return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int key) {
		return false;
	}
}
